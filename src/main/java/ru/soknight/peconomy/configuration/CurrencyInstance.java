package ru.soknight.peconomy.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class CurrencyInstance {

    private final String id;
    private final String name;
    private final String symbol;
    private final float limit;
    private final float newbieAmount;
    
    public CurrencyInstance(String id, String name, String symbol) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.limit = 0F;
        this.newbieAmount = 0F;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        CurrencyInstance that = (CurrencyInstance) o;
        return Float.compare(that.limit, limit) == 0 &&
                Float.compare(that.newbieAmount, newbieAmount) == 0 &&
                Objects.equals(id, that.id) &&
                Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, symbol, limit, newbieAmount);
    }

    @Override
    public String toString() {
        return "CurrencyInstance{" +
                "id='" + id + '\'' +
                ", symbol='" + symbol + '\'' +
                ", limit=" + limit +
                ", newbieAmount=" + newbieAmount +
                '}';
    }

}
