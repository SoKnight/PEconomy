package ru.soknight.peconomy.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.configuration.file.FileConfiguration;

import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.files.Config;
import ru.soknight.peconomy.utils.Logger;

public class Database {

	static String database_type;
	static String database_url;
	static String mysql_host;
	static String mysql_name;
	static String mysql_user;
	static String mysql_password;
	static String sqlite_file;
	static int mysql_port;
	
	public Database() throws Exception {
		FileConfiguration c = Config.config;
		database_type = c.getString("database.type");
		if(database_type.equals("mysql")) {
			mysql_host = c.getString("database.host");
			mysql_name = c.getString("database.name");
			mysql_user = c.getString("database.user");
			mysql_password = c.getString("database.password");
			mysql_port = c.getInt("database.port");
			database_url = "jdbc:mysql://" + mysql_host + ":" + mysql_port + "/" + mysql_name;
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} else {
			sqlite_file = c.getString("database.file");
			database_url = "jdbc:sqlite:" + PEconomy.getInstance().getDataFolder() + File.separator + sqlite_file;
			Class.forName("org.sqlite.JDBC").newInstance();
		}
		
		Connection connection = getConnection();
		Statement s = connection.createStatement();
		
		s.executeUpdate("CREATE TABLE IF NOT EXISTS balances (player TEXT, dollars FLOAT, euro FLOAT);");
		
		s.close();
		connection.close();
		Logger.info("Database type " + database_type + " connected!");
	}
	
	public Connection getConnection() throws SQLException {
		if(database_type.equals("mysql"))
			return DriverManager.getConnection(database_url, mysql_user, mysql_password);
		else return DriverManager.getConnection(database_url);
	}
	
}
