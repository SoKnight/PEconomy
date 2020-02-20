package ru.soknight.peconomy.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.files.Messages;
import ru.soknight.peconomy.utils.Utils;

public class CommandReset extends AbstractSubCommand {

	private final CommandSender sender;
	private final String[] args;
	
	public CommandReset(CommandSender sender, String[] args) {
		super(sender, args, "peco.reset", 3);
		this.sender = sender;
		this.args = args;
	}
	
	@Override
	public void execute() {
		if(!hasPermission()) return;
		if(!isCorrectUsage()) return;
		
		String name = args[1], wallet = args[2];
		if(!isCorrectWallet(wallet)) return;
		if(!isPlayerInDatabase(name)) return;
		
		DatabaseManager dbm = PEconomy.getInstance().getDBManager();
		float current = dbm.resetAmount(name, wallet);
		
		String cs = Utils.format(current);
		String fsender, freceiver;
		if(wallet.equals("euro")) {
			fsender = Messages.formatMessage("reset-euro", "%player%", name, "%current%", cs);
			freceiver = Messages.formatMessage("reset-euro-myself", "%current%", cs);
		} else {
			fsender = Messages.formatMessage("reset-dollars", "%player%", name, "%current%", cs);
			freceiver = Messages.formatMessage("reset-dollars-myself", "%current%", cs);
		}
		
		sender.sendMessage(fsender);
		OfflinePlayer offtrgt = Bukkit.getOfflinePlayer(name);
		if(offtrgt.isOnline()) offtrgt.getPlayer().sendMessage(freceiver);
		return;
	}
	
}
