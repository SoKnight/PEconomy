package ru.soknight.peconomy;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.command.CommandBalance;
import ru.soknight.peconomy.command.CommandPay;
import ru.soknight.peconomy.command.SubcommandHandler;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.MessagesProvider;
import ru.soknight.peconomy.database.Database;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.hook.PEcoExpansion;
import ru.soknight.peconomy.hook.VaultEconomy;
import ru.soknight.peconomy.listener.PlayerJoinListener;

public class PEconomy extends JavaPlugin {

	private static PEcoAPI api;
	
	protected DatabaseManager databaseManager;
	protected CurrenciesManager currenciesManager;
	
	protected Configuration pluginConfig;
	protected Messages messages;
	
	@Override
	public void onEnable() {
		long start = System.currentTimeMillis();
		
		// Configs initialization
		refreshConfigs();
		
		// Database initialization
		try {
			Database database = new Database(this, pluginConfig);
			this.databaseManager = new DatabaseManager(this, database);
		} catch (Exception e) {
			getLogger().severe("Failed to initialize database: " + e.getLocalizedMessage());
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		// Commands executors initialization
		registerCommands();
		
		// Join event listener initialization
		PlayerJoinListener joinListener = new PlayerJoinListener(databaseManager, currenciesManager);
		getServer().getPluginManager().registerEvents(joinListener, this);
		
		// PEconomy API initialization
		api = new PEcoAPI(databaseManager);
		
		// Trying to hook into PAPI and Vault
		hookInto();
		
		long time = System.currentTimeMillis() - start;
		getLogger().info("Bootstrapped in " + time + " ms.");
	}
	
	@Override
	public void onDisable() {
		if(databaseManager != null) databaseManager.shutdown();
	}
	
	public void refreshConfigs() {
		this.pluginConfig = new Configuration(this, "config.yml");
		this.pluginConfig.refresh();
		
		this.messages = new MessagesProvider(this, pluginConfig).getMessages();
		this.currenciesManager = new CurrenciesManager(this, pluginConfig);
	}
	
	public void registerCommands() {
		SubcommandHandler subcommandHandler = new SubcommandHandler(this, currenciesManager, databaseManager, pluginConfig, messages);
		CommandBalance commandBalance = new CommandBalance(databaseManager, currenciesManager, pluginConfig, messages);
		CommandPay commandPay = new CommandPay(databaseManager, currenciesManager, pluginConfig, messages);
		
		PluginCommand peco = getCommand("peco");
		PluginCommand balance = getCommand("balance");
		PluginCommand pay = getCommand("pay");
		
		peco.setExecutor(subcommandHandler);
		peco.setTabCompleter(subcommandHandler);
		
		balance.setExecutor(commandBalance);
		balance.setTabCompleter(commandBalance);
		
		pay.setExecutor(commandPay);
		pay.setTabCompleter(commandPay);
	}
	
	private void hookInto() {
		if(pluginConfig.getBoolean("hooks.papi")) {
			if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
				PEcoExpansion papiExpansion = new PEcoExpansion(this, databaseManager);
				
				if(papiExpansion.register())
					getLogger().info("Hooked into PlaceholdersAPI successfully.");
				else getLogger().warning("Hooking into PlaceholdersAPI failed.");
				
			} else getLogger().info("Couldn't find PlaceholdersAPI to hook into, ignoring it.");
		}
		
		if(pluginConfig.getBoolean("hooks.vault")) {
			if(Bukkit.getPluginManager().getPlugin("Vault") != null) {
				Economy economy = new VaultEconomy(databaseManager, currenciesManager, pluginConfig, messages);
			
				ServicesManager sm = getServer().getServicesManager();
				sm.register(Economy.class, economy, this, ServicePriority.Highest);
			
				RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
				if(rsp != null)
					getLogger().info("Hooked into Vault successfully.");
				else getLogger().warning("Hooking into Vault failed.");
				
			} else getLogger().info("Couldn't find Vault to hook into, ignoring it.");
		}
	}
	
	/**
	 * Gets initialized API instance or null if PEconomy is not initialized fully
	 * @return PEconomy API instance (may be null)
	 */
	public static PEcoAPI getAPI() {
		return api;
	}
	
}
