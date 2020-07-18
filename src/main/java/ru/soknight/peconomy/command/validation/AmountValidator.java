package ru.soknight.peconomy.command.validation;

import ru.soknight.lib.validation.CommandExecutionData;
import ru.soknight.lib.validation.ValidationResult;
import ru.soknight.lib.validation.validator.Validator;

public class AmountValidator implements Validator {

	private final String message;
	
	private final ValidationResult passed;
	private final ValidationResult skipped;
	
	public AmountValidator(String message) {
		this.message = message;
		
		this.passed = new ValidationResult(true);
		this.skipped = new ValidationResult(false);
	}
	
	@Override
	public ValidationResult validate(CommandExecutionData data) {
		if(!(data instanceof WalletExecutionData))
			return skipped;
		
		WalletExecutionData richdata = (WalletExecutionData) data;
		String amount = richdata.getAmountAsString();
		
		ValidationResult failed = new ValidationResult(false, message.replace("%argument%", amount));
		if(amount == null || amount.equals("")) return failed;
		
		boolean validated = false;
		
		try {
			float a = Float.parseFloat(amount);
			if(a > 0) validated = true;
		} catch (NumberFormatException ignored) {}
		
		return validated ? passed : failed;
	}
	
}
