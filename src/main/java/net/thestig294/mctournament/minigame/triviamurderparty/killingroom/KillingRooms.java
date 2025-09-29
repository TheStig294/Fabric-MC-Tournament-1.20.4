package net.thestig294.mctournament.minigame.triviamurderparty.killingroom;

import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.deathroom.Anvil;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.killingroom.Tattoos;

import java.util.*;

public class KillingRooms {
    private static final List<KillingRoom> KILLING_ROOMS = new ArrayList<>();
    private static final List<DeathRoom> DEATH_ROOMS = new ArrayList<>();
    private static int KILLING_ROOM_INDEX = 0;
    private static int DEATH_ROOM_INDEX = 0;
    private static final Map<String, KillingRoom> ID_TO_KILLING_ROOM = new HashMap<>();

    private static void register(KillingRoom killingRoom) {
        KILLING_ROOMS.add(killingRoom);
        ID_TO_KILLING_ROOM.put(killingRoom.getID(), killingRoom);
    }

    private static void register(DeathRoom deathRoom) {
        DEATH_ROOMS.add(deathRoom);
    }

    public static void register() {
        register(new Tattoos());

        register(new Anvil());
    }

    public static void begin(boolean isClient) {
        KILLING_ROOMS.forEach((killingRoom) -> {
            if (isClient) {
                killingRoom.clientInit();
            } else {
                killingRoom.init();
            }
        });

        if (!isClient) shuffleKillingRooms();
        if (!isClient) shuffleDeathRooms();
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
}
