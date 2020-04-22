package ru.soknight.peconomy.command.validation;

import org.bukkit.command.CommandSender;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.soknight.lib.validation.CommandExecutionData;

@Getter
@AllArgsConstructor
public class WalletExecutionData implements CommandExecutionData {

	@Getter private final CommandSender sender;
	@Getter private final String[] args;

	@Getter private final String owner;
	@Getter private final String currency;
	private final String amount;
	
	public String getAmountAsString() {
		return this.amount;
	}
	
	public Float getAmount() {
		try {
			return Float.parseFloat(this.amount);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
}
