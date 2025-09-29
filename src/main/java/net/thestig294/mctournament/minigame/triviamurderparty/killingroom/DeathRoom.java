package net.thestig294.mctournament.minigame.triviamurderparty.killingroom;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.thestig294.mctournament.minigame.MinigameScoreboard;
import net.thestig294.mctournament.minigame.Minigames;
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
    private static final BlockPos DEFAULT_STRUCTURE_OFFSET = new BlockPos(7,0,15);

    private static final List<BlockPos> DEFAULT_REDSTONE_OFFSETS = List.of(
            new BlockPos(-2,20,-4), // 0
            new BlockPos(2,20,-4), // 1
            new BlockPos(-4,20,-8), // 2
            new BlockPos(0,20,-8), // 3
            new BlockPos(4,20,-8), // 4
            new BlockPos(-4,20,-12), // 5
            new BlockPos(0,20,-12), // 6
            new BlockPos(4,20,-12)  // 7
    );

    private static final List<BlockPos> DEFAULT_DEATH_POSITIONS = List.of(
            new BlockPos(-2,0,-4), // 0
            new BlockPos(2,0,-4), // 1
            new BlockPos(-4,0,-8), // 2
            new BlockPos(0,0,-8), // 3
            new BlockPos(4,0,-8), // 4
            new BlockPos(-4,0,-12), // 5
            new BlockPos(0,0,-12), // 6
            new BlockPos(4,0,-12)  // 7
    );

    public static void setPlayerInvisible(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, StatusEffectInstance.INFINITE));
    }

    private final Structure structure;
    private final Set<Integer> killableTeamNumbers;
    private final TriviaMurderParty minigame;
    private BlockPos position;

    public DeathRoom() {
        this.structure = ModStructures.registerStructure(TriviaMurderParty.ID, "/death_room/" + this.getID(), this.getStructureOffset());
        this.killableTeamNumbers = new HashSet<>();
        this.minigame = Minigames.TRIVIA_MURDER_PARTY;
        this.position = BlockPos.ORIGIN;
    }

    public abstract String getID();

    public abstract float getDeathDelay();

    public void init() {
        MinigameScoreboard scoreboard = this.minigame().scoreboard();

        ModStructures.place(this.getStructure(), this.getPosition());
        List<BlockPos> deathPositions = this.getDeathPositions();
        this.killableTeamNumbers.clear();

        ModUtil.forAllPlayers(player -> {
            BlockPos teleportPos;
            Direction direction;

            if (scoreboard.getBoolean(player, TriviaMurderParty.Objectives.IS_KILLABLE)) {
                int teamNumber = Tournament.inst().scoreboard().getTeamNumber(player);
                this.killableTeamNumbers.add(teamNumber);
                teleportPos = this.getPosition().add(deathPositions.get(teamNumber));
                direction = Direction.SOUTH;
            } else {
                teleportPos = this.getPosition();
                direction = Direction.NORTH;
            }

            ModUtil.teleportFacing(player, teleportPos, direction);
        });

        ModTimer.simple(false, BEGIN_DELAY, () -> {
            this.killableTeamNumbers.forEach(teamNumber ->
                    ModUtil.placeRedstoneBlock(this.getPosition().add(this.getRedstoneOffsets().get(teamNumber))));

            this.begin();
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

    public abstract void begin();

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

    public void setPosition(BlockPos deathRoomPos) {
        this.position = deathRoomPos;
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public TriviaMurderParty minigame() {
        return this.minigame;
    }

    @SuppressWarnings("unused")
    public Set<Integer> getKillableTeamNumbers() {
        return this.killableTeamNumbers;
    }
}
