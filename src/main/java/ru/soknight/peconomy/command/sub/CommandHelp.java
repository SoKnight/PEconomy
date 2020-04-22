package ru.soknight.peconomy.command.sub;

import org.bukkit.command.CommandSender;

import ru.soknight.lib.command.ExtendedSubcommandExecutor;
import ru.soknight.lib.command.help.HelpMessage;
import ru.soknight.lib.command.help.HelpMessageFactory;
import ru.soknight.lib.command.help.HelpMessageItem;
import ru.soknight.lib.command.placeholder.Placeholder;
import ru.soknight.lib.command.placeholder.SimplePlaceholder;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.validation.validator.PermissionValidator;

public class CommandHelp extends ExtendedSubcommandExecutor {
	
	private final HelpMessage message;
	
	public CommandHelp(Messages messages) {
		super(messages);
		
		HelpMessageFactory factory = new HelpMessageFactory(messages, "peco.command.%command%");
		
		Placeholder pplayerreq = new SimplePlaceholder(messages, "player");
		Placeholder pplayeropt = new SimplePlaceholder(messages, "player-optional");
		Placeholder pcurrency = new SimplePlaceholder(messages, "currency");
		Placeholder pamount = new SimplePlaceholder(messages, "amount");
		Placeholder ppage = new SimplePlaceholder(messages, "page");
		Placeholder pid = new SimplePlaceholder(messages, "id");
		
		String d = "help.descriptions.";
		HelpMessageItem help = new HelpMessageItem("peco help", messages, d + "help");
		HelpMessageItem history = new HelpMessageItem("peco history", messages, d + "history", pplayerreq, ppage);
		HelpMessageItem info = new HelpMessageItem("peco info", messages, d + "info", pid);
		HelpMessageItem add = new HelpMessageItem("peco add", messages, d + "add", pplayerreq, pamount, pcurrency);
		HelpMessageItem set = new HelpMessageItem("peco set", messages, d + "set", pplayerreq, pamount, pcurrency);
		HelpMessageItem reset = new HelpMessageItem("peco reset", messages, d + "reset", pplayerreq, pcurrency);
		HelpMessageItem take = new HelpMessageItem("peco take", messages, d + "take", pplayerreq, pamount, pcurrency);
		HelpMessageItem reload = new HelpMessageItem("peco reload", messages, d + "reload");
		HelpMessageItem balance = new HelpMessageItem("balance", messages, pplayeropt);
		HelpMessageItem pay = new HelpMessageItem("pay", messages, pplayerreq, pamount, pcurrency);
		
		factory.appendItems(true, help, history, info, add, set, reset, take, reload, balance, pay);
		
		this.message = factory.build();
		
		String permmsg = messages.get("error.no-permissions");
		
		PermissionValidator permval = new PermissionValidator("peco.command.help", permmsg);
		
		super.addValidators(permval);
	}

	@Override
	public void executeCommand(CommandSender sender, String[] args) {
		if(!validateExecution(sender, args)) return;
		
		message.send(sender);
	}
	
}
