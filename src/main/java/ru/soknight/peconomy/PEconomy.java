package ru.soknight.peconomy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.database.Database;
import ru.soknight.peconomy.api.PEconomyAPI;
import ru.soknight.peconomy.command.CommandBalance;
import ru.soknight.peconomy.command.CommandPay;
import ru.soknight.peconomy.command.CommandPeconomy;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.MessagesProvider;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.format.AmountFormatter;
import ru.soknight.peconomy.hook.PEconomyExpansion;
import ru.soknight.peconomy.hook.VaultEconomyProvider;
import ru.soknight.peconomy.listener.PlayerJoinListener;

import java.sql.SQLException;

public class PEconomy extends JavaPlugin {

    private static PEconomyAPI apiInstance;
    
    private Configuration config;
    private MessagesProvider messagesProvider;
    private Messages messages;
    
    private DatabaseManager databaseManager;
    private CurrenciesManager currenciesManager;
    private VaultEconomyProvider economyProvider;
    
    @Override
    public void onEnable() {
        // configurations initialization
        loadConfigurations();
        
        // database initialization
        try {
            Database database = new Database(this, config)
                    .createTable(WalletModel.class)
                    .createTable(TransactionModel.class)
                    .complete();

            this.databaseManager = new DatabaseManager(this, database);
        } catch (SQLException ex) {
            getLogger().severe("Database connection cannot be established: " + ex.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        } catch (Exception ex) {
            getLogger().severe("Database cannot be initialized successfully!");
            ex.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // currencies manager initialization
        this.currenciesManager = new CurrenciesManager(this, config);

        // Vault economy provider initialization
        this.economyProvider = new VaultEconomyProvider(this);
        
        // commands executors initialization
        registerCommands();
        
        // event listeners initialization
        registerListeners();
        
        // hooking into some plugins
        hookInto();
        
        // PEconomy API initialization
        apiInstance = new PEconomyAPIImpl(databaseManager, currenciesManager, economyProvider);
        
        getLogger().info("Yep, I am ready!");
    }
    
    @Override
    public void onDisable() {
        if(economyProvider != null)
            economyProvider.unregisterEconomyService();
        if(databaseManager != null)
            databaseManager.shutdown();
    }
    
    private void hookInto() {
        try {
            // PlaceholdersAPI hook
            if(config.getBoolean("hooks.papi", true)) {
                if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    new PEconomyExpansion(this, databaseManager);
                } else {
                    getLogger().info("PlaceholdersAPI isn't installed, ignoring it...");
                }
            }
        
            // Vault hook
            if(config.getBoolean("hooks.vault.enabled", false)) {
                if(Bukkit.getPluginManager().isPluginEnabled("Vault")) {
                    this.economyProvider.registerEconomyService(config, messages, databaseManager, currenciesManager);
                    getLogger().info("Registered as Vault economy provider!");
                } else {
                    getLogger().info("Vault is not installed, ignoring it...");
                }
            }
        } catch (Exception ignored) {}
    }
    
    private void loadConfigurations() {
        this.config = new Configuration(this, "config.yml");
        this.config.refresh();
        
        this.messagesProvider = new MessagesProvider(this, config);
        this.messages = messagesProvider.getMessages();

        updateAmountFormat();
    }
    
    private void registerCommands() {
        new CommandPeconomy(this, config, messages, databaseManager, currenciesManager).register(this, true);
        new CommandBalance(config, messages, databaseManager, currenciesManager).register(this, true);
        new CommandPay(config, messages, databaseManager, currenciesManager).register(this, true);
    }
    
    private void registerListeners() {
        new PlayerJoinListener(this, databaseManager, currenciesManager);
    }

    private void updateAmountFormat() {
        String format = config.getString("amount-format");
        AmountFormatter.setFormat(this, format);
    }

    public void reload() {
        config.refresh();
        messagesProvider.update(config);
        
        currenciesManager.refreshCurrencies();
        
        registerCommands();
        updateAmountFormat();
    }
    
    /**
     * Gets initialized API instance or null if PEconomy wasn't initialized fully
     * @return PEconomy API instance (may be null)
     */
    public static PEconomyAPI getAPI() {
        return apiInstance;
    }
    
}
