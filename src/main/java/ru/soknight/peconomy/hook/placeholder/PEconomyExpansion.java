package ru.soknight.peconomy.hook.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.database.DatabaseManager;

public final class PEconomyExpansion extends AbstractExpansion {

    private final Expansion expansion;
    
    public PEconomyExpansion(PEconomy plugin, DatabaseManager databaseManager, CurrenciesManager currenciesManager) {
        super(plugin, databaseManager, currenciesManager);
        this.expansion = new Expansion();
        registerIfNotRegisteredYet(true);
    }

    @Override
    public boolean isRegistered() {
        return expansion.isRegistered();
    }

    @Override
    public void register() {
        expansion.register();
    }

    @Override
    public void unregister() {
        expansion.unregister();
    }

    private Object handlePlaceholderRequest(OfflinePlayer player, String query) {
        if(query.startsWith("balance_"))
            return getBalance(player, query.substring(8).split("_"));
        else if(query.startsWith("has_"))
            return hasAmount(player, query.substring(4).split("_"));
        else if(query.startsWith("top_"))
            return getBalanceTopPlace(query.substring(4).split("_"));
        else if(query.startsWith("toppn_"))
            return getPlayerBalanceTopPlacePosition(player, query.substring(6).split("_"));
        else if(query.startsWith("toppf_"))
            return getPlayerBalanceTopPlaceFormatted(player, query.substring(6).split("_"));

        return "UNKNOWN PLACEHOLDER";
    }

    private final class Expansion extends PlaceholderExpansion {

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

    }

}
