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

    @SuppressWarnings("unused")
    public static class TriviaMurderParty {
        public static final String YOGSCAST = register(Minigames.TRIVIA_MURDER_PARTY, "yogscast");
        public static final String AUSSIE = register(Minigames.TRIVIA_MURDER_PARTY, "aussie");
        public static final String SILLY = register(Minigames.TRIVIA_MURDER_PARTY, "silly");
    }

    @SuppressWarnings("unused")
    public static class Towerfall {
        public static final String LAST_ONE_STANDING = register(Minigames.TOWERFALL, "last_one_standing");
        public static final String TEAM_DEATHMATCH = register(Minigames.TOWERFALL, "team_deathmatch");
    }

    @SuppressWarnings("unused")
    public static class MarioKart {
        public static final String YOSHIS_CIRCUIT = register(Minigames.MARIO_KART, "yoshis_circuit");
        public static final String COCONUT_MALL = register(Minigames.MARIO_KART, "coconut_mall");
        public static final String BOWSERS_CASTLE = register(Minigames.MARIO_KART, "bowsers_castle");
        public static final String RAINBOW_ROAD = register(Minigames.MARIO_KART, "rainbow_road");
        public static final String BABY_PARK = register(Minigames.MARIO_KART, "baby_park");
    }

    public static String register(Identifier minigame, String variant) {
        if (!REGISTERED.containsKey(minigame)) {
            REGISTERED.put(minigame, new ArrayList<>());
            REGISTERED.get(minigame).add(DEFAULT);
        }

        REGISTERED.get(minigame).add(variant);
        return variant;
    }

    public static String getRandomVariant(Identifier minigame) {
        return getRandomVariant(minigame, true);
    }

    public static String getRandomVariant(Identifier minigame, boolean includeDefaultVariant) {
        List<String> variants = REGISTERED.get(minigame);
        int randomIndex = Random.create().nextBetween(includeDefaultVariant ? 0 : 1, variants.size() - 1);

        return variants.get(randomIndex);
    }
}