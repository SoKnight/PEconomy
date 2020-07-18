package ru.soknight.peconomy.command.sub;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.ExtendedSubcommandExecutor;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.validation.validator.ArgsCountValidator;
import ru.soknight.lib.validation.validator.PermissionValidator;
import ru.soknight.lib.validation.validator.Validator;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.command.tool.AmountFormatter;
import ru.soknight.peconomy.command.tool.SourceFormatter;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.Transaction;
import ru.soknight.peconomy.database.TransactionType;

public class CommandInfo extends ExtendedSubcommandExecutor {

	private final Logger logger;
	private final DatabaseManager databaseManager;
	private final CurrenciesManager currenciesManager;
	
	private final Configuration config;
	private final Messages messages;
	
	private final String header;
	private final Set<String> keys;
	private final String footer;
	
	public CommandInfo(PEconomy plugin, DatabaseManager databaseManager, CurrenciesManager currenciesManager,
			Configuration config, Messages messages) {
		
		super(messages);
		
		this.logger = plugin.getLogger();
		this.databaseManager = databaseManager;
		this.currenciesManager = currenciesManager;
		
		this.config = config;
		this.messages = messages;
		
		this.header = messages.get("info.header");
		this.keys = messages.getFileConfig().getConfigurationSection("info.list").getKeys(false);
		this.footer = messages.get("info.footer");
		
		String permmsg = messages.get("error.no-permissions");
		String argsmsg = messages.get("error.wrong-syntax");
		
		Validator permval = new PermissionValidator("peco.command.info", permmsg);
		Validator argsval = new ArgsCountValidator(1, argsmsg);
		
		super.addValidators(permval, argsval);
	}

	@Override
	public void executeCommand(CommandSender sender, CommandArguments args) {
		if(!validateExecution(sender, args)) return;
		
		// Getting transaction ID
		int id = 1;
		
		try {
			id = Integer.parseInt(args.get(0));
		} catch (NumberFormatException e) {
			messages.sendFormatted(sender, "error.arg-is-not-int", "%arg%", args.get(0));
			return;
		}
		
		// Getting transaction if it's exist
		Transaction transaction = databaseManager.getTransactionByID(id);
		if(transaction == null) {
			messages.sendFormatted(sender, "info.not-found", "%id%", id);
			return;
		}
		
		String owner = transaction.getOwner();
		
		// Checking for trying see transaction of other wallet
		if(!owner.equals(sender.getName())) {
			if(!sender.hasPermission("peco.command.info.other")) {
				messages.getAndSend(sender, "info.other-info");
				return;
			}
			
			OfflinePlayer offline = Bukkit.getOfflinePlayer(owner);
			if(offline != null && !offline.isOnline() && !sender.hasPermission("peco.command.info.offline")) {
				messages.getAndSend(sender, "info.offline-info");
				return;
			}
		}
		
		String formatPattern = config.getColoredString("transactions-history.date-format");
		DateFormat tempFormat;
		
		try {
			tempFormat = new SimpleDateFormat(formatPattern);
		} catch (Exception e) {
			logger.severe("Hey! You use invalid transaction date format: " + formatPattern);
			tempFormat = new SimpleDateFormat("dd.MM.yy - kk:mm:ss");
		}
		
		final DateFormat format = tempFormat;

		String source = SourceFormatter.format(config, transaction.getSource(), sender);
		String date = transaction.formatDate(format);
			
		float pre = transaction.getPreBalance();
		float post = transaction.getPostBalance();
			
		String operation = pre < post ? "increase" : "decrease";
		operation = messages.get("operation." + operation);
			
		TransactionType type = transaction.getType();
		String action = messages.getFormatted("action." + type.toString().toLowerCase(), "%source%", source);
			
		CurrencyInstance currency = currenciesManager.getCurrency(transaction.getCurrency());
		String symbol = currency == null ? "?" : currency.getSymbol();
		
		// Preparing messages data
		Map<String, Object[]> data = new HashMap<>();
		data.put("id", new Object[] { "%id%", id });
		data.put("owner", new Object[] { "%owner%", owner });
		data.put("action", new Object[] { "%action%", action });
		data.put("pre", new Object[] { "%pre%", AmountFormatter.format(pre), "%symbol%", symbol });
		data.put("post", new Object[] { "%post%", AmountFormatter.format(post), "%symbol%", symbol });
		data.put("currency", new Object[] { "%currency%", transaction.getCurrency(), "%symbol%", symbol });
		data.put("date", new Object[] { "%date%", date });
		
		/*
		 * Sending info messages block
		 */
		
		messages.send(sender, header);
		
		if(!keys.isEmpty())
			keys.forEach(k -> {
				String message = data.containsKey(k)
						? messages.getFormatted("info.list." + k, data.get(k))
						: messages.get("info.list." + k);
						
				messages.send(sender, message);
			});
		
		messages.send(sender, footer);
	}
	
}
