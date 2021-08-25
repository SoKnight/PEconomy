package ru.soknight.peconomy.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
@AllArgsConstructor
public final class BalanceTopSetup {

    private final boolean enabled;
    private final int updatePeriod;
    private final int maxSize;
    private final String formatExist;
    private final String formatEmpty;

    public BalanceTopSetup() {
        this(false, 0, 9, "", "");
    }

    public boolean isValid() {
        return enabled && updatePeriod > 0 && maxSize > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BalanceTopSetup that = (BalanceTopSetup) o;
        return enabled == that.enabled &&
                updatePeriod == that.updatePeriod &&
                maxSize == that.maxSize &&
                Objects.equals(formatExist, that.formatExist) &&
                Objects.equals(formatEmpty, that.formatEmpty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, updatePeriod, maxSize, formatExist, formatEmpty);
    }

    @Override
    public @NotNull String toString() {
        return "BalanceTopSetup{" +
                "enabled=" + enabled +
                ", updatePeriod=" + updatePeriod +
                ", maxSize=" + maxSize +
                ", formatExist='" + formatExist + '\'' +
                ", formatEmpty='" + formatEmpty + '\'' +
                '}';
    }

}
