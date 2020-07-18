package ru.soknight.peconomy.listener;

import java.util.Collection;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.Wallet;

public class PlayerJoinListener implements Listener {

	private final Plugin plugin;
	private final BukkitScheduler scheduler;
	
	private final DatabaseManager databaseManager;
	private final CurrenciesManager currenciesManager;
	
	public PlayerJoinListener(Plugin plugin, DatabaseManager databaseManager,
			CurrenciesManager currenciesManager) {
		
		this.plugin = plugin;
		this.scheduler = plugin.getServer().getScheduler();
		
		this.databaseManager = databaseManager;
		this.currenciesManager = currenciesManager;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		String name = e.getPlayer().getName();
		
		// Moved to async task
		scheduler.runTaskAsynchronously(plugin, () -> {
			if(databaseManager.hasWallet(name)) return;
			
			Wallet wallet = new Wallet(name);
			
			Collection<CurrencyInstance> currencies = currenciesManager.getCurrencies();
			if(!currencies.isEmpty())
				currencies.forEach(currency -> wallet.setAmount(currency.getID(), currency.getNewbieAmount()));
			
			databaseManager.createWallet(wallet);
		});
	}
	
}
