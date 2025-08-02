package net.thestig294.mctournament.minigame;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.minigames.MarioKart;
import net.thestig294.mctournament.minigame.minigames.TournamentEnd;
import net.thestig294.mctournament.minigame.minigames.Towerfall;
import net.thestig294.mctournament.minigame.minigames.TriviaMurderParty;
import net.thestig294.mctournament.util.ModUtil;

import java.util.*;
import java.util.function.Predicate;

public class Minigames {
    private static final Map<Identifier, Minigame> REGISTERED_MINIGAMES = new HashMap<>();

    public static final Identifier TOURNAMENT_END = register("tournament_end", new TournamentEnd());
    public static final Identifier TRIVIA_MURDER_PARTY = register("trivia_murder_party", new TriviaMurderParty());
    public static final Identifier TOWERFALL = register("towerfall", new Towerfall());
    public static final Identifier MARIO_KART = register("mario_kart", new MarioKart());


    private static Identifier register(String name, Minigame minigame) {
        return register(new Identifier(MCTournament.MOD_ID, name), minigame);
    }

    public static Identifier register(Identifier id, Minigame minigame) {
        REGISTERED_MINIGAMES.put(id, minigame);
        return id;
    }

    public static void registerMinigames(boolean isClient) {
        MCTournament.LOGGER.info("Registering minigames for " + MCTournament.MOD_ID);
        MCTournament.LOGGER.info(isClient ? "Client register" : "Server register");

        if (isClient) {
            for (final var entry : REGISTERED_MINIGAMES.entrySet()) {
                entry.getValue().clientInit();
            }
        } else {
            for (final var entry : REGISTERED_MINIGAMES.entrySet()) {
                entry.getValue().serverInit();
            }
        }
    }

//    Gets all minigame IDs, except for the special "tournament end" minigame, as it's not a valid minigame to actually play...
    public static ArrayList<Identifier> getMinigameIds() {
        return new ArrayList<>(REGISTERED_MINIGAMES.keySet().stream().filter(identifier -> !identifier.equals(TOURNAMENT_END)).toList());
    }

    public static List<Identifier> getRandomMinigames(int count){
        count = ModUtil.clampInt(count, 1, REGISTERED_MINIGAMES.size());
        if (count == 1) {
            return List.of(getRandomMinigame());
        }

        List<Identifier> idList = getMinigameIds();
        Collections.shuffle(idList);

        List<Identifier> result = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            result.add(idList.get(i));
        }

        return result;
    }

    public static Identifier getRandomMinigame() {
        Random random = Random.create();
        List<Identifier> idList = getMinigameIds();
        int randomIndex = random.nextBetween(0, idList.size());

        return idList.get(randomIndex);
    }

    public static Minigame get(Identifier id) {
        return REGISTERED_MINIGAMES.get(id);
    }

    public static List<Minigame> get(List<Identifier> ids) {
        List<Minigame> result = new ArrayList<>();
        for (final var id : ids) {
            result.add(get(id));
        }
        return result;
    }
}
