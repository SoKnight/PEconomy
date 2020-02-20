package ru.soknight.peconomy.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.files.Messages;
import ru.soknight.peconomy.utils.Utils;

public class CommandTake extends AbstractSubCommand {

	private final CommandSender sender;
	private final String[] args;
	
	public CommandTake(CommandSender sender, String[] args) {
		super(sender, args, "peco.take", 4);
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
		float current = dbm.getAmount(name, wallet);
		String as = Utils.format(amount), cs = Utils.format(current);
		if(!dbm.hasAmount(name, amount, wallet)) {
			if(wallet.equals("euro")) {
				String msg = Messages.formatMessage("taking-failed-euro", "%player%", name, "%current%", cs, "%amount%", as);
				sender.sendMessage(msg);
			} else {
				String msg = Messages.formatMessage("taking-failed-dollars", "%player%", name, "%current%", cs, "%amount%", as);
				sender.sendMessage(msg);
			}
			return;
		}
		current = dbm.takeAmount(name, amount, wallet);
		float newbal = current - amount;
		
		String ns = Utils.format(newbal); cs = Utils.format(current);
		String fsender, freceiver;
		if(wallet.equals("euro")) {
			fsender = Messages.formatMessage("taked-euro", "%player%", name, "%taked%", as, "%current%", cs, "%new%", ns);
			freceiver = Messages.formatMessage("taked-euro-myself", "%taked%", as, "%current%", cs, "%new%", ns);
		} else {
			fsender = Messages.formatMessage("taked-dollars", "%player%", name, "%taked%", as, "%current%", cs, "%new%", ns);
			freceiver = Messages.formatMessage("taked-dollars-myself", "%taked%", as, "%current%", cs, "%new%", ns);
		}
		
		sender.sendMessage(fsender);
		OfflinePlayer offtrgt = Bukkit.getOfflinePlayer(name);
		if(offtrgt.isOnline()) offtrgt.getPlayer().sendMessage(freceiver);
		return;
	}
	
}
