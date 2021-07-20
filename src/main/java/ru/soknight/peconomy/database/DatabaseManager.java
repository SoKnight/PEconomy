package ru.soknight.peconomy.database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import ru.soknight.lib.database.Database;
import ru.soknight.lib.executable.quiet.AbstractQuietExecutor;
import ru.soknight.lib.executable.quiet.ThrowableHandler;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;

public final class DatabaseManager extends AbstractQuietExecutor {
    
    private final Logger logger;
    private final ConnectionSource connection;
    
    private final Dao<WalletModel, String> walletsDao;
    private final Dao<TransactionModel, Integer> transactionsDao;
    
    public DatabaseManager(PEconomy plugin, Database database) throws SQLException {
        super(ThrowableHandler.createForDatabase(plugin));

        this.logger = plugin.getLogger();
        this.connection = database.establishConnection();
        
        this.walletsDao = DaoManager.createDao(connection, WalletModel.class);
        this.transactionsDao = DaoManager.createDao(connection, TransactionModel.class);
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
