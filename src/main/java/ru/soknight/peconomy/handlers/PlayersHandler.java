package ru.soknight.peconomy.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import ru.soknight.peconomy.database.Balance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.files.Config;

public class PlayersHandler implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		String name = e.getPlayer().getName();
		if(DatabaseManager.isInDatabase(name)) return;
		
		Balance balance = new Balance(name, Config.dollars, Config.euro);
		DatabaseManager.setBalance(name, balance);
		return;
	}
	
}
