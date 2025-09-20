package net.thestig294.mctournament.screen;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.util.ModUtil;
import net.thestig294.mctournament.util.ModUtilClient;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.IntConsumer;


@Environment(EnvType.CLIENT)
// Behold, the "Curiously Recurring Template Pattern"
// https://medium.com/@AbhineyKumar/why-use-the-curiously-recurring-template-pattern-crtp-in-java-a9a192022849
// This is all basically to allow for the screens implementing this class
// to have access to themselves inside their State enums, e.g.
// public void render(ExampleScreen screen) {
public abstract class AnimatedScreen<
        T extends AnimatedScreen<T, E>,
        E extends Enum<E> & AnimatedScreen.State<T>>
        extends Screen {

    private static boolean INITIALISED;

    private final Class<T> childClass;
    private final Class<E> childStateClass;
    private final Screen parent;

    private float uptimeSecs;
    private E state;
    private float stateEndTime;
    private float stateStartTime;
    private float stateProgress;
    private int stateProgressPercent;
    private boolean firstStateTick;
    private boolean firstState;

    public AnimatedScreen(Class<T> childClass, Class<E> childStateClass, E startingState) {
        super(Text.empty());
        this.childClass = childClass;
        this.childStateClass = childStateClass;
        this.parent = MCTournament.client().currentScreen;

        this.uptimeSecs = 0.0f;
        this.state = startingState;
        this.stateEndTime = 0.0f;
        this.stateStartTime = 0.0f;
        this.stateProgress = 0.0f;
        this.stateProgressPercent = 0;
        this.firstStateTick = true;
        this.firstState = true;

        if (!INITIALISED) {
            this.networkingInit();
            INITIALISED = true;
        }
    }

    protected abstract void createWidgets();

    @Override
    public abstract boolean shouldPause();

    @Override
    public abstract boolean shouldCloseOnEsc();

    protected abstract void networkingInit();

    private T toChild() {
        if (!this.childClass.isInstance(this)) {
            MCTournament.LOGGER.error("""
                    Cannot cast AnimatedScreen!
                    An AnimatedScreen implementation must be a generic of itself, and its state enum:
                    E.g. "ExampleScreen extends AnimatedScreen<ExampleScreen, ExampleScreen.State>\"""");
            return null;
        }

        return this.childClass.cast(this);
    }

    @Override
    protected void init() {
        super.init();
        this.createWidgets();
        this.setListAlpha(this.children(), 0.0f);
        this.state.refresh(this.toChild());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (this.stateEndTime <= this.uptimeSecs) {
            this.switchToNextState();
        }

        this.uptimeSecs += delta / ModUtilClient.getTicksPerSecond();
        this.stateProgress = ModUtil.lerpPercent(this.stateStartTime, this.stateEndTime, this.uptimeSecs);
        this.stateProgressPercent = (int) (this.stateProgress * 100);

        this.state.render(this.toChild());
        this.firstStateTick = false;
    }

    private @Nullable E getNextState() {
        if (this.state == null) return null;

        if (!this.childClass.isInstance(this.state)) {
            MCTournament.LOGGER.error("""
                    Cannot cast AnimatedScreen.State!
                    An AnimatedScreen implementation must be a generic of itself, and its state enum:
                    E.g. "class ExampleScreen extends AnimatedScreen<ExampleScreen, ExampleScreen.State>"
                    An AnimatedScreen.State implementation must be an enum generic on its screen:
                    E.g. "enum State implements AnimatedScreen.State<ExampleScreen>\"""");
            return null;
        }

        return this.childStateClass.cast(this.state.next(this.toChild()));
    }

    private void switchToNextState() {
        if (!this.firstState) this.state = this.getNextState();

        if (this.state == null) {
            this.close();
            return;
        }

        this.firstState = false;
        float lastEndTime = this.stateEndTime;
        this.stateEndTime = this.uptimeSecs + this.state.duration(this.toChild());
        this.stateStartTime = lastEndTime;
        this.firstStateTick = true;
    }

    @Override
    public void close() {
        MCTournament.client().setScreen(this.parent);
    }

    protected void animate(IntConsumer lambda, int start, int end) {
        lambda.accept((int) ModUtil.lerpLinear(start, end, this.stateProgress));
    }

    protected void animate(FloatConsumer lambda, float start, float end) {
        lambda.accept(ModUtil.lerpLinear(start, end, this.stateProgress));
    }

    protected void listAnimateAlpha(List<? extends Element> widgets, float start, float end) {
        for (final var child : widgets) {
            if (child instanceof ClickableWidget widget) {
                this.animate(widget::setAlpha, start, end);
            }
        }
    }

    protected void setListAlpha(List<? extends Element> widgets, float alpha) {
        for (final var child : widgets) {
            if (child instanceof ClickableWidget widget) {
                widget.setAlpha(alpha);
            }
        }
    }

    protected void everyStatePercent(int percent, Runnable function) {
        if (this.stateProgressPercent % percent == 0) function.run();
    }

    protected void ifFirstStateTick(Runnable function) {
        if (this.firstStateTick) function.run();
    }

    protected void forceStateEnd() {
        this.stateEndTime = this.uptimeSecs;
    }

    protected State<T> toChildStateClass(Enum<?> childState) {
        return this.childStateClass.cast(childState);
    }

    public interface State<ChildClass extends AnimatedScreen<ChildClass, ? extends State<ChildClass>>> {
        void render(ChildClass screen);

        void refresh(ChildClass screen);

        float duration(ChildClass screen);

//        All to avoid having to declare the next state in order in every state that doesn't
//        have special behaviour. The last state returns null and triggers the screen's .close()
//        function to eventually be called
//        (Basically replicating: default -> values()[this.ordinal() + 1];)
        default State<ChildClass> next(ChildClass screen) {
            if (!(this instanceof Enum<?> child)) {
                MCTournament.LOGGER.error("Trying to implement AnimatedScreen.State on a non-enum class!");
                return null;
            }

            Enum<?>[] values = child.getDeclaringClass().getEnumConstants();
            int nextOrdinal = child.ordinal() + 1;
            return nextOrdinal < values.length ? screen.toChildStateClass(values[nextOrdinal]) : null;
        }
    }
}
