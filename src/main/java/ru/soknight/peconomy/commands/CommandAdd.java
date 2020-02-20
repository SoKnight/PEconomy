package ru.soknight.peconomy.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.files.Messages;
import ru.soknight.peconomy.utils.Utils;

public class CommandAdd extends AbstractSubCommand {

	private final CommandSender sender;
	private final String[] args;
	
	public CommandAdd(CommandSender sender, String[] args) {
		super(sender, args, "peco.add", 4);
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
		float current = dbm.addAmount(name, amount, wallet);
		float newbal = current + amount;
		
		String as = Utils.format(amount), cs = Utils.format(current), ns = Utils.format(newbal);
		String fsender, freceiver;
		if(wallet.equals("euro")) {
			fsender = Messages.getMessage("added-euro").replace("%player%", name).replace("%added%", as)
					.replace("%current%", cs).replace("%new%", ns);
			freceiver = Messages.getMessage("added-euro-myself").replace("%added%", as).replace("%current%", cs)
					.replace("%new%", ns);
		} else {
			fsender = Messages.getMessage("added-dollars").replace("%player%", name).replace("%added%", as)
					.replace("%current%", cs).replace("%new%", ns);
			freceiver = Messages.getMessage("added-dollars-myself").replace("%added%", as).replace("%current%", cs)
					.replace("%new%", ns);
		}
		
		sender.sendMessage(fsender);
		OfflinePlayer offtrgt = Bukkit.getOfflinePlayer(name);
		if(offtrgt.isOnline()) offtrgt.getPlayer().sendMessage(freceiver);
		return;
	}
	
}
