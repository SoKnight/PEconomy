package ru.soknight.peconomy.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.balancetop.BalanceTop;

import java.util.Objects;

@Getter
@AllArgsConstructor
public final class CurrencyInstance {

    private final String id;
    private final String name;
    private final String symbol;
    private final float limit;
    private final float newbieAmount;

    private final BalanceTopSetup balanceTopSetup;
    private final BalanceTop balanceTop;
    
    public CurrencyInstance(String id, String name, String symbol) {
        this(id, name, symbol, 0F, 0F, null, null);
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
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(symbol, that.symbol) &&
                Objects.equals(balanceTop, that.balanceTop) &&
                Objects.equals(balanceTopSetup, that.balanceTopSetup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, symbol, limit, newbieAmount, balanceTop, balanceTopSetup);
    }

    @Override
    public @NotNull String toString() {
        return "CurrencyInstance{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", limit=" + limit +
                ", newbieAmount=" + newbieAmount +
                ", balanceTop=" + balanceTop +
                ", balanceTopSetup=" + balanceTopSetup +
                '}';
    }

}
