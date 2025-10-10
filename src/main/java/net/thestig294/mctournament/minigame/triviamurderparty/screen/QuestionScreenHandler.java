package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.thestig294.mctournament.minigame.MinigameScoreboard;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.question.Questions;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.structure.ModStructures;
import net.thestig294.mctournament.util.ModTimer;
import net.thestig294.mctournament.util.ModUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.thestig294.mctournament.structure.ModStructures.registerJigsawStartPool;

public class QuestionScreenHandler {
    public static final int CORRECT_ANSWER_POINTS = 1000;
    private static final int ANSWERING_TIME_SECONDS = 20;
//    The time in seconds the game secretly lets you answer even though the time is up on the server, for lag compensation
    private static final int ANSWERING_TIME_FORGIVENESS = 2;
    private static final Identifier CORRIDOR_STRUCTURE = registerJigsawStartPool(TriviaMurderParty.ID, "corridor");
    private static final BlockPos LIGHTS_OFF_REDSTONE_BLOCK_OFFSET = new BlockPos(-3, -2, -73);
    private static final BlockPos LIGHTS_ON_REDSTONE_BLOCK_OFFSET = new BlockPos(3, -2, -73);
    private static final BlockPos CORRIDOR_TEAM_OFFSET = new BlockPos(10,0,0);

    private final TriviaMurderParty minigame;
    private final MinigameScoreboard scoreboard;
    private final Map<String, Boolean> answeredCaptains;
    private final Map<String, BlockPos> playerRedstonePositions;
    private State state;
    private BlockPos corridorStartingPos;

    public QuestionScreenHandler() {
        this.minigame = Minigames.TRIVIA_MURDER_PARTY;
        this.scoreboard = minigame.scoreboard();
        this.answeredCaptains = new HashMap<>();
        this.playerRedstonePositions = new HashMap<>();
        this.state = State.PRE_ANSWERING;
        this.corridorStartingPos = BlockPos.ORIGIN;

        ModNetworking.serverReceive(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERING_BEGIN, serverReceiveInfo -> {
            if (this.state != State.PRE_ANSWERING) return;
            this.state = State.ANSWERING;
//            Ending answering time once time runs out, and the forgiveness buffer runs out too
            ModTimer.create(false, ANSWERING_TIME_SECONDS + ANSWERING_TIME_FORGIVENESS,
                    "QuestionScreenAnsweringTimeUp", this::broadcastAnsweringEnd);
        });

        ModNetworking.serverReceive(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERED, serverReceiveInfo -> {
            if (this.state != State.ANSWERING) return;
            PacketByteBuf buffer = serverReceiveInfo.buf();
            String playerName = buffer.readString();
            boolean isCaptain = buffer.readBoolean();
            int answerPosition = buffer.readInt();
            boolean isCorrect = buffer.readBoolean();
            Team team = this.minigame.teams().getTeam(playerName);

            ModNetworking.broadcast(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERED, PacketByteBufs.create()
                    .writeString(playerName)
                    .writeBoolean(isCaptain)
                    .writeInt(answerPosition)
                    .writeBoolean(isCorrect)
            );

            if (isCaptain && !this.answeredCaptains.containsKey(playerName)) {
                this.answeredCaptains.put(playerName, isCorrect);
                if (isCorrect && team != null) this.scoreboard.addScore(team, CORRECT_ANSWER_POINTS);

//                Ending answering once all captains have answered
                int answeredPlayers = this.answeredCaptains.size();
                int playersToAnswer = this.minigame.teams().getTeamCaptains().size();

                if (answeredPlayers >= playersToAnswer) {
                    this.broadcastAnsweringEnd();
                }
            }
        });

        ModNetworking.serverReceive(TriviaMurderParty.NetworkIDs.QUESTION_ALL_CORRECT_LOOP_BACK, serverReceiveInfo -> {
            if (this.state != State.POST_ANSWERING) return;
            for (final var captain : this.minigame.teams().getTeamCaptains()) {
                if (!this.answeredCaptains.getOrDefault(captain.getNameForScoreboard(), false)) return;
            }
            this.broadcastNextQuestionScreen(Entrypoint.QUESTION_NUMBER_IN);
        });

        ModNetworking.serverReceive(TriviaMurderParty.NetworkIDs.QUESTION_TRIGGER_LIGHTS_OFF, serverReceiveInfo -> {
            if (this.state != State.POST_ANSWERING) return;
            ServerPlayerEntity player = serverReceiveInfo.player();
            BlockPos pos = this.playerRedstonePositions.getOrDefault(player.getNameForScoreboard(), player.getBlockPos().add(LIGHTS_OFF_REDSTONE_BLOCK_OFFSET));
            ModUtil.placeRedstoneBlock(pos);
        });

        ModNetworking.serverReceive(TriviaMurderParty.NetworkIDs.QUESTION_SCREEN_START_KILLING_ROOM, serverReceiveInfo -> {
            if (this.state != State.POST_ANSWERING) return;
            this.state = State.DISABLED;

            this.answeredCaptains.forEach((captainName, isCorrect) -> {
                ServerPlayerEntity captain = ModUtil.getServerPlayer(captainName);
                if (captain == null) return;

                Team team = captain.getScoreboardTeam();
                if (team == null) return;
                List<ServerPlayerEntity> teamMembers = this.minigame.teams().getConnectedTeamMembers(team);

                teamMembers.forEach(player ->
                        this.scoreboard.setBoolean(player, TriviaMurderParty.Objectives.IS_CORRECT, isCorrect));
            });

            this.minigame.startKillingRoom();
        });
    }

    public void begin(BlockPos pos) {
        this.corridorStartingPos = pos;
        Questions.shuffleCategory(this.minigame.getVariant());
        this.broadcastNextQuestionScreen(Entrypoint.TITLE_IN);
    }

    public void broadcastNextQuestionScreen() {
        this.broadcastNextQuestionScreen(Entrypoint.SCREEN_IN);
    }

    private void broadcastNextQuestionScreen(Entrypoint entrypoint) {
        this.answeredCaptains.clear();
        ModTimer.remove(false, "QuestionScreenAnsweringTimeUp");
        this.state = State.PRE_ANSWERING;

//        The "Question number" entrypoint for the question screen is only used for asking a new question after all players answered correctly,
//        we don't want to teleport players back to the spawn point of the corridor, because they're already in it!
        if (entrypoint != Entrypoint.QUESTION_NUMBER_IN) {
            this.minigame.teams().forAllConnectedTeamPlayers((team, player) -> {
                int teamNumber = this.minigame.teams().getTeamNumber(team);
                BlockPos playerPos = this.corridorStartingPos.add(CORRIDOR_TEAM_OFFSET.multiply(teamNumber));
                ModUtil.teleportFacing(player, playerPos, Direction.NORTH);

                if (this.minigame.teams().isTeamCaptain(player)) {
                    ModStructures.jigsawPlace(CORRIDOR_STRUCTURE, player);
                    ModUtil.placeRedstoneBlock(playerPos.add(LIGHTS_ON_REDSTONE_BLOCK_OFFSET));
                    this.playerRedstonePositions.put(player.getNameForScoreboard(), playerPos.add(LIGHTS_OFF_REDSTONE_BLOCK_OFFSET));
                }
            });
        }

        ModNetworking.broadcast(TriviaMurderParty.NetworkIDs.QUESTION_SCREEN, PacketByteBufs.create()
                .writeInt(Questions.getNext().id())
                .writeInt(Questions.getQuestionNumber())
                .writeInt(ANSWERING_TIME_SECONDS)
                .writeEnumConstant(entrypoint)
        );
    }

    private void broadcastAnsweringEnd() {
        if (!this.state.equals(State.ANSWERING)) return;
        this.state = State.POST_ANSWERING;
        ModNetworking.broadcast(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERING_END);
    }

    private enum State {
        PRE_ANSWERING,
        ANSWERING,
        POST_ANSWERING,
        DISABLED
    }

    public enum Entrypoint {
        TITLE_IN, // New quiz
        SCREEN_IN, // Returning from a killing room
        QUESTION_NUMBER_IN // All correct loopback
    }
}
