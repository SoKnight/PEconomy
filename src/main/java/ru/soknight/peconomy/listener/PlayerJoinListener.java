package ru.soknight.peconomy.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.event.wallet.WalletCreateEvent;
import ru.soknight.peconomy.event.wallet.holding.HoldingUpdateByUsernameEvent;
import ru.soknight.peconomy.event.wallet.holding.HoldingUpdateByUuidEvent;

public final class PlayerJoinListener implements Listener {

    private final Configuration config;
    private final DatabaseManager databaseManager;
    private final CurrenciesManager currenciesManager;
    
    public PlayerJoinListener(
            @NotNull Plugin plugin,
            @NotNull Configuration config,
            @NotNull DatabaseManager databaseManager,
            @NotNull CurrenciesManager currenciesManager
    ) {
        this.config = config;
        this.databaseManager = databaseManager;
        this.currenciesManager = currenciesManager;
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();

        databaseManager.getWalletUsingActualIdentifier(player)
                .thenAccept(wallet -> validateWalletExistAndHoldingStatus(player, wallet))
                .exceptionally(t -> { t.printStackTrace(); return null; });
    }

    private void validateWalletExistAndHoldingStatus(@NotNull Player player, @Nullable WalletModel wallet) {
        boolean existing = true;

        if(wallet == null) {
            wallet = new WalletModel(player);
            existing = false;
        }

        currenciesManager.getCurrencies().forEach(wallet::loadCurrency);

        // updating holding status
        if(existing && shouldUpdateHoldingStatus()) {
            if(shouldUpdateUsername(player, wallet)) {
                HoldingUpdateByUsernameEvent event = new HoldingUpdateByUsernameEvent(wallet, wallet.getPlayerName(), player.getName());
                event.fireAsync();

                if(!event.isCancelled() && event.isSomethingChanged()) {
                    databaseManager.transferWallet(wallet, event.getCurrent()).join();
                }
            }

            if(shouldUpdateUUID(player, wallet)) {
                HoldingUpdateByUuidEvent event = new HoldingUpdateByUuidEvent(wallet, wallet.getPlayerUUID().orElse(null), player.getUniqueId());
                event.fireAsync();

                if(!event.isCancelled() && event.isSomethingChanged()) {
                    wallet.updateUUID(player.getUniqueId());
                }
            }
        }

        databaseManager.saveWallet(wallet).join();

        if(!existing)
            new WalletCreateEvent(wallet).fireAsync();
    }

    private boolean shouldUpdateUsername(@NotNull Player player, @NotNull WalletModel wallet) {
        if(player.getName().equals(wallet.getPlayerName()))
            return false;

        return config.getBoolean("holding-status-updater.identify-by-uuid", false);
    }

    private boolean shouldUpdateUUID(@NotNull Player player, @NotNull WalletModel wallet) {
        if(!wallet.getPlayerUUID().isPresent())
            return true;

        if(player.getUniqueId().equals(wallet.getPlayerUUID().get()))
            return false;

        return !config.getBoolean("holding-status-updater.identify-by-uuid", false);
    }

    private boolean shouldUpdateHoldingStatus() {
        return config.getBoolean("holding-status-updater.update-on-join", true);
    }
    
}
