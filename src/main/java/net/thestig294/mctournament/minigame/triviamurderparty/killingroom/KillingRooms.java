package net.thestig294.mctournament.minigame.triviamurderparty.killingroom;

import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.room.Tattoos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KillingRooms {
    private static final List<KillingRoom> REGISTERED = new ArrayList<>();
    private static int ROOM_INDEX = 0;

    public static final Tattoos TATTOOS = (Tattoos) register(new Tattoos());

    private static KillingRoom register(KillingRoom killingRoom) {
        REGISTERED.add(killingRoom);
        return killingRoom;
    }

    public static void init(boolean isClient) {
        if (isClient) {
            REGISTERED.forEach(KillingRoom::clientInit);
        } else {
            REGISTERED.forEach(KillingRoom::init);
            shuffle();
        }
    }

    public static void shuffle() {
        ROOM_INDEX = 0;
        Collections.shuffle(REGISTERED);
    }

    public static KillingRoom getNext() {
        if (ROOM_INDEX >= REGISTERED.size()) shuffle();

        KillingRoom killingRoom = REGISTERED.get(ROOM_INDEX);
        ROOM_INDEX++;
        return killingRoom;
    }
}
