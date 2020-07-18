package ru.soknight.peconomy;

import lombok.AllArgsConstructor;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.Transaction;
import ru.soknight.peconomy.database.Wallet;

@AllArgsConstructor
public class PEcoAPI {
	
	private final DatabaseManager databaseManager;
	private final CurrenciesManager currenciesManager;
	
	/**
	 * Gets total count of wallets in the database
	 * @return Total count of wallets
	 */
	public int getWalletsCount() {
		return databaseManager.getWalletsCount();
	}
	
	/**
	 * Checks if specified player is {@link Wallet} owner
	 * @param player - Name of player to check
	 * @return True if player has wallet or false if not
	 */
	public boolean hasWallet(String player) {
		return databaseManager.hasWallet(player);
	}
	
	/**
	 * Gets player's wallet from the database if it's exists
	 * @param player - The potential owner of wallet
	 * @return Exist player's wallet or null if it's not exist
	 */
	public Wallet getWallet(String player) {
		return databaseManager.getWallet(player);
	}
	
	/**
	 * Updates player's wallet in the database
	 * @param wallet - Player's wallet which will be updated in the database
	 */
	public void updateWallet(Wallet wallet) {
		if(databaseManager.hasWallet(wallet.getOwner()))
			databaseManager.updateWallet(wallet);
		else databaseManager.createWallet(wallet);
	}
	
	/**
	 * Adds amount of specified currency to player's balance
	 * @param player - Name of target player
	 * @param currency - Target currency's ID
	 * @param amount - Amount of currency to add
	 * @return
	 * @return Player's {@link Wallet} after transaction (may be null)
	 */
	public Wallet addAmount(String player, String currency, float amount) {
		Wallet wallet = getWallet(player);
		
		if(wallet == null) return null;
		
		wallet.addAmount(currency, amount);
		return wallet;
	}
	
	/**
	 * Gets amount of specified currency on the player's balance
	 * @param player - Name of target player
	 * @param currency - Target currency's ID
	 * @return Amount of specified currency on the balance
	 */
	public float getAmount(String player, String currency) {
		Wallet wallet = getWallet(player);
		
		return wallet == null ? 0F : wallet.getAmount(currency);
	}
	
	/**
	 * Checks if specified amount is on player's balance
	 * @param player - Name of player to check
	 * @param currency - Target currency's ID
	 * @param amount - Amount of currency to check
	 * @return True if player has this amount on him balance or false if not
	 */
	public boolean hasAmount(String player, String currency, float amount) {
		Wallet wallet = getWallet(player);
		
		return wallet == null ? false : wallet.hasAmount(currency, amount);
	}
	
	/**
	 * Sets specified amount of currency on the player's balance
	 * @param player - Name of target player
	 * @param currency - Target currency's ID
	 * @param amount - New amount of this currency
	 * @return Player's {@link Wallet} after transaction (may be null)
	 */
	public Wallet setAmount(String player, String currency, float amount) {
		Wallet wallet = getWallet(player);
		if(wallet == null) return null;
		
		wallet.addAmount(currency, amount);
		return wallet;
	}
	
	/**
	 * Nullifies currency's balance in the player's wallet
	 * @param player - Name of target player
	 * @param currency - Target currency's ID
	 * @return Player's {@link Wallet} after transaction (may be null)
	 */
	public Wallet resetAmount(String player, String currency) {
		Wallet wallet = getWallet(player);
		if(wallet == null) return null;
		
		wallet.resetWallet(currency);
		return wallet;
	}
	
	/**
	 * Taking specified amount of currency from player's balance
	 * @param player - Name of target player
	 * @param currency - Target currency's ID
	 * @param amount - Amount of currency to take
	 * @return Player's {@link Wallet} after transaction (may be null)
	 */
	public Wallet takeAmount(String player, String currency, float amount) {
		Wallet wallet = getWallet(player);
		if(wallet == null) return null;
		
		wallet.takeAmount(currency, amount);
		return wallet;
	}
	
	/**
	 * Gets economy transaction by ID if it has been completed
	 * @param id - Transaction's ID
	 * @return Exist transaction object or null if transaction with this ID cannot be found
	 */
	public Transaction getTransaction(int id) {
		return databaseManager.getTransactionByID(id);
	}

	/**
	 * Saves transaction and gets her ID which will be set by database manager
	 * @param transaction - Transaction to save
	 * @return Transaction's ID from database (may be null if saving will be failed)
	 */
	public Integer saveTransaction(Transaction transaction) {
		databaseManager.saveTransaction(transaction);
		return transaction.getId();
	}
	
	/**
	 * Gets currency instance by ID if requested currency has been initialized by PEconomy
	 * @param id - Target currency's ID
	 * @return Exist currency instance or null if currency with this ID has not been initialized
	 */
	public CurrencyInstance getCurrencyByID(String id) {
		return currenciesManager.getCurrency(id);
	}
	
	/**
	 * Checks if currency instance with specified ID is initialized by PEconomy or not
	 * @param id - Target currency's ID
	 * @return 'true' if this currency instance initialized or 'false' if not
	 */
	public boolean isCurrencyInitialized(String id) {
		return getCurrencyByID(id) != null;
	}
	
}
