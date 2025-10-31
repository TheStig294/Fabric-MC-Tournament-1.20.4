package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.structure.ModStructures;
import net.thestig294.mctournament.structure.Structure;
import net.thestig294.mctournament.tournament.Teams;
import net.thestig294.mctournament.util.ModTimer;
import net.thestig294.mctournament.util.ModUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.thestig294.mctournament.structure.ModStructures.registerStructure;

public class ScoreScreenHandler {
    private static final Structure SCORE_ROOM = registerStructure(TriviaMurderParty.ID, "score_room",
            6,1,15);

    private static final BlockPos REDSTONE_OFFSET = new BlockPos(1,1,-12);

    private static final List<BlockPos> ELEVATOR_LIGHT_REDSTONE_OFFSETS = List.of(
            new BlockPos(7,0,-6), // 0
            new BlockPos(7,1,-6), // 1
            new BlockPos(7,2,-6), // 2
            new BlockPos(7,3,-6), // 3
            new BlockPos(7,4,-6), // 4
            new BlockPos(7,5,-6) // 5
    );

    private static final List<BlockPos> CAPTAIN_POSITIONS = List.of(
            new BlockPos(-3,4,-8), // 0
            new BlockPos(2,3,-8), // 1
            new BlockPos(1,2,-8), // 2
            new BlockPos(0,1,-8), // 3
            new BlockPos(-1,1,-8), // 4
            new BlockPos(-2,1,-8), // 5
            new BlockPos(-3,1,-8), // 6
            new BlockPos(-4,1,-8)  // 7
    );

    private final TriviaMurderParty minigame;
    private BlockPos position;
    private Teams teams;
    private int timesInScoreRoom;

    public ScoreScreenHandler() {
        this.minigame = Minigames.TRIVIA_MURDER_PARTY;
        this.position = BlockPos.ORIGIN;
        this.timesInScoreRoom = 0;
    }

    public void begin(BlockPos roomPos) {
        this.position = roomPos;
        this.teams = this.minigame.teams();
        this.timesInScoreRoom = 0;
    }

    @SuppressWarnings("unused")
    private float getQuipLength(boolean finalRound) {
        return 5.0f;
    }

    /**
     * Gets the team that should be the last alive team going into the final round. <p>
     * If there is more than one team still alive, returns {@code null} to signify the final round should not yet begin. </p>
     * If all teams are dead, the team with the highest score is selected, ties are broken randomly.
     * @return The final {@link Team} to go into the final round, or {@code null} if the final round shouldn't start yet
     */
    private @Nullable Team getFinalRoundAliveTeam() {
        List<Team> connectedTeams = this.teams.getConnectedTeams();
        Team lastAliveTeam = null;
        int highestScore = -1;
        List<Team> highestScoreTeams = new ArrayList<>();

        for (final var team : connectedTeams) {
            PlayerEntity captain = this.teams.getTeamCaptain(team);
            if (captain == null) continue;

            if (this.minigame.isDead(captain)) {
                if (lastAliveTeam == null) {
                    lastAliveTeam = team;
                } else {
                    return null;
                }
            }

            int score = this.minigame.scoreboard().getScore(team);

            if (highestScore < score) {
                highestScore = score;
                highestScoreTeams.clear();
                highestScoreTeams.add(team);
            } else if (highestScore == score) {
                highestScoreTeams.add(team);
            }
        }

        if (lastAliveTeam == null) {
            Team highestScoreTeam = ModUtil.getRandomElement(false, highestScoreTeams);
            if (highestScoreTeam == null) return null;
            this.teams.forAllConnectedTeamMembers(highestScoreTeam, (player) ->
                    this.minigame.setDead(player, false));
            return highestScoreTeam;
        } else {
            return lastAliveTeam;
        }
    }

    public void broadcastNextScoreScreen() {
        ModStructures.place(SCORE_ROOM, this.position);

        this.timesInScoreRoom++;
        for (int i = 0; i < Math.min(this.timesInScoreRoom, ELEVATOR_LIGHT_REDSTONE_OFFSETS.size()); i++) {
            ModUtil.placeRedstoneBlock(this.position.add(ELEVATOR_LIGHT_REDSTONE_OFFSETS.get(i)));
        }

//        Used for triggering the redstone timer for the redstone lamps in the build
        ModUtil.placeRedstoneBlock(this.position.add(REDSTONE_OFFSET));
        ModTimer.simple(false, 0.1f, () -> ModUtil.placeAirBlock(this.position.add(REDSTONE_OFFSET)));

        this.minigame.scoreboard().setDisplay(ScoreboardDisplaySlot.BELOW_NAME);

        PriorityQueue<PlayerScore> captainScoreQueue = new PriorityQueue<>(Comparator.comparingInt(playerPos -> -playerPos.score));

        ModUtil.forAllPlayers(player -> {
            if (this.teams.isTeamCaptain(player)) {
                Team team = player.getScoreboardTeam();
                if (team != null) captainScoreQueue.add(new PlayerScore(this.minigame.scoreboard().getScore(team), player));
            } else {
                ModUtil.teleportFacing(player, this.position, Direction.NORTH);
            }
        });

        int position = 1;

        while (!captainScoreQueue.isEmpty()) {
            if (position > 8) break;

            PlayerScore captainScore = captainScoreQueue.poll();
            PlayerEntity captain = captainScore.player;
            int score = captainScore.score;

            Team team = captain.getScoreboardTeam();
            if (team == null) continue;

            this.teams.sendChatMessage(team,
                    Text.translatable("screen." + TriviaMurderParty.ID + ".score", ModUtil.getPositionString(position), score));

            ModUtil.teleportFacing(captain, this.position.add(CAPTAIN_POSITIONS.get(position - 1)), Direction.SOUTH);
            position++;
        }

        Team finalRoundAliveTeam = this.getFinalRoundAliveTeam();
        boolean isFinalRound = finalRoundAliveTeam != null;
        float quipDelaySecs = this.getQuipLength(isFinalRound);
        
        ModTimer.simple(false, quipDelaySecs, () -> {
            if (isFinalRound) {
                this.minigame.startFinalRound();
            } else {
                this.minigame.startNextQuestion();
            }
        });
    }

    private record PlayerScore(Integer score, PlayerEntity player) {}
}
