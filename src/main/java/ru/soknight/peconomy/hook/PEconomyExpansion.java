package ru.soknight.peconomy.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.balancetop.BalanceTopPlace;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.WalletModel;

import java.util.Optional;

public final class PEconomyExpansion extends PlaceholderExpansion {
    
    private final PEconomy plugin;
    private final DatabaseManager databaseManager;
    private final CurrenciesManager currenciesManager;
    
    public PEconomyExpansion(PEconomy plugin, DatabaseManager databaseManager, CurrenciesManager currenciesManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.currenciesManager = currenciesManager;

        if(isRegistered())
            unregister();
        
        register();
    }
    
    @Override
    public @NotNull String getAuthor() {
        return "SoKnight";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "peco";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String query) {
        return onRequest(player, query);
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String query) {
        return String.valueOf(handlePlaceholderRequest(player, query));
    }

    private Object handlePlaceholderRequest(OfflinePlayer player, String query) {
        if(query.startsWith("balance_"))
            return getBalance(player, query.substring(8));
        else if(query.startsWith("has_"))
            return hasAmount(player, query.substring(4));
        else if(query.startsWith("top_"))
            return getBalanceTopPlace(player, query.substring(4));
        else if(query.startsWith("toppn_"))
            return getPlayerBalanceTopPlace(player, query.substring(6), false);
        else if(query.startsWith("toppf_"))
            return getPlayerBalanceTopPlace(player, query.substring(6), true);

        return "UNKNOWN PLACEHOLDER";
    }

    private String getBalance(OfflinePlayer player, String currencyId) {
        if(player == null || currencyId.isEmpty())
            return "";

        WalletModel wallet = databaseManager.getWallet(player.getName()).join();
        return plugin.getFormatter().formatAmount(wallet != null ? wallet.getAmount(currencyId) : 0F);
    }

    private boolean hasAmount(OfflinePlayer player, String query) {
        if(player == null)
            return false;

        try {
            String[] parts = query.split("_");
            String currencyId = parts[0];
            float amount = Float.parseFloat(parts[1]);

            if(currencyId.isEmpty() || amount <= 0F)
                return false;

            WalletModel wallet = databaseManager.getWallet(player.getName()).join();
            return wallet != null && wallet.hasAmount(currencyId, amount);
        } catch (Exception ignored) {
            return false;
        }
    }

    private String getBalanceTopPlace(OfflinePlayer player, String query) {
        if(player == null)
            return "";

        try {
            String[] parts = query.split("_");
            String currencyId = parts[0];
            int position = Integer.parseInt(parts[1]);

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

    private Object getPlayerBalanceTopPlace(OfflinePlayer player, String currencyId, boolean format) {
        if(player == null || currencyId.isEmpty())
            return "";

        try {
            CurrencyInstance currency = currenciesManager.getCurrency(currencyId);
            if(currency == null || !currency.useBalanceTop())
                return "";

            WalletModel wallet = databaseManager.getOrCreateWallet(player.getName()).join();
            if(wallet == null)
                return "";

            Optional<BalanceTopPlace> place = currency.getBalanceTop().getPlace(wallet);
            if(!place.isPresent())
                return "";

            if(format)
                return currenciesManager.formatPlace(currency.getBalanceTopSetup(), place.get());
            else
                return place.get().getPosition();
        } catch (Exception ignored) {
            return "";
        }
    }

}
