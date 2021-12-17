package ru.soknight.peconomy.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.database.Database;
import ru.soknight.lib.executable.quiet.AbstractQuietExecutor;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.event.wallet.WalletCreateEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class DatabaseManager extends AbstractQuietExecutor {

    private final Configuration config;
    private final ConnectionSource connection;
    
    private final Dao<WalletModel, String> walletsDao;
    private final Dao<TransactionModel, Integer> transactionsDao;
    
    public DatabaseManager(@NotNull PEconomy plugin, @NotNull Configuration config, @NotNull Database database) throws SQLException {
        this.config = config;
        this.connection = database.establishConnection();
        
        this.walletsDao = DaoManager.createDao(connection, WalletModel.class);
        this.transactionsDao = DaoManager.createDao(connection, TransactionModel.class);

        super.useDatabaseThrowableHandler(plugin);
        super.useCachedThreadPoolAsyncExecutor();
    }
    
    public void shutdown() {
        try {
            if(connection != null)
                connection.close();
        } catch (IOException ignored) {}
    }
    
    // --- wallets
    private @NotNull WalletModel createWallet(@NotNull OfflinePlayer bukkitPlayer) {
        return createWallet(bukkitPlayer.getName(), bukkitPlayer.getUniqueId());
    }

    private @NotNull WalletModel createWallet(@NotNull String playerName, @NotNull UUID playerUUID) {
        WalletModel wallet = new WalletModel(playerName, playerUUID);
        saveWallet(wallet).join();

        new WalletCreateEvent(wallet).fireAsync();
        return wallet;
    }

    public @NotNull CompletableFuture<WalletModel> getOrCreateWallet(@NotNull OfflinePlayer bukkitPlayer) {
        return getWalletUsingActualIdentifier(bukkitPlayer).thenApply(wallet -> wallet != null ? wallet : createWallet(bukkitPlayer));
    }

    public @NotNull CompletableFuture<WalletModel> getOrCreateWallet(@NotNull String playerName, @NotNull UUID playerUUID) {
        return getWallet(playerName).thenApply(wallet -> wallet != null ? wallet : createWallet(playerName, playerUUID));
    }

    public @NotNull CompletableFuture<List<WalletModel>> getAllWallets() {
        return supplyQuietlyAsync(walletsDao::queryForAll);
    }

    public @NotNull CompletableFuture<WalletModel> getWalletUsingActualIdentifier(@NotNull OfflinePlayer bukkitPlayer) {
        boolean identifyingByUuid = config.getBoolean("holding-status-updater.identify-by-uuid", false);
        if(identifyingByUuid)
            return getWallet(bukkitPlayer.getUniqueId())
                    .thenApply(wallet -> wallet != null ? wallet : getWallet(bukkitPlayer.getName()).join());

        return getWallet(bukkitPlayer.getName());
    }
    
    public @NotNull CompletableFuture<WalletModel> getWallet(@NotNull String playerName) {
        return supplyQuietlyAsync(() -> walletsDao.queryForId(playerName));
    }

    public @NotNull CompletableFuture<WalletModel> getWallet(@NotNull UUID playerUUID) {
        return supplyQuietlyAsync(() -> walletsDao.queryBuilder()
                .where()
                .eq(WalletModel.COLUMN_PLAYER_UUID, playerUUID)
                .queryForFirst()
        );
    }
    
    public @NotNull CompletableFuture<Long> getWalletsCount() {
        return supplyQuietlyAsync(() -> walletsDao.queryBuilder().countOf());
    }
    
    public @NotNull CompletableFuture<Boolean> hasWallet(@NotNull String playerName) {
        return supplyQuietlyAsync(() -> walletsDao.idExists(playerName));
    }

    public @NotNull CompletableFuture<Void> transferWallet(@NotNull WalletModel wallet, @NotNull String playerName) {
        return runQuietlyAsync(() -> walletsDao.updateId(wallet, playerName));
    }
    
    public @NotNull CompletableFuture<Void> saveWallet(@NotNull WalletModel wallet) {
        return runQuietlyAsync(() -> walletsDao.createOrUpdate(wallet));
    }
    
    // --- transactions
    public @NotNull CompletableFuture<TransactionModel> getTransactionByID(int id) {
        return supplyQuietlyAsync(() -> transactionsDao.queryForId(id));
    }
    
    public @NotNull CompletableFuture<List<TransactionModel>> getTransactionHistory(@NotNull String walletHolder) {
        return supplyQuietlyAsync(() -> transactionsDao.queryBuilder()
                .where()
                .eq(TransactionModel.COLUMN_WALLET_HOLDER, walletHolder)
                .query()
        );
    }
    
    public @NotNull CompletableFuture<Void> saveTransaction(@NotNull TransactionModel transaction) {
        return runQuietlyAsync(() -> transactionsDao.createOrUpdate(transaction));
    }
    
}
