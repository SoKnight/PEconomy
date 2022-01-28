package ru.soknight.peconomy.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.PEconomy;

public final class PluginEnableListener implements Listener {

    private final PEconomy plugin;

    public PluginEnableListener(@NotNull PEconomy plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPluginEnable(@NotNull PluginEnableEvent event) {
        String pluginName = event.getPlugin().getName();
        switch (pluginName) {
            case "PlaceholderAPI":
                plugin.registerPlaceholderApiHook();
                break;
            case "Vault":
                plugin.registerVaultEconomyHook();
                break;
            default:
                break;
        }
    }

}
