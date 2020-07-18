package ru.soknight.peconomy.database;

import java.io.File;
import java.sql.SQLException;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import ru.soknight.lib.configuration.Configuration;
import ru.soknight.peconomy.PEconomy;

public class Database {

	private final String url;
	private final boolean useSQLite;
	
	private String user;
	private String password;
	
	public Database(PEconomy plugin, Configuration config) throws Exception {
		this.useSQLite = config.getBoolean("database.use-sqlite", true);
		
		if(!useSQLite) {
			String host = config.getString("database.host", "localhost");
			String name = config.getString("database.name", "peconomy");
			
			int port = config.getInt("database.port", 3306);
			this.user = config.getString("database.user", "admin");
			this.password = config.getString("database.password", "peconomy");
			
			String url = "jdbc:mysql://" + host + ":" + port + "/" + name;
			
			// Thanks to Ansandr for this issue
			if(config.getBoolean("database.reconnect", true))
				url += "?autoReconnect=true";
			
			this.url = url;
			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} else {
			String file = config.getString("database.file", "peconomy.db");
			
			this.url = "jdbc:sqlite:" + plugin.getDataFolder().getPath() + File.separator + file;
			
			Class.forName("org.sqlite.JDBC").newInstance();
		}
		
		// Allowing only ORMLite errors logging
		System.setProperty("com.j256.ormlite.logger.type", "LOCAL");
		System.setProperty("com.j256.ormlite.logger.level", "ERROR");
				
		ConnectionSource source = getConnection();

		TableUtils.createTableIfNotExists(source, Wallet.class);
		TableUtils.createTableIfNotExists(source, Transaction.class);
		
		source.close();
		
		plugin.getLogger().info("Database type " + (useSQLite ? "SQLite" : "MySQL") + " connected!");
	}
	
	public ConnectionSource getConnection() throws SQLException {
		return useSQLite ? new JdbcConnectionSource(url) : new JdbcConnectionSource(url, user, password);
	}
	
}
