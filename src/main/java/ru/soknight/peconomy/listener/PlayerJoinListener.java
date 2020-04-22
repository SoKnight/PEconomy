package ru.soknight.peconomy.listener;

import java.util.Collection;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import lombok.AllArgsConstructor;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.Wallet;

@AllArgsConstructor
public class PlayerJoinListener implements Listener {

	private final DatabaseManager databaseManager;
	private final CurrenciesManager currenciesManager;
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		String name = e.getPlayer().getName();
		
		if(databaseManager.hasWallet(name)) return;
		
		Wallet wallet = new Wallet(name);
		
		Collection<CurrencyInstance> currencies = currenciesManager.getCurrencies();
		if(!currencies.isEmpty())
			currencies.forEach(currency -> wallet.setAmount(currency.getID(), currency.getNewbieAmount()));
		
		databaseManager.createWallet(wallet);
	}
	
}
