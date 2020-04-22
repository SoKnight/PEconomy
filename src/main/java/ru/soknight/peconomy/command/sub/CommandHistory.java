package ru.soknight.peconomy.command.sub;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.soknight.lib.command.ExtendedSubcommandExecutor;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.tool.CollectionsTool;
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

public class CommandHistory extends ExtendedSubcommandExecutor {

	private final Logger logger;
	private final DatabaseManager databaseManager;
	private final CurrenciesManager currenciesManager;
	
	private final Configuration config;
	private final Messages messages;
	
	public CommandHistory(PEconomy plugin, DatabaseManager databaseManager, CurrenciesManager currenciesManager,
			Configuration config, Messages messages) {
		
		super(messages);
		
		this.logger = plugin.getLogger();
		this.databaseManager = databaseManager;
		this.currenciesManager = currenciesManager;
		
		this.config = config;
		this.messages = messages;
		
		String permmsg = messages.get("error.no-permissions");
		String argsmsg = messages.get("error.wrong-syntax");
		
		Validator permval = new PermissionValidator("peco.command.history", permmsg);
		Validator argsval = new ArgsCountValidator(2, argsmsg);
		
		super.addValidators(permval, argsval);
	}

	@Override
	public void executeCommand(CommandSender sender, String[] args) {
		if(!validateExecution(sender, args)) return;
		
		String owner = args[1];
		boolean other = false;
		
		// Check for trying to see other history
		if(!owner.equals(sender.getName())) {
			if(!sender.hasPermission("peco.command.history.other")) {
				messages.getAndSend(sender, "history.other-history");
				return;
			}
			
			OfflinePlayer offline = Bukkit.getOfflinePlayer(owner);
			if(offline != null && !offline.isOnline() && !sender.hasPermission("peco.command.history.offline")) {
				messages.getAndSend(sender, "history.offline-history");
				return;
			}
			
			other = true;
		}
		
		// Check for wallet exist
		if(!databaseManager.hasWallet(owner)) {
			messages.sendFormatted(sender, "error.unknown-wallet", "%player%", owner);
			return;
		}
		
		// Getting target page
		int page = 1;
		if(args.length > 2) {
			try {
				page = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				messages.sendFormatted(sender, "error.arg-is-not-int", "%arg%", args[2]);
				return;
			}
		}
		
		List<Transaction> transactions = databaseManager.getWalletTransactions(owner);
		if(transactions == null) {
			if(other)
				messages.sendFormatted(sender, "history.empty-other", "%player%", owner);
			else messages.getAndSend(sender, "history.empty-self");
			return;
		}
		
		int size = config.getInt("messages.list-size");
		List<Transaction> onpage = CollectionsTool.getSubList(transactions, size, page);
		
		if(onpage.isEmpty()) {
			messages.sendFormatted(sender, "history.empty-page", "%page%", page);
			return;
		}
		
		int total = transactions.size() / size;
		if(transactions.size() % size != 0) total++;
		
		String formatPattern = config.getColoredString("transactions-history.date-format");
		DateFormat tempFormat;
		
		try {
			tempFormat = new SimpleDateFormat(formatPattern);
		} catch (Exception e) {
			logger.severe("Hey! You use invalid transaction date format: " + formatPattern);
			tempFormat = new SimpleDateFormat("dd.MM.yy - kk:mm:ss");
		}
		
		final DateFormat format = tempFormat;

		String body = messages.get("history.body");
		List<String> output = new ArrayList<>();
		
		onpage.forEach(t -> {
			int id = t.getId();
			String source = SourceFormatter.format(config, t.getSource(), sender);
			String date = t.formatDate(format);
			
			float pre = t.getPreBalance();
			float post = t.getPostBalance();
			
			String operation = pre < post ? "increase" : "decrease";
			operation = messages.get("operation." + operation);
			
			TransactionType type = t.getType();
			String action = messages.getFormatted("action." + type.toString().toLowerCase(), "%source%", source);
			
			CurrencyInstance currency = currenciesManager.getCurrency(t.getCurrency());
			String symbol = currency == null ? "?" : currency.getSymbol();
			
			output.add(messages.format(body,
					"%id%", id,
					"%date%", date,
					"%from%", AmountFormatter.format(pre),
					"%currency%", symbol,
					"%operation%", operation,
					"%to%", AmountFormatter.format(post),
					"%action%", action
			));
		});
		
		String header = other
				? messages.getFormatted("history.header-other", "%player%", owner, "%page%", page, "%total%", total)
				: messages.getFormatted("history.header-self", "%page%", page, "%total%", total);
				
		String footer = messages.get("history.footer-" + (other ? "other" : "self"));
		
		messages.send(sender, header);
		output.forEach(b -> messages.send(sender, b));
		messages.send(sender, footer);
	}
	
	@Override
	public List<String> executeTabCompletion(CommandSender sender, String[] args) {
		if(args.length != 2) return null;
		if(!validateTabCompletion(sender, args)) return null;
		
		List<String> output = new ArrayList<>();
		
		// Adds other online players if interaction with them is permitted
		if(sender.hasPermission("peco.command.history.other")) {
			Collection<? extends Player> players = Bukkit.getOnlinePlayers();
			if(!players.isEmpty()) {
				String arg = args[1].toLowerCase();
				players.parallelStream()
						.filter(p -> p.getName().toLowerCase().startsWith(arg))
						.forEach(p -> output.add(p.getName()));
			}
			// Adds other offline players if interaction with them is permitted
			if(sender.hasPermission("peco.command.history.offline")) {
				OfflinePlayer[] oplayers = Bukkit.getOfflinePlayers();
				if(oplayers.length != 0) {
					String arg = args[1].toLowerCase();
					Arrays.stream(oplayers).parallel()
							.filter(p -> !p.isOnline() && p.getName().toLowerCase().startsWith(arg))
							.forEach(p -> output.add(p.getName()));
				}
			}
		} else if(sender instanceof Player)
			output.add(sender.getName());
		
		return output;
	}
	
}
