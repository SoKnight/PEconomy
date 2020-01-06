package ru.soknight.peconomy.commands;

import org.bukkit.command.CommandSender;

import ru.soknight.peconomy.files.Config;
import ru.soknight.peconomy.files.Messages;

public class CommandReload {

	public static void execute(CommandSender sender) {
		Config.refresh();
		Messages.refresh();
		sender.sendMessage(Messages.getMessage("reload-success"));
		return;
	}
	
}
