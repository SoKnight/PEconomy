package ru.soknight.peconomy.hook;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.format.AmountFormatter;
import ru.soknight.peconomy.format.OperatorFormatter;

import java.util.List;

@SuppressWarnings("deprecation")
public final class PEconomyService implements Economy {

    private final VaultEconomyProvider economyProvider;
    private final Configuration config;
    private final Messages messages;

    private final DatabaseManager databaseManager;
    private final CurrenciesManager currenciesManager;
    private final CurrencyInstance currency;
    
    private final String plural;
    private final String singular;
    
    public PEconomyService(
            Plugin plugin,
            VaultEconomyProvider economyProvider,
            Configuration config,
            Messages messages,
            DatabaseManager databaseManager,
            CurrenciesManager currenciesManager
    ) {
        this.economyProvider = economyProvider;
        this.config = config;
        this.messages = messages;
        
        this.databaseManager = databaseManager;
        this.currenciesManager = currenciesManager;
        this.currency = currenciesManager.getVaultCurrency();
        
        this.plural = currenciesManager.getColoredString("vault.plural");
        this.singular = currenciesManager.getColoredString("vault.singular");
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "PEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return economyProvider.getBankingProvider().hasBankSupport();
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return AmountFormatter.format((float) amount);
    }

    @Override
    public String currencyNamePlural() {
        return plural;
    }

    @Override
    public String currencyNameSingular() {
        return singular;
    }

    @Override
    public boolean hasAccount(String playerName) {
        return databaseManager.hasWallet(playerName).join();
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return hasAccount(player.getName());
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player.getName());
    }

    @Override
    public double getBalance(String playerName) {
        if(currency == null)
            return 0;
        
        WalletModel wallet = databaseManager.getWallet(playerName).join();
        if(wallet == null)
            return 0;
        
        return wallet.getAmount(currency.getId());
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return getBalance(player.getName());
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player.getName());
    }

    @Override
    public boolean has(String playerName, double amount) {
        if(currency == null)
            return false;
        
        WalletModel wallet = databaseManager.getWallet(playerName).join();
        if(wallet == null)
            return false;
        
        return wallet.hasAmount(currency.getId(), (float) amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return has(player.getName(), amount);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player.getName(), amount);
    }

    @Override
    @SuppressWarnings("deprecation")
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        if(currency == null)
            return new EconomyResponse(amount, 0, ResponseType.NOT_IMPLEMENTED, null);
        
        WalletModel wallet = databaseManager.getWallet(playerName).join();
        if(wallet == null) {
            String message = messages.getFormatted("error.unknown-wallet", "%player%", playerName);
            return new EconomyResponse(amount, 0D, ResponseType.FAILURE, message);
        }
        
        float pre = wallet.getAmount(this.currency.getId());
        float post = pre - (float) amount;
        
        if(post < 0) {
            String message = messages.getFormatted("take.not-enough",
                    "%amount%", AmountFormatter.format(pre),
                    "%currency%", currency.getSymbol(),
                    "%player%", playerName,
                    "%requested%", format(amount)
            );
            return new EconomyResponse(amount, pre, ResponseType.FAILURE, message);
        }

        TransactionModel transaction = wallet.takeAmount(this.currency.getId(), (float) amount, "#vault");
        databaseManager.saveWallet(wallet);
        
        // saving transaction
        if(config.getBoolean("hooks.vault.transactions", true)) {
            databaseManager.saveTransaction(transaction).join();
            
            OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);
            if(offline != null && offline.isOnline()) {
                messages.sendFormatted(offline.getPlayer(), "take.success.holder",
                        "%amount%", format(amount),
                        "%currency%", currency.getSymbol(),
                        "%player%", playerName,
                        "%from%", AmountFormatter.format(pre),
                        "%operation%", messages.get("operation.decrease"),
                        "%to%", AmountFormatter.format(post),
                        "%source%", OperatorFormatter.format(config, messages, "#vault", offline.getPlayer()),
                        "%id%", transaction.getId()
                );
            }
        }
        
        return new EconomyResponse(amount, post, ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return withdrawPlayer(player.getName(), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player.getName(), amount);
    }

    @Override
    @SuppressWarnings("deprecation")
    public EconomyResponse depositPlayer(String playerName, double amount) {
        if(currency == null)
            return new EconomyResponse(amount, 0, ResponseType.NOT_IMPLEMENTED, null);
        
        WalletModel walletModel = databaseManager.getWallet(playerName).join();
        if(walletModel == null) {
            String message = messages.getFormatted("error.unknown-wallet", "%player%", playerName);
            return new EconomyResponse(amount, 0D, ResponseType.FAILURE, message);
        }
        
        float pre = walletModel.getAmount(this.currency.getId());
        float post = pre + (float) amount;
        
        float limit = currency.getLimit();
        if(limit != 0 && post > limit) {
            String limitstr = AmountFormatter.format(limit);
            String message = messages.getFormatted("add.limit",
                    "%amount%", AmountFormatter.format(pre),
                    "%currency%", currency.getSymbol(),
                    "%player%", playerName,
                    "%limit%", limitstr);
            return new EconomyResponse(amount, pre, ResponseType.FAILURE, message);
        }

        TransactionModel transaction = walletModel.addAmount(this.currency.getId(), (float) amount, "#vault");
        databaseManager.saveWallet(walletModel);
        
        // saving transaction
        if(config.getBoolean("hooks.vault.transactions", true)) {
            databaseManager.saveTransaction(transaction).join();
            
            OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);
            if(offline != null && offline.isOnline()) {
                messages.sendFormatted(offline.getPlayer(), "add.success.holder",
                        "%amount%", format(amount),
                        "%currency%", currency.getSymbol(),
                        "%player%", playerName,
                        "%from%", AmountFormatter.format(pre),
                        "%operation%", messages.get("operation.increase"),
                        "%to%", AmountFormatter.format(post),
                        "%source%", OperatorFormatter.format(config, messages, "#vault", offline.getPlayer()),
                        "%id%", transaction.getId()
                );
            }
        }
        
        return new EconomyResponse(amount, post, ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return depositPlayer(player.getName(), amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player.getName(), amount);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return economyProvider.getBankingProvider().createBank(name, player);
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return economyProvider.getBankingProvider().createBank(name, player);
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return economyProvider.getBankingProvider().deleteBank(name);
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return economyProvider.getBankingProvider().bankBalance(name);
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return economyProvider.getBankingProvider().bankHas(name, amount);
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return economyProvider.getBankingProvider().bankWithdraw(name, amount);
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return economyProvider.getBankingProvider().bankDeposit(name, amount);
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return economyProvider.getBankingProvider().isBankOwner(name, playerName);
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return economyProvider.getBankingProvider().isBankOwner(name, player);
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return economyProvider.getBankingProvider().isBankMember(name, playerName);
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return economyProvider.getBankingProvider().isBankMember(name, player);
    }

    @Override
    public List<String> getBanks() {
        return economyProvider.getBankingProvider().getBanks();
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        if(databaseManager.hasWallet(playerName).join())
            return false;
        
        WalletModel wallet = new WalletModel(playerName);
        currenciesManager.getCurrencies().forEach(wallet::loadCurrency);
        databaseManager.saveWallet(wallet).join();
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return createPlayerAccount(player.getName());
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player.getName());
    }

}
