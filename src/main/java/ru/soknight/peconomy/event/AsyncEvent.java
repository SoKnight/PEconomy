package ru.soknight.peconomy.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

public abstract class AsyncEvent extends Event {

    protected AsyncEvent() {
        super(true);
    }

    public void fire() {
        Bukkit.getPluginManager().callEvent(this);
    }

}
