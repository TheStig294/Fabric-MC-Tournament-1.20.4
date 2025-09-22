package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.util.math.BlockPos;
import net.thestig294.mctournament.minigame.MinigameScoreboard;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.KillingRoom;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.KillingRooms;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.structure.ModStructures;
import net.thestig294.mctournament.util.ModUtil;
import org.jetbrains.annotations.Nullable;

public class KillingRoomScreenHandler {
    private final TriviaMurderParty minigame;
    private final MinigameScoreboard scoreboard;
    private BlockPos roomPos;
    @Nullable
    private KillingRoom room;

    public KillingRoomScreenHandler(TriviaMurderParty minigame) {
        this.minigame = minigame;
        this.scoreboard = minigame.scoreboard();
        this.roomPos = BlockPos.ORIGIN;
        this.room = null;
    }

    public void begin(BlockPos pos) {
        this.roomPos = pos;
    }

    public void broadcastNextKillingRoom() {
        this.room = KillingRooms.getNext();
        ModStructures.place(this.room.properties().structure(), this.roomPos);

        ModUtil.forAllPlayers(player -> {
            ModUtil.teleportFacingNorth(player, this.roomPos);
        });

        if (this.room != null) ModNetworking.broadcast(TriviaMurderParty.NetworkIDs.KILLING_ROOM_SCREEN, PacketByteBufs.create()
                .writeString(this.room.properties().id())
        );
    }
}
