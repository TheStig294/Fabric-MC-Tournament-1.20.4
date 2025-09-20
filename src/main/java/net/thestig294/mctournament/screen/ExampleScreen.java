package net.thestig294.mctournament.screen;

import net.thestig294.mctournament.minigame.triviamurderparty.widget.QuestionBox;
import net.thestig294.mctournament.minigame.triviamurderparty.widget.QuestionWidget;
import net.thestig294.mctournament.util.ModColors;

public class ExampleScreen extends AnimatedScreen<ExampleScreen, ExampleScreen.State> {
    QuestionWidget someWidget;

    public ExampleScreen(State startingState) {
        super(ExampleScreen.class, State.class, startingState);
    }

    @Override
    protected void createWidgets() {
        this.someWidget = this.addDrawableChild(new QuestionBox(1,2,3,4, ModColors.RED));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void networkingInit() {

    }

    public void exampleClassSpecificMethod() {

    }

    public enum State implements AnimatedScreen.State<ExampleScreen> {
        STATE1 {
            @Override
            public void render(ExampleScreen screen) {
                screen.exampleClassSpecificMethod();
            }

            @Override
            public void refresh(ExampleScreen screen) {
                screen.exampleClassSpecificMethod();
            }

            @Override
            public float duration(ExampleScreen screen) {
                return 2.5f;
            }
        },
        STATE2 {
            @Override
            public void render(ExampleScreen screen) {

            }

            @Override
            public void refresh(ExampleScreen screen) {

            }

            @Override
            public float duration(ExampleScreen screen) {
                return 0;
            }

            @Override
            public State next(ExampleScreen screen) {
                return STATE1;
            }
        }
    }
}
