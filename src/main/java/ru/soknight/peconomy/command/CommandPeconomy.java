package ru.soknight.peconomy.command;

import ru.soknight.lib.command.preset.ModifiedDispatcher;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.command.peconomy.*;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.database.DatabaseManager;

public class CommandPeconomy extends ModifiedDispatcher {
    
    public CommandPeconomy(
            PEconomy plugin,
            Configuration config,
            Messages messages,
            DatabaseManager databaseManager,
            CurrenciesManager currenciesManager
    ) {
        super("peconomy", messages);
        
        super.setExecutor("help", new CommandHelp(messages));
        super.setExecutor("add", new CommandAdd(config, messages, databaseManager, currenciesManager));
        super.setExecutor("set", new CommandSet(config, messages, databaseManager, currenciesManager));
        super.setExecutor("reset", new CommandReset(config, messages, databaseManager, currenciesManager));
        super.setExecutor("take", new CommandTake(config, messages, databaseManager, currenciesManager));
        super.setExecutor("info", new CommandInfo(plugin, messages, databaseManager, currenciesManager));
        super.setExecutor("history", new CommandHistory(plugin, messages, databaseManager, currenciesManager));
        super.setExecutor("convert", new CommandConvert(config, messages, databaseManager, currenciesManager));
        super.setExecutor("reload", new CommandReload(plugin, messages));

        super.register(plugin, true);
    }

}
