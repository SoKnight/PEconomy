package ru.soknight.peconomy.command.sub;

import org.bukkit.command.CommandSender;

import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.ExtendedSubcommandExecutor;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.validation.validator.PermissionValidator;
import ru.soknight.lib.validation.validator.Validator;
import ru.soknight.peconomy.PEconomy;

public class CommandReload extends ExtendedSubcommandExecutor {

	private final PEconomy plugin;
	private final Messages messages;
	
	public CommandReload(PEconomy plugin, Messages messages) {
		super(messages);
		
		this.plugin = plugin;
		this.messages = messages;
		
		String permmsg = messages.get("error.no-permissions");
		
		Validator permval = new PermissionValidator("peco.command.reload", permmsg);
		
		super.addValidators(permval);
	}

	@Override
	public void executeCommand(CommandSender sender, CommandArguments args) {
		if(!validateExecution(sender, args)) return;
		
		plugin.refresh();
		
		messages.getAndSend(sender, "reload-success");
	}
	
}
