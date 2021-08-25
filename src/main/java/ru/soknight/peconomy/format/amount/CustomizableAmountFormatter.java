package ru.soknight.peconomy.format.amount;

import org.jetbrains.annotations.NotNull;
import ru.soknight.lib.configuration.Configuration;

import java.util.IllegalFormatException;

final class CustomizableAmountFormatter implements AmountFormatter {

    private static final String DEFAULT_FORMAT = "%.2f";
    private String currentFormat;

    @Override
    public String formatAmount(double amount) {
        return String.format(currentFormat, amount);
    }

    @Override
    public boolean updateFormat(@NotNull Configuration config) {
        return updateFormat(config, "amount-format");
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean updateFormat(@NotNull Configuration config, @NotNull String key) {
        String configFormat = config.getString(key, DEFAULT_FORMAT);
        try {
            String.format(configFormat, 0.128D);
            this.currentFormat = configFormat;
            return true;
        } catch (IllegalFormatException ex) {
            this.currentFormat = DEFAULT_FORMAT;
            return false;
        }
    }

}
