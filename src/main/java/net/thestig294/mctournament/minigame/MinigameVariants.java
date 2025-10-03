package net.thestig294.mctournament.minigame;

import net.thestig294.mctournament.util.ModUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinigameVariants {
    private static final Map<String, List<String>> REGISTERED = new HashMap<>();

    public static final String DEFAULT = "default";
    public static final String RANDOM = "random";

    public static String registerVariant(String minigameID, String variant) {
        if (!REGISTERED.containsKey(minigameID)) {
            REGISTERED.put(minigameID, new ArrayList<>());
            REGISTERED.get(minigameID).add(DEFAULT);
        }

        REGISTERED.get(minigameID).add(variant);
        return variant;
    }

    public static String getRandom(String minigameID) {
        return getRandom(minigameID, true);
    }

    public static String getRandom(String minigameID, boolean includeDefaultVariant) {
        List<String> variants = REGISTERED.get(minigameID);
        int randomIndex = ModUtil.random(false).nextBetween(includeDefaultVariant ? 0 : 1, variants.size() - 1);

        return variants.get(randomIndex);
    }
}
