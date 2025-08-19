package net.thestig294.mctournament.minigame.triviamurderparty.question;

import net.thestig294.mctournament.minigame.MinigameVariants;

import java.util.*;

public class Questions {
    private static final Map<String, List<Question>> REGISTERED = new HashMap<>();
    private static int QUESTION_INDEX = 0;
    private static List<Question> QUESTION_LIST = new ArrayList<>();
    private static final List<Question> QUESTION_LIST_BY_ID = new ArrayList<>();
    private static int NEXT_QUESTION_ID = 0;

    public static void register(String q, String a1, String a2, String a3, String a4, int answer, float holdTime,
                                String... categories) {
        if (categories.length == 0) categories = new String[]{MinigameVariants.DEFAULT};

        Question question = new Question(q, a1, a2, a3, a4, answer, holdTime, NEXT_QUESTION_ID);
        QUESTION_LIST_BY_ID.add(question);

        for (final var category : categories) {
            if (!REGISTERED.containsKey(category)) {
                REGISTERED.put(category, new ArrayList<>());
            }
            REGISTERED.get(category).add(question);
        }

        NEXT_QUESTION_ID++;
    }

    public static void shuffleCategory(String category) {
        if (!REGISTERED.containsKey(category)) return;

        QUESTION_INDEX = 0;
        QUESTION_LIST = REGISTERED.get(category);

        Collections.shuffle(QUESTION_LIST);
    }

    public static Question getNext() {
        if (QUESTION_INDEX >= QUESTION_LIST.size()) QUESTION_INDEX = 0;

        Question question = QUESTION_LIST.get(QUESTION_INDEX);
        QUESTION_INDEX++;
        return question;
    }

    public static int getQuestionNumber() {
        return QUESTION_INDEX;
    }

    public static Question getQuestionByID(int id) {
        return QUESTION_LIST_BY_ID.get(id);
    }


    public static void register() {
        register("In what Daniel Day-Lewis film does he say the line “Stay alive! No matter what occurs. I will find you!”?",
                "There Will Be Blood",
                "In the Name of the Father",
                "My Beautiful Laundrette",
                "The Last of the Mohicans",
                4, 8.0f);
        register("How long does it take a freshly laid egg to hatch into a baby chicken?",
                "10 days",
                "3 weeks",
                "2 months",
                "Half a year",
                2, 3.5f);
        register("Which of these is Indian?",
                "Rooibos tea",
                "Zavarka tea",
                "Pu’erh tea",
                "Masala chai",
                4, 1.5f);
        register("Which country straddles two continents?",
                "Turkey",
                "Romania",
                "Yemen",
                "India",
                1, 2.0f);
        register("Which of these movies stars somebody in prison for a crime they did NOT commit?",
                "The Shawshank Redemption",
                "Cool Hand Luke",
                "Chicago",
                "Dead Man Walking",
                1, 4.0f);
    }
}
