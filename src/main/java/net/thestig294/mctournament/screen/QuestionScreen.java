package net.thestig294.mctournament.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.font.ModFonts;
import net.thestig294.mctournament.util.ModUtil;
import net.thestig294.mctournament.widget.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionScreen extends Screen {
    private static final float QUESTION_NUMBER_FADE_TIME = 0.0f;
    private static final float QUESTION_NUMBER_HOLD_TIME = 0.0f;
    private static final float TEXT_ZOOM_TIME = 0.0f;
    private static final float ALL_CORRECT_LOOP_BACK_TIME = 0.0f;
    private static final float KILLING_ROOM_TRANSITION_MOVE_TIME = 0.0f;
    private static final float KILLING_ROOM_TRANSITION_LIGHTS_TIME = 0.0f;

    private final Screen parent;
    private float totalDelta;
    private float totalSeconds;
    private QuestionScreen.State state;
    private float nextStateTime;

    private List<QuestionPlayer> playerWidgets;
    private QuestionText questionNumberWidget;
    private QuestionText questionWidget;
    private List<QuestionButton> answerWidgets;
    private List<QuestionText> answeredCountWidgets;
    private QuestionTimer timerWidget;
    private List<QuestionText> roomCodeWidgets;

    public QuestionScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
        this.totalDelta = 0.0f;
        this.totalSeconds = 0.0f;
        this.state = State.QUESTION_NUMBER_IN;
        this.nextStateTime = 0.0f;

        this.playerWidgets = new ArrayList<>();
        this.answerWidgets = new ArrayList<>();
        this.answeredCountWidgets = new ArrayList<>();
        this.roomCodeWidgets = new ArrayList<>();
    }

    @Override
    protected void init() {
        super.init();

        if (MCTournament.CLIENT.world == null) return;
        int i = 0;
        for (final var player : MCTournament.CLIENT.world.getPlayers()) {
            this.playerWidgets.add(this.addDrawableChild(new QuestionPlayer(i * this.width / 8, 5, this.width / 8, this.height / 3, player)));
            i++;
        }

        this.questionNumberWidget = this.addDrawableChild(new QuestionText(this.width / 2, this.height / 2, "1", ModFonts.QUESTION_NUMBER, 20, Color.YELLOW,this.width / 2, this.textRenderer));

        this.questionWidget = this.addDrawableChild(new QuestionText(this.width / 30, this.height / 3, "In what Daniel Day-Lewis film does he say the line “Stay alive! No matter what occurs. I will find you!”?", ModFonts.QUESTION,25, Color.WHITE, this.width * 3 / 5, this.textRenderer));

        this.answerWidgets.add(this.addDrawableChild(new QuestionButton(this, this.width * 2/3, this.height / 2 - 30, 140, 20, this.textRenderer, 1, "There Will Be Blood", false)));
        this.answerWidgets.add(this.addDrawableChild(new QuestionButton(this, this.width * 2/3, this.height / 2, 140, 20, this.textRenderer, 2, "In the Name of the Father", false)));
        this.answerWidgets.add(this.addDrawableChild(new QuestionButton(this, this.width * 2/3, this.height / 2 + 30, 140, 20, this.textRenderer, 3, "My Beautiful Laundrette", false)));
        this.answerWidgets.add(this.addDrawableChild(new QuestionButton(this, this.width * 2/3, this.height / 2 + 60, 140, 20, this.textRenderer, 4, "The Last of the Mohicans", true)));

        this.answeredCountWidgets.add(this.addDrawableChild(new QuestionText(5, this.height - 65, "ANSWER\nNOW!", ModFonts.QUESTION_ANSWER, 15, Color.RED, 100, this.textRenderer)));
        this.answeredCountWidgets.add(this.addDrawableChild(new QuestionText( 15, this.height - 40, "0", ModFonts.QUESTION_NUMBER, 20, Color.ORANGE, 100, this.textRenderer)));
        this.answeredCountWidgets.add(this.addDrawableChild(new QuestionText( 0, this.height - 15, "ANSWERED", ModFonts.QUESTION_ANSWER, 10, Color.GRAY, 100, this.textRenderer)));

        this.timerWidget = this.addDrawableChild(new QuestionTimer(this.width / 3, this.height - 64, 64, 64, 20, 1.0f, 0));

        this.roomCodeWidgets.add(this.addDrawableChild(new QuestionText( this.width - 75, this.height - 30, "MINECRAFT.TV", ModFonts.QUESTION_ANSWER, 10, Color.GRAY, 100, this.textRenderer)));
        this.roomCodeWidgets.add(this.addDrawableChild(new QuestionText( this.width - 40, this.height - 15, ModUtil.getRandomString(4, 2), ModFonts.QUESTION_ANSWER, 10, Color.RED, 100, this.textRenderer)));

        for (final var child : this.children()) {
            if (child instanceof ClickableWidget widget) {
                widget.setAlpha(0.0f);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.totalDelta += delta;
        this.totalSeconds = totalDelta / 20.0f;
        this.updateState();
    }

    private void updateState() {
        switch (this.state) {

        }
        this.state = this.state.next();
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(this.parent);
    }

    public enum State {
        QUESTION_NUMBER_IN,
        QUESTION_NUMBER_HOLD,
        QUESTION_NUMBER_OUT,
        QUESTION_IN,
        QUESTION_HOLD,
        QUESTION_OUT,
        ANSWERING,
        ANSWER_REVEAL_IN,
        ANSWER_REVEAL_HOLD,
        ALL_CORRECT_LOOP_BACK,
        KILLING_ROOM_TRANSITION;

        private boolean playerIncorrect = false;

        public void setPlayerIncorrect(boolean playerIncorrect) {
            this.playerIncorrect = playerIncorrect;
        }

        public State next() {
            if (this.equals(State.ALL_CORRECT_LOOP_BACK)) {
                return State.QUESTION_NUMBER_IN;
            } else if (this.equals(State.KILLING_ROOM_TRANSITION)) {
                return State.KILLING_ROOM_TRANSITION;
            } else if (this.equals(State.ANSWER_REVEAL_HOLD)){
                return this.playerIncorrect ? State.KILLING_ROOM_TRANSITION : State.ALL_CORRECT_LOOP_BACK;
            } else {
                return values()[ordinal() + 1];
            }
        }
    }
}
