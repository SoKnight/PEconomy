package ru.soknight.peconomy.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.standalone.OmnipotentCommand;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.PEconomyPlugin;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.event.payment.PaymentFinishEvent;
import ru.soknight.peconomy.event.payment.PaymentPrepareEvent;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.format.Formatter;
import ru.soknight.peconomy.transaction.TransactionCause;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CommandPay extends OmnipotentCommand {

    private final Messages messages;
    
    private final DatabaseManager databaseManager;
    private final CurrenciesManager currenciesManager;
    
    public CommandPay(
            PEconomyPlugin plugin,
            Messages messages,
            DatabaseManager databaseManager,
            CurrenciesManager currenciesManager
    ) {
        super("pay", null, "peco.command.pay", 3, messages);

        this.messages = messages;
        this.databaseManager = databaseManager;
        this.currenciesManager = currenciesManager;

        register(plugin, true);
    }
    
    @Override
    @SuppressWarnings("deprecation")
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

        CurrencyInstance currency = currenciesManager.getCurrency(currencyId);
        if(currency == null) {
            messages.sendFormatted(sender, "error.unknown-currency", "%currency%", currencyId);
            return;
        }

        if(!currency.isTransferable()) {
            messages.getAndSend(sender, "pay.failed.untransferable");
            return;
        }
        
        if(walletHolder.equals(receiver)) {
            messages.getAndSend(sender, "pay.failed.to-self");
            return;
        }
        
        CompletableFuture<WalletModel> receiverWalletFuture = databaseManager.getWallet(receiver);

        Formatter formatter = PEconomyPlugin.getApiInstance().getFormatter();
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

            String amountstr = formatter.formatAmount(amount);
            String symbol = currency.getSymbol();
            
            float preSender = senderWallet.getAmount(currencyId);
            float postSender = preSender - amount;
            
            String preSenderStr = formatter.formatAmount(preSender);
            String postSenderStr = formatter.formatAmount(postSender);
            
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
            
            String preReceiverStr = formatter.formatAmount(preReceiver);
            String postReceiverStr = formatter.formatAmount(postReceiver);
            
            // checking for balance limit reached
            float limit = currency.getLimit();
            if(limit > 0F && postReceiver > limit) {
                messages.sendFormatted(sender, "pay.failed.limit-reached",
                        "%limit%", formatter.formatAmount(limit),
                        "%currency%", symbol
                );
                return;
            }

            // creating transactions
            TransactionModel senderTransaction = new TransactionModel(
                    walletHolder, currencyId, preSender, postSender, receiver, TransactionCause.PAYMENT_OUTCOMING
            );
            TransactionModel receiverTransaction = new TransactionModel(
                    receiver, currencyId, preReceiver, postReceiver, walletHolder, TransactionCause.PAYMENT_INCOMING
            );

            // processing prepare event
            OfflinePlayer receiverPlayer = Bukkit.getOfflinePlayer(receiver);
            PaymentPrepareEvent event = new PaymentPrepareEvent(player, receiverPlayer, senderWallet, receiverWallet, senderTransaction, receiverTransaction, amount);
            event.fireAsync().join();

            if(event.isCancelled())
                return;

            senderWallet.takeAmount(currencyId, amount, receiver);
            receiverWallet.addAmount(currencyId, amount, walletHolder);

            // updating database
            databaseManager.saveWallet(senderWallet).join();
            databaseManager.saveWallet(receiverWallet).join();

            databaseManager.saveTransaction(senderTransaction).join();
            databaseManager.saveTransaction(receiverTransaction).join();

            new PaymentFinishEvent(player, receiverPlayer, senderWallet, receiverWallet, senderTransaction, receiverTransaction, amount).fireAsync();

            if(event.isQuiet())
                return;
            
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

            if(receiverPlayer.isOnline())
                messages.sendFormatted(receiverPlayer.getPlayer(), "pay.success.receiver",
                        "%amount%", amountstr,
                        "%currency%", symbol,
                        "%sender%", formatter.formatOperator(walletHolder, receiverPlayer.getPlayer()),
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
