package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.DeathRoom;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.KillingRoom;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.KillingRooms;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.structure.ModStructures;
import net.thestig294.mctournament.util.ModUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KillingRoomScreenHandler {
    private final TriviaMurderParty minigame;
    private BlockPos killingRoomPos;
    private BlockPos deathRoomPos;
    @Nullable
    private KillingRoom killingRoom;
    private int timerIndex;
    private State state;

    public KillingRoomScreenHandler() {
        this.minigame = Minigames.TRIVIA_MURDER_PARTY;
        this.killingRoomPos = BlockPos.ORIGIN;
        this.deathRoomPos = BlockPos.ORIGIN;
        this.killingRoom = null;
        this.timerIndex = -1;
        this.state = State.INTRO;

        ModNetworking.serverReceive(TriviaMurderParty.NetworkIDs.KILLING_ROOM_BEGIN, serverReceiveInfo -> {
            if (this.state != State.INTRO) return;
            this.state = State.ACTIVE;

            if (this.killingRoom != null) {
                this.timerIndex++;
                this.killingRoom.setPosition(this.killingRoomPos);
                this.killingRoom.begin();
                this.broadcastHudTimer();
            }
        });

        ModNetworking.serverReceive(TriviaMurderParty.NetworkIDs.KILLING_ROOM_TIMER_UP, serverReceiveInfo -> {
            int timerIndex = serverReceiveInfo.buf().readInt();
            if (this.state != State.ACTIVE || timerIndex < this.timerIndex) return;

            if (this.killingRoom != null) {
                List<KillingRoom.Timer> timers = this.killingRoom.getTimers();
                String timerName = timers.get(this.timerIndex).name();
                this.killingRoom.timerEnd(timerName);
                this.timerIndex++;

                net.thestig294.mctournament.util.ModTimer.simple(false, this.killingRoom.getTimerQuipLength(timerName), () -> {
                    if (this.timerIndex < timers.size()) {
                        this.broadcastHudTimer();
                    } else {
                        this.state = State.DEATH_ROOM;
                        this.startDeathRoom();
                    }
                });
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, player, alive) -> {
            if (this.minigame.scoreboard().getBoolean(player, TriviaMurderParty.Objectives.IS_DEAD)) {
                DeathRoom.setPlayerInvisible(player);
            }
        });
    }

    public void begin(BlockPos killingRoomPos, BlockPos deathRoomPos) {
        this.killingRoomPos = killingRoomPos;
        this.deathRoomPos = deathRoomPos;
    }

    public void broadcastNextKillingRoom() {
        this.killingRoom = KillingRooms.getNextKillingRoom();
        this.timerIndex = -1;
        this.state = State.INTRO;

        ModStructures.place(this.killingRoom.getStructure(), this.killingRoomPos);
        ModUtil.forAllPlayers(player -> ModUtil.teleportFacing(player, this.killingRoomPos, Direction.NORTH));

        if (this.killingRoom != null) ModNetworking.broadcast(TriviaMurderParty.NetworkIDs.KILLING_ROOM_SCREEN, PacketByteBufs.create()
                .writeEnumConstant(Entrypoint.TITLE_IN)
                .writeString(this.killingRoom.getID())
                .writeInt(this.timerIndex)
        );
    }

    public void broadcastHudTimer() {
        if (this.killingRoom == null || this.killingRoom.getTimers().size() <= this.timerIndex) return;

        ModNetworking.broadcast(TriviaMurderParty.NetworkIDs.KILLING_ROOM_SCREEN, PacketByteBufs.create()
                .writeEnumConstant(Entrypoint.TIMER_IN)
                .writeString(this.killingRoom.getID())
                .writeInt(this.timerIndex)
        );
    }

    private void startDeathRoom() {
        if (this.killingRoom == null) return;

        DeathRoom deathRoom = KillingRooms.getNextDeathRoom();
        deathRoom.setPosition(this.deathRoomPos);
        deathRoom.init();
        this.state = State.DISABLED;
    }

    private enum State {
        INTRO,
        ACTIVE,
        DEATH_ROOM,
        DISABLED
    }

    public enum Entrypoint {
        TITLE_IN, // For a new killing room
        TIMER_IN // For a killing room that requires multiple timers
    }
}
