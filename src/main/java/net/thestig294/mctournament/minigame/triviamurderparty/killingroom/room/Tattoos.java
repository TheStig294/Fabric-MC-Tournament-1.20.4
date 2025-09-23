package net.thestig294.mctournament.minigame.triviamurderparty.killingroom.room;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.KillingRoom;
import net.thestig294.mctournament.util.ModTimer;
import net.thestig294.mctournament.util.ModUtil;

import java.util.List;

public class Tattoos extends KillingRoom {
    private static final List<BlockPos> BUILD_ROOM_OFFSETS = List.of(
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
    public Properties properties() {
        return new Properties(0,0,0, "tattoos", 30, 2.0f, 3.0f, 3.0f);
    }

    @Override
    public void init() {

    }

    @Override
    public void begin() {
        BlockPos roomPos = this.screenHandler().getRoomPos();

        this.tournamentScoreboard().forAllConnectedTeamPlayers((team, player) -> {
            if (!this.minigame().isPlayerDead(player)) {
                ModUtil.runConsoleCommand(player, "/gamemode creative");
            }
            int teamNumber = this.tournamentScoreboard().getTeamNumber(team);
            BlockPos offsetPosition = roomPos.add(BUILD_ROOM_OFFSETS.get(teamNumber));
            ModUtil.teleportFacingNorth(player, offsetPosition);
        });

        this.screenHandler().broadcastHudTimer();
    }

    @Override
    public void timerEnd(int timerIndex) {
        ModTimer.simple(false, this.properties().timerQuipLength(), () -> {
            this.tournamentScoreboard().forAllConnectedTeamPlayers((team, player) -> {

            });
        });
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientInit() {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientBegin() {

    }
}
