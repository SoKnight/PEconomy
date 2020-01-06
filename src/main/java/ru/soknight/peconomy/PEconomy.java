package ru.soknight.peconomy;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import ru.soknight.peconomy.commands.CommandBalance;
import ru.soknight.peconomy.commands.CommandPay;
import ru.soknight.peconomy.commands.SubCommands;
import ru.soknight.peconomy.database.Database;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.files.Config;
import ru.soknight.peconomy.files.Messages;
import ru.soknight.peconomy.handlers.PlayersHandler;
import ru.soknight.peconomy.utils.Logger;

public class PEconomy extends JavaPlugin {

	//public static DecimalFormat df = new DecimalFormat("#0.00");
	private static PEconomy instance;
	private Database database;
	
	@Override
	public void onEnable() {
		instance = this;
		Config.refresh();
		Messages.refresh();
		
		// Loading database
		try {
			database = new Database();
			DatabaseManager.loadFromDatabase();
		} catch (Exception e) {
			Logger.error("Couldn't connect database type " + Config.config.getString("database.type") + ":");
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
		}
		
		// Try hook into PAPI
		hookIntoPapi();
		
		// Register command executors
		PluginCommand peco = getCommand("peco"), balance = getCommand("balance"), pay = getCommand("pay");
		peco.setExecutor(new SubCommands()); 		peco.setTabCompleter(new SubCommands());
		balance.setExecutor(new CommandBalance()); 	balance.setTabCompleter(new CommandBalance());
		pay.setExecutor(new CommandPay()); 			pay.setTabCompleter(new CommandPay());
		
		// Register event listeners
		getServer().getPluginManager().registerEvents(new PlayersHandler(), this);
		
		Logger.info("Enabled!");
	}
	
	@Override
	public void onDisable() {
		DatabaseManager.saveToDatabase();
		Logger.info("Disabled!");
	}
	
	private void hookIntoPapi() {
		if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            boolean hooked = new PEcoExpansion(this).register();
            if(hooked) Logger.info("Hooked into PlaceholdersAPI.");
            else Logger.warning("Hooking to PlaceholdersAPI failed.");
		} else Logger.info("PlaceholdersAPI not found, hooking cancelled.");
	}

	public static PEconomy getInstance() {
		return instance;
	}

	public Database getDatabase() {
		return database;
	}
	
}
