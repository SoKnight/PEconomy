package ru.soknight.peconomy.configuration;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import ru.soknight.lib.configuration.AbstractConfiguration;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.task.PluginTask;
import ru.soknight.peconomy.balancetop.BalanceTop;
import ru.soknight.peconomy.balancetop.BalanceTopPlace;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.api.PEconomyAPI;
import ru.soknight.peconomy.task.BalanceTopUpdateTask;

import java.util.*;

@Getter
public final class CurrenciesManager extends AbstractConfiguration {

    private final Configuration config;
    private final Map<String, CurrencyInstance> currencies;
    private final Map<String, PluginTask> updateTasks;
    private CurrencyInstance vaultCurrency;
    
    public CurrenciesManager(@NotNull PEconomy plugin, @NotNull Configuration config) {
        super(plugin, "currencies.yml");

        this.config = config;
        this.currencies = new HashMap<>();
        this.updateTasks = new HashMap<>();
        
        refreshCurrencies();
    }

    public void shutdownUpdatingTasks() {
        updateTasks.values().forEach(PluginTask::shutdown);
    }
    
    public void refreshCurrencies() {
        shutdownUpdatingTasks();

        super.refresh();
        
        this.currencies.clear();
        this.updateTasks.clear();
        
        ConfigurationSection currenciesConfig = getFileConfig().getConfigurationSection("currencies");
        Set<String> keys = currenciesConfig.getKeys(false);
        if (keys.isEmpty())
            return;
        
        keys.forEach(currencyId -> {
            ConfigurationSection currencyConfig = currenciesConfig.getConfigurationSection(currencyId);
            if (!currencyConfig.isString("symbol")) {
                getPlugin().getLogger().warning("Couldn't find the 'symbol' parameter for currency '" + currencyId + "', skipped.");
                return;
            }

            String name = colorize(currencyConfig.getString("name", currencyId));
            String symbol = colorize(currencyConfig.getString("symbol"));
            
            float limit = (float) currencyConfig.getDouble("max-amount", 0F);
            float newbie = (float) currencyConfig.getDouble("newbie-amount", 0F);

            boolean visible = currencyConfig.getBoolean("visible", true);
            boolean transferable = currencyConfig.getBoolean("transferable", true);

            BalanceTopSetup balanceTopSetup = parseBalanceTopSetup(currencyConfig.getConfigurationSection("balance-top"));
            BalanceTop balanceTop = null;

            if(balanceTopSetup != null && balanceTopSetup.isValid())
                balanceTop = BalanceTop.create(
                        getPlugin(),
                        currencyId,
                        balanceTopSetup.getMaxSize(),
                        place -> formatPlace(balanceTopSetup, place)
                );

            CurrencyInstance currency = new CurrencyInstance(currencyId, name, symbol, limit, newbie, visible, transferable, balanceTopSetup, balanceTop);
            currencies.put(currencyId, currency);

            if(balanceTop != null) {
                PluginTask updateTask = new BalanceTopUpdateTask(getPlugin(), currency, balanceTop);
                updateTask.start();
                updateTasks.put(currencyId, updateTask);
            }
        });
        
        if (config.getBoolean("hooks.vault.enabled")) {
            String vault = getFileConfig().getString("vault.currency");
            if (vault == null) {
                getPlugin().getLogger().info("Vault default currency is not specified, ignoring it.");
            } else if (!currencies.containsKey(vault)) {
                getPlugin().getLogger().severe("Failed to set vault default currency: Unknown currency '" + vault + "'.");
            } else {
                this.vaultCurrency = currencies.get(vault);
                getPlugin().getLogger().info("Currency '" + vault + "' will be used for Vault economy.");
            }
        }

        getPlugin().getLogger().info("Loaded " + currencies.size() + " currencies.");
    }

    private BalanceTopSetup parseBalanceTopSetup(ConfigurationSection config) {
        if(config == null)
            return null;

        boolean enabled = config.getBoolean("enabled", false);
        int updatePeriod = config.getInt("update-period", 0);
        int maxSize = config.getInt("max-size", 0);
        String formatExist = colorize(config.getString("format.exist", ""));
        String formatEmpty = colorize(config.getString("format.empty", ""));

        return new BalanceTopSetup(enabled, updatePeriod, maxSize, formatExist, formatEmpty);
    }
    
    public @Nullable CurrencyInstance getCurrency(String id) {
        return currencies.get(id);
    }

    public @NotNull @UnmodifiableView Set<String> getCurrenciesIDs() {
        return Collections.unmodifiableSet(currencies.keySet());
    }

    public @NotNull @UnmodifiableView Collection<CurrencyInstance> getCurrencies() {
        return Collections.unmodifiableCollection(currencies.values());
    }
    
    public boolean isCurrency(String id) {
        return currencies.containsKey(id);
    }

    public String formatPlace(BalanceTopSetup setup, @NotNull BalanceTopPlace place) {
        int position = place.getPosition();
        int positionIndex = place.getPositionIndex();

        if(place.isEmpty()) {
            String format = setup.getFormatEmpty();
            if(format.isEmpty())
                return format;

            return format(format,
                    "%position%", position,
                    "%position_index%", positionIndex
            );
        } else {
            String format = setup.getFormatExist();
            if(format.isEmpty())
                return format;

            return format(format,
                    "%position%", position,
                    "%position_index%", positionIndex,
                    "%player%", place.getWalletHolder(),
                    "%balance%", PEconomyAPI.get().getFormatter().formatAmount(place.getWalletBalance())
            );
        }
    }
    
}
