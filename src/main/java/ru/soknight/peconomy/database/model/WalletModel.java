package ru.soknight.peconomy.database.model;

import java.util.HashMap;
import java.util.Objects;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.transaction.TransactionCause;

@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "walletowners")
public class WalletModel {

    @DatabaseField(columnName = "owner", id = true)
    private String walletHolder;

    @DatabaseField(columnName = "wallets", dataType = DataType.SERIALIZABLE)
    private HashMap<String, Float> wallets;
    
    public WalletModel(String walletHolder) {
        this.walletHolder = walletHolder;
        this.wallets = new HashMap<>();
    }
    
    public void loadCurrency(CurrencyInstance currency) {
        if(!wallets.containsKey(currency.getId()))
            addAmount(currency.getId(), currency.getNewbieAmount());
    }

    public void addAmount(String currency, float amount) {
        synchronized (this) {
            float pre = getAmount(currency);
            float post = pre + amount;
            wallets.put(currency, post);
        }
    }
    
    public TransactionModel addAmount(String currency, float amount, String operator) {
        synchronized (this) {
            float pre = getAmount(currency);
            float post = pre + amount;
            wallets.put(currency, post);

            return new TransactionModel(walletHolder, currency, pre, post, operator, TransactionCause.STAFF_ADD);
        }
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
        synchronized (this) {
            float pre = getAmount(currency);
            wallets.put(currency, 0F);
        }
    }

    public TransactionModel resetWallet(String currency, String operator) {
        synchronized (this) {
            float pre = getAmount(currency);
            wallets.put(currency, 0F);

            return new TransactionModel(walletHolder, currency, pre, 0F, operator, TransactionCause.STAFF_RESET);
        }
    }
    
    public void setAmount(String currency, float amount) {
        synchronized (this) {
            wallets.put(currency, amount);
        }
    }

    public TransactionModel setAmount(String currency, float amount, String operator) {
        synchronized (this) {
            float pre = getAmount(currency);
            wallets.put(currency, amount);

            return new TransactionModel(walletHolder, currency, pre, amount, operator, TransactionCause.STAFF_SET);
        }
    }

    public void takeAmount(String currency, float amount) {
        takeAmount(currency, amount, false);
    }
    
    public void takeAmount(String currency, float amount, boolean ignoreNegativeBalance) {
        synchronized (this) {
            float pre = getAmount(currency);
            float post = pre - amount;

            if(post >= 0F || ignoreNegativeBalance)
                wallets.put(currency, post);
        }
    }

    public TransactionModel takeAmount(String currency, float amount, String operator) {
        return takeAmount(currency, amount, operator, false);
    }

    public TransactionModel takeAmount(String currency, float amount, String operator, boolean ignoreNegativeBalance) {
        synchronized (this) {
            float pre = getAmount(currency);
            float post = pre - amount;

            if(post >= 0F || ignoreNegativeBalance)
                wallets.put(currency, post);

            return new TransactionModel(walletHolder, currency, pre, post, operator, TransactionCause.STAFF_TAKE);
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        WalletModel that = (WalletModel) o;
        return Objects.equals(walletHolder, that.walletHolder) &&
                Objects.equals(wallets, that.wallets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(walletHolder, wallets);
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "holder='" + walletHolder + '\'' +
                ", wallets=" + wallets +
                '}';
    }

}
