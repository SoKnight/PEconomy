package ru.soknight.peconomy.listener;

import me.clip.placeholderapi.events.ExpansionsLoadedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.hook.placeholder.PEconomyExpansion;

public final class PapiExpansionsLoadListener implements Listener {

    private final PEconomyExpansion expansion;

    public PapiExpansionsLoadListener(@NotNull Plugin plugin, @NotNull PEconomyExpansion expansion) {
        this.expansion = expansion;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExpansionsLoad(@NotNull ExpansionsLoadedEvent event) {
        expansion.registerIfNotRegisteredYet(false);
    }

}
