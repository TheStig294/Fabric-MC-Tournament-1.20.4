package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.structure.ModStructures;
import net.thestig294.mctournament.structure.Structure;
import net.thestig294.mctournament.util.ModUtil;

import static net.thestig294.mctournament.structure.ModStructures.registerStructure;

public class ScoreScreenHandler {
    private static final Structure SCORE_ROOM = registerStructure(TriviaMurderParty.ID, "score_room", 0,0,0);

    private BlockPos position;

    public ScoreScreenHandler() {
        this.position = BlockPos.ORIGIN;
    }

    public void begin(BlockPos roomPos) {
        this.position = roomPos;
    }

    public void broadcastNextScoreScreen() {
        ModStructures.place(SCORE_ROOM, this.position);

        ModUtil.forAllPlayers(player -> ModUtil.teleportFacing(player, this.position, Direction.NORTH));
    }
}
