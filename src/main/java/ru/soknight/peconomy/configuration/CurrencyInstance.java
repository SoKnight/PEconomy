package ru.soknight.peconomy.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.soknight.peconomy.balancetop.BalanceTop;

import java.util.Objects;

@Getter
@AllArgsConstructor
public final class CurrencyInstance {

    private final @NotNull String id;
    private final @NotNull String name;
    private final @NotNull String symbol;
    private final float limit;
    private final float newbieAmount;
    private final boolean visible;
    private final boolean transferable;

    private final @Nullable BalanceTopSetup balanceTopSetup;
    private final @Nullable BalanceTop balanceTop;
    
    public CurrencyInstance(@NotNull String id, @NotNull String name, @NotNull String symbol) {
        this(id, name, symbol, 0F, 0F, true, true, null, null);
    }

    public boolean useBalanceTop() {
        return balanceTop != null && balanceTopSetup != null && balanceTopSetup.isValid();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CurrencyInstance that = (CurrencyInstance) o;
        return Float.compare(that.limit, limit) == 0 &&
                Float.compare(that.newbieAmount, newbieAmount) == 0 &&
                visible == that.visible &&
                transferable == that.transferable &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(symbol, that.symbol) &&
                Objects.equals(balanceTopSetup, that.balanceTopSetup) &&
                Objects.equals(balanceTop, that.balanceTop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id, name, symbol, limit, newbieAmount, visible,
                transferable, balanceTopSetup, balanceTop
        );
    }

    @Override
    public @NotNull String toString() {
        return "CurrencyInstance{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", limit=" + limit +
                ", newbieAmount=" + newbieAmount +
                ", visible=" + visible +
                ", transferable=" + transferable +
                ", balanceTopSetup=" + balanceTopSetup +
                ", balanceTop=" + balanceTop +
                '}';
    }

}
