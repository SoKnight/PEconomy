package ru.soknight.peconomy.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.soknight.lib.command.ExtendedCommandExecutor;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.validation.CommandExecutionData;
import ru.soknight.lib.validation.validator.ArgsCountValidator;
import ru.soknight.lib.validation.validator.PermissionValidator;
import ru.soknight.lib.validation.validator.SenderIsPlayerValidator;
import ru.soknight.lib.validation.validator.Validator;
import ru.soknight.peconomy.command.tool.AmountFormatter;
import ru.soknight.peconomy.command.tool.SourceFormatter;
import ru.soknight.peconomy.command.validation.AmountValidator;
import ru.soknight.peconomy.command.validation.CurrencyValidator;
import ru.soknight.peconomy.command.validation.WalletExecutionData;
import ru.soknight.peconomy.command.validation.WalletValidator;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.Transaction;
import ru.soknight.peconomy.database.TransactionType;
import ru.soknight.peconomy.database.Wallet;

public class CommandPay extends ExtendedCommandExecutor {
	
	private final DatabaseManager databaseManager;
	private final CurrenciesManager currenciesManager;
	
	private final Configuration config;
	private final Messages messages;
	
	public CommandPay(DatabaseManager databaseManager, CurrenciesManager currenciesManager,
			Configuration config, Messages messages) {
		super(messages);
		
		this.databaseManager = databaseManager;
		this.currenciesManager = currenciesManager;
		
		this.config = config;
		this.messages = messages;
		
		String sendermsg = messages.get("error.only-for-players");
		String permmsg = messages.get("error.no-permissions");
		String argsmsg = messages.get("error.wrong-syntax");
		String walletmsg = messages.get("error.unknown-wallet");
		String amountmsg = messages.get("error.arg-is-not-float");
		String currencymsg = messages.get("error.unknown-currency");
		
		Validator senderval = new SenderIsPlayerValidator(sendermsg);
		Validator permval = new PermissionValidator("peco.command.add", permmsg);
		Validator argsval = new ArgsCountValidator(3, argsmsg);
		Validator walletval = new WalletValidator(databaseManager, walletmsg);
		Validator amountval = new AmountValidator(amountmsg);
		Validator currencyval = new CurrencyValidator(currenciesManager, currencymsg);
		
		super.addValidators(senderval, permval, argsval, walletval, amountval, currencyval);
	}
	
	@Override
	public void executeCommand(CommandSender sender, String[] args) {
		String owner = args[0], amstr = args[1], currencyid = args[2];
		
		CommandExecutionData data = new WalletExecutionData(sender, args, owner, currencyid, amstr);
		if(!validateExecution(data)) return;
		
		String source = sender.getName();
		
		if(source.equals(owner)) {
			messages.getAndSend(sender, "pay.to-self");
			return;
		}
		
		OfflinePlayer offline = Bukkit.getOfflinePlayer(owner);
		if(!offline.isOnline() && sender.hasPermission("peco.command.pay.offline")) {
			messages.getAndSend(sender, "pay.offline-pay");
			return;
		}
		
		// Checking receiver wallet exist
		Wallet other = databaseManager.getWallet(owner);
		if(other == null) {
			messages.sendFormatted(sender, "error.unknown-wallet", "%player%", owner);
			return;
		}
		
		// Getting some values
		float amount = Float.parseFloat(amstr);
		Wallet self = databaseManager.getWallet(source);
		
		float preself = self.getAmount(currencyid);
		float postself = preself - amount;
		
		CurrencyInstance currency = currenciesManager.getCurrency(currencyid);
		
		// Formatting values
		String symbol = currency.getSymbol();
		String preselfstr = AmountFormatter.format(preself);
		
		// Checking for invalid self balance
		String amountstr = AmountFormatter.format(amount);
		if(postself < 0) {
			messages.sendFormatted(sender, "pay.not-enough",
					"%amount%", preselfstr,
					"%currency%", symbol,
					"%player%", owner,
					"%requested%", amountstr);
			return;
		}
		
		// Check if limit reached on receiver balance
		float preother = other.getAmount(currencyid);
		float postother = preother + amount;
		float limit = currency.getLimit();
		
		if(limit != 0 && postother > limit) {
			String limitstr = AmountFormatter.format(limit);
			messages.sendFormatted(sender, "pay.limit",
					"%currency%", symbol,
					"%limit%", limitstr);
			return;
		}
		
		// Updating DB
		self.takeAmount(currencyid, amount);
		other.addAmount(currencyid, amount);
		
		databaseManager.updateWallet(self);
		databaseManager.updateWallet(other);
		
		// Formatting values
		String preotherstr = AmountFormatter.format(preother);
		String postotherstr = AmountFormatter.format(postother);
		String postselfstr = AmountFormatter.format(postself);
		String operationself = messages.get("operation.decrease");
		String operationother = messages.get("operation.increase");
		
		// Saving transaction
		Transaction transaction = new Transaction(owner, currencyid, TransactionType.PAYMENT, preother, postother, source);
		databaseManager.saveTransaction(transaction);
		
		int id = transaction.getId();
				
		// Sending messages to sender and wallet owner if he is online
		messages.sendFormatted(sender, "pay.other",
				"%amount%", amountstr,
				"%currency%", symbol,
				"%player%", owner,
				"%from%", preselfstr,
				"%operation%", operationself,
				"%to%", postselfstr,
				"%id%", id);
		
		OfflinePlayer offlinetarget = Bukkit.getOfflinePlayer(owner);
		if(offlinetarget.isOnline())
			messages.sendFormatted(offlinetarget.getPlayer(), "pay.self",
					"%amount%", amountstr,
					"%currency%", symbol,
					"%source%", SourceFormatter.format(config, source, offlinetarget.getPlayer()),
					"%from%", preotherstr,
					"%operation%", operationother,
					"%to%", postotherstr,
					"%source%", source,
					"%id%", id);
	}
	
	@Override
	public List<String> executeTabCompletion(CommandSender sender, String[] args) {
		if(!validateTabCompletion(sender, args)) return null;
		if(!(sender instanceof Player)) return null;
		
		List<String> output = new ArrayList<>();
		
		if(args.length == 1) {
			if(!sender.hasPermission("peco.command.pay.offline")) {
				Collection<? extends Player> players = Bukkit.getOnlinePlayers();
				String arg = args[0].toLowerCase();
				players.parallelStream()
						.filter(p -> p.getName().toLowerCase().startsWith(arg))
						.forEach(p -> output.add(p.getName()));
			} else {
				OfflinePlayer[] players = Bukkit.getOfflinePlayers();
				if(players.length != 0) {
					String arg = args[0].toLowerCase();
					Arrays.stream(players).parallel()
							.filter(p -> p.getName().toLowerCase().startsWith(arg))
							.forEach(p -> output.add(p.getName()));
				}
			}
		} else if(args.length == 3) {
			Set<String> currencies = this.currenciesManager.getCurrenciesIDs();
			if(!currencies.isEmpty()) {
				String arg = args[2].toLowerCase();
				currencies.parallelStream()
						.filter(c -> c.toLowerCase().startsWith(arg))
						.forEach(c -> output.add(c));
			}
		} else return null;
		
		return output;
	}

	
	
}
