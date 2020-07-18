package ru.soknight.peconomy.command.sub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.ExtendedSubcommandExecutor;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.validation.CommandExecutionData;
import ru.soknight.lib.validation.validator.ArgsCountValidator;
import ru.soknight.lib.validation.validator.PermissionValidator;
import ru.soknight.lib.validation.validator.Validator;
import ru.soknight.peconomy.command.tool.AmountFormatter;
import ru.soknight.peconomy.command.tool.SourceFormatter;
import ru.soknight.peconomy.command.validation.CurrencyValidator;
import ru.soknight.peconomy.command.validation.WalletExecutionData;
import ru.soknight.peconomy.command.validation.WalletValidator;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.Transaction;
import ru.soknight.peconomy.database.TransactionType;
import ru.soknight.peconomy.database.Wallet;

public class CommandReset extends ExtendedSubcommandExecutor {

	private final DatabaseManager databaseManager;
	private final CurrenciesManager currenciesManager;
	
	private final Configuration config;
	private final Messages messages;
	
	public CommandReset(DatabaseManager databaseManager, CurrenciesManager currenciesManager,
			Configuration config, Messages messages) {
		
		super(messages);
		
		this.databaseManager = databaseManager;
		this.currenciesManager = currenciesManager;
		
		this.config = config;
		this.messages = messages;
		
		String permmsg = messages.get("error.no-permissions");
		String argsmsg = messages.get("error.wrong-syntax");
		String walletmsg = messages.get("error.unknown-wallet");
		String currencymsg = messages.get("error.unknown-currency");
		
		Validator permval = new PermissionValidator("peco.command.reset", permmsg);
		Validator argsval = new ArgsCountValidator(2, argsmsg);
		Validator walletval = new WalletValidator(databaseManager, walletmsg);
		Validator currencyval = new CurrencyValidator(currenciesManager, currencymsg);
		
		super.addValidators(permval, argsval, walletval, currencyval);
	}

	@Override
	public void executeCommand(CommandSender sender, CommandArguments args) {
		String owner = args.get(0), currencyid = args.get(1);
		
		CommandExecutionData data = new WalletExecutionData(sender, args, owner, currencyid, null);
		if(!validateExecution(data)) return;
		
		// Preparing amount and source
		String source = sender instanceof Player ? sender.getName() : config.getColoredString("console-source");
		
		// Getting some values
		Wallet wallet = databaseManager.getWallet(owner);
		
		float pre = wallet.getAmount(currencyid);
		
		CurrencyInstance currency = currenciesManager.getCurrency(currencyid);
		
		// Formatting values
		String symbol = currency.getSymbol();
		String prestr = AmountFormatter.format(pre);
		
		// Checking for already empty balance
		if(pre == 0F) {
			messages.sendFormatted(sender, "reset.already", "%player%", owner);
			return;
		}
		
		// Updating DB
		wallet.resetWallet(currencyid);
		databaseManager.updateWallet(wallet);
		
		// Formatting values
		String operation = messages.get("operation.decrease");
		
		// Saving transaction
		Transaction transaction = new Transaction(owner, currencyid, TransactionType.RESET, pre, 0F, source);
		databaseManager.saveTransaction(transaction);
		
		int id = transaction.getId();
		
		// Sending messages to sender and wallet owner if he is online
		messages.sendFormatted(sender, "reset.other",
				"%currency%", symbol,
				"%player%", owner,
				"%from%", prestr,
				"%operation%", operation,
				"%id%", id);
		
		OfflinePlayer offlinetarget = Bukkit.getOfflinePlayer(owner);
		if(offlinetarget.isOnline())
			messages.sendFormatted(offlinetarget.getPlayer(), "reset.self",
					"%currency%", symbol,
					"%from%", prestr,
					"%operation%", operation,
					"%source%", SourceFormatter.format(config, source, offlinetarget.getPlayer()),
					"%id%", id);
	}
	
	@Override
	public List<String> executeTabCompletion(CommandSender sender, CommandArguments args) {
		if(!validateTabCompletion(sender, args)) return null;
		
		List<String> output = new ArrayList<>();
		
		if(args.size() == 1) {
			Collection<? extends Player> players = Bukkit.getOnlinePlayers();
			if(!players.isEmpty()) {
				String arg = args.get(0).toLowerCase();
				players.parallelStream()
						.filter(p -> p.getName().toLowerCase().startsWith(arg))
						.forEach(p -> output.add(p.getName()));
			}
		} else if(args.size() == 2) {
			Set<String> currencies = this.currenciesManager.getCurrenciesIDs();
			if(!currencies.isEmpty()) {
				String arg = args.get(1).toLowerCase();
				currencies.parallelStream()
						.filter(c -> c.toLowerCase().startsWith(arg))
						.forEach(c -> output.add(c));
			}
		} else return null;
		
		return output;
	}
	
}
