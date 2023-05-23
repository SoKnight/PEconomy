package ru.soknight.peconomy.task;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public final class BukkitTaskScheduler {
    private final Plugin plugin;

    public @Nullable BukkitTask asyncLater(final @NotNull Runnable task, final long delay, final @NotNull TimeUnit unit) {
        return delay >= 0 ? plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, task, unit.toSeconds(delay) * 20L) : null;
    }
}
