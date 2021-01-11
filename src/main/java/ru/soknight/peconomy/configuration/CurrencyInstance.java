package ru.soknight.peconomy.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CurrencyInstance {

    private final String ID;
    private final String symbol;
    private float limit;
    private float newbieAmount;
    
    public CurrencyInstance(String id, String symbol) {
        this.ID = id;
        this.symbol = symbol;
        this.limit = 0F;
        this.newbieAmount = 0F;
    }
    
}
