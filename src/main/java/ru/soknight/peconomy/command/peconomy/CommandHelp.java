package ru.soknight.peconomy.command.peconomy;

import ru.soknight.lib.command.enhanced.help.command.EnhancedHelpExecutor;
import ru.soknight.lib.command.response.CommandResponseType;
import ru.soknight.lib.configuration.Messages;

public class CommandHelp extends EnhancedHelpExecutor {
    
    public CommandHelp(Messages messages) {
        super(messages);
        
        super.setHeaderFrom("help.header");
        super.setFooterFrom("help.footer");
        
        super.factory()
                .helpLineFormatFrom("help.body")
                .permissionFormat("peco.command.%s")
        
                // /peco help
                .newLine()
                        .command("peco help")
                        .descriptionFrom("help")
                        .permission("help")
                        .add()
                // /balance [player]
                .newLine()
                        .command("balance", true)
                        .argumentFrom("player-opt")
                        .add()
                // /pay <player> <amount> <currency>
                .newLine()
                        .command("pay", true)
                        .argumentsFrom("player-req", "amount", "currency")
                        .add()
                // /peco add <player> <amount> <currency>
                .newLine()
                        .command("peco add")
                        .argumentsFrom("player-req", "amount", "currency")
                        .descriptionFrom("add")
                        .permission("add")
                        .add()
                // /peco set <player> <amount> <currency>
                .newLine()
                        .command("peco set")
                        .argumentsFrom("player-req", "amount", "currency")
                        .descriptionFrom("set")
                        .permission("set")
                        .add()
                // /peco reset <player> <currency>
                .newLine()
                        .command("peco reset")
                        .argumentsFrom("player-req", "currency")
                        .descriptionFrom("reset")
                        .permission("reset")
                        .add()
                // /peco take <player> <amount> <currency>
                .newLine()
                        .command("peco take")
                        .argumentsFrom("player-req", "amount", "currency")
                        .descriptionFrom("take")
                        .permission("take")
                        .add()
                // /peco history [player] [page]
                .newLine()
                        .command("peco history")
                        .argumentsFrom("player-opt", "page")
                        .descriptionFrom("history")
                        .permission("history")
                        .add()
                // /peco info <id>
                .newLine()
                        .command("peco info")
                        .argumentFrom("id")
                        .descriptionFrom("info")
                        .permission("info")
                        .add()
                // /peco reload
                .newLine()
                        .command("peco reload")
                        .descriptionFrom("reload")
                        .permission("reload")
                        .add();
                
        super.completeMessage();
        
        super.setPermission("peco.command.help");
        super.setResponseMessageByKey(CommandResponseType.NO_PERMISSIONS, "error.no-permissions");
    }
    
}
