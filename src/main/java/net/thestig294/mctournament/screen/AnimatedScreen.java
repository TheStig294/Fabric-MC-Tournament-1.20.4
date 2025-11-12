package net.thestig294.mctournament.screen;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.mixin.MinecraftClientInvoker;
import net.thestig294.mctournament.util.ModUtil;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;


@Environment(EnvType.CLIENT)
public abstract class AnimatedScreen<
        T extends AnimatedScreen<T, E>,
        E extends Enum<E> & AnimatedScreen.State<T>>
        extends Screen {

    public static Screen PAUSED_SCREEN = null;
    public static boolean HUD_HOOK_CREATED = false;
    public static AnimatedScreen<?,?> ACTIVE_HUD_SCREEN = null;

    private final Class<T> childClass;
    private final Class<E> stateClass;

    @Nullable
    private E state;
    private float lastTickTime;
    private float uptimeSecs;
    private float stateEndTime;
    private float stateStartTime;
    private float stateProgress;
    private int stateProgressPercent;
    private boolean firstStateTick;
    private boolean firstState;
    private boolean wasHudState;

    public AnimatedScreen(@Nullable E startingState, Class<T> childClass, Class<E> stateClass) {
        super(Text.empty());
        this.childClass = childClass;
        this.stateClass = stateClass;

        this.state = startingState;
        this.lastTickTime = Util.getMeasuringTimeMs() / 1000.0f;
        this.uptimeSecs = 0.0f;
        this.stateEndTime = 0.0f;
        this.stateStartTime = 0.0f;
        this.stateProgress = 0.0f;
        this.stateProgressPercent = 0;
        this.firstStateTick = true;
        this.firstState = true;
        this.wasHudState = false;

        if (!HUD_HOOK_CREATED) {
            HudRenderCallback.EVENT.register((context, delta) -> {
                if (ACTIVE_HUD_SCREEN != null) {
                    ACTIVE_HUD_SCREEN.render(context, ACTIVE_HUD_SCREEN.width / 2,
                            ACTIVE_HUD_SCREEN.height / 2, delta);
                }
            });

            HUD_HOOK_CREATED = true;
        }
    }

    /**
     * Called on {@link Screen#init()}, this is the place to create all screen widgets inside {@link Screen#addDrawableChild(Element)}. <br>
     * For handling a screen refresh, see: {@link State#refresh(AnimatedScreen)}
     */
    protected abstract void createWidgets();

    private T toChild() {
        return this.childClass.cast(this);
    }

    @Override
    protected void init() {
        super.init();
        this.createWidgets();
        this.setListAlpha(this.children(), 0.0f);
        if (this.state != null) this.state.refresh(this.toChild());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (this.stateEndTime <= this.uptimeSecs) {
            if (this.state != null) this.state.end(this.toChild());
            this.switchToNextState();
            if (this.state == null) return;
        }

        float currentTickTime = Util.getMeasuringTimeMs() / 1000.0f;
        this.uptimeSecs += currentTickTime - this.lastTickTime;
        this.lastTickTime = currentTickTime;
        this.stateProgress = ModUtil.lerpPercent(this.stateStartTime, this.stateEndTime, this.uptimeSecs);
        this.stateProgressPercent = (int) (this.stateProgress * 100);

        if (this.firstStateTick) {
            if (this.state != null) this.state.begin(this.toChild());
            this.firstStateTick = false;
        }

        if (this.state != null) this.state.render(this.toChild());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.state == null || !this.isHudState()) super.renderBackground(context, mouseX, mouseY, delta);
    }

    private @Nullable E getNextState() {
        return this.state == null ? null : this.stateClass.cast(this.state.next(this.toChild()));
    }

    private void switchToNextState() {
        if (this.firstState && this.state != null && this.isHudState()) {
//        If the first state is to draw on the HUD, then MinecraftClient.setScreen() might not necessarily be called!
//        So just in case, we initialise the screen's important properties here, like the text renderer and the width/height
//        This is safe to manually call, as other vanilla screens like the AnvilScreen do this!
            MinecraftClient client = MCTournament.client();
            this.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        } else {
            this.wasHudState = this.isHudState();
            this.state = this.getNextState();
        }

        if (this.state == null) {
            this.close();
            ACTIVE_HUD_SCREEN = null;
            return;
        } else if (this.isHudState()) {
            ACTIVE_HUD_SCREEN = this.toChild();
            if (MCTournament.client().currentScreen instanceof AnimatedScreen<?,?> animatedScreen) {
                animatedScreen.close();
            }
        } else if (!this.isHudState() && this.wasHudState){
            ACTIVE_HUD_SCREEN = null;
            MCTournament.client().setScreen(this);
        }

        this.firstState = false;
        float lastEndTime = this.stateEndTime;
        if (this.state != null) this.stateEndTime = this.uptimeSecs + this.state.duration(this.toChild());
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
        MCTournament.client().setScreen(null);
        PAUSED_SCREEN = this;
        MCTournament.client().openGameMenu(false);
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        MinecraftClient client = MCTournament.client();

        if (keyCode == GLFW.GLFW_KEY_SLASH) {
            ((MinecraftClientInvoker) client).callOpenChatScreen("");

            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
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

    protected void forceStateEnd() {
        this.stateEndTime = this.uptimeSecs;
    }

    @SuppressWarnings("SameParameterValue")
    protected boolean isState(State<T> state) {
        return Objects.equals(this.state, state);
    }

    protected boolean isHudState() {
        return this.state != null && this.state.isHudState(this.toChild());
    }

    /**
     * Must be used instead of {@link AnimatedScreen#width} during HUD states
     * @return The width of the screen during a HUD state
     */
    public int hudWidth() {
        return MCTournament.client().getWindow().getScaledWidth();
    }

    /**
     * Must be used instead of {@link AnimatedScreen#height} during HUD states
     * @return The height of the screen during a HUD state
     */
    @SuppressWarnings("unused")
    public int hudHeight() {
        return MCTournament.client().getWindow().getScaledHeight();
    }

    public Class<E> getStateClass() {
        return this.stateClass;
    }

    public float getUptimeSecs() {
        return this.uptimeSecs;
    }

    public interface State<T extends AnimatedScreen<T, ? extends State<T>>> {
        /**
         * Whether this state should be rendered on the HUD and allow for players to interact with the world and move
         * @param screen Instance of your screen during this state
         * @return If this state should render on the HUD
         */
        default boolean isHudState(T screen) {return false;}

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
         * Run on the final render tick
         * @param screen Instance of your screen during this state
         */
        default void end(T screen) {}

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
         * except for the final state, where its next state is {@code null}. </p>
         * Returning {@code null} closes the screen via {@link AnimatedScreen#close()}
         * @param screen Instance of your screen during this state
         * @return The screen state after this one
         */
        default State<T> next(T screen) {
            Enum<?> child = (Enum<?>) this;
            Enum<?>[] values = child.getDeclaringClass().getEnumConstants();
            int nextOrdinal = child.ordinal() + 1;
            return nextOrdinal >= values.length ? null : screen.getStateClass().cast(values[nextOrdinal]);
        }
    }
}
