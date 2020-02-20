package ru.soknight.peconomy.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.files.Messages;
import ru.soknight.peconomy.utils.Requirements;
import ru.soknight.peconomy.utils.Utils;

public class CommandPay implements CommandExecutor, TabCompleter {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(Requirements.isInvalidUsage(sender, args, 3)) return true;
		
		String target = args[0], amstr = args[1], wallet = args[2], name = sender.getName();
		if(Requirements.isInvalidWallet(sender, wallet)) return true;
		if(!Requirements.argIsFloat(sender, amstr)) return true;
		if(!Requirements.playerExist(sender, target)) return true;
		if(!Requirements.isPlayer(sender)) return true;
		
		if(target.equals(name)) {
			sender.sendMessage("pay-failed-to-myself");
			return true;
		}
		
		DatabaseManager dbm = PEconomy.getInstance().getDBManager();
		
		float amount = Float.parseFloat(amstr);
		if(!dbm.hasAmount(name, amount, wallet)) {
			sender.sendMessage("error-not-enough-money");
			return true;
		}
		
		float scurrent = dbm.takeAmount(name, amount, wallet);
		float rcurrent = dbm.addAmount(name, amount, wallet);
		float snew = scurrent - amount, rnew = rcurrent + amount;
		
		String pay = Utils.format(amount), tosender, toreceiver;
		String sc = Utils.format(scurrent), rc = Utils.format(rcurrent);
		String sn = Utils.format(snew), rn = Utils.format(rnew);
		
		if(wallet.equals("euro")) {
			tosender = Messages.formatMessage("pay-euro-sender", "%pay%", pay);
			toreceiver = Messages.formatMessage("pay-euro-receiver", "%pay%", pay);
		} else {
			tosender = Messages.formatMessage("pay-dollars-sender", "%pay%", pay);
			toreceiver = Messages.formatMessage("pay-dollars-receiver", "%pay%", pay);
		}
		
		tosender = tosender.replace("%receiver%", target).replace("%current%", sc).replace("%new%", sn);
		toreceiver = toreceiver.replace("%sender%", name).replace("%current%", rc).replace("%new%", rn);
		
		sender.sendMessage(tosender);
		OfflinePlayer offtrgt = Bukkit.getOfflinePlayer(name);
		if(offtrgt.isOnline()) offtrgt.getPlayer().sendMessage(toreceiver);
		return true;
	}

	private static List<String> wallets = Arrays.asList("dollars", "euro");
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("peco.pay")) return null;
		List<String> output = new ArrayList<>();
		if(args.length == 1) {
			for(Player p : Bukkit.getServer().getOnlinePlayers())
				if(p.getName().toLowerCase().startsWith(args[0].toLowerCase())) output.add(p.getName());
			for(OfflinePlayer op : Bukkit.getServer().getOfflinePlayers())
				if(op.getName().toLowerCase().startsWith(args[0].toLowerCase()) && !output.contains(op.getName()))
					output.add(op.getName());
		}
		if(args.length == 3)
			for(String wallet : wallets)
				if(wallet.startsWith(args[2])) output.add(wallet);
		return output;
	}

	
	
}
