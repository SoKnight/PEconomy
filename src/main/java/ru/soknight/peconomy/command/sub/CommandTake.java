package ru.soknight.peconomy.command.sub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.soknight.lib.command.ExtendedSubcommandExecutor;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.validation.CommandExecutionData;
import ru.soknight.lib.validation.validator.ArgsCountValidator;
import ru.soknight.lib.validation.validator.PermissionValidator;
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

public class CommandTake extends ExtendedSubcommandExecutor {

	private final DatabaseManager databaseManager;
	private final CurrenciesManager currenciesManager;
	
	private final Configuration config;
	private final Messages messages;
	
	public CommandTake(DatabaseManager databaseManager, CurrenciesManager currenciesManager,
			Configuration config, Messages messages) {
		
		super(messages);
		
		this.databaseManager = databaseManager;
		this.currenciesManager = currenciesManager;
		
		this.config = config;
		this.messages = messages;
		
		String permmsg = messages.get("error.no-permissions");
		String argsmsg = messages.get("error.wrong-syntax");
		String walletmsg = messages.get("error.unknown-wallet");
		String amountmsg = messages.get("error.arg-is-not-float");
		String currencymsg = messages.get("error.unknown-currency");
		
		Validator permval = new PermissionValidator("peco.command.take", permmsg);
		Validator argsval = new ArgsCountValidator(4, argsmsg);
		Validator walletval = new WalletValidator(databaseManager, walletmsg);
		Validator amountval = new AmountValidator(amountmsg);
		Validator currencyval = new CurrencyValidator(currenciesManager, currencymsg);
		
		super.addValidators(permval, argsval, walletval, amountval, currencyval);
	}

	@Override
	public void executeCommand(CommandSender sender, String[] args) {
		String owner = args[1], amstr = args[2], currencyid = args[3];
		
		CommandExecutionData data = new WalletExecutionData(sender, args, owner, currencyid, amstr);
		if(!validateExecution(data)) return;
		
		// Preparing amount and source
		float amount = Float.parseFloat(amstr);
		String source = sender instanceof Player ? sender.getName() : config.getColoredString("console-source");
		
		// Getting some values
		Wallet wallet = databaseManager.getWallet(owner);
		
		float pre = wallet.getAmount(currencyid);
		float post = pre - amount;
		
		CurrencyInstance currency = currenciesManager.getCurrency(currencyid);
		
		// Formatting values
		String symbol = currency.getSymbol();
		String prestr = AmountFormatter.format(pre);
		
		// Checking for invalid balance
		String amountstr = AmountFormatter.format(amount);
		if(post < 0) {
			messages.sendFormatted(sender, "take.not-enough",
					"%amount%", prestr,
					"%currency%", symbol,
					"%player%", owner,
					"%requested%", amountstr);
			return;
		}
		
		// Updating DB
		wallet.takeAmount(currencyid, amount);
		databaseManager.updateWallet(wallet);
		
		// Formatting values
		String poststr = AmountFormatter.format(post);
		String operation = messages.get("operation.decrease");
		
		// Saving transaction
		Transaction transaction = new Transaction(owner, currencyid, TransactionType.TAKE, pre, post, source);
		databaseManager.saveTransaction(transaction);
		
		int id = transaction.getId();
		
		// Sending messages to sender and wallet owner if he is online
		messages.sendFormatted(sender, "take.other",
				"%amount%", amountstr,
				"%currency%", symbol,
				"%player%", owner,
				"%from%", prestr,
				"%operation%", operation,
				"%to%", poststr,
				"%id%", id);
		
		OfflinePlayer offlinetarget = Bukkit.getOfflinePlayer(owner);
		if(offlinetarget.isOnline())
			messages.sendFormatted(offlinetarget.getPlayer(), "take.self",
					"%amount%", amountstr,
					"%currency%", symbol,
					"%player%", owner,
					"%from%", prestr,
					"%operation%", operation,
					"%to%", poststr,
					"%source%", SourceFormatter.format(config, source, offlinetarget.getPlayer()),
					"%id%", id);
	}
	
	@Override
	public List<String> executeTabCompletion(CommandSender sender, String[] args) {
		if(!validateTabCompletion(sender, args)) return null;
		
		List<String> output = new ArrayList<>();
		
		if(args.length == 2) {
			Collection<? extends Player> players = Bukkit.getOnlinePlayers();
			if(!players.isEmpty()) {
				String arg = args[1].toLowerCase();
				players.parallelStream()
						.filter(p -> p.getName().toLowerCase().startsWith(arg))
						.forEach(p -> output.add(p.getName()));
			}
		} else if(args.length == 4) {
			Set<String> currencies = this.currenciesManager.getCurrenciesIDs();
			if(!currencies.isEmpty()) {
				String arg = args[3].toLowerCase();
				currencies.parallelStream()
						.filter(c -> c.toLowerCase().startsWith(arg))
						.forEach(c -> output.add(c));
			}
		} else return null;
		
		return output;
	}
	
}
