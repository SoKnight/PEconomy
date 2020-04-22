package ru.soknight.peconomy.command.validation;

import ru.soknight.lib.validation.CommandExecutionData;
import ru.soknight.lib.validation.ValidationResult;
import ru.soknight.lib.validation.validator.Validator;
import ru.soknight.peconomy.configuration.CurrenciesManager;

public class CurrencyValidator implements Validator {

	private final CurrenciesManager currenciesManager;
	private final String message;
	
	private final ValidationResult passed;
	private final ValidationResult skipped;
	
	public CurrencyValidator(CurrenciesManager currenciesManager, String message) {
		this.currenciesManager = currenciesManager;
		this.message = message;
		
		this.passed = new ValidationResult(true);
		this.skipped = new ValidationResult(false);
	}
	
	@Override
	public ValidationResult validate(CommandExecutionData data) {
		if(!(data instanceof WalletExecutionData))
			return skipped;
		
		WalletExecutionData richdata = (WalletExecutionData) data;
		String currency = richdata.getCurrency();
		
		ValidationResult failed = new ValidationResult(false, message.replace("%currency%", currency));
		if(currency == null || currency.equals("")) return failed;
		
		return currenciesManager.isCurrency(currency.toLowerCase()) ? passed : failed;
	}

}
