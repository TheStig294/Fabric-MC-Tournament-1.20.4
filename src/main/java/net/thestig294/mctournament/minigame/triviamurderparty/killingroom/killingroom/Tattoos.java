package net.thestig294.mctournament.minigame.triviamurderparty.killingroom.killingroom;

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
    private static final BlockPos STRUCTURE_OFFSET = new BlockPos(32,2,11);
    private static final List<Timer> TIMERS = List.of(
            new Timer("building", 60),
            new Timer("voting", 60));
    private static final List<Float> DESCRIPTION_LENGTHS = List.of(3.0f, 3.0f);
    private static final BlockPos REDSTONE_OFFSET = new BlockPos(5,7,-8);

    private static final List<BlockPos> BUILD_ROOM_STARTS = List.of(
            new BlockPos(-29,1,1), // 0
            new BlockPos(-17,1,1), // 1
            new BlockPos(-29,1,12), // 2
            new BlockPos(-17,1,12), // 3
            new BlockPos(-5,1,12), // 4
            new BlockPos(-29,1,23), // 5
            new BlockPos(-17,1,23), // 6
            new BlockPos(-5,1,23)  // 7
    );

    private static final List<BlockPos> BUILD_ROOM_ENDS = List.of(
            new BlockPos(-18,5,-8), // 0
            new BlockPos(-6,5,-8), // 1
            new BlockPos(-18,5,2), // 2
            new BlockPos(-6,5,2), // 3
            new BlockPos(5,5,2), // 4
            new BlockPos(-18,5,13), // 5
            new BlockPos(-6,5,13), // 6
            new BlockPos(5,5,13)  // 7
    );

    @Override
    public void init() {

    }

    @Override
    public void begin() {
        this.forAllConnectedTeamPlayers((team, player) -> {
            GameMode gamemode = this.isOnTrial(player) ? GameMode.CREATIVE : GameMode.SPECTATOR;
            ModUtil.setGamemode(player, gamemode);
            int teamNumber = this.tournamentScoreboard().getTeamNumber(team);
            ModUtil.teleportFacing(player, this.getPosition().add(BUILD_ROOM_STARTS.get(teamNumber)), Direction.NORTH);
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
        ModUtil.placeRedstoneBlock(this.getPosition().add(REDSTONE_OFFSET));

        this.forAllConnectedTeamPlayers((team, player) -> {
            player.getInventory().clear();
            ModUtil.setGamemode(player, GameMode.ADVENTURE);
            ModUtil.teleportFacing(player, this.getPosition(), Direction.WEST);
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
                ModUtil.chatMessage(this.tournamentScoreboard().getTeamName(false, i) + " - " + playerCount);
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
