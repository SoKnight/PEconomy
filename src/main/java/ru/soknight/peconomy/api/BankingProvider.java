package ru.soknight.peconomy.api;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.List;

@SuppressWarnings("DeprecatedIsStillUsed")
public interface BankingProvider {

    BankingProvider DEFAULT = new BankingProviderStub();

    /**
     * Returns true if the given implementation supports banks.
     * @return true if the implementation supports banks
     */
    default boolean hasBankSupport() {
        return true;
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {{@link #createBank(String, OfflinePlayer)} instead.
     */
    @Deprecated
    EconomyResponse createBank(String name, String player);

    /**
     * Creates a bank account with the specified name and the player as the owner
     * @param name of account
     * @param player the account should be linked to
     * @return EconomyResponse Object
     */
    EconomyResponse createBank(String name, OfflinePlayer player);

    /**
     * Deletes a bank account with the specified name.
     * @param name of the back to delete
     * @return if the operation completed successfully
     */
    EconomyResponse deleteBank(String name);

    /**
     * Returns the amount the bank has
     * @param name of the account
     * @return EconomyResponse Object
     */
    EconomyResponse bankBalance(String name);

    /**
     * Returns true or false whether the bank has the amount specified - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name of the account
     * @param amount to check for
     * @return EconomyResponse Object
     */
    EconomyResponse bankHas(String name, double amount);

    /**
     * Withdraw an amount from a bank account - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name of the account
     * @param amount to withdraw
     * @return EconomyResponse Object
     */
    EconomyResponse bankWithdraw(String name, double amount);

    /**
     * Deposit an amount into a bank account - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name of the account
     * @param amount to deposit
     * @return EconomyResponse Object
     */
    EconomyResponse bankDeposit(String name, double amount);

    /**
     * @deprecated As of VaultAPI 1.4 use {{@link #isBankOwner(String, OfflinePlayer)} instead.
     */
    @Deprecated
    EconomyResponse isBankOwner(String name, String playerName);

    /**
     * Check if a player is the owner of a bank account
     *
     * @param name of the account
     * @param player to check for ownership
     * @return EconomyResponse Object
     */
    EconomyResponse isBankOwner(String name, OfflinePlayer player);

    /**
     * @deprecated As of VaultAPI 1.4 use {{@link #isBankMember(String, OfflinePlayer)} instead.
     */
    @Deprecated
    EconomyResponse isBankMember(String name, String playerName);

    /**
     * Check if the player is a member of the bank account
     *
     * @param name of the account
     * @param player to check membership
     * @return EconomyResponse Object
     */
    EconomyResponse isBankMember(String name, OfflinePlayer player);

    /**
     * Gets the list of banks
     * @return the List of Banks
     */
    List<String> getBanks();

}
