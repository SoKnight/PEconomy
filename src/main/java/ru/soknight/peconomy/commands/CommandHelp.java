package ru.soknight.peconomy.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import ru.soknight.peconomy.files.Messages;

public class CommandHelp extends AbstractSubCommand {
	
	private final CommandSender sender;
	
	public CommandHelp(CommandSender sender) {
		super(sender, null, "peco.help", 1);
		this.sender = sender;
	}

	@Override
	public void execute() {
		if(!hasPermission()) return;
		
		String header = Messages.getRawMessage("help-header");
		String footer = Messages.getRawMessage("help-footer");
		List<String> body = new ArrayList<>();
		
		body.add(Messages.getHelpString("help").toString());
		if(sender.hasPermission("peco.balance")) 		body.add(Messages.getHelpString("balance").toString());
		if(sender.hasPermission("peco.balance.other")) 	body.add(Messages.getHelpString("balance-other").toString());
		if(sender.hasPermission("peco.pay")) 			body.add(Messages.getHelpString("pay").toString());
		if(sender.hasPermission("peco.add")) 			body.add(Messages.getHelpString("add").toString());
		if(sender.hasPermission("peco.set")) 			body.add(Messages.getHelpString("set").toString());
		if(sender.hasPermission("peco.reset")) 			body.add(Messages.getHelpString("reset").toString());
		if(sender.hasPermission("peco.take")) 			body.add(Messages.getHelpString("take").toString());
		if(sender.hasPermission("peco.reload")) 		body.add(Messages.getHelpString("reload").toString());
		
		sender.sendMessage(header);
		body.forEach(str -> sender.sendMessage(str));
		sender.sendMessage(footer);
		return;
	}
	
}
