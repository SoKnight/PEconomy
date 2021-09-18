package ru.soknight.peconomy.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BukkitEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private static final ExecutorService ASYNC_EXECUTOR_SERVICE = Executors.newFixedThreadPool(3);

    protected BukkitEvent() {
        this(true);
    }

    protected BukkitEvent(boolean isAsync) {
        super(isAsync);
    }

    public void fire() {
        Bukkit.getPluginManager().callEvent(this);
    }

    public CompletableFuture<Void> fireAsync() {
        return CompletableFuture.runAsync(this::fire, ASYNC_EXECUTOR_SERVICE);
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

}
