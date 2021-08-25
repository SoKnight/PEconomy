package ru.soknight.peconomy.balancetop;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.soknight.peconomy.api.PEconomyAPI;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.format.ObjectFormatter;

import java.util.List;
import java.util.Optional;

public interface BalanceTop {

    /**
     * Create your custom balance top instance with custom formatter and more
     * @param plugin a plugin that requested this balance top
     * @param currencyId currency ID that you want to use
     * @param topSize amount of places in this balance top
     * @param formatter balance top place formatter (instance -> string)
     * @return A new {@link BalanceTop} instance
     * @see PEconomyAPI#createBalanceTop(Plugin, String, int, ObjectFormatter)
     */
    static @NotNull BalanceTop create(@NotNull Plugin plugin, @NotNull String currencyId, int topSize, @Nullable ObjectFormatter<BalanceTopPlace> formatter) {
        return SimpleBalanceTop.create(plugin, currencyId, topSize, formatter);
    }

    /**
     * Create a balance top builder to customize your balance top with it
     * @param plugin a plugin that requested this balance top
     * @return A new {@link BalanceTop.Builder} instance
     * @see PEconomyAPI#buildBalanceTop(Plugin)
     */
    static @NotNull BalanceTop.Builder build(@NotNull Plugin plugin) {
        return SimpleBalanceTop.build(plugin);
    }

    /**
     * Get plugin which requested this top
     * @return The providing plugin
     */
    @NotNull Plugin getPlugin();

    /**
     * Get currency ID that used in this top
     * @return The currency ID
     */
    @NotNull String getCurrencyId();

    /**
     * Get max amount of places (top size) in this top
     * @return The top size
     * @see Builder#topSize(int)
     */
    int getTopSize();

    /**
     * Get string formatter for balance top place instances
     * @return The places formatter
     * @see #getPlaceFormatted(int)
     * @see Builder#formatter(ObjectFormatter)
     */
    @Nullable ObjectFormatter<BalanceTopPlace> getFormatter();

    /**
     * Get balance top place by this position index (can be empty)
     * @param positionIndex an index of top position (starts from 0)
     * @return The balance top place instance placed on this position (optional)
     * @see #getAllPlaces()
     * @see #getPlaceFormatted(int)
     * @see #hasPlace(int)
     */
    BalanceTopPlace getPlace(int positionIndex);

    /**
     * Get balance top place of this wallet (optional, always non-empty if not null)
     * @param wallet a wallet instance to query
     * @return The related balance top place instance (optional)
     * @see #getPlace(int)
     * @see #getPlaceFormatted(WalletModel)
     */
    Optional<BalanceTopPlace> getPlace(WalletModel wallet);

    /**
     * Get balance top place by this position index (can be empty) and format it
     * @param positionIndex an index of top position (starts from 0)
     * @return The formatted balance top place instance placed on this position (optional)
     * @see #getAllPlaces()
     * @see #getPlace(int)
     * @see #hasPlace(int)
     */
    String getPlaceFormatted(int positionIndex);

    /**
     * Get balance top place of this wallet (optional, always non-empty if not null)
     * @param wallet a wallet instance to query
     * @return The related balance top place instance (optional)
     * @see #getPlace(WalletModel)
     * @see #getPlaceFormatted(int)
     */
    Optional<String> getPlaceFormatted(WalletModel wallet);

    /**
     * Get all balance top place instances that currently presented in this top
     * @return All balance top places
     * @see #getPlace(int)
     * @see #getPlaceFormatted(int)
     * @see #getTopSize()
     */
    @NotNull List<BalanceTopPlace> getAllPlaces();

    /**
     * Check if this top contains a place in position with this index
     * @param positionIndex an index of position to check (starts from 0)
     * @return 'true' if actually contains, overwise 'false'
     * @see #getPlace(int)
     * @see #getPlaceFormatted(int)
     * @see #getTopSize()
     */
    boolean hasPlace(int positionIndex);

    /**
     * Refresh this balance top and load actual information about top places
     * @see #getAllPlaces()
     */
    void refresh();

    interface Builder {

        /**
         * Finish balance top building and get completed instance
         * @return Completed {@link BalanceTop} instance
         */
        @NotNull BalanceTop build();

        /**
         * Set the currency ID for this balance top
         * @param currencyId currency ID that you want to use
         * @return The builder instance to provide method chaining
         * @see BalanceTop#getCurrencyId()
         */
        @NotNull Builder currencyId(@NotNull String currencyId);

        /**
         * Set a size of this balance top (equals to amount of places)
         * @param amountOfPlaces amount of places in this top
         * @return The builder instance to provide method chaining
         * @see BalanceTop#getTopSize()
         */
        @NotNull Builder topSize(int amountOfPlaces);

        /**
         * Set a formatter for all balance top place instances
         * @param formatter balance top places formatter
         * @return The builder instance to provide method chaining
         * @see BalanceTop#getPlaceFormatted(int)
         * @see BalanceTop#getFormatter()
         */
        @NotNull Builder formatter(@Nullable ObjectFormatter<BalanceTopPlace> formatter);

    }

}
