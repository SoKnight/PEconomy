package ru.soknight.peconomy.balancetop;

import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.database.model.WalletModel;

public interface BalanceTopPlace {

    /**
     * Create empty balance top place instance using this parameters
     * @param currencyId currency ID that you want use
     * @param positionIndex an index of position in the balance top
     * @return Created {@link BalanceTopPlace} instance
     */
    static @NotNull BalanceTopPlace create(@NotNull String currencyId, int positionIndex) {
        return create(null, currencyId, positionIndex);
    }

    /**
     * Create new balance top place instance using this parameters
     * @param wallet an original wallet instance
     * @param currencyId currency ID that you want use
     * @param positionIndex an index of position in the balance top
     * @return Created {@link BalanceTopPlace} instance
     */
    static @NotNull BalanceTopPlace create(@NotNull WalletModel wallet, @NotNull String currencyId, int positionIndex) {
        return SimpleBalanceTopPlace.create(wallet, currencyId, positionIndex);
    }

    /**
     * Get position (starts from 1) in the balance top
     * @return The top position
     */
    int getPosition();

    /**
     * Get position index (starts from 0) in the balance top
     * @return The top position index
     */
    int getPositionIndex();

    /**
     * Check if this balance top place is empty
     * @return 'true' if actually empty, overwise 'false'
     */
    boolean isEmpty();

    /**
     * Get original wallet that placed on this top position
     * @return The original {@link WalletModel} instance
     */
    @NotNull WalletModel getWallet();

    /**
     * Get wallet holder (player's name) that placed here
     * @return The wallet holder (player's name)
     */
    @NotNull String getWalletHolder();

    /**
     * Get currency ID that used in the balance top context
     * @return The currency ID
     */
    @NotNull String getCurrencyId();

    /**
     * Get wallet balance for this currency
     * @return The wallet balance
     */
    float getWalletBalance();

}
