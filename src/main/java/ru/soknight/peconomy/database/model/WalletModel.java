package ru.soknight.peconomy.database.model;

import java.util.HashMap;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.soknight.peconomy.configuration.CurrencyInstance;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "walletowners")
public class WalletModel {

    @DatabaseField(id = true, columnName = "owner")
    private String walletHolder;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private HashMap<String, Float> wallets;
    
    public WalletModel(String walletHolder) {
        this.walletHolder = walletHolder;
        this.wallets = new HashMap<>();
    }
    
    public void loadCurrency(CurrencyInstance currency) {
        if(!wallets.containsKey(currency.getID()))
            addAmount(currency.getID(), currency.getNewbieAmount());
    }
    
    public void addAmount(String currency, float amount) {
        float pre = getAmount(currency);
        float post = pre + amount;
        
        wallets.put(currency, post);
    }
    
    public float getAmount(String currency) {
        return hasWallet(currency) ? wallets.get(currency) : 0F;
    }
    
    public boolean hasAmount(String currency, float amount) {
        return amount <= getAmount(currency);
    }
    
    public boolean hasWallet(String currency) {
        return wallets.containsKey(currency);
    }

    public void resetWallet(String currency) {
        wallets.put(currency, 0F);
    }
    
    public void setAmount(String currency, float amount) {
        wallets.put(currency, amount);
    }
    
    public void takeAmount(String currency, float amount) {
        float pre = getAmount(currency);
        float post = pre - amount;
        
        if(post >= 0F)
            wallets.put(currency, post);
    }
    
}
