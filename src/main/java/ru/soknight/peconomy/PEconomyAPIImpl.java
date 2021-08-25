package ru.soknight.peconomy;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import ru.soknight.peconomy.api.BankingProvider;
import ru.soknight.peconomy.api.PEconomyAPI;
import ru.soknight.peconomy.balancetop.BalanceTop;
import ru.soknight.peconomy.balancetop.BalanceTopPlace;
import ru.soknight.peconomy.balancetop.function.BalanceTopPlaceFinder;
import ru.soknight.peconomy.balancetop.function.BalanceTopPlacesProvider;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.format.Formatter;
import ru.soknight.peconomy.format.ObjectFormatter;
import ru.soknight.peconomy.hook.VaultEconomyProvider;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class PEconomyAPIImpl implements PEconomyAPI {

    private final DatabaseManager databaseManager;
    private final CurrenciesManager currenciesManager;
    private final VaultEconomyProvider economyProvider;
    private final Formatter formatter;

    @Override
    public @NotNull Formatter getFormatter() {
        return formatter;
    }

    @Override
    public void registerBankingProvider(@NotNull BankingProvider provider) {
        economyProvider.registerBankingProvider(provider);
    }

    @Override
    public void unregisterBankingProvider() {
        economyProvider.unregisterBankingProvider();
    }

    @Override
    public @NotNull BalanceTop createBalanceTop(@NotNull Plugin plugin, @NotNull String currencyId, int topSize, @Nullable ObjectFormatter<BalanceTopPlace> formatter) {
        return BalanceTop.create(plugin, currencyId, topSize, formatter);
    }

    @Override
    public @NotNull BalanceTop.Builder buildBalanceTop(@NotNull Plugin plugin) {
        return BalanceTop.build(plugin);
    }

    @Override
    public @NotNull BalanceTopPlacesProvider getBalanceTopPlacesProvider() {
        return (balanceTop, comparator) -> databaseManager.getAllWallets()
                .thenApply(wallets -> mapWalletsToTopPlaces(balanceTop, comparator, wallets))
                .join();
    }

    private List<BalanceTopPlace> mapWalletsToTopPlaces(
            BalanceTop balanceTop,
            Comparator<WalletModel> comparator,
            List<WalletModel> wallets
    ) {
        if(wallets == null || wallets.isEmpty())
            return Collections.emptyList();

        String currencyId = balanceTop.getCurrencyId();
        int topSize = balanceTop.getTopSize();

        AtomicInteger positionCounter = new AtomicInteger();
        return wallets.stream()
                .sorted(comparator)
                .limit(topSize)
                .map(wallet -> BalanceTopPlace.create(wallet, currencyId, positionCounter.getAndIncrement()))
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull BalanceTopPlaceFinder getBalanceTopPlaceFinder() {
        return (wallet, balanceTop, comparator) -> databaseManager.getAllWallets()
                .thenApply(wallets -> findWalletPlace(wallet, balanceTop, comparator, wallets))
                .join();
    }

    private Optional<BalanceTopPlace> findWalletPlace(
            WalletModel findingWallet,
            BalanceTop balanceTop,
            Comparator<WalletModel> comparator,
            List<WalletModel> wallets
    ) {
        if(wallets == null || wallets.isEmpty())
            return Optional.empty();

        String currencyId = balanceTop.getCurrencyId();

        AtomicInteger positionCounter = new AtomicInteger();
        return wallets.stream()
                .sorted(comparator)
                .map(wallet -> BalanceTopPlace.create(wallet, currencyId, positionCounter.getAndIncrement()))
                .filter(place -> Objects.equals(place.getWallet(), findingWallet))
                .findAny();
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
        if(wallet == null)
            return null;

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
    public @Nullable WalletModel setAmount(String player, String currency, float amount) {
        WalletModel wallet = getWallet(player);
        if(wallet == null)
            return null;

        wallet.addAmount(currency, amount);
        return wallet;
    }

    @Override
    public @Nullable WalletModel resetAmount(String player, String currency) {
        WalletModel wallet = getWallet(player);
        if(wallet == null)
            return null;

        wallet.resetWallet(currency);
        return wallet;
    }

    @Override
    public @Nullable WalletModel takeAmount(String player, String currency, float amount) {
        WalletModel wallet = getWallet(player);
        if(wallet == null)
            return null;

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
    public @NotNull @UnmodifiableView Collection<CurrencyInstance> getLoadedCurrencies() {
        return Collections.unmodifiableCollection(currenciesManager.getCurrencies());
    }

    @Override
    public boolean isCurrencyInitialized(String id) {
        return getCurrencyByID(id) != null;
    }

}
