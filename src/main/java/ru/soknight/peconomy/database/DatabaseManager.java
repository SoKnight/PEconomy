package ru.soknight.peconomy.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import org.jetbrains.annotations.NotNull;
import ru.soknight.lib.database.Database;
import ru.soknight.lib.executable.quiet.AbstractQuietExecutor;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class DatabaseManager extends AbstractQuietExecutor {

    private final ConnectionSource connection;
    
    private final Dao<WalletModel, String> walletsDao;
    private final Dao<TransactionModel, Integer> transactionsDao;
    
    public DatabaseManager(@NotNull PEconomy plugin, @NotNull Database database) throws SQLException {
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
    
    /*****************
     *    WALLETS    *
     ****************/

    private @NotNull WalletModel createWallet(String player) {
        WalletModel wallet = new WalletModel(player);
        saveWallet(wallet).join();
        return wallet;
    }

    public CompletableFuture<WalletModel> getOrCreateWallet(String player) {
        return getWallet(player).thenApply(wallet -> wallet != null ? wallet : createWallet(player));
    }

    public CompletableFuture<List<WalletModel>> getAllWallets() {
        return supplyQuietlyAsync(walletsDao::queryForAll);
    }
    
    public CompletableFuture<WalletModel> getWallet(String player) {
        return supplyQuietlyAsync(() -> walletsDao.queryForId(player));
    }
    
    public CompletableFuture<Long> getWalletsCount() {
        return supplyQuietlyAsync(() -> walletsDao.queryBuilder().countOf());
    }
    
    public CompletableFuture<Boolean> hasWallet(String player) {
        return supplyQuietlyAsync(() -> walletsDao.idExists(player));
    }
    
    public CompletableFuture<Void> saveWallet(WalletModel wallet) {
        return runQuietlyAsync(() -> walletsDao.createOrUpdate(wallet));
    }
    
    public CompletableFuture<Void> transferWallet(WalletModel wallet, String player) {
        return runQuietlyAsync(() -> walletsDao.updateId(wallet, player));
    }
    
    /**********************
     *    TRANSACTIONS    *
     *********************/
    
    public CompletableFuture<TransactionModel> getTransactionByID(int id) {
        return supplyQuietlyAsync(() -> transactionsDao.queryForId(id));
    }
    
    public CompletableFuture<List<TransactionModel>> getTransactionHistory(String owner) {
        return supplyQuietlyAsync(() -> transactionsDao.queryBuilder()
                .where()
                .eq("owner", owner)
                .query()
        );
    }
    
    public CompletableFuture<Void> saveTransaction(TransactionModel transaction) {
        return runQuietlyAsync(() -> transactionsDao.createOrUpdate(transaction));
    }
    
}
