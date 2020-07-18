package ru.soknight.peconomy.command.validation;

import ru.soknight.lib.validation.CommandExecutionData;
import ru.soknight.lib.validation.ValidationResult;
import ru.soknight.lib.validation.validator.Validator;
import ru.soknight.peconomy.database.DatabaseManager;

public class WalletValidator implements Validator {

	private final DatabaseManager databaseManager;
	private final String message;
	
	private final ValidationResult passed;
	private final ValidationResult skipped;
	
	public WalletValidator(DatabaseManager databaseManager, String message) {
		this.databaseManager = databaseManager;
		this.message = message;
		
		this.passed = new ValidationResult(true);
		this.skipped = new ValidationResult(false);
	}
	
	@Override
	public ValidationResult validate(CommandExecutionData data) {
		if(!(data instanceof WalletExecutionData))
			return skipped;
		
		WalletExecutionData richdata = (WalletExecutionData) data;
		String owner = richdata.getOwner();
		
		ValidationResult failed = new ValidationResult(false, message.replace("%player%", owner));
		if(owner == null || owner.equals("")) return failed;
		
		return databaseManager.hasWallet(owner) ? passed : failed;
	}
	
}
