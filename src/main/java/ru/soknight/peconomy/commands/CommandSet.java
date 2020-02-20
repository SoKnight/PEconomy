package ru.soknight.peconomy.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.files.Messages;
import ru.soknight.peconomy.utils.Utils;

public class CommandSet extends AbstractSubCommand {

	private final CommandSender sender;
	private final String[] args;
	
	public CommandSet(CommandSender sender, String[] args) {
		super(sender, args, "peco.set", 4);
		this.sender = sender;
		this.args = args;
	}
	
	@Override
	public void execute() {
		if(!hasPermission()) return;
		if(!isCorrectUsage()) return;
		
		String name = args[1], amstr = args[2], wallet = args[3];
		if(!isCorrectWallet(wallet)) return;
		if(!argIsInteger(amstr)) return;
		if(!isPlayerInDatabase(name)) return;
		
		DatabaseManager dbm = PEconomy.getInstance().getDBManager();
		
		float amount = Float.parseFloat(amstr);
		float current = dbm.setAmount(name, amount, wallet);
		float newbal = amount;
		
		String cs = Utils.format(current), ns = Utils.format(newbal);
		String fsender, freceiver;
		if(wallet.equals("euro")) {
			fsender = Messages.formatMessage("set-euro", "%player%", name, "%current%", cs, "%new%", ns);
			freceiver = Messages.formatMessage("set-euro-myself", "%current%", cs, "%new%", ns);
		} else {
			fsender = Messages.formatMessage("set-dollars", "%player%", name, "%current%", cs, "%new%", ns);
			freceiver = Messages.formatMessage("set-dollars-myself", "%current%", cs, "%new%", ns);
		}
		
		sender.sendMessage(fsender);
		OfflinePlayer offtrgt = Bukkit.getOfflinePlayer(name);
		if(offtrgt.isOnline()) offtrgt.getPlayer().sendMessage(freceiver);
		return;
	}
	
}
