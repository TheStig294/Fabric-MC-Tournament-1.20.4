package net.thestig294.mctournament.screen;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.thestig294.mctournament.util.ModUtil;
import net.thestig294.mctournament.util.ModUtilClient;

import java.util.List;
import java.util.function.IntConsumer;


@Environment(EnvType.CLIENT)
// Behold, the "Curiously Recurring Template Pattern"
// https://medium.com/@AbhineyKumar/why-use-the-curiously-recurring-template-pattern-crtp-in-java-a9a192022849
// This is all basically to allow for the screens implementing this class
// to have access to themselves inside their State enums, e.g.
// public void render(ExampleScreen screen) {
public abstract class AnimatedScreen<
        ChildClass extends AnimatedScreen<ChildClass, ChildStateClass>,
        ChildStateClass extends Enum<ChildStateClass> & AnimatedScreen.State<ChildClass>>
        extends Screen {
    private ChildStateClass state;
    private final Class<ChildClass> childClazz;

//    Clazz? zz? Yeah, it's weird, but it's what Java's Class class names Class objects/instances
//    to differentiate them from an actual instance/object of the class
    protected AnimatedScreen(Class<ChildClass> childClazz, ChildStateClass state) {
        super(Text.empty());
        this.state = state;
        this.childClazz = childClazz;
    }

//    Behold... we have access to the child instance in the parent class!
//    This is better than just "return (ChildClass) this", since if the cast
//    fails, we get the error here rather than leaving it unchecked!
    private ChildClass toChild() {
        return this.childClazz.cast(this);
    }

    @Override
    public abstract void init();

    @Override
    public abstract boolean shouldPause();

    @Override
    public abstract boolean shouldCloseOnEsc();

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (this.stateEndTime <= this.uptimeSecs) {
            this.nextState();
        }

        this.uptimeSecs += delta / ModUtilClient.getTicksPerSecond();
        this.stateProgress = ModUtil.lerpPercent(this.stateStartTime, this.stateEndTime, this.uptimeSecs);
        this.stateProgressPercent = (int) (this.stateProgress * 100);

        this.renderState();
        this.state.render(this.toChild());
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

//    protected void renderState(Enum<?> state) {
//
//    }
//
//    protected void onRefresh(Enum<?> state) {
//
//    }
//
//    protected float getStateTime() {
//
//    }
//
//    private void endState(Enum<?> state) {
//
//    }
//
//    protected Enum<?> getNextState() {
//        var values = this.state.getClass().getEnumConstants();
//        return this.state.ordinal() < values.length ? values[this.state.ordinal() + 1] : values[0];
//    }

    public interface State<ChildClass extends AnimatedScreen<ChildClass, ? extends State<ChildClass>>> {
        void render(ChildClass screen);

        void refresh(ChildClass screen);

        float getDuration(ChildClass screen);

        State<ChildClass> next(ChildClass screen);
    }
}
