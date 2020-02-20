package ru.soknight.peconomy.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.database.Balance;
import ru.soknight.peconomy.database.DatabaseManager;

public class PlayersHandler implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		String name = e.getPlayer().getName();
		
		DatabaseManager dbm = PEconomy.getInstance().getDBManager();
		if(dbm.isInDatabase(name)) return;
		
		dbm.create(new Balance(name));
		return;
	}
	
}
