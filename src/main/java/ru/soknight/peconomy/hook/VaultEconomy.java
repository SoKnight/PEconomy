package ru.soknight.peconomy.hook;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.command.tool.AmountFormatter;
import ru.soknight.peconomy.command.tool.SourceFormatter;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.Transaction;
import ru.soknight.peconomy.database.TransactionType;
import ru.soknight.peconomy.database.Wallet;

public class VaultEconomy implements Economy {

	private final DatabaseManager databaseManager;
	private final CurrencyInstance currency;
	
	private final Configuration config;
	private final Messages messages;
	
	private final String plural;
	private final String singular;
	
	public VaultEconomy(DatabaseManager databaseManager, CurrenciesManager currenciesManager,
			Configuration config, Messages messages) {
		
		this.databaseManager = databaseManager;
		this.currency = currenciesManager.getVaultCurrency();
		
		this.config = config;
		this.messages = messages;
		
		this.plural = currenciesManager.getColoredString("vault.plural");
		this.singular = currenciesManager.getColoredString("vault.singular");
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getName() {
		return "PEconomy";
	}

	@Override
	public boolean hasBankSupport() {
		return false;
	}

	@Override
	public int fractionalDigits() {
		return 2;
	}

	@Override
	public String format(double amount) {
		return AmountFormatter.format((float) amount);
	}

	@Override
	public String currencyNamePlural() {
		return this.plural;
	}

	@Override
	public String currencyNameSingular() {
		return this.singular;
	}

	@Override
	public boolean hasAccount(String playerName) {
		return this.databaseManager.hasWallet(playerName);
	}

	@Override
	public boolean hasAccount(OfflinePlayer player) {
		return hasAccount(player.getName());
	}

	@Override
	public boolean hasAccount(String playerName, String worldName) {
		return hasAccount(playerName);
	}

	@Override
	public boolean hasAccount(OfflinePlayer player, String worldName) {
		return hasAccount(player.getName());
	}

	@Override
	public double getBalance(String playerName) {
		Wallet wallet = this.databaseManager.getWallet(playerName);
		if(wallet == null) return 0;
		
		return wallet.getAmount(this.currency.getID());
	}

	@Override
	public double getBalance(OfflinePlayer player) {
		return getBalance(player.getName());
	}

	@Override
	public double getBalance(String playerName, String world) {
		return getBalance(playerName);
	}

	@Override
	public double getBalance(OfflinePlayer player, String world) {
		return getBalance(player.getName());
	}

	@Override
	public boolean has(String playerName, double amount) {
		Wallet wallet = this.databaseManager.getWallet(playerName);
		if(wallet == null) return false;
		
		return wallet.hasAmount(this.currency.getID(), (float) amount);
	}

	@Override
	public boolean has(OfflinePlayer player, double amount) {
		return has(player.getName(), amount);
	}

	@Override
	public boolean has(String playerName, String worldName, double amount) {
		return has(playerName, amount);
	}

	@Override
	public boolean has(OfflinePlayer player, String worldName, double amount) {
		return has(player.getName(), amount);
	}

	@Override
	public EconomyResponse withdrawPlayer(String playerName, double amount) {
		Wallet wallet = this.databaseManager.getWallet(playerName);
		if(wallet == null) {
			String message = messages.getFormatted("error.unknown-wallet", "%player%", playerName);
			return new EconomyResponse(amount, 0D, ResponseType.FAILURE, message);
		}
		
		float pre = wallet.getAmount(this.currency.getID());
		float post = pre - (float) amount;
		
		if(post < 0) {
			String message = messages.getFormatted("take.not-enough",
					"%amount%", AmountFormatter.format(pre),
					"%currency%", currency.getSymbol(),
					"%player%", playerName,
					"%requested%", format(amount));
			return new EconomyResponse(amount, pre, ResponseType.FAILURE, message);
		}
		
		wallet.takeAmount(this.currency.getID(), (float) amount);
		databaseManager.updateWallet(wallet);
		
		// Saving transaction
		Transaction transaction = new Transaction(playerName, currency.getID(), TransactionType.TAKE, pre, post, "#vault");
		databaseManager.saveTransaction(transaction);
		
		String message = messages.getFormatted("take.other",
				"%amount%", format(amount),
				"%currency%", currency.getSymbol(),
				"%player%", playerName,
				"%from%", AmountFormatter.format(pre),
				"%operation%", messages.get("operation.decrease"),
				"%to%", AmountFormatter.format(post),
				"%id%", transaction.getId());
		
		OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);
		if(offline != null && offline.isOnline()) {
			messages.sendFormatted(offline.getPlayer(), "take.self",
					"%amount%", format(amount),
					"%currency%", currency.getSymbol(),
					"%player%", playerName,
					"%from%", AmountFormatter.format(pre),
					"%operation%", messages.get("operation.decrease"),
					"%to%", AmountFormatter.format(post),
					"%source%", SourceFormatter.format(config, "#vault", offline.getPlayer()),
					"%id%", transaction.getId());
		}
		
		return new EconomyResponse(amount, post, ResponseType.SUCCESS, message);
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
		return withdrawPlayer(player.getName(), amount);
	}

	@Override
	public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
		return withdrawPlayer(playerName, amount);
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
		return withdrawPlayer(player.getName(), amount);
	}

	@Override
	public EconomyResponse depositPlayer(String playerName, double amount) {
		Wallet wallet = this.databaseManager.getWallet(playerName);
		if(wallet == null) {
			String message = messages.getFormatted("error.unknown-wallet", "%player%", playerName);
			return new EconomyResponse(amount, 0D, ResponseType.FAILURE, message);
		}
		
		float pre = wallet.getAmount(this.currency.getID());
		float post = pre + (float) amount;
		
		float limit = currency.getLimit();
		if(limit != 0 && post > limit) {
			String limitstr = AmountFormatter.format(limit);
			String message = messages.getFormatted("add.limit",
					"%amount%", AmountFormatter.format(pre),
					"%currency%", currency.getSymbol(),
					"%player%", playerName,
					"%limit%", limitstr);
			return new EconomyResponse(amount, pre, ResponseType.FAILURE, message);
		}
		
		wallet.addAmount(this.currency.getID(), (float) amount);
		databaseManager.updateWallet(wallet);
		
		// Saving transaction
		Transaction transaction = new Transaction(playerName, currency.getID(), TransactionType.ADD, pre, post, "#vault");
		databaseManager.saveTransaction(transaction);
		
		String message = messages.getFormatted("add.other",
				"%amount%", format(amount),
				"%currency%", currency.getSymbol(),
				"%player%", playerName,
				"%from%", AmountFormatter.format(pre),
				"%operation%", messages.get("operation.increase"),
				"%to%", AmountFormatter.format(post),
				"%id%", transaction.getId());
		
		OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);
		if(offline != null && offline.isOnline()) {
			messages.sendFormatted(offline.getPlayer(), "add.self",
					"%amount%", format(amount),
					"%currency%", currency.getSymbol(),
					"%player%", playerName,
					"%from%", AmountFormatter.format(pre),
					"%operation%", messages.get("operation.increase"),
					"%to%", AmountFormatter.format(post),
					"%source%", SourceFormatter.format(config, "#vault", offline.getPlayer()),
					"%id%", transaction.getId());
		}
		
		return new EconomyResponse(amount, post, ResponseType.SUCCESS, message);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
		return depositPlayer(player.getName(), amount);
	}

	@Override
	public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
		return depositPlayer(playerName, amount);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
		return depositPlayer(player.getName(), amount);
	}

	@Override
	public EconomyResponse createBank(String name, String player) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, ChatColor.RED + "Banks is not supported.");
	}

	@Override
	public EconomyResponse createBank(String name, OfflinePlayer player) {
		return createBank(name, player.getName());
	}

	@Override
	public EconomyResponse deleteBank(String name) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, ChatColor.RED + "Banks is not supported.");
	}

	@Override
	public EconomyResponse bankBalance(String name) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, ChatColor.RED + "Banks is not supported.");
	}

	@Override
	public EconomyResponse bankHas(String name, double amount) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, ChatColor.RED + "Banks is not supported.");
	}

	@Override
	public EconomyResponse bankWithdraw(String name, double amount) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, ChatColor.RED + "Banks is not supported.");
	}

	@Override
	public EconomyResponse bankDeposit(String name, double amount) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, ChatColor.RED + "Banks is not supported.");
	}

	@Override
	public EconomyResponse isBankOwner(String name, String playerName) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, ChatColor.RED + "Banks is not supported.");
	}

	@Override
	public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, ChatColor.RED + "Banks is not supported.");
	}

	@Override
	public EconomyResponse isBankMember(String name, String playerName) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, ChatColor.RED + "Banks is not supported.");
	}

	@Override
	public EconomyResponse isBankMember(String name, OfflinePlayer player) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, ChatColor.RED + "Banks is not supported.");
	}

	@Override
	public List<String> getBanks() {
		return new ArrayList<>();
	}

	@Override
	public boolean createPlayerAccount(String playerName) {
		if(databaseManager.hasWallet(playerName)) return false;
		
		Wallet wallet = new Wallet(playerName);
		wallet.setAmount(currency.getID(), currency.getNewbieAmount());
		databaseManager.createWallet(wallet);
		return true;
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer player) {
		return createPlayerAccount(player.getName());
	}

	@Override
	public boolean createPlayerAccount(String playerName, String worldName) {
		return createPlayerAccount(playerName);
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
		return createPlayerAccount(player.getName());
	}

}
