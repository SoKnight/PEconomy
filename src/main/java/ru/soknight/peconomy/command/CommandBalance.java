package ru.soknight.peconomy.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.ExtendedCommandExecutor;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.validation.validator.PermissionValidator;
import ru.soknight.lib.validation.validator.Validator;
import ru.soknight.peconomy.command.tool.AmountFormatter;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.Wallet;

public class CommandBalance extends ExtendedCommandExecutor {
	
	private final DatabaseManager databaseManager;
	private final CurrenciesManager currenciesManager;
	
	private final Configuration config;
	private final Messages messages;
	
	public CommandBalance(DatabaseManager databaseManager, CurrenciesManager currenciesManager,
			Configuration config, Messages messages) {
		super(messages);
		
		this.databaseManager = databaseManager;
		this.currenciesManager = currenciesManager;
		
		this.config = config;
		this.messages = messages;
		
		String permmsg = messages.get("error.no-permissions");
		
		Validator permval = new PermissionValidator("peco.command.balance", permmsg);
		
		super.addValidators(permval);
	}

	@Override
	public void executeCommand(CommandSender sender, CommandArguments args) {
		if(!validateExecution(sender, args)) return;
		
		String name = sender.getName();
		boolean other = false;
		
		// Other balance checking execution
		if(!args.isEmpty()) {
			if(!sender.hasPermission("peco.command.balance.other")) {
				messages.getAndSend(sender, "balance.other-balance");
				return;
			}
			
			name = args.get(0);
			
			OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
			if(offline == null) {
				messages.sendFormatted(sender, "error.unknown-wallet", "%player%", name);
				return;
			}
			
			if(!offline.isOnline()) {
				messages.getAndSend(sender, "balance.offline-balance");
				return;
			}
			
			if(!name.equals(sender.getName()))
				other = true;
		} else {
			if(!(sender instanceof Player)) {
				messages.getAndSend(sender, "error.only-for-players");
				return;
			}
		}
		
		Wallet wallet = databaseManager.getWallet(name);
		if(wallet == null) {
			messages.sendFormatted(sender, "error.unknown-wallet", "%player%", name);
			return;
		}
		
		String format = messages.get("balance.format");
		Map<String, Float> wallets = wallet.getWallets();
		
		if(wallets == null || wallets != null && wallets.isEmpty()) {
			if(other)
				messages.sendFormatted(sender, "balance.empty-other", "%player%", name);
			else messages.getAndSend(sender, "balance.empty-self");
			return;
		}
		
		List<String> balances = new ArrayList<>();
		
		wallets.forEach((c, a) -> {
			CurrencyInstance instance = currenciesManager.getCurrency(c);
			String symbol;
			
			if(instance == null) {
				if(config.getBoolean("hide-unknown-currencies")) return;
				else symbol = "N/A";
			} else symbol = instance.getSymbol();
			
			String amount = AmountFormatter.format(a);
			balances.add(messages.format(format, "%amount%", amount, "%currency%", symbol));
		});
		
		if(balances.isEmpty()) {
			if(other)
				messages.sendFormatted(sender, "balance.empty-other", "%player%", name);
			else messages.getAndSend(sender, "balance.empty-self");
		}
		
		String balance;
		if(balances.size() == 1)
			balance = balances.get(0);
		else {
			StringBuilder builder = new StringBuilder(balances.get(0));
			String separator = messages.get("balance.separator");
			
			for(int i = 1; i < balances.size(); i++) {
				builder.append(separator);
				builder.append(balances.get(i));
			}
			
			balance = builder.toString();
		}
		
		if(other)
			messages.sendFormatted(sender, "balance.other", "%player%", name, "%balance%", balance);
		else messages.sendFormatted(sender, "balance.self", "%balance%", balance);
	}
	
	@Override
	public List<String> executeTabCompletion(CommandSender sender, CommandArguments args) {
		if(args.isEmpty() || !validateTabCompletion(sender, args)) return null;
		
		List<String> output = new ArrayList<>();
		
		// Adds other online players if interaction with them is permitted
		if(sender.hasPermission("peco.command.balance.other")) {
			Collection<? extends Player> players = Bukkit.getOnlinePlayers();
			if(!players.isEmpty()) {
				String arg = args.get(0).toLowerCase();
				players.parallelStream()
						.filter(p -> p.getName().toLowerCase().startsWith(arg))
						.forEach(p -> output.add(p.getName()));
			}
		}
		
		// Adds other offline players if interaction with them is permitted
		if(sender.hasPermission("peco.command.balance.offline")) {
			OfflinePlayer[] players = Bukkit.getOfflinePlayers();
			if(players.length != 0) {
				String arg = args.get(0).toLowerCase();
				Arrays.stream(players).parallel()
						.filter(p -> !p.isOnline() && p.getName().toLowerCase().startsWith(arg))
						.forEach(p -> output.add(p.getName()));
			}
		}
		
		return output;
	}
	
}
