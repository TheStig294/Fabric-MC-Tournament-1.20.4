package net.thestig294.mctournament.screen;

import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.triviamurderparty.widget.QuestionBox;
import net.thestig294.mctournament.minigame.triviamurderparty.widget.QuestionWidget;
import net.thestig294.mctournament.util.ModColors;

import java.util.Objects;

public class ExampleScreen extends AnimatedScreen<ExampleScreen, ExampleScreen.State> {
    QuestionWidget someWidget;

    public ExampleScreen(State startingState) {
        super(startingState);
    }

    @Override
    protected void createWidgets() {
        this.someWidget = this.addDrawableChild(new
                QuestionBox(this.width / 2,this.height / 2,this.width / 2,this.height / 2, ModColors.WHITE));
    }

    @Override
    protected void networkingInit() {

    }

    public void exampleClassSpecificMethod() {
        Objects.requireNonNull(MCTournament.client().player).sendMessage(Text.literal("State 1 begun!"));
    }

    public enum State implements AnimatedScreen.State<ExampleScreen> {
        STATE1 {
            @Override
            public void begin(ExampleScreen screen) {
                screen.exampleClassSpecificMethod();
            }

            @Override
            public void render(ExampleScreen screen) {
                screen.animate(screen.someWidget::setAlpha, 0.0f, 1.0f);
            }

            @Override
            public void refresh(ExampleScreen screen) {
                screen.someWidget.setAlpha(1.0f);
            }

            @Override
            public float duration(ExampleScreen screen) {
                return 2.5f;
            }

            @Override
            public State next(ExampleScreen screen) {
                return STATE2;
            }
        },
        STATE2 {
            @Override
            public void render(ExampleScreen screen) {
                screen.animate(screen.someWidget::setAlpha, 1.0f, 0.0f);
            }

            @Override
            public void refresh(ExampleScreen screen) {
                screen.someWidget.setAlpha(0.0f);
            }

            @Override
            public float duration(ExampleScreen screen) {
                return 1.5f;
            }
        },
        @SuppressWarnings("unused")
        STATE3 {
            @Override
            public void begin(ExampleScreen screen) {
                Objects.requireNonNull(MCTournament.client().player).sendMessage(Text.literal("State 3 begun!"));
            }

            @Override
            public void render(ExampleScreen screen) {

            }

            @Override
            public void refresh(ExampleScreen screen) {

            }

            @Override
            public float duration(ExampleScreen screen) {
                return 1.0f;
            }
        }
    }
}
