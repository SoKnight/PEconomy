package ru.soknight.peconomy.database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import ru.soknight.peconomy.PEconomy;

public class DatabaseManager {
	
	private final Logger logger;
	private final ConnectionSource source;
	
	private final Dao<Wallet, String> ownersDao;
	private final Dao<Transaction, Integer> transactionsDao;
	
	public DatabaseManager(PEconomy plugin, Database database) throws SQLException {
		this.logger = plugin.getLogger();
		this.source = database.getConnection();
		
		this.ownersDao = DaoManager.createDao(source, Wallet.class);
		this.transactionsDao = DaoManager.createDao(source, Transaction.class);
	}
	
	public void shutdown() {
		try {
			source.close();
			logger.info("Database connection closed.");
		} catch (IOException e) {
			logger.severe("Failed to close database connection: " + e.getLocalizedMessage());
		}
	}
	
	/*
	 * Players wallets
	 */
	
	public boolean createWallet(Wallet owner) {
		try {
			return this.ownersDao.create(owner) != 0;
		} catch (SQLException e) {
			logger.severe("Failed to create wallet of player '" + owner.getOwner() + "': " + e.getMessage());
			return false;
		}
	}
	
	public Wallet getWallet(String name) {
		try {
			return this.ownersDao.queryForId(name);
		} catch (SQLException e) {
			logger.severe("Failed to get wallet for player '" + name + "': " + e.getMessage());
			return null;
		}
	}
	
	public int getWalletsCount() {
		try {
			return this.ownersDao.queryForAll().size();
		} catch (SQLException e) {
			logger.severe("Failed to get wallets count: " + e.getMessage());
			return 0;
		}
	}
	
	public boolean hasWallet(String name) {
		return getWallet(name) != null;
	}
	
	public boolean transferWallet(Wallet owner, String name) {
		try {
			return this.ownersDao.updateId(owner, name) != 0;
		} catch (SQLException e) {
			logger.severe("Failed to transfer wallet of player '" + owner.getOwner() + "' to '" + name + "': "
					+ e.getMessage());
			return false;
		}
	}
	
	public boolean updateWallet(Wallet wallet) {
		try {
			return this.ownersDao.update(wallet) != 0;
		} catch (SQLException e) {
			logger.severe("Failed to update wallet of player '" + wallet.getOwner() + "': " + e.getMessage());
			return false;
		}
	}
	
	/*
	 * Transactions
	 */
	
	public Transaction getTransactionByID(int id) {
		try {
			return this.transactionsDao.queryForId(id);
		} catch (SQLException e) {
			logger.severe("Failed to get transaction by ID #" + id + ": " + e.getMessage());
			return null;
		}
	}
	
//	public List<Transaction> getAllTransactions() {
//		try {
//			return this.transactionsDao.queryForAll();
//		} catch (SQLException e) {
//			logger.severe("Failed to get all transactions: " + e.getMessage());
//			return null;
//		}
//	}
	
	public List<Transaction> getWalletTransactions(String owner) {
		try {
			QueryBuilder<Transaction, Integer> builder = transactionsDao.queryBuilder();
			Where<Transaction, Integer> where = builder.where();
			
			where.eq("owner", owner);
			
			return builder.query();
		} catch (SQLException e) {
			logger.severe("Failed to get transactions for " + owner + "'s wallet: " + e.getMessage());
			return null;
		}
	}
	
	public boolean hasTransactionWithID(int id) {
		return getTransactionByID(id) != null;
	}
	
	public boolean saveTransaction(Transaction transaction) {
		try {
			return this.transactionsDao.create(transaction) != 0;
		} catch (SQLException e) {
			logger.severe("Failed to save transaction: " + e.getMessage());
			return false;
		}
	}
	
}
