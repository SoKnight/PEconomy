package ru.soknight.peconomy.commands;

public interface SubCommand {
    
    void execute();

    boolean hasPermission();
    
    boolean isPlayerRequired();
    
    boolean isPlayerInDatabase(String name);
    
    boolean argIsInteger(String arg);
    
    boolean isCorrectWallet(String wallet);
    
    boolean isCorrectUsage();
	
}
