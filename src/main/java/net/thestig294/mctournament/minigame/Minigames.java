package net.thestig294.mctournament.minigame;

import net.minecraft.util.math.random.Random;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.mariokart.MarioKart;
import net.thestig294.mctournament.minigame.tournamentbegin.TournamentBegin;
import net.thestig294.mctournament.minigame.tournamentend.TournamentEnd;
import net.thestig294.mctournament.minigame.towerfall.Towerfall;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.util.ModUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Minigames {
    private static final Map<String, Minigame> REGISTERED = new HashMap<>();

    /**
     * A special minigames that getKillingRoom automatically played at the beginning and end of every tournament
     */
    public static final TournamentBegin TOURNAMENT_BEGIN = (TournamentBegin) register(new TournamentBegin());
    public static final TournamentEnd TOURNAMENT_END = (TournamentEnd) register(new TournamentEnd());

    public static final TriviaMurderParty TRIVIA_MURDER_PARTY = (TriviaMurderParty) register(new TriviaMurderParty());
    @SuppressWarnings("unused")
    public static final Towerfall TOWERFALL = (Towerfall) register(new Towerfall());
    @SuppressWarnings("unused")
    public static final MarioKart MARIO_KART = (MarioKart) register(new MarioKart());


    public static Minigame register(Minigame minigame) {
        REGISTERED.put(minigame.getID(), minigame);
        return minigame;
    }

    public static void registerMinigames(boolean isClient) {
        MCTournament.LOGGER.info("Registering minigames for " + MCTournament.MOD_ID);

        for (final var entry : REGISTERED.entrySet()) {
            Minigame minigame = entry.getValue();
            MCTournament.LOGGER.info("Registering minigame {} on {}", minigame.getID(), isClient ? "client" : "server");

            if (isClient) {
                minigame.clientPreInit();
                minigame.clientInit();
            } else {
                minigame.serverPreInit();
                minigame.serverInit();
            }
        }
    }

//    Gets all minigame IDs, except for the special minigames, as they aren't a valid minigame to actually play...
    public static List<String> getMinigameIds() {
        return new ArrayList<>(REGISTERED.keySet().stream()
                .filter(id -> !id.equals(TOURNAMENT_END.getID()) && !id.equals(TOURNAMENT_BEGIN.getID()))
                .toList());
    }

    @SuppressWarnings("unused")
    public static List<String> getRandomMinigames(int count){
        count = ModUtil.clampInt(count, 1, REGISTERED.size());
        if (count == 1) {
            return List.of(getRandomMinigame());
        }

        List<String> idList = getMinigameIds();
        Collections.shuffle(idList);

        List<String> result = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            result.add(idList.get(i));
        }

        return result;
    }

    public static String getRandomMinigame() {
        List<String> idList = getMinigameIds();
        int randomIndex = Random.create().nextBetween(0, idList.size());

        return idList.get(randomIndex);
    }

    @Nullable
    public static Minigame get(String id) {
        return REGISTERED.get(id);
    }

    public static List<Minigame> get(List<String> ids) {
        List<Minigame> result = new ArrayList<>();
        for (final var id : ids) {
            result.add(get(id));
        }
        return result;
    }
}
