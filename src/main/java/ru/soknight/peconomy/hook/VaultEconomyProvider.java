package ru.soknight.peconomy.hook;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.tool.Validate;
import ru.soknight.peconomy.api.BankingProvider;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.database.DatabaseManager;

public final class VaultEconomyProvider {

    private final Plugin plugin;

    private BankingProvider bankingProvider;
    private PEconomyService service;

    public VaultEconomyProvider(Plugin plugin) {
        this.plugin = plugin;
    }

    public BankingProvider getBankingProvider() {
        return bankingProvider;
    }

    public void registerBankingProvider(BankingProvider bankingProvider) {
        Validate.notNull(bankingProvider, "bankingProvider");
        this.bankingProvider = bankingProvider;
    }

    public void unregisterBankingProvider() {
        this.bankingProvider = BankingProvider.DEFAULT;
    }

    public void registerEconomyService(
            Configuration config,
            Messages messages,
            DatabaseManager databaseManager,
            CurrenciesManager currenciesManager
    ) {
        this.service = new PEconomyService(plugin, this, config, messages, databaseManager, currenciesManager);
        ServicesManager servicesManager = plugin.getServer().getServicesManager();
        servicesManager.register(Economy.class, service, plugin, ServicePriority.Highest);
    }

    public void unregisterEconomyService() {
        if(service == null)
            return;

        ServicesManager servicesManager = plugin.getServer().getServicesManager();
        servicesManager.unregister(service);
        this.service = null;
    }

}
