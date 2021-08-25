package ru.soknight.peconomy.command.peconomy;

import org.bukkit.command.CommandSender;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.subcommand.ArgumentableSubcommand;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.format.Formatter;

public class CommandInfo extends ArgumentableSubcommand {

    private final PEconomy plugin;
    private final Messages messages;
    
    private final DatabaseManager databaseManager;
    private final CurrenciesManager currenciesManager;
    
    public CommandInfo(
            PEconomy plugin,
            Messages messages,
            DatabaseManager databaseManager,
            CurrenciesManager currenciesManager
    ) {
        super(null, "peco.command.info", 1, messages);

        this.plugin = plugin;
        this.messages = messages;
        
        this.databaseManager = databaseManager;
        this.currenciesManager = currenciesManager;
    }

    @Override
    public void executeCommand(CommandSender sender, CommandArguments args) {
        int id = args.getAsInteger(0);
        if(id <= 0) {
            messages.sendFormatted(sender, "error.arg-is-not-int", "%arg%", args.get(0));
            return;
        }

        Formatter formatter = plugin.getFormatter();
        databaseManager.getTransactionByID(id).thenAccept(transaction -> {
            if(transaction == null) {
                messages.sendFormatted(sender, "info.failed.unknown-id", "%id%", id);
                return;
            }
            
            String walletHolder = transaction.getWalletHolder();
            if(!walletHolder.equals(sender.getName()) && !sender.hasPermission("peco.command.info.other")) {
                messages.getAndSend(sender, "error.no-permissions");
                return;
            }
            
            float pre = transaction.getBalanceBefore();
            float post = transaction.getBalanceAfter();
            
            String type = transaction.getCause();
            String typePath = type.toLowerCase().replace("_", ".");
            
            String source = formatter.formatOperator(transaction.getOperator(), sender);
            String action = messages.getFormatted("action." + typePath, "%source%", source);
            
            CurrencyInstance currency = currenciesManager.getCurrency(transaction.getCurrency());
            
            messages.sendFormatted(sender, "info.success",
                    "%id%", transaction.getId(),
                    "%owner%", transaction.getWalletHolder(),
                    "%action%", action,
                    "%pre%", formatter.formatAmount(pre),
                    "%post%", formatter.formatAmount(post),
                    "%currency%", currency == null ? "???" : currency.getName(),
                    "%symbol%", currency == null ? "?" : currency.getSymbol(),
                    "%date%", formatter.formatDateTime(transaction.getPassedAt())
            );
        });
    }
    
}
