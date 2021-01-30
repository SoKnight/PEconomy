package ru.soknight.peconomy.command.peconomy;

import java.text.DateFormat;

import org.bukkit.command.CommandSender;

import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.subcommand.ArgumentableSubcommand;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.util.AmountFormatter;
import ru.soknight.peconomy.util.OperatorFormatter;

public class CommandInfo extends ArgumentableSubcommand {

    private final Configuration config;
    private final Messages messages;
    
    private final DatabaseManager databaseManager;
    private final CurrenciesManager currenciesManager;
    
    private final DateFormat dateFormatter;
    
    public CommandInfo(
            Configuration config, Messages messages, DatabaseManager databaseManager,
            CurrenciesManager currenciesManager, DateFormat dateFormatter
    ) {
        super(null, "peco.command.info", 1, messages);
        
        this.config = config;
        this.messages = messages;
        
        this.databaseManager = databaseManager;
        this.currenciesManager = currenciesManager;
        
        this.dateFormatter = dateFormatter;
    }

    @Override
    public void executeCommand(CommandSender sender, CommandArguments args) {
        int id = args.getAsInteger(0);
        if(id <= 0) {
            messages.sendFormatted(sender, "error.arg-is-not-int", "%arg%", args.get(0));
            return;
        }
        
        databaseManager.getTransactionByID(id).thenAcceptAsync(transaction -> {
            if(transaction == null) {
                messages.sendFormatted(sender, "info.failed.unknown-id", "%id%", id);
                return;
            }
            
            String walletHolder = transaction.getWalletHolder();
            if(!walletHolder.equals(sender.getName()) && !sender.hasPermission("peco.command.info.other")) {
                messages.getAndSend(sender, "error.no-permissions");
                return;
            }
            
            float pre = transaction.getPreBalance();
            float post = transaction.getPostBalance();
            
            String type = transaction.getType();
            String typePath = type.toLowerCase().replace("_", ".");
            
            String source = OperatorFormatter.format(config, transaction.getOperator(), sender);
            String action = messages.getFormatted("action." + typePath, "%source%", source);
            
            CurrencyInstance currency = currenciesManager.getCurrency(transaction.getCurrency());
            
            messages.sendFormatted(sender, "info.success",
                    "%id%", transaction.getId(),
                    "%owner%", transaction.getWalletHolder(),
                    "%action%", action,
                    "%pre%", AmountFormatter.format(pre),
                    "%post%", AmountFormatter.format(post),
                    "%currency%", currency == null ? "???" : currency.getID(),
                    "%symbol%", currency == null ? "?" : currency.getSymbol(),
                    "%date%", dateFormatter.format(transaction.getTimestamp())
            );
        });
    }
    
}
