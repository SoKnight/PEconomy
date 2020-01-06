package ru.soknight.peconomy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.utils.Logger;

public class DatabaseManager {
	
    private static Map<String, Balance> balances = new HashMap<>();
	
    public static int loadFromDatabase() {
    	Database db = PEconomy.getInstance().getDatabase();
		String query = "SELECT player, dollars, euro FROM balances";
		int count = 0; 
		
		try {
			Connection connection = db.getConnection();
			Statement stm = connection.createStatement();
			
			ResultSet output = stm.executeQuery(query);
			Logger.info("Loading balances from database...");
			long start = System.currentTimeMillis();
			while(output.next()) {
				String name = output.getString("player");
				float dollars = output.getFloat("dollars");
				float euro = output.getFloat("euro");
				Balance balance = new Balance(name, dollars, euro);
				balances.put(name, balance);
				count++;
			}
			long current = System.currentTimeMillis();
			Logger.info("Loaded " + count + " balances. Time took: " + (current - start) + " ms.");
			stm.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
    }
    
	public static void saveToDatabase() {
		if(balances.isEmpty()) return;
		 
		Database db = PEconomy.getInstance().getDatabase();
		String query = "INSERT INTO balances (dollars, euro, player) VALUES (?, ?, ?);";
		
		try {
			Connection connection = db.getConnection();
			Statement delstm = connection.createStatement();
			
			delstm.execute("DELETE FROM balances");
			
			for(String name : balances.keySet()) {
				PreparedStatement stm = connection.prepareStatement(query);
				Balance b = balances.get(name);
				stm.setFloat(1, b.getDollars());
				stm.setFloat(2, b.getEuro());
				stm.setString(3, name);
				stm.executeUpdate();
				stm.close(); }
			
			Logger.info(balances.size() + " balances saved to database.");
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return;
	}
	
	public static int getBalancesCount() {
		return balances.size();
	}
	
	public static boolean isInDatabase(String name) {
		return balances.containsKey(name);
	}
	
	public static void setBalance(String name, Balance balance) {
		balances.put(name, balance);
	}
	
	public static Balance getBalance(String name) {
		if(!isInDatabase(name)) return null;
		return balances.get(name);
	}
	
	public static float addAmount(String name, float amount, String wallet) {
		Balance balance = new Balance(name, 0f, 0f);
		if(isInDatabase(name)) balance = getBalance(name);
		
		float current = amount;
		if(wallet.equals("euro")) current = balance.addEuro(amount);
		else current = balance.addDollars(amount);
		setBalance(name, balance);
		return current;
	}
	
	public static float getAmount(String name, String wallet) {
		if(!isInDatabase(name)) return 0;
		Balance balance = getBalance(name);
		
		float current;
		if(wallet.equals("euro")) current = balance.getEuro();
		else current = balance.getDollars();
		return current;
	}
	
	public static boolean hasAmount(String name, float amount, String wallet) {
		if(!isInDatabase(name)) return false;
		Balance balance = getBalance(name);
		
		if(wallet.equals("euro")) return balance.hasEuro(amount);
		return balance.hasDollars(amount);
	}
	
	public static float setAmount(String name, float amount, String wallet) {
		Balance balance = new Balance(name, 0f, 0f);
		if(isInDatabase(name)) balance = getBalance(name);
		
		float current = 0;
		if(wallet.equals("euro")) {
			current = balance.getEuro();
			balance.setEuro(amount);
		} else {
			current = balance.getDollars();
			balance.setDollars(amount);
		}
		setBalance(name, balance);
		return current;
	}
	
	public static float resetAmount(String name, String wallet) {
		if(!isInDatabase(name)) return 0;
		Balance balance = getBalance(name);
		
		float current = 0;
		if(wallet.equals("euro")) {
			current = balance.getEuro();
			balance.setEuro(0f);
		} else {
			current = balance.getDollars();
			balance.setDollars(0f);
		}
		setBalance(name, balance);
		return current;
	}
	
	public static float takeAmount(String name, float amount, String wallet) {
		if(!isInDatabase(name)) return 0;
		Balance balance = getBalance(name);
		
		float current = 0;
		if(wallet.equals("euro")) {
			current = balance.getEuro();
			balance.takeEuro(amount);
		} else {
			current = balance.getDollars();
			balance.takeDollars(amount);
		}
		setBalance(name, balance);
		return current;
	}
	
}
