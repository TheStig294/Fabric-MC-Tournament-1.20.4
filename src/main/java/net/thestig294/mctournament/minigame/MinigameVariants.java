package net.thestig294.mctournament.minigame;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinigameVariants {
    private static final Map<Identifier, List<String>> REGISTERED = new HashMap<>();

    public static final String DEFAULT = "default";
    public static final String RANDOM = "random";

    public static String registerVariant(Identifier minigame, String variant) {
        if (!REGISTERED.containsKey(minigame)) {
            REGISTERED.put(minigame, new ArrayList<>());
            REGISTERED.get(minigame).add(DEFAULT);
        }

        REGISTERED.get(minigame).add(variant);
        return variant;
    }

    public static String getRandom(Identifier minigame) {
        return getRandom(minigame, true);
    }

    public static String getRandom(Identifier minigame, boolean includeDefaultVariant) {
        List<String> variants = REGISTERED.get(minigame);
        int randomIndex = Random.create().nextBetween(includeDefaultVariant ? 0 : 1, variants.size() - 1);

        return variants.get(randomIndex);
    }
}
