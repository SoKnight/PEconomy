package ru.soknight.peconomy.command;

import ru.soknight.lib.command.AbstractSubcommandsHandler;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.command.sub.CommandAdd;
import ru.soknight.peconomy.command.sub.CommandHelp;
import ru.soknight.peconomy.command.sub.CommandHistory;
import ru.soknight.peconomy.command.sub.CommandInfo;
import ru.soknight.peconomy.command.sub.CommandReload;
import ru.soknight.peconomy.command.sub.CommandReset;
import ru.soknight.peconomy.command.sub.CommandSet;
import ru.soknight.peconomy.command.sub.CommandTake;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.database.DatabaseManager;

public class SubcommandHandler extends AbstractSubcommandsHandler {
	
	public SubcommandHandler(PEconomy plugin, CurrenciesManager currenciesManager, DatabaseManager databaseManager,
			Configuration config, Messages messages) {
		
		super(messages);
		
		super.setExecutor("help", new CommandHelp(messages));
		super.setExecutor("history", new CommandHistory(plugin, databaseManager, currenciesManager, config, messages));
		super.setExecutor("info", new CommandInfo(plugin, databaseManager, currenciesManager, config, messages));
		super.setExecutor("add", new CommandAdd(databaseManager, currenciesManager, config, messages));
		super.setExecutor("set", new CommandSet(databaseManager, currenciesManager, config, messages));
		super.setExecutor("reset", new CommandReset(databaseManager, currenciesManager, config, messages));
		super.setExecutor("take", new CommandTake(databaseManager, currenciesManager, config, messages));
		super.setExecutor("reload", new CommandReload(plugin, messages));
	}

}
