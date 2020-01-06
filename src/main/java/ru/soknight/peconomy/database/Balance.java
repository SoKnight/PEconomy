package ru.soknight.peconomy.database;

public class Balance {

	private String name;
	private float dollars, euro;
	
	public Balance(String name, float dollars, float euro) {
		this.name = name;
		this.setDollars(dollars);
		this.euro = euro;
	}

	public String getName() {
		return name;
	}
	
	//
	//  DOLLAR WALLET
	//
	public float addDollars(float dollars) {
		float current = this.dollars;
		this.dollars += dollars;
		return current;
	}

	public float getDollars() {
		return dollars;
	}
	
	public boolean hasDollars(float dollars) {
		return (dollars <= this.dollars);
	}

	public void setDollars(float dollars) {
		this.dollars = dollars;
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

	public float getEuro() {
		return euro;
	}
	
	public boolean hasEuro(float euro) {
		return (euro <= this.euro);
	}

	public void setEuro(float euro) {
		this.euro = euro;
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
