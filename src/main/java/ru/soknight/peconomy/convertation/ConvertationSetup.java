package ru.soknight.peconomy.convertation;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import ru.soknight.peconomy.configuration.CurrenciesManager;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class ConvertationSetup {

    private final String currencyId;
    private final Map<String, ConvertationRate> rates;

    public ConvertationSetup(@NotNull String currencyId) {
        this.currencyId = currencyId;
        this.rates = new LinkedHashMap<>();
    }

    public void load(@NotNull CurrenciesManager currenciesManager, @NotNull ConfigurationSection config) {
        rates.clear();

        Logger logger = currenciesManager.getPlugin().getLogger();

        config.getValues(false).forEach((key, value) -> {
            if(value == null)
                return;

            if(key.equals(currencyId)) {
                showWarning(logger, "This currency has convertation rate for itself");
                return;
            }

            if(!currenciesManager.isCurrency(key)) {
                showWarning(logger, "This currency has convertation rate for unknown currency '%s'", key);
                return;
            }

            try {
                ConvertationRate rate = ConvertationRate.parse(value);
                rates.put(key, rate);
            } catch (ConvertationRateParseException ex) {
                showError(logger, ex.getMessage());
            }
        });
    }

    public @Nullable ConvertationRate getRate(@NotNull String currencyId) {
        return this.currencyId.equals(currencyId) ? ConvertationRate.DEFAULT : rates.get(currencyId);
    }

    public @NotNull @UnmodifiableView Set<String> getCurrenciesIDs() {
        return Collections.unmodifiableSet(rates.keySet());
    }

    public boolean hasRate(@NotNull String currencyId) {
        return this.currencyId.equals(currencyId) || rates.containsKey(currencyId);
    }

    private void showWarning(@NotNull Logger logger, @NotNull String message, @Nullable Object... args) {
        String formattedMessage = String.format(message, args);
        logger.warning(String.format("[%s]: %s", currencyId, formattedMessage));
    }

    private void showError(@NotNull Logger logger, @NotNull String message, @Nullable Object... args) {
        String formattedMessage = String.format(message, args);
        logger.severe(String.format("[%s]: %s", currencyId, formattedMessage));
    }

}
