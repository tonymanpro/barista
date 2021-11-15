package de.waldorfaugsburg.barista.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class Scheduler {

    private static final ScheduledExecutorService SERVICE = Executors.newScheduledThreadPool(8);

    private Scheduler() {

    }

    public static ScheduledFuture<?> runLater(final Runnable runnable, final long delay) {
        return SERVICE.schedule(() -> runWitTryCatch(runnable), delay, TimeUnit.MILLISECONDS);
    }

    public static ScheduledFuture<?> schedule(final Runnable runnable, final long period) {
        return schedule(() -> runWitTryCatch(runnable), 0, period);
    }

    public static ScheduledFuture<?> schedule(final Runnable runnable, final long delay, final long period) {
        return SERVICE.scheduleAtFixedRate(() -> runWitTryCatch(runnable), delay, period, TimeUnit.MILLISECONDS);
    }

    private static void runWitTryCatch(final Runnable runnable) {
        try {
            runnable.run();
        } catch (final Exception e) {
            log.error("An error occurred while running scheduled task", e);
        }
    }
}