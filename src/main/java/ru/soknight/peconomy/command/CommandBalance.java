package ru.soknight.peconomy.command;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.standalone.PermissibleCommand;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.util.AmountFormatter;

public class CommandBalance extends PermissibleCommand {
    
    private final Configuration config;
    private final Messages messages;
    
    private final DatabaseManager databaseManager;
    private final CurrenciesManager currenciesManager;
    
    public CommandBalance(
            Configuration config, Messages messages,
            DatabaseManager databaseManager, CurrenciesManager currenciesManager
    ) {
        super("balance", "peco.command.balance", messages);
        
        this.config = config;
        this.messages = messages;
        
        this.databaseManager = databaseManager;
        this.currenciesManager = currenciesManager;
    }

    @Override
    public void executeCommand(CommandSender sender, CommandArguments args) {
        String target = null;
        boolean other = false;
        
        if(args.isEmpty()) {
            if(!isPlayer(sender)) {
                messages.getAndSend(sender, "error.wrong-syntax");
                return;
            }
            
            target = sender.getName();
        } else {
            target = args.get(0);
            
            if(isPlayer(sender) && !target.equals(sender.getName())) {
                if(!sender.hasPermission("peco.command.balance.other"))
                    target = sender.getName();
                else
                    other = true;
            }
        }
        
        String walletHolder = target;
        boolean forOtherPlayer = other;
        
        databaseManager.getWallet(walletHolder).thenAcceptAsync(wallet -> {
            if(wallet == null || wallet.getWallets().isEmpty()) {
                if(forOtherPlayer)
                    messages.sendFormatted(sender, "balance.failed.empty.other", "%player%", walletHolder);
                else
                    messages.getAndSend(sender, "balance.failed.empty.self");
                return;
            }
            
            // formatting balances string from wallets balances
            Map<String, Float> wallets = wallet.getWallets();
            String balances = wallets.entrySet()
                    .stream()
                    .filter(e -> shouldShowBalance(e.getKey()))
                    .sorted((e1, e2) -> e1.getKey().compareToIgnoreCase(e2.getKey()))
                    .map(e -> messages.getFormatted("balance.format",
                            "%amount%", AmountFormatter.format(e.getValue()),
                            "%currency%", getCurrencySymbol(e.getKey())))
                    .collect(Collectors.joining(messages.get("balance.separator")));
            
            // balances string may be empty
            if(balances.isEmpty()) {
                if(forOtherPlayer)
                    messages.sendFormatted(sender, "balance.failed.empty.other", "%player%", walletHolder);
                else
                    messages.getAndSend(sender, "balance.failed.empty.self");
                return;
            }
            
            // overwise we showing the wallet's balance
            if(forOtherPlayer)
                messages.sendFormatted(sender, "balance.success.other",
                        "%player%", walletHolder,
                        "%balance%", balances
                );
            else
                messages.sendFormatted(sender, "balance.success.self",
                        "%balance%", balances
                );
        });
    }
    
    @Override
    public List<String> executeTabCompletion(CommandSender sender, CommandArguments args) {
        if(args.size() != 1 || !sender.hasPermission("peco.command.balance.other")) return null;
        
        String arg = getLastArgument(args, true);
        return Bukkit.getOnlinePlayers().stream()
                .map(OfflinePlayer::getName)
                .filter(n -> n.toLowerCase().startsWith(arg))
                .collect(Collectors.toList());
    }
    
    private boolean shouldShowBalance(String currencyId) {
        if(currenciesManager.isCurrency(currencyId)) return true;
        
        return !config.getBoolean("hide-unknown-currencies");
    }
    
    private String getCurrencySymbol(String currencyId) {
        if(!currenciesManager.isCurrency(currencyId)) return "N/A";
        
        return currenciesManager.getCurrency(currencyId).getSymbol();
    }
    
}
