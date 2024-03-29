package ru.soknight.peconomy.command.peconomy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.subcommand.PermissibleSubcommand;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.tool.CollectionsTool;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.format.Formatter;

import java.util.List;
import java.util.stream.Collectors;

public class CommandHistory extends PermissibleSubcommand {

    private final PEconomy plugin;
    private final Messages messages;
    
    private final DatabaseManager databaseManager;
    private final CurrenciesManager currenciesManager;
    
    public CommandHistory(
            PEconomy plugin,
            Messages messages,
            DatabaseManager databaseManager,
            CurrenciesManager currenciesManager
    ) {
        super("peco.command.history", messages);

        this.plugin = plugin;
        this.messages = messages;
        
        this.databaseManager = databaseManager;
        this.currenciesManager = currenciesManager;
    }

    @Override
    public void executeCommand(CommandSender sender, CommandArguments args) {
        String target = sender.getName();
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

        boolean other = !isPlayer(sender) || !target.equals(sender.getName());
        if(other && !sender.hasPermission("peco.command.history.other")) {
            if(!isPlayer(sender)) {
                messages.getAndSend(sender, "error.wrong-syntax");
                return;
            }

            target = sender.getName();
            other = false;
        }
        
        if(page < 1)
            page = 1;
        
        int finalPage = page;
        boolean finalOther = other;
        String finalTarget = target;
        
        databaseManager.getTransactionHistory(target).thenAccept(transactions -> {
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
            String header = messages.getFormatted(path, "%player%", finalTarget, "%page%", finalPage, "%total%", total);
            String footer = messages.getFormatted("history.footer", "%player%", finalTarget, "%page%", finalPage, "%total%", total);
            
            String body = onpage.stream()
                    .map(t -> formatTransaction(sender, t))
                    .collect(Collectors.joining("\n"));
            
            String message = header + "\n" + body + "\n" + footer;
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
        float pre = transaction.getBalanceBefore();
        float post = transaction.getBalanceAfter();
        
        String type = transaction.getCause();
        String typePath = type.toLowerCase().replace("_", ".");

        Formatter formatter = plugin.getFormatter();
        String source = formatter.formatOperator(transaction.getOperator(), sender);
        String action = messages.getFormatted("action." + typePath, "%source%", source);
        
        CurrencyInstance currency = currenciesManager.getCurrency(transaction.getCurrency());
        
        return messages.getFormatted("history.body",
                "%id%", transaction.getId(),
                "%date%", formatter.formatDateTime(transaction.getPassedAt()),
                "%from%", formatter.formatAmount(pre),
                "%to%", formatter.formatAmount(post),
                "%currency%", currency == null ? "?" : currency.getSymbol(),
                "%operation%", messages.get("operation." + (pre < post ? "increase" : "decrease")),
                "%action%", action
        );
    }
    
}
