package ru.soknight.peconomy.commands;

import org.bukkit.command.CommandSender;

import ru.soknight.peconomy.files.Config;
import ru.soknight.peconomy.files.Messages;

public class CommandReload extends AbstractSubCommand {
	
	private final CommandSender sender;
	
	public CommandReload(CommandSender sender) {
		super(sender, null, "peco.reload", 1);
		this.sender = sender;
	}

	@Override
	public void execute() {
		if(!hasPermission()) return;
		
		Config.refresh();
		Messages.refresh();
		sender.sendMessage(Messages.getMessage("reload-success"));
		return;
	}
	
}
