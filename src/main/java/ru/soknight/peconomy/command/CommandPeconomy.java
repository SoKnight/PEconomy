package ru.soknight.peconomy.command;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.bukkit.plugin.Plugin;

import ru.soknight.lib.command.preset.ModifiedDispatcher;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.command.peconomy.CommandAdd;
import ru.soknight.peconomy.command.peconomy.CommandHelp;
import ru.soknight.peconomy.command.peconomy.CommandHistory;
import ru.soknight.peconomy.command.peconomy.CommandInfo;
import ru.soknight.peconomy.command.peconomy.CommandReload;
import ru.soknight.peconomy.command.peconomy.CommandReset;
import ru.soknight.peconomy.command.peconomy.CommandSet;
import ru.soknight.peconomy.command.peconomy.CommandTake;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.database.DatabaseManager;

public class CommandPeconomy extends ModifiedDispatcher {
    
    public CommandPeconomy(
            PEconomy plugin, Configuration config, Messages messages,
            DatabaseManager databaseManager, CurrenciesManager currenciesManager
    ) {
        super("peconomy", messages);
        
        DateFormat dateFormatter = getDateFormatter(plugin, config);
        
        super.setExecutor("help", new CommandHelp(messages));
        super.setExecutor("history", new CommandHistory(config, messages, databaseManager, currenciesManager, dateFormatter));
        super.setExecutor("info", new CommandInfo(config, messages, databaseManager, currenciesManager, dateFormatter));
        super.setExecutor("add", new CommandAdd(messages, databaseManager, currenciesManager));
        super.setExecutor("set", new CommandSet(messages, databaseManager, currenciesManager));
        super.setExecutor("reset", new CommandReset(messages, databaseManager, currenciesManager));
        super.setExecutor("take", new CommandTake(messages, databaseManager, currenciesManager));
        super.setExecutor("reload", new CommandReload(plugin, messages));
    }
    
    private DateFormat getDateFormatter(Plugin plugin, Configuration config) {
        String formatPattern = config.getColoredString("transactions-history.date-format");
        
        try {
            return new SimpleDateFormat(formatPattern);
        } catch (Exception e) {
            plugin.getLogger().severe("Hey! You use invalid transaction date format: " + formatPattern);
            return new SimpleDateFormat("dd.MM.yy - kk:mm:ss");
        }
    }

}
