package ru.soknight.peconomy.api;

import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;

import java.util.Collection;

public interface PEconomyAPI {

    /***************************
     *    BANKING PROVIDING    *
     **************************/

    /**
     * Register your custom banking provider for the Vault economy system
     * @param provider a provider instance to register
     */
    void registerBankingProvider(BankingProvider provider);

    /**
     * Unregister already registered banking provider if it's exists
     */
    void unregisterBankingProvider();

    /*****************
     *    WALLETS    *
     ****************/
    
    /**
     * Gets total count of wallets in the database
     * @return Total count of wallets
     */
    long getWalletsCount();
    
    /**
     * Checks if specified player is {@link WalletModel} owner
     * @param player - Name of player to check
     * @return True if player has wallet or false if not
     */
    boolean hasWallet(String player);
    
    /**
     * Gets player's wallet from the database if it's exists
     * @param player - The potential owner of wallet
     * @return Exist player's wallet or null if it's not exist
     */
    WalletModel getWallet(String player);
    
    /**
     * Updates player's wallet in the database
     * @param wallet - Player's wallet which will be updated in the database
     */
    void updateWallet(WalletModel wallet);

    /****************************
     *    WALLETS MANAGEMENT    *
     ***************************/
    
    /**
     * Adds amount of specified currency to player's balance
     * @param player - Name of target player
     * @param currency - Target currency's ID
     * @param amount - Amount of currency to add
     * @return Player's {@link WalletModel} after transaction (may be null)
     */
    WalletModel addAmount(String player, String currency, float amount);
    
    /**
     * Gets amount of specified currency on the player's balance
     * @param player - Name of target player
     * @param currency - Target currency's ID
     * @return Amount of specified currency on the balance
     */
    float getAmount(String player, String currency);
    
    /**
     * Checks if specified amount is on player's balance
     * @param player - Name of player to check
     * @param currency - Target currency's ID
     * @param amount - Amount of currency to check
     * @return True if player has this amount on him balance or false if not
     */
    boolean hasAmount(String player, String currency, float amount);
    
    /**
     * Sets specified amount of currency on the player's balance
     * @param player - Name of target player
     * @param currency - Target currency's ID
     * @param amount - New amount of this currency
     * @return Player's {@link WalletModel} after transaction (may be null)
     */
    WalletModel setAmount(String player, String currency, float amount);
    
    /**
     * Nullifies currency's balance in the player's wallet
     * @param player - Name of target player
     * @param currency - Target currency's ID
     * @return Player's {@link WalletModel} after transaction (may be null)
     */
    WalletModel resetAmount(String player, String currency);
    
    /**
     * Taking specified amount of currency from player's balance
     * @param player - Name of target player
     * @param currency - Target currency's ID
     * @param amount - Amount of currency to take
     * @return Player's {@link WalletModel} after transaction (may be null)
     */
    WalletModel takeAmount(String player, String currency, float amount);

    /**********************
     *    TRANSACTIONS    *
     *********************/
    
    /**
     * Gets economy transaction by ID if it has been completed
     * @param id - Transaction's ID
     * @return Exist transaction object or null if transaction with this ID cannot be found
     */
    TransactionModel getTransaction(int id);

    /**
     * Saves transaction and gets her ID which will be set by database manager
     * @param transactionModel - Transaction to save
     */
    void saveTransaction(TransactionModel transactionModel);

    /*******************************
     *    CURRENCIES MANAGEMENT    *
     ******************************/

    /**
     * Gets currency instance by ID if requested currency has been initialized by PEconomy
     * @param id - Target currency's ID
     * @return Exist currency instance or null if currency with this ID has not been initialized
     */
    CurrencyInstance getCurrencyByID(String id);
    
    /**
     * Gets currencies instances successfully initialized by PEconomy
     * @return All initialized currencies instances collection (read-only)
     */
    Collection<CurrencyInstance> getLoadedCurrencies();
    
    /**
     * Checks if currency instance with specified ID is initialized by PEconomy or not
     * @param id - Target currency's ID
     * @return 'true' if this currency instance initialized or 'false' if not
     */
    boolean isCurrencyInitialized(String id);

}
