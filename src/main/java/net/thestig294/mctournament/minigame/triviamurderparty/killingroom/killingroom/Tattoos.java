package net.thestig294.mctournament.minigame.triviamurderparty.killingroom.killingroom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.KillingRoom;
import net.thestig294.mctournament.util.ModUtil;

import java.util.*;

public class Tattoos extends KillingRoom {
    private static final BlockPos STRUCTURE_OFFSET = new BlockPos(32,2,11);
    private static final List<Timer> TIMERS = List.of(
            new Timer("building", 5),
            new Timer("voting", 5));
    private static final List<Float> DESCRIPTION_LENGTHS = List.of(1.0f, 1.0f);
    private static final BlockPos REDSTONE_OFFSET = new BlockPos(5,7,-8);

    private static final List<BlockPos> BUILD_ROOM_STARTS = List.of(
            new BlockPos(-29,0,1), // 0
            new BlockPos(-17,0,1), // 1
            new BlockPos(-29,0,12), // 2
            new BlockPos(-17,0,12), // 3
            new BlockPos(-5,0,12), // 4
            new BlockPos(-29,0,23), // 5
            new BlockPos(-17,0,23), // 6
            new BlockPos(-5,0,23)  // 7
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
        this.teams().forAllConnectedTeamPlayers((team, player) -> {
            GameMode gamemode = this.isOnTrial(player) ? GameMode.CREATIVE : GameMode.SPECTATOR;
            ModUtil.setGamemode(player, gamemode);
            int teamNumber = this.teams().getTeamNumber(team);
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

        this.teams().forAllConnectedTeamPlayers((team, player) -> {
            player.getInventory().clear();
            ModUtil.setGamemode(player, GameMode.ADVENTURE);
            ModUtil.teleportFacing(player, this.getPosition(), Direction.WEST);
        });
    }

    private void endVoting() {
        ModUtil.broadcastChatMessage("= Votes =");
        int minVotes = ModUtil.getPlayers().size() + 1;
        List<Team> killableTeams = new ArrayList<>();

        for (final var player : ModUtil.getPlayers()) {
            Team team = player.getScoreboardTeam();
            if (!this.isOnTrial(player) || team == null) continue;

            int teamNumber = this.teams().getTeamNumber(team);

            int playerCount = ModUtil.getPlayersWithinBound(
                    this.getPosition().add(BUILD_ROOM_STARTS.get(teamNumber)).down(),
                    this.getPosition().add(BUILD_ROOM_ENDS.get(teamNumber)))
                    .size();

            if (playerCount < minVotes) {
                minVotes = playerCount;
                killableTeams.clear();
                killableTeams.add(team);
            } else if (playerCount == minVotes) {
                killableTeams.add(team);
            }

            ModUtil.broadcastChatMessage(this.teams().getTeamName(team) + ": " + playerCount);
        }

//        If teams tie votes, a random team is killed instead
        Team randomTeam = ModUtil.getRandomElement(false, killableTeams);
        if (randomTeam == null) return;
        this.setTeamKillable(randomTeam);
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
