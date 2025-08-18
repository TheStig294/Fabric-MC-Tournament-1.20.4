package net.thestig294.mctournament.minigame;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.mariokart.MarioKart;
import net.thestig294.mctournament.minigame.tournamentbegin.TournamentBegin;
import net.thestig294.mctournament.minigame.tournamentend.TournamentEnd;
import net.thestig294.mctournament.minigame.towerfall.Towerfall;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.util.ModUtil;

import java.util.*;

public class Minigames {
    private static final Map<Identifier, Minigame> REGISTERED = new HashMap<>();

    /**
     * A special minigames that get automatically played at the beginning and end of every tournament
     */
    public static final Identifier TOURNAMENT_BEGIN = register(new TournamentBegin());
    public static final Identifier TOURNAMENT_END = register(new TournamentEnd());

    public static final Identifier TRIVIA_MURDER_PARTY = register(new TriviaMurderParty());
    public static final Identifier TOWERFALL = register(new Towerfall());
    public static final Identifier MARIO_KART = register(new MarioKart());


    public static Identifier register(Minigame minigame) {
        return register(minigame.getID(), minigame);
    }

    private static Identifier register(String id, Minigame minigame) {
        return register(new Identifier(MCTournament.MOD_ID, id), minigame);
    }

    public static Identifier register(Identifier id, Minigame minigame) {
        REGISTERED.put(id, minigame);
        return id;
    }

    public static void registerMinigames(boolean isClient) {
        MCTournament.LOGGER.info("Registering minigames for " + MCTournament.MOD_ID);

        for (final var entry : REGISTERED.entrySet()) {
            Minigame minigame = entry.getValue();
            MCTournament.LOGGER.info("Registering minigame {} on {}", minigame.getID(), isClient ? "client" : "server");

            if (isClient) {
                minigame.clientInit();
            } else {
                minigame.serverInit();
            }
        }
    }

//    Gets all minigame IDs, except for the special minigames, as they aren't a valid minigame to actually play...
    public static ArrayList<Identifier> getMinigameIds() {
        return new ArrayList<>(REGISTERED.keySet().stream()
                .filter(identifier -> !identifier.equals(TOURNAMENT_END) && !identifier.equals(TOURNAMENT_BEGIN)).toList());
    }

    public static List<Identifier> getRandomMinigames(int count){
        count = ModUtil.clampInt(count, 1, REGISTERED.size());
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
        List<Identifier> idList = getMinigameIds();
        int randomIndex = Random.create().nextBetween(0, idList.size());

        return idList.get(randomIndex);
    }

    public static Minigame get(Identifier id) {
        return REGISTERED.get(id);
    }

    public static List<Minigame> get(List<Identifier> ids) {
        List<Minigame> result = new ArrayList<>();
        for (final var id : ids) {
            result.add(get(id));
        }
        return result;
    }

    public static void logRegistration(String type, Identifier minigameID) {
        ModUtil.logRegistration(type, get(minigameID).getID());
    }
}
