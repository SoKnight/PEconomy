package ru.soknight.peconomy.format;

import org.bukkit.command.CommandSender;

import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;

public class OperatorFormatter {

    public static String format(Configuration config, Messages messages, String operator, CommandSender sender) {
        if(operator == null || operator.isEmpty())
            return messages.get("console-operator");

        if(!config.getBoolean("transaction-source-hiding.enabled"))
            return operator;
        
        if(sender.hasPermission("peco.transaction.sourcespy"))
            return operator;
        
        if(!config.getList("transaction-source-hiding.staffs").contains(operator))
            return operator;
        
        return config.getColoredString("transaction-source-hiding.value");
    }
    
}
