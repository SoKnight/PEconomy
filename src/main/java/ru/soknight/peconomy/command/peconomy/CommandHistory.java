package ru.soknight.peconomy.command.peconomy;

import java.text.DateFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.subcommand.PermissibleSubcommand;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.tool.CollectionsTool;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.TransactionModel.TransactionType;
import ru.soknight.peconomy.util.AmountFormatter;
import ru.soknight.peconomy.util.OperatorFormatter;

public class CommandHistory extends PermissibleSubcommand {

    private final Configuration config;
    private final Messages messages;
    
    private final DatabaseManager databaseManager;
    private final CurrenciesManager currenciesManager;
    
    private final DateFormat dateFormatter;
    
    public CommandHistory(
            Configuration config, Messages messages, DatabaseManager databaseManager,
            CurrenciesManager currenciesManager, DateFormat dateFormatter
    ) {
        super("peco.command.history", messages);
        
        this.config = config;
        this.messages = messages;
        
        this.databaseManager = databaseManager;
        this.currenciesManager = currenciesManager;
        
        this.dateFormatter = dateFormatter;
    }

    @Override
    public void executeCommand(CommandSender sender, CommandArguments args) {
        String target = sender.getName();
        boolean other = false;
        int page = 1;
        
        if(args.isEmpty()) {
            if(!isPlayer(sender)) {
                messages.getAndSend(sender, "error.wrong-syntax");
                return;
            }
        } else if(args.size() == 1) {
            String arg = args.get(0);
            if(isInteger(arg)) {
                page = Integer.parseInt(arg);
            } else {
                target = arg;
            }
        } else {
            target = args.get(0);
            page = args.getAsInteger(1);
        }
        
        if(isPlayer(sender) && !target.equals(sender.getName())) {
            if(!sender.hasPermission("peco.command.history.other"))
                target = sender.getName();
            else
                other = true;
        }
        
        if(page < 1)
            page = 1;
        
        int finalPage = page;
        boolean finalOther = other;
        String finalTarget = target;
        
        databaseManager.getTransactionHistory(target).thenAcceptAsync(transactions -> {
            if(transactions == null || transactions.isEmpty()) {
                messages.getAndSend(sender, "history.failed.no-transactions");
                return;
            }
            
            int size = messages.getInt("history.page-size");
            List<TransactionModel> onpage = CollectionsTool.getSubList(transactions, size, finalPage);
            
            if(onpage.isEmpty()) {
                messages.sendFormatted(sender, "history.failed.page-is-empty", "%page%", finalPage);
                return;
            }
            
            int total = transactions.size() / size;
            if(transactions.size() % size != 0) total++;
            
            String path = "history.header." + (finalOther ? "other" : "self");
            String header = messages.getFormatted(path,
                    "%player%", finalTarget,
                    "%page%", finalPage,
                    "%total%", total
            );
            
            String body = onpage.stream()
                    .map(t -> formatTransaction(sender, t))
                    .collect(Collectors.joining("\n"));
            
            String message = header + "\n" + body + "\n" + messages.get("history.footer");
            sender.sendMessage(message);
        });
    }
    
    @Override
    public List<String> executeTabCompletion(CommandSender sender, CommandArguments args) {
        if(args.size() != 1 || !sender.hasPermission("peco.command.history.other")) return null;
        
        String arg = getLastArgument(args, true);
        return Bukkit.getOnlinePlayers().stream()
                .map(OfflinePlayer::getName)
                .filter(n -> n.toLowerCase().startsWith(arg))
                .collect(Collectors.toList());
    }
    
    private String formatTransaction(CommandSender sender, TransactionModel transaction) {
        float pre = transaction.getPreBalance();
        float post = transaction.getPostBalance();
        
        TransactionType type = transaction.getType();
        String typePath = type.name().toLowerCase().replace("_", ".");
        
        String source = OperatorFormatter.format(config, transaction.getOperator(), sender);
        String action = messages.getFormatted("action." + typePath, "%source%", source);
        
        CurrencyInstance currency = currenciesManager.getCurrency(transaction.getCurrency());
        
        return messages.getFormatted("history.body",
                "%id%", transaction.getId(),
                "%date%", dateFormatter.format(transaction.getTimestamp()),
                "%from%", AmountFormatter.format(pre),
                "%to%", AmountFormatter.format(post),
                "%currency%", currency == null ? "?" : currency.getSymbol(),
                "%operation%", messages.get("operation." + (pre < post ? "increase" : "decrease")),
                "%action%", action
        );
    }
    
}
