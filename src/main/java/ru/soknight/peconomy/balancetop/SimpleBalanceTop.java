package ru.soknight.peconomy.balancetop;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import ru.soknight.lib.tool.Validate;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.api.PEconomyAPI;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.format.ObjectFormatter;

import java.util.*;

@Getter
final class SimpleBalanceTop implements BalanceTop {

    private final Plugin plugin;
    private final String currencyId;
    private final int topSize;
    private final ObjectFormatter<BalanceTopPlace> formatter;

    private final Comparator<WalletModel> comparator;
    private final List<BalanceTopPlace> places;

    static @NotNull BalanceTop create(@NotNull Plugin plugin, @NotNull String currencyId, int topSize, @Nullable ObjectFormatter<BalanceTopPlace> formatter) {
        return new SimpleBalanceTop(plugin, currencyId, topSize, formatter);
    }

    static @NotNull BalanceTop.Builder build(@NotNull Plugin plugin) {
        return new SimpleBalanceTop.Builder(plugin);
    }

    private SimpleBalanceTop(@NotNull Plugin plugin, @NotNull String currencyId, int topSize, @Nullable ObjectFormatter<BalanceTopPlace> formatter) {
        Validate.notNull(plugin, "plugin");
        Validate.notEmpty(currencyId, "currencyId");

        this.plugin = plugin;
        this.currencyId = currencyId;
        this.topSize = topSize;
        this.formatter = formatter;

        Comparator<WalletModel> comparator = Comparator.comparingDouble(wallet -> wallet.getAmount(currencyId));
        this.comparator = comparator.reversed();
        this.places = new ArrayList<>();
    }

    @Override
    public @NotNull BalanceTopPlace getPlace(int positionIndex) {
        return hasPlace(positionIndex) ? places.get(positionIndex) : BalanceTopPlace.create(currencyId, positionIndex);
    }

    @Override
    public Optional<BalanceTopPlace> getPlace(WalletModel wallet) {
        return PEconomyAPI.get().getBalanceTopPlaceFinder().findPlace(wallet, this, comparator);
    }

    @Override
    public @NotNull String getPlaceFormatted(int positionIndex) {
        if(formatter == null)
            throw new IllegalStateException("places formatter isn't provided for this balance top!");

        return formatter.format(getPlace(positionIndex));
    }

    @Override
    public Optional<String> getPlaceFormatted(WalletModel wallet) {
        if(formatter == null)
            throw new IllegalStateException("places formatter isn't provided for this balance top!");

        return getPlace(wallet).map(formatter::format);
    }

    @Override
    public @NotNull @UnmodifiableView List<BalanceTopPlace> getAllPlaces() {
        return Collections.unmodifiableList(places);
    }

    @Override
    public boolean hasPlace(int positionIndex) {
        return positionIndex >= 0 && positionIndex < places.size();
    }

    @Override
    public synchronized void refresh() {
        List<BalanceTopPlace> places = PEconomy.getAPI().getBalanceTopPlacesProvider().queryPlaces(this, comparator);
        this.places.clear();
        this.places.addAll(places);
    }

    @Override
    public @NotNull String toString() {
        return "BalanceTop{" +
                "plugin=" + plugin +
                ", currencyId='" + currencyId + '\'' +
                ", topSize=" + topSize +
                ", formatter=" + formatter +
                ", places=" + places +
                '}';
    }

    private static final class Builder implements BalanceTop.Builder {

        private final Plugin plugin;
        private String currencyId;
        private int topSize;
        private ObjectFormatter<BalanceTopPlace> formatter;

        private Builder(Plugin plugin) {
            Validate.notNull(plugin, "plugin");
            this.plugin = plugin;
        }

        @Override
        public @NotNull BalanceTop build() {
            return new SimpleBalanceTop(plugin, currencyId, topSize, formatter);
        }

        @Override
        public @NotNull BalanceTop.Builder currencyId(@NotNull String currencyId) {
            Validate.notNull(currencyId, "currencyId");
            this.currencyId = currencyId;
            return this;
        }

        @Override
        public @NotNull BalanceTop.Builder topSize(int amountOfPlaces) {
            this.topSize = amountOfPlaces;
            return this;
        }

        @Override
        public @NotNull BalanceTop.Builder formatter(@Nullable ObjectFormatter<BalanceTopPlace> formatter) {
            this.formatter = formatter;
            return this;
        }

        @Override
        public @NotNull String toString() {
            return "BalanceTop.Builder{" +
                    "plugin=" + plugin +
                    ", currencyId='" + currencyId + '\'' +
                    ", topSize=" + topSize +
                    ", formatter=" + formatter +
                    '}';
        }

    }

}
