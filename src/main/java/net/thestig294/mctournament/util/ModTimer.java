package net.thestig294.mctournament.util;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.PriorityQueue;

public class ModTimer {
    private static final PriorityQueue<QueuedFunction> SERVER_QUEUE = newQueue();
    private static final PriorityQueue<QueuedFunction> CLIENT_QUEUE = newQueue();

    private static long SERVER_TICKS = 0;
    private static long CLIENT_TICKS = 0;

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
            QueuedFunction repeat = front.getRepeat(totalTicks);
            if (repeat != null) {
                queue.add(repeat);
            }
            queue.remove();
        }

        if (isClient) {
            CLIENT_TICKS++;
        } else {
            SERVER_TICKS++;
        }
    }

    private static PriorityQueue<QueuedFunction> getQueue(boolean isClient) {
        return isClient ? CLIENT_QUEUE : SERVER_QUEUE;
    }

    private static long getTime(boolean isClient, float secsDelay) {
        long time = isClient ? CLIENT_TICKS : SERVER_TICKS;
        float ticksPerSecond = isClient ? ModUtilClient.getTicksPerSecond() : ModUtil.getTicksPerSecond();
        time += (long) (secsDelay * ticksPerSecond);
        return time;
    }

    public static void simple(boolean isClient, float secsDelay, Runnable function) {
        getQueue(isClient).add(new QueuedFunction(getTime(isClient, secsDelay), function));
    }

    public static void create(boolean isClient, String id, float secsDelay, int repeatCount, Runnable function) {
        getQueue(isClient).add(new QueuedFunction(getTime(isClient, secsDelay), function, id, secsDelay, repeatCount));
    }

    public static void remove(boolean isClient, String id) {
        getQueue(isClient).removeIf(queuedFunction -> queuedFunction.id.equals(id));
    }

    private record QueuedFunction(long time, Runnable function, String id, float delay, int repeats){
        public QueuedFunction(long time, Runnable function) {
            this(time, function, "", 0.0f, 0);
        }

        public @Nullable QueuedFunction getRepeat(long currTicks) {
            return this.repeats > 0 ?
                    new QueuedFunction((long) (currTicks + this.delay), this.function, this.id, this.delay, this.repeats - 1)
                    : null;
        }
    }
}
