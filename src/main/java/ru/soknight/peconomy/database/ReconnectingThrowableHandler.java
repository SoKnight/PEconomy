package ru.soknight.peconomy.database;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import org.jetbrains.annotations.NotNull;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.executable.quiet.ThrowableHandler;
import ru.soknight.peconomy.configuration.HookingConfiguration;
import ru.soknight.peconomy.configuration.IntervalsQueue;
import ru.soknight.peconomy.task.BukkitTaskScheduler;
import ru.soknight.peconomy.task.ReconnectTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

/**
 *  Initiates database reconnection if detects a communications
 * link failure or a closed connection.
 */
public final class ReconnectingThrowableHandler implements ThrowableHandler {
    public static final String DO_RECONNECT_PATH = "database.reconnect.auto";
    public static final boolean DO_RECONNECT_DEFAULT = false;
    public static final String RECONNECTION_INTERVALS_PATH = "database.reconnect.intervals-in-seconds";
    public static final TimeUnit DEFAULT_INTERVALS_TIME_UNIT = TimeUnit.SECONDS;

    private final Supplier<Boolean> isEnabledSupplier;
    private final IntervalsQueue intervalsQueue = new IntervalsQueue();
    private final ReconnectTask reconnectTask;
    private final Logger log;

    public ReconnectingThrowableHandler(
            final @NotNull HookingConfiguration config,
            final @NotNull BukkitTaskScheduler taskScheduler,
            final @NotNull Logger log,
            final @NotNull Callable<Boolean> reconnect,
            final @NotNull Callable<Boolean> testConnection) {
        this.isEnabledSupplier = () -> config.getBoolean(DO_RECONNECT_PATH, DO_RECONNECT_DEFAULT);
        this.log = log;
        updateIntervals(config);
        config.subscribeOnRefreshed(this::updateIntervals);
        this.reconnectTask = new ReconnectTask(intervalsQueue, taskScheduler, log, reconnect, testConnection);
    }

    @Override public void handle(final @NotNull Throwable th) {
        if (!isEnabledSupplier.get()) {
            log.info("Auto reconnect was disabled, skipping reconnection task setup.");
            reportException(th);
            return;
        }
        if (isConnectionFailureMessage(th, th.getMessage().toLowerCase()))
            reconnectTask.startIfNotRunning();
        else
            reportException(th);
    }

    private void reportException(final @NotNull Throwable th) {
        log.warning(format("Exception on db query {0}: {1}.", th.getClass(), th.getMessage()));
    }

    private boolean isConnectionFailureMessage(final Throwable th, final String message) {
        return th instanceof CommunicationsException
                || message.contains("communications link failure")
                /* will be funny if the message was like "connection established, file closed" :clown: */
                || (message.contains("connection") && message.contains("closed"));
    }

    private void updateIntervals(final Configuration configuration) {
        List<String> strIntervals = configuration.getList(RECONNECTION_INTERVALS_PATH);
        List<Long> intervals = new ArrayList<>(strIntervals.size());
        for (final String strInterval : strIntervals) {
            try {
                intervals.add(Long.parseLong(strInterval));
            } catch (final NumberFormatException nfe) {
                log.warning(format("Invalid interval {0}->{1}", nfe.getClass().getSimpleName(), nfe.getMessage()));
            }
        }
        this.intervalsQueue.resetIntervals(intervals, DEFAULT_INTERVALS_TIME_UNIT);
    }
}
