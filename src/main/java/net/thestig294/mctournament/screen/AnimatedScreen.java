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

    public static Screen PAUSED_SCREEN = null;

    private final Screen parent;

    private float uptimeSecs;
    private E state;
    private float stateEndTime;
    private float stateStartTime;
    private float stateProgress;
    private int stateProgressPercent;
    private boolean firstStateTick;
    private boolean firstState;

    public AnimatedScreen(E startingState) {
        super(Text.empty());
        this.parent = MCTournament.client().currentScreen;

        this.uptimeSecs = 0.0f;
        this.state = startingState;
        this.stateEndTime = 0.0f;
        this.stateStartTime = 0.0f;
        this.stateProgress = 0.0f;
        this.stateProgressPercent = 0;
        this.firstStateTick = true;
        this.firstState = true;
    }

    /**
     * Called on {@link Screen#init()}, this is the place to create all screen widgets inside {@link Screen#addDrawableChild(Element)}. <br>
     * For handling a screen refresh, see: {@link State#refresh(AnimatedScreen)}
     */
    protected abstract void createWidgets();

//    Suppress the warning we're not type checking at runtime, because we're doing it at compile time instead!
//    (The power of CRTP...)
    @SuppressWarnings("unchecked")
    private T toChild() {
        return (T) this;
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
            if (this.state == null) return;
        }

        this.uptimeSecs += delta / ModUtilClient.getTicksPerSecond();
        this.stateProgress = ModUtil.lerpPercent(this.stateStartTime, this.stateEndTime, this.uptimeSecs);
        this.stateProgressPercent = (int) (this.stateProgress * 100);

        if (this.firstStateTick) {
            this.state.begin(this.toChild());
            this.firstStateTick = false;
        }

        this.state.render(this.toChild());
    }

    @SuppressWarnings("unchecked")
    private @Nullable E getNextState() {
        return this.state == null ? null : (E) this.state.next(this.toChild());
    }

    private void switchToNextState() {
        if (!this.firstState) this.state = this.getNextState();

        if (this.state == null) {
            this.onNullStateClose();
            this.close();
            return;
        }

        this.firstState = false;
        float lastEndTime = this.stateEndTime;
        this.stateEndTime = this.uptimeSecs + this.state.duration(this.toChild());
        this.stateStartTime = lastEndTime;
        this.firstStateTick = true;
    }

    /**
     * Used in {@link net.thestig294.mctournament.mixin.AnimatedScreenMixin} <br>
     * Allows for {@link AnimatedScreen}s to open the pause menu on pressing the escape key
     * @return Always {@code false}
     */
    @Override
    public boolean shouldCloseOnEsc() {
        PAUSED_SCREEN = this;
        MCTournament.client().openGameMenu(false);
        return false;
    }

    @Override
    public void close() {
        MCTournament.client().setScreen(this.parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    protected void onNullStateClose() {

    }

    @SuppressWarnings("unused")
    protected void animate(IntConsumer lambda, int start, int end) {
        lambda.accept((int) ModUtil.lerpLinear(start, end, this.stateProgress));
    }

    protected void animate(FloatConsumer lambda, float start, float end) {
        lambda.accept(ModUtil.lerpLinear(start, end, this.stateProgress));
    }

    @SuppressWarnings("unused")
    protected void listAnimateAlpha(List<? extends Element> widgets, float start, float end) {
        for (final var child : widgets) {
            if (child instanceof ClickableWidget widget) {
                this.animate(widget::setAlpha, start, end);
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    protected void setListAlpha(List<? extends Element> widgets, float alpha) {
        for (final var child : widgets) {
            if (child instanceof ClickableWidget widget) {
                widget.setAlpha(alpha);
            }
        }
    }

    @SuppressWarnings("unused")
    protected void everyStatePercent(int percent, Runnable function) {
        if (this.stateProgressPercent % percent == 0) function.run();
    }

    @SuppressWarnings("unused")
    protected void forceStateEnd() {
        this.stateEndTime = this.uptimeSecs;
    }

    public interface State<T extends AnimatedScreen<T, ? extends State<T>>> {
        /**
         * Run on the first render tick
         * @param screen Instance of your screen during this state
         */
        default void begin(T screen) {}

        /**
         * Run on every tick this is state is active
         * @param screen Instance of your screen during this state
         */
        void render(T screen);

        /**
         * Run once every time the Minecraft window is resized or set to full-screen. <br>
         * By default, all screen elements have their {@link ClickableWidget#setAlpha(float)} function called with a value of {@code 0.0f}
         * @param screen Instance of your screen during this state
         */
        void refresh(T screen);

        /**
         * Defines the length of the state
         * @param screen Instance of your screen during this state
         * @return State length as a float
         */
        float duration(T screen);

        /**
         * Defines the next state after this one (optional). <p>
         * If not implemented, the next state is the next one defined in the enum,
         * except for the final state, which has a state of {@code null}. </p>
         * Returning {@code null} triggers a call to {@link AnimatedScreen#close()}.
         * @param screen Instance of your screen during this state
         * @return The screen state after this one
         */
        @SuppressWarnings("unchecked")
        default State<T> next(T screen) {
            Enum<?> child = (Enum<?>) this;
            Enum<?>[] values = child.getDeclaringClass().getEnumConstants();
            int nextOrdinal = child.ordinal() + 1;
            return nextOrdinal >= values.length ? null : (State<T>) values[nextOrdinal];
        }
    }
}
