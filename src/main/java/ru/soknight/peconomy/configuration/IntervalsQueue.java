package ru.soknight.peconomy.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class IntervalsQueue {
    private final Queue<Long> intervals = new ConcurrentLinkedQueue<>();
    private final AtomicLong lastRetrieved = new AtomicLong(-1);
    private TimeUnit unit = TimeUnit.SECONDS;

    /**
     *  Retrieves a next interval from this queue, removing queue's head,
     * or returns the last one if there's no intervals left.
     *
     * @return a next interval if able to pop, the last interval retrieved if
     *          preset or a negative number if there were no retrievals yet.
     */
    public long nextOrLast() {
        final Long nextInterval = intervals.poll();
        if (nextInterval != null) {
            lastRetrieved.set(nextInterval);
            return nextInterval;
        }
        return lastRetrieved.get();
    }

    /** Gets a {@link TimeUnit} for all intervals within this queue. */
    public @NotNull TimeUnit intervalTimeUnit() {
        return unit;
    }

    public void resetIntervals(final @NotNull List<Long> intervals) {
        resetIntervals(intervals, unit);
    }

    public synchronized void resetIntervals(final @NotNull List<Long> intervals, final @NotNull TimeUnit unit) {
        this.intervals.clear();
        if (intervals.isEmpty()) {
            lastRetrieved.set(-1);
            return;
        }
        this.intervals.addAll(intervals);
        this.unit = unit;
    }
}

