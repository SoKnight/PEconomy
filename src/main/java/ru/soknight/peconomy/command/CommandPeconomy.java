package ru.soknight.peconomy.command;

import ru.soknight.lib.command.preset.ModifiedDispatcher;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.PEconomyPlugin;
import ru.soknight.peconomy.command.peconomy.*;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.database.DatabaseManager;

public class CommandPeconomy extends ModifiedDispatcher {
    
    public CommandPeconomy(
            PEconomyPlugin plugin,
            Messages messages,
            DatabaseManager databaseManager,
            CurrenciesManager currenciesManager
    ) {
        super("peconomy", messages);
        
        super.setExecutor("help", new CommandHelp(messages));
        super.setExecutor("info", new CommandInfo(plugin, messages, databaseManager, currenciesManager));
        super.setExecutor("history", new CommandHistory(plugin, messages, databaseManager, currenciesManager));
        super.setExecutor("add", new CommandAdd(messages, databaseManager, currenciesManager));
        super.setExecutor("set", new CommandSet(messages, databaseManager, currenciesManager));
        super.setExecutor("reset", new CommandReset(messages, databaseManager, currenciesManager));
        super.setExecutor("take", new CommandTake(messages, databaseManager, currenciesManager));
        super.setExecutor("reload", new CommandReload(plugin, messages));

        super.register(plugin, true);
    }

}
