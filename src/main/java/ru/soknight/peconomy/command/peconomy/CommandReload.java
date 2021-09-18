package ru.soknight.peconomy.command.peconomy;

import org.bukkit.command.CommandSender;

import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.subcommand.PermissibleSubcommand;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.PEconomyPlugin;

public class CommandReload extends PermissibleSubcommand {

    private final PEconomyPlugin plugin;
    private final Messages messages;
    
    public CommandReload(PEconomyPlugin plugin, Messages messages) {
        super("peco.command.reload", messages);
        
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public void executeCommand(CommandSender sender, CommandArguments args) {
        plugin.reload();
        
        messages.getAndSend(sender, "reload-success");
    }
    
}
