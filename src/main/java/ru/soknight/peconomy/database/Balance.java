package ru.soknight.peconomy.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.soknight.peconomy.files.Config;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "balances")
public class Balance {

	@DatabaseField(id = true)
	private String name;
	@DatabaseField
	private float dollars;
	@DatabaseField
	private float euro;
	
	public Balance(String name) {
		this.name = name;
		this.dollars = (float) Config.getConfig().getDouble("default.dollars", 0f);
		this.euro = (float) Config.getConfig().getDouble("default.euro", 0f);
	}
	
	//
	//  DOLLAR WALLET
	//
	public float addDollars(float dollars) {
		float current = this.dollars;
		this.dollars += dollars;
		return current;
	}
	
	public boolean hasDollars(float dollars) {
		return (dollars <= this.dollars);
	}

	public void resetDollars() {
		dollars = 0;
	}
	
	public float takeDollars(float dollars) {
		float current = this.dollars;
		this.dollars -= dollars;
		return current;
	}
	
	//
	//  EURO WALLET
	//
	public float addEuro(float euro) {
		float current = this.euro;
		this.euro += euro;
		return current;
	}
	
	public boolean hasEuro(float euro) {
		return (euro <= this.euro);
	}

	public void resetEuro() {
		euro = 0;
	}
	
	public float takeEuro(float euro) {
		float current = this.euro;
		this.euro -= euro;
		return current;
	}
	
}
