package ru.soknight.peconomy;

import ru.soknight.peconomy.database.Balance;
import ru.soknight.peconomy.database.DatabaseManager;

public class PEcoAPI {

	/**
	 * Getting count of balances in the database
	 * @return Count of balances in the database
	 */
	public static int getBalancesCount() {
		return DatabaseManager.getBalancesCount();
	}
	
	/**
	 * Checking player exist in the database
	 * @param name - Name of target player
	 * @return Exist player (true) or not (false)
	 */
	public static boolean isInDatabase(String name) {
		return DatabaseManager.isInDatabase(name);
	}
	
	/**
	 * Setup balance for player
	 * @param name - Name of target player
	 * @param balance - New balance for player
	 */
	public static void setBalance(String name, Balance balance) {
		DatabaseManager.setBalance(name, balance);
	}
	
	/**
	 * Getting player's balance
	 * @param name - Name of target player
	 * @return Balance of player
	 */
	public static Balance getBalance(String name) {
		return DatabaseManager.getBalance(name);
	}
	
	/**
	 * Adding amount of specified wallet to player's balance
	 * @param name - Name of target player
	 * @param amount - Amount to adding
	 * @param wallet - Target wallet (dollars or euro)
	 * @return Wallet balance before adding
	 */
	public static float addAmount(String name, float amount, String wallet) {
		return DatabaseManager.addAmount(name, amount, wallet);
	}
	
	/**
	 * Getting amount of specified wallet on player's balance
	 * @param name - Name of target player
	 * @param wallet - Target wallet (dollars or euro)
	 * @return Amount of wallet on player's balance
	 */
	public static float getAmount(String name, String wallet) {
		return DatabaseManager.getAmount(name, wallet);
	}
	
	/**
	 * Checking amount of specified wallet on player's balance
	 * @param name - Name of target player
	 * @param amount - Amount of wallet for checking
	 * @param wallet - Target wallet (dollars or euro)
	 * @return True if player has amount on balance or false if not
	 */
	public static boolean hasAmount(String name, float amount, String wallet) {
		return DatabaseManager.hasAmount(name, amount, wallet);
	}
	
	/**
	 * Setup specified amount of wallet on player's balance
	 * @param name - Name of target player
	 * @param amount - New amount of wallet
	 * @param wallet - Target wallet (dollars or euro)
	 * @return Wallet balance before setup
	 */
	public static float setAmount(String name, float amount, String wallet) {
		return DatabaseManager.setAmount(name, amount, wallet);
	}
	
	/**
	 * Reset amount of wallet on player's balance
	 * @param name - Name of target player
	 * @param wallet - Target wallet (dollars or euro)
	 * @return Wallet balance before reset
	 */
	public static float resetAmount(String name, String wallet) {
		return DatabaseManager.resetAmount(name, wallet);
	}
	
	/**
	 * Taking specified amount of wallet from player's balance
	 * @param name - Name of target player
	 * @param amount - Amount of wallet for taking
	 * @param wallet - Target wallet (dollars or euro)
	 * @return Wallet balance before taking
	 */
	public static float takeAmount(String name, float amount, String wallet) {
		return DatabaseManager.takeAmount(name, amount, wallet);
	}
	
}
