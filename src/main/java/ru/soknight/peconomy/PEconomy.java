package ru.soknight.peconomy;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.command.CommandBalance;
import ru.soknight.peconomy.command.CommandPay;
import ru.soknight.peconomy.command.CommandPeconomy;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.MessagesProvider;
import ru.soknight.peconomy.database.Database;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.hook.PEcoExpansion;
import ru.soknight.peconomy.hook.VaultEconomy;
import ru.soknight.peconomy.listener.PlayerJoinListener;

public class PEconomy extends JavaPlugin {

    private static PEcoAPI api;
    
    private Configuration config;
    private MessagesProvider messagesProvider;
    private Messages messages;
    
    private DatabaseManager databaseManager;
    private CurrenciesManager currenciesManager;
    
    @Override
    public void onEnable() {
        // configs initialization
        loadConfigurations();
        
        // database initialization
        try {
            Database database = new Database(this, config);
            this.databaseManager = new DatabaseManager(this, database);
        } catch (SQLException ex) {
            getLogger().severe("Couldn't initialize database connection!");
            ex.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        } catch (Exception ex) {
            getLogger().severe("Couldn't connect to database!");
            ex.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // currencies manager initialization
        this.currenciesManager = new CurrenciesManager(this, config);
        
        // commands executors initialization
        registerCommands();
        
        // event listeners initialization
        registerListeners();
        
        // hooking into some plugins
        hookInto();
        
        // PEconomy API initialization
        api = new PEcoAPI(databaseManager, currenciesManager);
        
        getLogger().info("Yep, I am ready!");
    }
    
    @Override
    public void onDisable() {
        if(databaseManager != null)
            databaseManager.shutdown();
    }
    
    private void hookInto() {
        try {
            // PlaceholdersAPI hook
            if(config.getBoolean("hooks.papi", true)) {
                if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    new PEcoExpansion(this, databaseManager);
                    getLogger().info("[Hooks] Successfully hooked into PlaceholderAPI.");
                } else {
                    getLogger().info("[Hooks] PlaceholdersAPI isn't installed, ignoring it.");
                }
            }
        
            // Vault hook
            if(config.getBoolean("hooks.vault.enabled", false)) {
                if(Bukkit.getPluginManager().isPluginEnabled("Vault")) {
                    new VaultEconomy(this, config, messages, databaseManager, currenciesManager);
                    getLogger().info("[Hooks] Successfylly hooked into Vault.");
                } else {
                    getLogger().info("[Hooks] Vault is not installed, ignoring it.");
                }
            }
        } catch (Exception ignored) {}
    }
    
    private void loadConfigurations() {
        this.config = new Configuration(this, "config.yml");
        this.config.refresh();
        
        this.messagesProvider = new MessagesProvider(this, config);
        this.messages = messagesProvider.getMessages();
    }
    
    private void registerCommands() {
        new CommandPeconomy(this, config, messages, databaseManager, currenciesManager).register(this, true);
        new CommandBalance(config, messages, databaseManager, currenciesManager).register(this, true);
        new CommandPay(config, messages, databaseManager, currenciesManager).register(this, true);
    }
    
    private void registerListeners() {
        new PlayerJoinListener(this, databaseManager, currenciesManager);
    }
    
    public void reload() {
        config.refresh();
        messagesProvider.update(config);
        
        currenciesManager.refreshCurrencies();
        
        registerCommands();
    }
    
    /**
     * Gets initialized API instance or null if PEconomy wasn't initialized fully
     * @return PEconomy API instance (may be null)
     */
    public static PEcoAPI getAPI() {
        return api;
    }
    
}
