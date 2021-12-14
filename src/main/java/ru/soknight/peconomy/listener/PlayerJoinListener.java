package ru.soknight.peconomy.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.event.wallet.WalletCreateEvent;

public final class PlayerJoinListener implements Listener {

    private final DatabaseManager databaseManager;
    private final CurrenciesManager currenciesManager;
    
    public PlayerJoinListener(
            @NotNull Plugin plugin,
            @NotNull DatabaseManager databaseManager,
            @NotNull CurrenciesManager currenciesManager
    ) {
        this.databaseManager = databaseManager;
        this.currenciesManager = currenciesManager;
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // moved to async task
        databaseManager.getWallet(player.getName()).thenAccept(wallet -> {
            boolean existing = true;

            if(wallet == null) {
                wallet = new WalletModel(player);
                existing = false;
            }
            
            currenciesManager.getCurrencies().forEach(wallet::loadCurrency);
            databaseManager.saveWallet(wallet).join();

            if(!existing)
                new WalletCreateEvent(wallet).fireAsync();
        });
    }
    
}
