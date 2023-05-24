package ru.soknight.peconomy.hook.placeholder;

import org.bukkit.OfflinePlayer;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.balancetop.BalanceTopPlace;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.WalletModel;

import java.util.Optional;

public abstract class AbstractExpansion {

    protected final PEconomy plugin;
    protected final DatabaseManager databaseManager;
    protected final CurrenciesManager currenciesManager;

    public AbstractExpansion(PEconomy plugin, DatabaseManager databaseManager, CurrenciesManager currenciesManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.currenciesManager = currenciesManager;
    }

    public abstract boolean isRegistered();

    public abstract void register();

    public abstract void unregister();

    public void registerIfNotRegisteredYet(boolean unregister) {
        if(isRegistered()) {
            if(unregister) {
                unregister();
            } else {
                return;
            }
        }

        register();
    }

    protected String getBalance(OfflinePlayer player, String[] arguments) {
        if(player == null || arguments.length != 1)
            return "";

        String currencyId = arguments[0];
        if(currencyId.isEmpty())
            return "";

        WalletModel wallet = databaseManager.getWallet(player.getName()).join();
        return plugin.getFormatter().formatAmount(wallet != null ? wallet.getAmount(currencyId) : 0F);
    }

    protected boolean hasAmount(OfflinePlayer player, String[] arguments) {
        if(player == null || arguments.length != 2)
            return false;

        try {
            String currencyId = arguments[0];
            float amount = Float.parseFloat(arguments[1]);

            if(currencyId.isEmpty() || amount <= 0F)
                return false;

            WalletModel wallet = databaseManager.getWallet(player.getName()).join();
            return wallet != null && wallet.hasAmount(currencyId, amount);
        } catch (Exception ignored) {
            return false;
        }
    }

    protected String getBalanceTopPlace(String[] arguments) {
        if (arguments.length != 2)
            return "";

        try {
            String currencyId = arguments[0];
            int position = Integer.parseInt(arguments[1]);

            if(currencyId.isEmpty() || position < 1)
                return "";

            CurrencyInstance currency = currenciesManager.getCurrency(currencyId);
            if(currency == null || !currency.useBalanceTop())
                return "";

            return currency.getBalanceTop().getPlaceFormatted(position - 1);
        } catch (Exception ignored) {
            return "";
        }
    }

    protected String getPlayerBalanceTopPlacePosition(OfflinePlayer player, String[] arguments) {
        return getPlayerBalanceTopPlace(player, arguments)
                .map(BalanceTopPlace::getPosition)
                .map(String::valueOf)
                .orElse("");
    }

    protected String getPlayerBalanceTopPlaceFormatted(OfflinePlayer player, String[] arguments) {
        return getPlayerBalanceTopPlace(player, arguments)
                .map(currenciesManager::formatPlace)
                .orElse("");
    }

    protected Optional<BalanceTopPlace> getPlayerBalanceTopPlace(OfflinePlayer player, String[] arguments) {
        if (player == null || arguments.length != 1)
            return Optional.empty();

        String currencyId = arguments[0];
        if(currencyId.isEmpty())
            return Optional.empty();

        try {
            CurrencyInstance currency = currenciesManager.getCurrency(currencyId);
            if(currency == null || !currency.useBalanceTop())
                return Optional.empty();

            WalletModel wallet = databaseManager.getOrCreateWallet(player).join();
            if(wallet == null)
                return Optional.empty();

            return currency.getBalanceTop().getPlace(wallet);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

}
