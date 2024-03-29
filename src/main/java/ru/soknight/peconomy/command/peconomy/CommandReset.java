package ru.soknight.peconomy.command.peconomy;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.subcommand.ArgumentableSubcommand;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.event.initiator.Initiator;
import ru.soknight.peconomy.event.wallet.TransactionFinishEvent;
import ru.soknight.peconomy.event.wallet.TransactionPrepareEvent;
import ru.soknight.peconomy.format.Formatter;

import java.util.List;
import java.util.stream.Collectors;

public class CommandReset extends ArgumentableSubcommand {

    private final Configuration config;
    private final Messages messages;

    private final DatabaseManager databaseManager;
    private final CurrenciesManager currenciesManager;
    
    public CommandReset(
            Configuration config,
            Messages messages,
            DatabaseManager databaseManager,
            CurrenciesManager currenciesManager
    ) {
        super(null, "peco.command.reset", 2, messages);

        this.config = config;
        this.messages = messages;

        this.databaseManager = databaseManager;
        this.currenciesManager = currenciesManager;
    }

    @Override
    public void executeCommand(CommandSender sender, CommandArguments args) {
        String walletHolder = args.get(0);
        String currencyId = args.get(1);
        
        if(!currenciesManager.isCurrency(currencyId)) {
            messages.sendFormatted(sender, "error.unknown-currency", "%currency%", currencyId);
            return;
        }

        Formatter formatter = PEconomy.getAPI().getFormatter();
        databaseManager.getWallet(walletHolder).thenAccept(wallet -> {
            if(wallet == null) {
                messages.sendFormatted(sender, "error.unknown-wallet", "%player%", walletHolder);
                return;
            }
            
            float pre = wallet.getAmount(currencyId);
            if(pre == 0F) {
                messages.sendFormatted(sender, "reset.failed.already",
                        "%player%", walletHolder
                );
                return;
            }
            
            CurrencyInstance currency = currenciesManager.getCurrency(currencyId);
            String symbol = currency.getSymbol();

            TransactionModel transaction = wallet.resetWallet(currencyId, isPlayer(sender) ? sender.getName() : null);

            Initiator initiator = Initiator.createAsCommandSender(sender);
            TransactionPrepareEvent event = new TransactionPrepareEvent(wallet, initiator, transaction);
            event.fireAsync().join();

            if(event.isCancelled())
                return;

            databaseManager.saveWallet(wallet).join();

            if (config.getBoolean("transactions-history.enabled", true))
                databaseManager.saveTransaction(transaction).join();

            new TransactionFinishEvent(wallet, initiator, transaction).fireAsync();

            if(event.isQuiet())
                return;

            String prestr = formatter.formatAmount(pre);
            String operation = messages.get("operation.decrease");
            
            // sending messages to sender and wallet owner if he is online
            messages.sendFormatted(sender, "reset.success.operator",
                    "%currency%", symbol,
                    "%player%", walletHolder,
                    "%from%", prestr,
                    "%operation%", operation,
                    "%id%", transaction.getId()
            );
            
            Player player = Bukkit.getPlayer(walletHolder);
            if(player != null)
                messages.sendFormatted(player, "reset.success.holder",
                        "%currency%", symbol,
                        "%from%", prestr,
                        "%operation%", operation,
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
        } else if(args.size() == 2) {
            return currenciesManager.getCurrenciesIDs().stream()
                    .filter(c -> c.toLowerCase().startsWith(arg))
                    .collect(Collectors.toList());
        }
        
        return null;
    }
    
}
