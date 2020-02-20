package ru.soknight.peconomy.database;

import java.io.IOException;
import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import ru.soknight.peconomy.utils.Logger;

public class DatabaseManager {
	
	private ConnectionSource source;
	private Dao<Balance, String> dao;
	
	public DatabaseManager(Database database) throws SQLException {
		source = database.getConnection();
		dao = DaoManager.createDao(source, Balance.class);
	}
	
	public void shutdown() {
		try {
			source.close();
			Logger.info("Database connection closed.");
		} catch (IOException e) {
			Logger.error("Failed close database connection: " + e.getLocalizedMessage());
		}
	}
	
	public Balance get(String name) {
		try {
			Balance balance = dao.queryForId(name);
			return balance;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Balance getOrCreate(String name) {
		try {
			Balance balance = dao.queryForId(name);
			if(balance != null) return balance;
			
			balance = new Balance(name);
			create(balance);
			return balance;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int create(Balance balance) {
		try {
			return dao.create(balance);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public int update(Balance balance) {
		try {
			return dao.update(balance);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public int getBalancesCount() {
		try {
			return dao.queryForAll().size();
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public boolean isInDatabase(String name) {
		try {
			Balance balance = dao.queryForId(name);
			return balance != null;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public float addAmount(String name, float amount, String wallet) {
		Balance balance = getOrCreate(name);
		
		float current = amount;
		if(wallet.equals("euro")) current = balance.addEuro(amount);
		else current = balance.addDollars(amount);
		update(balance);
		return current;
	}
	
	public float getAmount(String name, String wallet) {
		if(!isInDatabase(name)) return 0;
		Balance balance = get(name);
		
		float current;
		if(wallet.equals("euro")) current = balance.getEuro();
		else current = balance.getDollars();
		return current;
	}
	
	public boolean hasAmount(String name, float amount, String wallet) {
		if(!isInDatabase(name)) return false;
		Balance balance = get(name);
		
		if(wallet.equals("euro")) return balance.hasEuro(amount);
		return balance.hasDollars(amount);
	}
	
	public float setAmount(String name, float amount, String wallet) {
		Balance balance = getOrCreate(name);
		
		float current = 0;
		if(wallet.equals("euro")) {
			current = balance.getEuro();
			balance.setEuro(amount);
		} else {
			current = balance.getDollars();
			balance.setDollars(amount);
		}
		update(balance);
		return current;
	}
	
	public float resetAmount(String name, String wallet) {
		if(!isInDatabase(name)) return 0;
		Balance balance = get(name);
		
		float current = 0;
		if(wallet.equals("euro")) {
			current = balance.getEuro();
			balance.setEuro(0f);
		} else {
			current = balance.getDollars();
			balance.setDollars(0f);
		}
		update(balance);
		return current;
	}
	
	public float takeAmount(String name, float amount, String wallet) {
		if(!isInDatabase(name)) return 0;
		Balance balance = get(name);
		
		float current = 0;
		if(wallet.equals("euro")) {
			current = balance.getEuro();
			balance.takeEuro(amount);
		} else {
			current = balance.getDollars();
			balance.takeDollars(amount);
		}
		update(balance);
		return current;
	}
	
}
