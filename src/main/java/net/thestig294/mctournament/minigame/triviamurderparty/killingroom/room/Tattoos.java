package net.thestig294.mctournament.minigame.triviamurderparty.killingroom.room;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.KillingRoom;
import net.thestig294.mctournament.util.ModUtil;

import java.util.List;

public class Tattoos extends KillingRoom {
    private static final BlockPos STRUCTURE_OFFSET = new BlockPos(0,0,0);
    private static final List<Timer> TIMERS = List.of(
            new Timer("building", 60),
            new Timer("voting", 20));
    private static final List<Float> DESCRIPTION_LENGTHS = List.of(3.0f, 3.0f);

    private static final List<BlockPos> BUILD_ROOM_STARTS = List.of(
            new BlockPos(0,0,0), // 0
            new BlockPos(0,0,0), // 1
            new BlockPos(0,0,0), // 2
            new BlockPos(0,0,0), // 3
            new BlockPos(0,0,0), // 4
            new BlockPos(0,0,0), // 5
            new BlockPos(0,0,0), // 6
            new BlockPos(0,0,0)  // 7
    );

    private static final List<BlockPos> BUILD_ROOM_ENDS = List.of(
            new BlockPos(0,0,0), // 0
            new BlockPos(0,0,0), // 1
            new BlockPos(0,0,0), // 2
            new BlockPos(0,0,0), // 3
            new BlockPos(0,0,0), // 4
            new BlockPos(0,0,0), // 5
            new BlockPos(0,0,0), // 6
            new BlockPos(0,0,0)  // 7
    );

    @Override
    public void init() {

    }

    @Override
    public void begin(BlockPos roomPos) {
        this.forAllConnectedTeamPlayers((team, player) -> {
            GameMode gamemode = this.isOnTrial(player) ? GameMode.CREATIVE : GameMode.SPECTATOR;
            ModUtil.setGamemode(player, gamemode);
            int teamNumber = this.tournamentScoreboard().getTeamNumber(team);
            BlockPos offsetPosition = roomPos.add(BUILD_ROOM_STARTS.get(teamNumber));
            ModUtil.teleportFacing(player, offsetPosition, Direction.SOUTH);
        });
    }

    @Override
    public void timerEnd(String timerName) {
        switch (timerName) {
            case "building" -> this.startVoting();
            case "voting" -> this.endVoting();
        }
    }

    private void startVoting() {
        this.forAllConnectedTeamPlayers((team, player) -> {
            player.getInventory().clear();
            ModUtil.setGamemode(player, GameMode.ADVENTURE);
            ModUtil.teleportFacing(player, this.getStructureOffset(), Direction.WEST);
        });
    }

    private void endVoting() {
        ModUtil.chatMessage("Votes:");
        int minVotes = ModUtil.getPlayers().size();
        Team killableTeam = null;

        for (int i = 0; i < BUILD_ROOM_STARTS.size(); i++) {
            Team team = this.tournamentScoreboard().getTeam(i);
            if (!this.tournamentScoreboard().isTeamConnected(team)) continue;

            int playerCount = ModUtil.getPlayersWithinBound(BUILD_ROOM_STARTS.get(i), BUILD_ROOM_ENDS.get(i)).size();

            if (playerCount < minVotes) {
                minVotes = playerCount;
                killableTeam = this.tournamentScoreboard().getTeam(i);
            }

            if (playerCount > 0) {
                ModUtil.chatMessage(this.tournamentScoreboard().getTeamName(i) + " - " + playerCount);
            }
        }

        if (killableTeam == null) killableTeam = this.tournamentScoreboard().getRandomConnectedTeam();
        if (killableTeam != null) {
            this.setTeamKillable(killableTeam);
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientInit() {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientBegin() {

    }

    @Override
    public BlockPos getStructureOffset() {
        return STRUCTURE_OFFSET;
    }

    @Override
    public String getID() {
        return "tattoos";
    }

    @Override
    public List<Timer> getTimers() {
        return TIMERS;
    }

    @Override
    public float getTimerQuipLength(String timerName) {
        return 2.0f;
    }

    @Override
    public List<Float> getDescriptionLengths() {
        return DESCRIPTION_LENGTHS;
    }
}
