package ru.soknight.peconomy.format.amount;

import org.jetbrains.annotations.NotNull;
import ru.soknight.lib.configuration.Configuration;

public interface AmountFormatter {

    static @NotNull AmountFormatter createCustomizable() {
        return new CustomizableAmountFormatter();
    }

    String formatAmount(double amount);

    boolean updateFormat(@NotNull Configuration config);

    boolean updateFormat(@NotNull Configuration config, @NotNull String key);

}
