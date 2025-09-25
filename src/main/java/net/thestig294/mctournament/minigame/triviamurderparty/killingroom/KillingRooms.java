package net.thestig294.mctournament.minigame.triviamurderparty.killingroom;

import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.deathroom.Anvil;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.room.Tattoos;

import java.util.*;

public class KillingRooms {
    private static final List<KillingRoom> KILLING_ROOMS = new ArrayList<>();
    private static final List<DeathRoom> DEATH_ROOMS = new ArrayList<>();
    private static int KILLING_ROOM_INDEX = 0;
    private static int DEATH_ROOM_INDEX = 0;
    private static final Map<String, KillingRoom> ID_TO_KILLING_ROOM = new HashMap<>();
    private static final Map<String, DeathRoom> ID_TO_DEATH_ROOM = new HashMap<>();

    public static final Tattoos TATTOOS = (Tattoos) registerKillingRoom(new Tattoos());

    public static final Anvil ANVIL = (Anvil) registerDeathRoom(new Anvil());

    private static KillingRoom registerKillingRoom(KillingRoom killingRoom) {
        KILLING_ROOMS.add(killingRoom);
        ID_TO_KILLING_ROOM.put(killingRoom.getID(), killingRoom);
        return killingRoom;
    }

    private static DeathRoom registerDeathRoom(DeathRoom deathRoom) {
        DEATH_ROOMS.add(deathRoom);
        ID_TO_DEATH_ROOM.put(deathRoom.getID(), deathRoom);
        return deathRoom;
    }

    public static void begin(boolean isClient, TriviaMurderParty minigame) {
        KILLING_ROOMS.forEach((killingRoom) -> {
            killingRoom.setMinigame(minigame);

            if (isClient) {
                killingRoom.clientInit();
            } else {
                killingRoom.setScreenHandler(minigame.getKillingRoomScreenHandler());
                killingRoom.init();
            }
        });

        if (!isClient) shuffleKillingRooms();
    }

    public static void shuffleKillingRooms() {
        KILLING_ROOM_INDEX = 0;
        Collections.shuffle(KILLING_ROOMS);
    }

    public static void shuffleDeathRooms() {
        DEATH_ROOM_INDEX = 0;
        Collections.shuffle(DEATH_ROOMS);
    }

    public static KillingRoom getNextKillingRoom() {
        if (KILLING_ROOM_INDEX >= KILLING_ROOMS.size()) shuffleKillingRooms();

        KillingRoom killingRoom = KILLING_ROOMS.get(KILLING_ROOM_INDEX);
        KILLING_ROOM_INDEX++;
        return killingRoom;
    }

    public static DeathRoom getNextDeathRoom() {
        if (DEATH_ROOM_INDEX >= DEATH_ROOMS.size()) shuffleDeathRooms();

        DeathRoom deathRoom = DEATH_ROOMS.get(DEATH_ROOM_INDEX);
        DEATH_ROOM_INDEX++;
        return deathRoom;
    }

    public static KillingRoom getKillingRoom(String id) {
        return ID_TO_KILLING_ROOM.get(id);
    }

    public static DeathRoom getDeathRoom(String id) {
        return ID_TO_DEATH_ROOM.get(id);
    }
}
