package ru.soknight.peconomy.util;

public class AmountFormatter {

    private static final String format = "%.2f";
    
    public static String format(float amount) {
        return String.format(format, amount);
    }
    
}
