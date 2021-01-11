package ru.soknight.peconomy.util;

import org.bukkit.command.CommandSender;

import ru.soknight.lib.configuration.Configuration;

public class OperatorFormatter {

    public static String format(Configuration config, String operator, CommandSender sender) {
        if(!config.getBoolean("transaction-source-hiding.enabled"))
            return operator;
        
        if(sender.hasPermission("peco.transaction.sourcespy"))
            return operator;
        
        if(!config.getList("transaction-source-hiding.staffs").contains(operator))
            return operator;
        
        return config.getColoredString("transaction-source-hiding.value");
    }
    
}
