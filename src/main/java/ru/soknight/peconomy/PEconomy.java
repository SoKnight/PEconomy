package ru.soknight.peconomy;

import com.j256.ormlite.field.DataPersisterManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.database.Database;
import ru.soknight.lib.database.migration.annotation.ActualSchemaVersion;
import ru.soknight.lib.database.migration.runtime.DataConverters;
import ru.soknight.peconomy.api.PEconomyAPI;
import ru.soknight.peconomy.command.CommandBalance;
import ru.soknight.peconomy.command.CommandPay;
import ru.soknight.peconomy.command.CommandPeconomy;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.configuration.MessagesProvider;
import ru.soknight.peconomy.database.DatabaseManager;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.database.model.deprecated.TransactionModelV1;
import ru.soknight.peconomy.database.model.deprecated.WalletModelV1;
import ru.soknight.peconomy.database.persister.LocalDateTimePersister;
import ru.soknight.peconomy.format.Formatter;
import ru.soknight.peconomy.hook.PEconomyExpansion;
import ru.soknight.peconomy.hook.VaultEconomyProvider;
import ru.soknight.peconomy.listener.PapiExpansionsLoadListener;
import ru.soknight.peconomy.listener.PlayerJoinListener;

import java.sql.SQLException;

@SuppressWarnings("deprecation")
@ActualSchemaVersion(2)
public final class PEconomy extends JavaPlugin {

    private static PEconomyAPI apiInstance;
    
    private Configuration config;
    private MessagesProvider messagesProvider;
    private Messages messages;
    
    private DatabaseManager databaseManager;
    private CurrenciesManager currenciesManager;

    private VaultEconomyProvider economyProvider;

    @Getter
    private Formatter formatter;

    @Override
    public void onEnable() {
        // configurations initialization
        loadConfigurations();

        // database initialization
        try {
            DataPersisterManager.registerDataPersisters(LocalDateTimePersister.getSingleton());

            Database database = new Database(this, config)
                    .registerDataConverter(DataConverters.wrap(WalletModelV1.getConverter()))
                    .registerDataConverter(DataConverters.wrap(TransactionModelV1.getConverter()))
                    .performMigrations()
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

        // formatter initialization
        this.formatter = new Formatter(this, config, messages);
        this.formatter.reload();

        // Vault economy provider initialization
        this.economyProvider = new VaultEconomyProvider(this);

        // commands executors initialization
        registerCommands();

        // event listeners initialization
        registerListeners();

        // hooking into some plugins
        hookInto();

        // PEconomy API initialization
        apiInstance = new SimplePEconomyAPI(databaseManager, currenciesManager, economyProvider, formatter);

        getLogger().info("Let's go! (づ｡◕‿‿◕｡)づ");
    }
    
    @Override
    public void onDisable() {
        // shutting down balance top updating tasks
        if(currenciesManager != null)
            currenciesManager.shutdownUpdatingTasks();

        // unregistering Vault hook
        if(economyProvider != null)
            economyProvider.unregisterEconomyService();

        // shutting down database connection
        if(databaseManager != null)
            databaseManager.shutdown();
    }

    /**
     * Gets initialized API instance or null if PEconomy wasn't initialized fully
     * @return PEconomy API instance (maybe null)
     */
    public static @NotNull PEconomyAPI getAPI() {
        if(apiInstance == null)
            throw new IllegalStateException("PEconomy API is unavailable now, probably the plugin was not initialized correctly!");

        return apiInstance;
    }
    
    private void hookInto() {
        // PlaceholderAPI hook
        if(config.getBoolean("hooks.papi", true)) {
            if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                PEconomyExpansion expansion = new PEconomyExpansion(this, databaseManager, currenciesManager);
                new PapiExpansionsLoadListener(this, expansion);
            }
        }

        // Vault hook
        if(config.getBoolean("hooks.vault.enabled", false)) {
            if(Bukkit.getPluginManager().isPluginEnabled("Vault")) {
                this.economyProvider.registerEconomyService(config, messages, databaseManager, currenciesManager);
                getLogger().info("Registered as Vault economy provider!");
            }
        }
    }
    
    private void loadConfigurations() {
        this.config = new Configuration(this, "config.yml");
        this.config.refresh();
        
        this.messagesProvider = new MessagesProvider(this, config);
        this.messages = messagesProvider.getMessages();
    }
    
    private void registerCommands() {
        new CommandPeconomy(this, messages, databaseManager, currenciesManager);
        new CommandBalance(this, config, messages, databaseManager, currenciesManager);
        new CommandPay(this, messages, databaseManager, currenciesManager);
    }
    
    private void registerListeners() {
        // --- bukkit events listeners
        new PlayerJoinListener(this, databaseManager, currenciesManager);
    }

    public void reload() {
        config.refresh();
        messagesProvider.update(config);

        // currencies reloading
        currenciesManager.refreshCurrencies();

        // formatter updating
        formatter.reload();
    }
    
}
