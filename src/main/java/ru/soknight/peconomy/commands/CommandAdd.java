package ru.soknight.peconomy.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.files.Messages;
import ru.soknight.peconomy.utils.Requirements;
import ru.soknight.peconomy.utils.Utils;

public class CommandAdd {

	public static void execute(CommandSender sender, String[] args) {
		if(Requirements.isInvalidUsage(sender, args, 4)) return;
		
		String name = args[1], amstr = args[2], wallet = args[3];
		if(Requirements.isInvalidWallet(sender, wallet)) return;
		if(!Requirements.argIsFloat(sender, amstr)) return;
		if(!Requirements.playerExist(sender, name)) return;
		
		float amount = Float.parseFloat(amstr);
		float current = DatabaseManager.addAmount(name, amount, wallet);
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
