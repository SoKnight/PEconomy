package ru.soknight.peconomy;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import ru.soknight.peconomy.api.BankingProvider;
import ru.soknight.peconomy.api.PEconomyAPI;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.hook.VaultEconomyProvider;

import java.util.Collection;
import java.util.Collections;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class PEconomyAPIImpl implements PEconomyAPI {

    private final DatabaseManager databaseManager;
    private final CurrenciesManager currenciesManager;
    private final VaultEconomyProvider economyProvider;

    @Override
    public void registerBankingProvider(BankingProvider provider) {
        economyProvider.registerBankingProvider(provider);
    }

    @Override
    public void unregisterBankingProvider() {
        economyProvider.unregisterBankingProvider();
    }

    @Override
    public long getWalletsCount() {
        return databaseManager.getWalletsCount().join();
    }

    @Override
    public boolean hasWallet(String player) {
        return databaseManager.hasWallet(player).join();
    }

    @Override
    public WalletModel getWallet(String player) {
        return databaseManager.getWallet(player).join();
    }

    @Override
    public void updateWallet(WalletModel wallet) {
        databaseManager.saveWallet(wallet).join();
    }

    @Override
    public WalletModel addAmount(String player, String currency, float amount) {
        WalletModel wallet = getWallet(player);
        if(wallet == null) return null;

        wallet.addAmount(currency, amount);
        return wallet;
    }

    @Override
    public float getAmount(String player, String currency) {
        WalletModel wallet = getWallet(player);
        return wallet != null ? wallet.getAmount(currency) : 0F;
    }

    @Override
    public boolean hasAmount(String player, String currency, float amount) {
        WalletModel wallet = getWallet(player);
        return wallet != null && wallet.hasAmount(currency, amount);
    }

    @Override
    public WalletModel setAmount(String player, String currency, float amount) {
        WalletModel wallet = getWallet(player);
        if(wallet == null) return null;

        wallet.addAmount(currency, amount);
        return wallet;
    }

    @Override
    public WalletModel resetAmount(String player, String currency) {
        WalletModel wallet = getWallet(player);
        if(wallet == null) return null;

        wallet.resetWallet(currency);
        return wallet;
    }

    @Override
    public WalletModel takeAmount(String player, String currency, float amount) {
        WalletModel wallet = getWallet(player);
        if(wallet == null) return null;

        wallet.takeAmount(currency, amount);
        return wallet;
    }

    @Override
    public TransactionModel getTransaction(int id) {
        return databaseManager.getTransactionByID(id).join();
    }

    @Override
    public void saveTransaction(TransactionModel transactionModel) {
        databaseManager.saveTransaction(transactionModel);
    }

    @Override
    public CurrencyInstance getCurrencyByID(String id) {
        return currenciesManager.getCurrency(id);
    }

    @Override
    public Collection<CurrencyInstance> getLoadedCurrencies() {
        return Collections.unmodifiableCollection(currenciesManager.getCurrencies());
    }

    @Override
    public boolean isCurrencyInitialized(String id) {
        return getCurrencyByID(id) != null;
    }

}
