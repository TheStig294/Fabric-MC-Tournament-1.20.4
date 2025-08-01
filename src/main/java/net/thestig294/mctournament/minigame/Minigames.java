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

public class Minigames {
    private static final Map<Identifier, Minigame> REGISTERED_MINIGAMES = new HashMap<>();

    public static final Minigame TOURNAMENT_END = new TournamentEnd();
    public static final Minigame TRIVIA_MURDER_PARTY = register("trivia_murder_party", new TriviaMurderParty());
    @SuppressWarnings("unused")
    public static final Minigame TOWERFALL = register("towerfall", new Towerfall());
    @SuppressWarnings("unused")
    public static final Minigame MARIO_KART = register("mario_kart", new MarioKart());


    private static Minigame register(String name, Minigame minigame) {
        return register(new Identifier(MCTournament.MOD_ID, name), minigame);
    }

    public static Minigame register(Identifier id, Minigame minigame) {
        if (ModUtil.isClient()) {
            minigame.clientInit();
        } else {
            minigame.sharedInit();
        }

        REGISTERED_MINIGAMES.put(id, minigame);
        return minigame;
    }

    public static void registerMinigames() {
        MCTournament.LOGGER.info("Registering minigames for " + MCTournament.MOD_ID);
    }

    public static ArrayList<Identifier> getMinigameIds() {
        return new ArrayList<>(REGISTERED_MINIGAMES.keySet().stream().toList());
    }

    public static List<Minigame> getRandomMinigames(int count){
        if (count == 1) {
            return List.of(getRandomMinigame());
        }

        List<Identifier> idList = getMinigameIds();
        Collections.shuffle(idList);

        List<Minigame> result = new ArrayList<>();
        count = ModUtil.clampInt(count, 1, REGISTERED_MINIGAMES.size());

        for (int i = 0; i < count; i++) {
            result.add(REGISTERED_MINIGAMES.get(idList.get(i)));
        }

        return result;
    }

    public static Minigame getRandomMinigame() {
        Random random = Random.create();
        List<Identifier> idList = getMinigameIds();
        int randomIndex = random.nextBetween(0, idList.size());

        return REGISTERED_MINIGAMES.get(idList.get(randomIndex));
    }
}
