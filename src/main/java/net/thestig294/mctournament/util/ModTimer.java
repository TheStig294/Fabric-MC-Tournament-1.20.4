package net.thestig294.mctournament.util;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.Comparator;
import java.util.PriorityQueue;

public class ModTimer {
    private static final PriorityQueue<QueuedFunction> SERVER_QUEUE = newQueue();
    private static final PriorityQueue<QueuedFunction> CLIENT_QUEUE = newQueue();

    private static long SERVER_TICKS = 0;
    private static long CLIENT_TICKS = 0;
    private static final int SECONDS_PER_TICK = 20;

    private static PriorityQueue<QueuedFunction> newQueue() {
        return new PriorityQueue<>(Comparator.comparingLong(func -> func.time));
    }

    public static void init(boolean isClient) {
        if (isClient) {
            ClientTickEvents.START_CLIENT_TICK.register(client -> onTick(true, CLIENT_QUEUE));
        } else {
            ServerTickEvents.START_SERVER_TICK.register(server -> onTick(false, SERVER_QUEUE));
        }
    }

    private static void onTick(boolean isClient, PriorityQueue<QueuedFunction> queue) {
        long totalTicks = isClient ? CLIENT_TICKS : SERVER_TICKS;
        QueuedFunction front = queue.peek();

        if (front != null && front.time <= totalTicks) {
            front.function.run();
            queue.remove();
        }

        if (isClient) {
            CLIENT_TICKS++;
        } else {
            SERVER_TICKS++;
        }
    }

    public static void simple(boolean isClient, float secsDelay, Runnable function) {
        long time = isClient ? CLIENT_TICKS : SERVER_TICKS;
        time += (long) (secsDelay * SECONDS_PER_TICK);
        PriorityQueue<QueuedFunction> queue = isClient ? CLIENT_QUEUE : SERVER_QUEUE;
        queue.add(new QueuedFunction(time, function));
    }

    private record QueuedFunction(long time, Runnable function){}
}
