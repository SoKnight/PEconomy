package ru.soknight.peconomy.command.peconomy;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.subcommand.ArgumentableSubcommand;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.util.AmountFormatter;

public class CommandSet extends ArgumentableSubcommand {
    
    private final Messages messages;
    private final DatabaseManager databaseManager;
    private final CurrenciesManager currenciesManager;
    
    public CommandSet(Messages messages, DatabaseManager databaseManager, CurrenciesManager currenciesManager) {
        super(null, "peco.command.set", 3, messages);
        
        this.messages = messages;
        this.databaseManager = databaseManager;
        this.currenciesManager = currenciesManager;
    }

    @Override
    public void executeCommand(CommandSender sender, CommandArguments args) {
        String walletHolder = args.get(0);
        float amount = args.getAsFloat(1);
        String currencyId = args.get(2);
        
        if(amount <= 0F) {
            messages.sendFormatted(sender, "error.arg-is-not-float", "%arg%", args.get(1));
            return;
        }
        
        if(!currenciesManager.isCurrency(currencyId)) {
            messages.sendFormatted(sender, "error.unknown-currency", "%currency%", currencyId);
            return;
        }
        
        databaseManager.getWallet(walletHolder).thenAcceptAsync(wallet -> {
            if(wallet == null) {
                messages.sendFormatted(sender, "error.unknown-wallet", "%player%", walletHolder);
                return;
            }
            
            float pre = wallet.getAmount(currencyId);
            
            CurrencyInstance currency = currenciesManager.getCurrency(currencyId);
            String symbol = currency.getSymbol();
            
            // checking for balance limit reached
            float limit = currency.getLimit();
            if(limit > 0F && amount > limit) {
                messages.sendFormatted(sender, "set.failed.limit-reached",
                        "%limit%", AmountFormatter.format(limit),
                        "%currency%", symbol
                );
                return;
            }
            
            if(pre == amount) {
                messages.sendFormatted(sender, "set.failed.already-equals",
                        "%player%", walletHolder,
                        "%amount%", AmountFormatter.format(amount),
                        "%currency%", symbol
                );
                return;
            }
            
            wallet.setAmount(currencyId, amount);
            databaseManager.saveWallet(wallet).join();
            
            String operator = isPlayer(sender) ? sender.getName() : messages.get("console-operator");
            String amountstr = AmountFormatter.format(amount);
            String prestr = AmountFormatter.format(pre);
            String poststr = AmountFormatter.format(amount);
            String operation = messages.get("operation." + (pre < amount ? "increase" : "decrease"));
            
            // saving transaction
            TransactionModel transaction = new TransactionModel(
                    walletHolder, operator, currencyId, "set", pre, amount
            );
            databaseManager.saveTransaction(transaction).join();
            
            // sending messages to sender and wallet owner if he is online
            messages.sendFormatted(sender, "set.success.operator",
                    "%currency%", symbol,
                    "%player%", walletHolder,
                    "%from%", prestr,
                    "%operation%", operation,
                    "%to%", poststr,
                    "%id%", transaction.getId()
            );
            
            Player player = Bukkit.getPlayer(walletHolder);
            if(player != null)
                messages.sendFormatted(player, "set.success.holder",
                        "%currency%", symbol,
                        "%from%", prestr,
                        "%operation%", operation,
                        "%to%", poststr,
                        "%id%", transaction.getId()
                );
        });
    }
    
    @Override
    public List<String> executeTabCompletion(CommandSender sender, CommandArguments args) {
        String arg = getLastArgument(args, true);
        
        // wallet holders suggestions
        if(args.size() == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(arg))
                    .collect(Collectors.toList());
            
        // currencies suggestions
        } else if(args.size() == 3) {
            return currenciesManager.getCurrenciesIDs().stream()
                    .filter(c -> c.toLowerCase().startsWith(arg))
                    .collect(Collectors.toList());
        }
        
        return null;
    }
    
}
