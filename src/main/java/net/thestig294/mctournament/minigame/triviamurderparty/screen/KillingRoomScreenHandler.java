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
    private int timerIndex;
    private State state;

    public KillingRoomScreenHandler(TriviaMurderParty minigame) {
        this.minigame = minigame;
        this.scoreboard = minigame.scoreboard();
        this.roomPos = BlockPos.ORIGIN;
        this.room = null;
        this.timerIndex = 0;
        this.state = State.INTRO;

        ModNetworking.serverReceive(TriviaMurderParty.NetworkIDs.KILLING_ROOM_BEGIN, serverReceiveInfo -> {
            if (this.state != State.INTRO) return;
            this.state = State.TIMER;
            if (this.room != null) this.room.begin();
        });

        ModNetworking.serverReceive(TriviaMurderParty.NetworkIDs.KILLING_ROOM_TIMER_UP, serverReceiveInfo -> {
            if (this.state != State.TIMER) return;
            this.state = State.POST_TIMER;
            if (this.room != null) this.room.timerEnd(this.timerIndex);
        });
    }

    public void begin(BlockPos pos) {
        this.roomPos = pos;
    }

    public void broadcastNextKillingRoom() {
        this.timerIndex = 0;

        this.room = KillingRooms.getNext();
        this.room.setScreenHandler(this);
        this.room.setMinigame
        ModStructures.place(this.room.properties().structure(), this.roomPos);

        ModUtil.forAllPlayers(player -> {
            ModUtil.teleportFacingNorth(player, this.roomPos);
        });

        if (this.room != null) ModNetworking.broadcast(TriviaMurderParty.NetworkIDs.KILLING_ROOM_SCREEN, PacketByteBufs.create()
                .writeString(this.room.properties().id())
                .writeInt(this.room.properties().timerLength())
                .writeInt(this.timerIndex)
                .writeEnumConstant(Entrypoint.TITLE_IN)
        );
    }

    public void broadcastHudTimer() {
        if (this.room == null) return;
        this.broadcastHudTimer(this.room.properties().timerLength());
    }

    public void broadcastHudTimer(int timerLength) {
        if (this.room == null) return;
        this.timerIndex++;

        ModNetworking.broadcast(TriviaMurderParty.NetworkIDs.KILLING_ROOM_SCREEN, PacketByteBufs.create()
                .writeString(this.room.properties().id())
                .writeInt(timerLength)
                .writeInt(this.timerIndex)
                .writeEnumConstant(Entrypoint.TIMER_IN)
        );
    }

    public BlockPos getRoomPos() {
        return this.roomPos;
    }

    private enum State {
        INTRO,
        TIMER,
        POST_TIMER,
        DISABLED
    }

    public enum Entrypoint {
        TITLE_IN, // For a new killing room
        TIMER_IN // For a killing room that requires multiple timers
    }
}
