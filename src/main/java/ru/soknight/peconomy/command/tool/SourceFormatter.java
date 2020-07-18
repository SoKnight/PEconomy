package ru.soknight.peconomy.command.tool;

import org.bukkit.command.CommandSender;

import ru.soknight.lib.configuration.Configuration;

public class SourceFormatter {

	public static String format(Configuration config, String source, CommandSender viewing) {
		if(!config.getBoolean("transaction-source-hiding.enabled"))
			return source;
		
		else if(viewing.hasPermission("peco.transaction.sourcespy"))
			return source;
		
		else if(!config.getList("transaction-source-hiding.staffs").contains(source))
			return source;
		
		else return config.getColoredString("transaction-source-hiding.value");
	}
	
}
