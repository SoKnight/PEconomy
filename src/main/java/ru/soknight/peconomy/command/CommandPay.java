package ru.soknight.peconomy.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.standalone.OmnipotentCommand;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.format.AmountFormatter;
import ru.soknight.peconomy.format.OperatorFormatter;
import ru.soknight.peconomy.transaction.TransactionCause;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CommandPay extends OmnipotentCommand {
    
    private final Configuration config;
    private final Messages messages;
    
    private final DatabaseManager databaseManager;
    private final CurrenciesManager currenciesManager;
    
    public CommandPay(
            Configuration config,
            Messages messages,
            DatabaseManager databaseManager,
            CurrenciesManager currenciesManager
    ) {
        super("pay", null, "peco.command.pay", 3, messages);
        
        this.config = config;
        this.messages = messages;
        
        this.databaseManager = databaseManager;
        this.currenciesManager = currenciesManager;
    }
    
    @Override
    public void executeCommand(CommandSender sender, CommandArguments args) {
        Player player = (Player) sender;
        String walletHolder = player.getName();
        
        String receiver = args.get(0);
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
        
        if(walletHolder.equals(receiver)) {
            messages.getAndSend(sender, "pay.failed.to-self");
            return;
        }
        
        CompletableFuture<WalletModel> receiverWalletFuture = databaseManager.getWallet(receiver);
        
        databaseManager.getWallet(walletHolder).thenAccept(senderWallet -> {
            if(senderWallet == null) {
                messages.sendFormatted(sender, "error.unknown-wallet", "%player%", walletHolder);
                return;
            }
            
            WalletModel receiverWallet = receiverWalletFuture.join();
            if(receiverWallet == null) {
                messages.sendFormatted(sender, "error.unknown-wallet", "%player%", receiver);
                return;
            }
            
            CurrencyInstance currency = currenciesManager.getCurrency(currencyId);
            String amountstr = AmountFormatter.format(amount);
            String symbol = currency.getSymbol();
            
            float preSender = senderWallet.getAmount(currencyId);
            float postSender = preSender - amount;
            
            String preSenderStr = AmountFormatter.format(preSender);
            String postSenderStr = AmountFormatter.format(postSender);
            
            // checking for 0 reached (for payment sender)
            if(postSender < 0F) {
                messages.sendFormatted(sender, "pay.failed.not-enough",
                        "%amount%", preSenderStr,
                        "%currency%", symbol,
                        "%requested%", amountstr
                );
                return;
            }
            
            float preReceiver = receiverWallet.getAmount(currencyId);
            float postReceiver = preReceiver + amount;
            
            String preReceiverStr = AmountFormatter.format(preReceiver);
            String postReceiverStr = AmountFormatter.format(postReceiver);
            
            // checking for balance limit reached
            float limit = currency.getLimit();
            if(limit > 0F && postReceiver > limit) {
                messages.sendFormatted(sender, "pay.failed.limit-reached",
                        "%limit%", AmountFormatter.format(limit),
                        "%currency%", symbol
                );
                return;
            }
            
            // updating database
            senderWallet.takeAmount(currencyId, amount);
            receiverWallet.addAmount(currencyId, amount);

            databaseManager.saveWallet(senderWallet).join();
            databaseManager.saveWallet(receiverWallet).join();
            
            // saving transactions
            TransactionModel senderTransaction = new TransactionModel(
                    walletHolder, currencyId, preSender, postSender, receiver, TransactionCause.PAYMENT_OUTCOMING
            );
            TransactionModel receiverTransaction = new TransactionModel(
                    receiver, currencyId, preReceiver, postReceiver, walletHolder, TransactionCause.PAYMENT_INCOMING
            );

            databaseManager.saveTransaction(senderTransaction).join();
            databaseManager.saveTransaction(receiverTransaction).join();
            
            // sending messages to sender and wallet owner if he is online
            messages.sendFormatted(sender, "pay.success.sender",
                    "%amount%", amountstr,
                    "%currency%", symbol,
                    "%receiver%", receiver,
                    "%from%", preSenderStr,
                    "%operation%", messages.get("operation.decrease"),
                    "%to%", postSenderStr,
                    "%id%", senderTransaction.getId()
            );
            
            Player receiverPlayer = Bukkit.getPlayer(receiver);
            if(receiverPlayer != null)
                messages.sendFormatted(receiverPlayer, "pay.success.receiver",
                        "%amount%", amountstr,
                        "%currency%", symbol,
                        "%sender%", OperatorFormatter.format(config, messages, walletHolder, receiverPlayer),
                        "%from%", preReceiverStr,
                        "%operation%", messages.get("operation.increase"),
                        "%to%", postReceiverStr,
                        "%id%", receiverTransaction.getId()
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
