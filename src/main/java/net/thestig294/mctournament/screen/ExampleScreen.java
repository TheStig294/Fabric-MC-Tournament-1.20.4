package net.thestig294.mctournament.screen;

import net.thestig294.mctournament.minigame.triviamurderparty.widget.QuestionBox;
import net.thestig294.mctournament.minigame.triviamurderparty.widget.QuestionWidget;
import net.thestig294.mctournament.util.ModColors;

public class ExampleScreen extends AnimatedScreen<ExampleScreen, ExampleScreen.State> {
    QuestionWidget someWidget;

    protected ExampleScreen(Class<ExampleScreen> childClazz, State state) {
        super(childClazz, state);
    }

    @Override
    public void init() {
        this.someWidget = this.addDrawableChild(new QuestionBox(1,2,3,4, ModColors.RED));
        this.setListAlpha(this.children(), 0.0f);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
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
            public float getDuration(ExampleScreen screen) {
                return 2.5f;
            }

            @Override
            public AnimatedScreen.State<ExampleScreen> next(ExampleScreen screen) {
                return STATE2;
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
            public float getDuration(ExampleScreen screen) {
                return 0;
            }

            @Override
            public AnimatedScreen.State<ExampleScreen> next(ExampleScreen screen) {
                return STATE1;
            }
        }
    }
}
