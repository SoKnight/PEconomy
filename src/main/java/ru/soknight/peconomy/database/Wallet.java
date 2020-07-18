package ru.soknight.peconomy.database;

import java.util.HashMap;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "walletowners")
public class Wallet {

	@DatabaseField(id = true)
	private String owner;
	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private HashMap<String, Float> wallets;
	
	public Wallet(String owner) {
		this.owner = owner;
		this.wallets = new HashMap<>();
	}
	
	public void addAmount(String currency, float amount) {
		float pre = getAmount(currency);
		float post = pre + amount;
		
		this.wallets.put(currency, post);
	}
	
	public float getAmount(String currency) {
		return hasWallet(currency) ? this.wallets.get(currency) : 0F;
	}
	
	public boolean hasAmount(String currency, float amount) {
		return amount <= getAmount(currency);
	}
	
	public boolean hasWallet(String currency) {
		return this.wallets.containsKey(currency);
	}

	public void resetWallet(String currency) {
		this.wallets.put(currency, 0F);
	}
	
	public void setAmount(String currency, float amount) {
		this.wallets.put(currency, amount);
	}
	
	public void takeAmount(String currency, float amount) {
		float pre = getAmount(currency);
		float post = pre - amount;
		
		if(post >= 0F) this.wallets.put(currency, post);
	}
	
}
