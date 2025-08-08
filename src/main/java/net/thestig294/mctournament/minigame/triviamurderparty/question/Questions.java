package net.thestig294.mctournament.minigame.triviamurderparty.question;

import net.thestig294.mctournament.minigame.Minigames;

import java.util.*;

public class Questions {
    private static final Map<String, List<Question>> REGISTERED = new HashMap<>();
    private static int QUESTION_INDEX = 0;
    private static List<Question> QUESTION_LIST = new ArrayList<>();

    public static void register(String q, String a1, String a2, String a3, String a4, int answer, String... categories) {
        if (categories.length == 0) categories = new String[]{Minigames.DEFAULT_VARIANT};

        for (final var category : categories) {
            if (!REGISTERED.containsKey(category)) {
                REGISTERED.put(category, new ArrayList<>());
            }
            REGISTERED.get(category).add(new Question(q, a1, a2, a3, a4, answer));
        }
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

    public static void registerQuestions() {
        register("In what Daniel Day-Lewis film does he say the line “Stay alive! No matter what occurs. I will find you!”?",
                "There Will Be Blood",
                "In the Name of the Father",
                "My Beautiful Laundrette",
                "The Last of the Mohicans",
                4);
        register("How long does it take a freshly laid egg to hatch into a baby chicken?",
                "10 days",
                "3 weeks",
                "2 months",
                "Half a year",
                2);
        register("Which of these is Indian?",
                "Rooibos tea",
                "Zavarka tea",
                "Pu’erh tea",
                "Masala chai",
                4);
        register("Which country straddles two continents?",
                "Turkey",
                "Romania",
                "Yemen",
                "India",
                1);
        register("Which of these movies stars somebody in prison for a crime they did NOT commit?",
                "The Shawshank Redemption",
                "Cool Hand Luke",
                "Chicago",
                "Dead Man Walking",
                1);
    }
}
