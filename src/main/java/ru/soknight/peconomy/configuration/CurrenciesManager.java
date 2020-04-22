package ru.soknight.peconomy.configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;

import lombok.Getter;
import ru.soknight.lib.configuration.AbstractConfiguration;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.peconomy.PEconomy;

public class CurrenciesManager extends AbstractConfiguration {

	private Map<String, CurrencyInstance> currencies;
	@Getter private CurrencyInstance vaultCurrency;
	
	private final Configuration config;
	private final Logger logger;
	
	public CurrenciesManager(PEconomy plugin, Configuration config) {
		super(plugin, "currencies.yml");
		this.currencies = new HashMap<>();
		
		this.config = config;
		this.logger = plugin.getLogger();
		refreshCurrencies();
	}
	
	public void refreshCurrencies() {
		super.refresh();
		
		this.currencies = new HashMap<>();
		
		ConfigurationSection section = getFileConfig().getConfigurationSection("currencies");
		Set<String> keys = section.getKeys(false);
		
		if(keys.isEmpty()) {
			logger.severe("File currencies.yml is empty, there are no currencies to load.");
			return;
		}
		
		keys.forEach(id -> {
			ConfigurationSection subsection = section.getConfigurationSection(id);
			
			String symbol = subsection.getString("symbol");
			if(symbol == null) {
				logger.severe("Couldn't find the 'symbol' parameter for currency '" + id + "', loading skipped.");
				return;
			}
			
			float limit = (float) subsection.getDouble("max-amount", 0F);
			float newbie = (float) subsection.getDouble("newbie-amount", 0F);
			
			CurrencyInstance currency = new CurrencyInstance(id, symbol, limit, newbie);
			this.currencies.put(id, currency);
		});
		
		if(config.getBoolean("hooks.vault")) {
			String vault = getFileConfig().getString("vault.currency");
			if(vault == null)
				logger.info("Vault default currency is not specified, ignoring it.");
			else if(!currencies.containsKey(vault))
				logger.severe("Failed to set vault default currency: Unknown currency '" + vault + "'.");
			else {
				this.vaultCurrency = currencies.get(vault);
				logger.info("Currency '" + vault + "' will be used for Vault economy.");
			}
		}
		
		logger.info("Loaded " + this.currencies.size() + " currencies.");
	}
	
	public CurrencyInstance getCurrency(String id) {
		return this.currencies.get(id);
	}
	
	public Set<String> getCurrenciesIDs() {
		return this.currencies.keySet();
	}
	
	public Collection<CurrencyInstance> getCurrencies() {
		return this.currencies.values();
	}
	
	public boolean isCurrency(String id) {
		return this.currencies.containsKey(id);
	}
	
}
