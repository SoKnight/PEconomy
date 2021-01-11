package ru.soknight.peconomy.database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;

public class DatabaseManager {
    
    private final Logger logger;
    private final ConnectionSource connection;
    
    private final Dao<WalletModel, String> walletsDao;
    private final Dao<TransactionModel, Integer> transactionsDao;
    
    public DatabaseManager(PEconomy plugin, Database database) throws SQLException {
        this.logger = plugin.getLogger();
        this.connection = database.getConnection();
        
        this.walletsDao = DaoManager.createDao(connection, WalletModel.class);
        this.transactionsDao = DaoManager.createDao(connection, TransactionModel.class);
    }
    
    public void shutdown() {
        try {
            if(connection != null)
                connection.close();
        } catch (IOException ignored) {}
    }
    
    /*********************
     *  Players wallets  *
     ********************/
    
    public CompletableFuture<WalletModel> getWallet(String player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return walletsDao.queryForId(player);
            } catch (SQLException e) {
                logger.severe("Failed to query wallet for player " + player + ": " + e.getMessage());
                return null;
            }
        });
    }
    
    public CompletableFuture<Long> getWalletsCount() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return walletsDao.queryBuilder().countOf();
            } catch (SQLException e) {
                logger.severe("Failed to query wallets amount: " + e.getMessage());
                return null;
            }
        });
    }
    
    public CompletableFuture<Boolean> hasWallet(String player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return walletsDao.idExists(player);
            } catch (SQLException e) {
                logger.severe("Failed to check wallet exists for player " + player + ": " + e.getMessage());
                return false;
            }
        });
    }
    
    public CompletableFuture<Void> saveWallet(WalletModel wallet) {
        return CompletableFuture.runAsync(() -> {
            try {
                walletsDao.createOrUpdate(wallet);
            } catch (SQLException e) {
                logger.severe("Failed to save wallet for player " + wallet.getWalletHolder() + ": " + e.getMessage());
            }
        });
    }
    
    public CompletableFuture<Void> transferWallet(WalletModel wallet, String player) {
        return CompletableFuture.runAsync(() -> {
            try {
                walletsDao.updateId(wallet, player);
            } catch (SQLException e) {
                logger.severe("Failed to transfer wallet to player " + player + ": " + e.getMessage());
            }
        });
    }
    
    /******************
     *  Transactions  *
     *****************/
    
    public CompletableFuture<TransactionModel> getTransactionByID(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.transactionsDao.queryForId(id);
            } catch (SQLException e) {
                logger.severe("Failed to query transaction by ID #" + id + ": " + e.getMessage());
                return null;
            }
        });
    }
    
    public CompletableFuture<List<TransactionModel>> getTransactionHistory(String owner) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return transactionsDao.queryBuilder().where().eq("owner", owner).query();
            } catch (SQLException e) {
                logger.severe("Failed to query transaction history for " + owner + ": " + e.getMessage());
                return null;
            }
        });
    }
    
    public CompletableFuture<Void> saveTransaction(TransactionModel transaction) {
        return CompletableFuture.runAsync(() -> {
            try {
                transactionsDao.createOrUpdate(transaction);
            } catch (SQLException e) {
                logger.severe("Failed to save transaction: " + e.getMessage());
            }
        });
    }
    
}
