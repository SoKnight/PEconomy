package ru.soknight.peconomy.task;

import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitTask;
import ru.soknight.peconomy.configuration.IntervalsQueue;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

@RequiredArgsConstructor
public final class ReconnectTask {
    private final IntervalsQueue intervalsQueue;
    private final BukkitTaskScheduler taskScheduler;
    private final Logger log;
    private final Callable<Boolean> reconnect;
    private final Callable<Boolean> testConnection;
    private BukkitTask bukkitTask;

    public void startIfNotRunning() {
        if (bukkitTask != null && !bukkitTask.isCancelled())
            return; /* ignore as we are already running */
        log.info("Apparently we lost database connection, scheduling reconnection task...");
        bukkitTask = taskScheduler.asyncLater(this::reconnectOrScheduleNext, intervalsQueue.nextOrLast(), intervalsQueue.intervalTimeUnit());
    }

    private void reconnectOrScheduleNext() {
        boolean isReconnected = false;
        try {
            /* reconnect call is unnecessary as long as database credentials don't update */
            isReconnected = reconnect.call() && testConnection.call();
        } catch (final Exception reconnectException) {
            log.warning(format("Could not reconnect {0}->{1}", reconnectException.getClass().getSimpleName(),
                    reconnectException.getMessage().split("\n+")[0]));
        }
        if (!isReconnected) {
            final long nextAttemptIn = intervalsQueue.nextOrLast();
            final TimeUnit delayUnit = intervalsQueue.intervalTimeUnit();
            bukkitTask = taskScheduler.asyncLater(this::reconnectOrScheduleNext, nextAttemptIn, delayUnit);
            log.warning(format("Could not reconnect, scheduled next attempt in {0} {1}", nextAttemptIn, delayUnit));
        } else {
            log.info("Successfully reconnected.");
            cancel();
        }
    }

    private void cancel() {
        /* #cancel() method can go public later on if needed for a reconnect command
         * although then comes a good question on how they'd obtain this object
         * sounds more like a future me problem */
        if (bukkitTask != null) {
            if (!bukkitTask.isCancelled())
                bukkitTask.cancel();
            bukkitTask = null;
        }
    }
}
