package ru.soknight.peconomy;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import ru.soknight.peconomy.commands.CommandBalance;
import ru.soknight.peconomy.commands.CommandHandler;
import ru.soknight.peconomy.commands.CommandPay;
import ru.soknight.peconomy.database.Database;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.files.Config;
import ru.soknight.peconomy.files.Messages;
import ru.soknight.peconomy.handlers.PlayersHandler;
import ru.soknight.peconomy.utils.Logger;

public class PEconomy extends JavaPlugin {

	@Getter private static PEconomy instance;
	@Getter private DatabaseManager DBManager;
	
	@Override
	public void onEnable() {
		instance = this;
		Config.refresh();
		Messages.refresh();
		
		// Loading database
		try {
			Database database = new Database();
			DBManager = new DatabaseManager(database);
		} catch (Exception e) {
			Logger.error("Database initialization failed: " + e.getLocalizedMessage());
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		// Try hook into PAPI
		hookIntoPapi();
		
		// Register command executors
		PluginCommand peco = getCommand("peco"), balance = getCommand("balance"), pay = getCommand("pay");
		peco.setExecutor(new CommandHandler()); 		peco.setTabCompleter(new CommandHandler());
		balance.setExecutor(new CommandBalance()); 	balance.setTabCompleter(new CommandBalance());
		pay.setExecutor(new CommandPay()); 			pay.setTabCompleter(new CommandPay());
		
		// Register event listeners
		getServer().getPluginManager().registerEvents(new PlayersHandler(), this);
		
		Logger.info("Enabled!");
	}
	
	@Override
	public void onDisable() {
		if(DBManager != null) DBManager.shutdown();
	}
	
	private void hookIntoPapi() {
		if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            boolean hooked = new PEcoExpansion(this).register();
            if(hooked) Logger.info("Hooked into PlaceholdersAPI!");
            else Logger.warning("Hooking to PlaceholdersAPI failed.");
		} else Logger.info("PlaceholdersAPI not found, hooking cancelled.");
	}
	
}
