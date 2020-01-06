package ru.soknight.peconomy.utils;

import ru.soknight.peconomy.files.Messages;

public class HelpCommand {

	private String format, command, description;
	
	public HelpCommand(String command, String description) {
		this.format = Messages.format;
		this.command = command;
		this.description = Messages.getRawMessage("help-descriptions." + description);
	}
	
	public HelpCommand(String command, String description, String... args) {
		this.format = Messages.format;
		this.command = command;
		this.description = Messages.getRawMessage("help-descriptions." + description);
		for(String a : args)
			this.command += " " + Messages.getMessage("help-nodes." + a);
	}
	
	@Override
	public String toString() {
		return format.replace("%cmd%", command).replace("%desc%", description);
	}
	
}
