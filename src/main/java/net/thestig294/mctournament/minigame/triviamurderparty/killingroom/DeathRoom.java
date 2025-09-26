package net.thestig294.mctournament.minigame.triviamurderparty.killingroom;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.thestig294.mctournament.minigame.MinigameScoreboard;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.structure.ModStructures;
import net.thestig294.mctournament.structure.Structure;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.util.ModTimer;
import net.thestig294.mctournament.util.ModUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DeathRoom {
    private static final float BEGIN_DELAY = 3.0f;
    private static final float POST_DEATH_DELAY = 3.0f;

    private static final BlockPos DEFAULT_STRUCTURE_OFFSET =
            new BlockPos(0,0,0);
    private static final List<BlockPos> DEFAULT_REDSTONE_OFFSETS = List.of(
            new BlockPos(0,0,0), // 0
            new BlockPos(0,0,0), // 1
            new BlockPos(0,0,0), // 2
            new BlockPos(0,0,0), // 3
            new BlockPos(0,0,0), // 4
            new BlockPos(0,0,0), // 5
            new BlockPos(0,0,0), // 6
            new BlockPos(0,0,0)  // 7
    );
    private static final List<BlockPos> DEFAULT_DEATH_POSITIONS = List.of(
            new BlockPos(0,0,0), // 0
            new BlockPos(0,0,0), // 1
            new BlockPos(0,0,0), // 2
            new BlockPos(0,0,0), // 3
            new BlockPos(0,0,0), // 4
            new BlockPos(0,0,0), // 5
            new BlockPos(0,0,0), // 6
            new BlockPos(0,0,0)  // 7
    );

    public static void setPlayerInvisible(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, StatusEffectInstance.INFINITE));
    }

    private final Structure structure;
    private final Set<Integer> killableTeamNumbers;

    public DeathRoom() {
        this.structure = ModStructures.registerStructure(TriviaMurderParty.ID, "/death_room/" + this.getID(), this.getStructureOffset());
        this.killableTeamNumbers = new HashSet<>();
    }

    public abstract String getID();

    public abstract float getDeathDelay();

    public void init(BlockPos deathRoomPos, TriviaMurderParty minigame) {
        MinigameScoreboard scoreboard = minigame.scoreboard();

        ModStructures.place(this.getStructure(), deathRoomPos);
        List<BlockPos> deathPositions = this.getDeathPositions();
        this.killableTeamNumbers.clear();

        ModUtil.forAllPlayers(player -> {
            BlockPos teleportPos;
            Direction direction;

            if (scoreboard.getBoolean(player, TriviaMurderParty.Objectives.IS_KILLABLE)) {
                int teamNumber = Tournament.inst().scoreboard().getTeamNumber(player);
                this.killableTeamNumbers.add(teamNumber);
                teleportPos = deathRoomPos.add(deathPositions.get(teamNumber));
                direction = Direction.SOUTH;
            } else {
                teleportPos = deathRoomPos;
                direction = Direction.NORTH;
            }

            ModUtil.teleportFacing(player, teleportPos, direction);
        });

        ModTimer.simple(false, BEGIN_DELAY, () -> {
            this.killableTeamNumbers.forEach(teamNumber ->
                    ModUtil.placeRedstoneBlock(deathRoomPos.add(this.getRedstoneOffsets().get(teamNumber))));

            this.begin(deathRoomPos, this.killableTeamNumbers);
        });

        ModTimer.simple(false, this.getDeathDelay(), () -> ModUtil.forAllPlayers(player -> {
            if (scoreboard.getBoolean(player, TriviaMurderParty.Objectives.IS_KILLABLE)) {
                scoreboard.setBoolean(player, TriviaMurderParty.Objectives.IS_KILLABLE, false);
                scoreboard.setBoolean(player, TriviaMurderParty.Objectives.IS_DEAD, true);
                setPlayerInvisible(player);
            }
        }));

        ModTimer.simple(false, this.getDeathDelay() + POST_DEATH_DELAY, minigame::startScoreScreen);
    }

    public abstract void begin(BlockPos roomPos, Set<Integer> killableTeamNumbers);

    public Structure getStructure() {
        return this.structure;
    }

    public BlockPos getStructureOffset() {
        return DEFAULT_STRUCTURE_OFFSET;
    }

    public List<BlockPos> getRedstoneOffsets() {
        return DEFAULT_REDSTONE_OFFSETS;
    }

    public List<BlockPos> getDeathPositions() {
        return DEFAULT_DEATH_POSITIONS;
    }
}
