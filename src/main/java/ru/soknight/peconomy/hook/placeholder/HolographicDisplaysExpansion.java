package ru.soknight.peconomy.hook.placeholder;

import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.placeholder.GlobalPlaceholder;
import me.filoghost.holographicdisplays.api.placeholder.IndividualPlaceholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.peconomy.PEconomy;
import ru.soknight.peconomy.configuration.CurrenciesManager;
import ru.soknight.peconomy.database.DatabaseManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class HolographicDisplaysExpansion extends AbstractExpansion {

    private final Configuration config;
    private final HolographicDisplaysAPI api;

    private final Map<String, GlobalPlaceholder> globalPlaceholders;
    private final Map<String, IndividualPlaceholder> individualPlaceholders;

    private int refreshIntervalTicks;

    public HolographicDisplaysExpansion(
            PEconomy plugin,
            Configuration config,
            DatabaseManager databaseManager,
            CurrenciesManager currenciesManager
    ) {
        super(plugin, databaseManager, currenciesManager);

        this.config = config;
        this.api = HolographicDisplaysAPI.get(plugin);

        this.globalPlaceholders = new LinkedHashMap<>();
        this.individualPlaceholders = new LinkedHashMap<>();
        createDefaultPlaceholders();

        registerIfNotRegisteredYet(true);
    }

    @Override
    public boolean isRegistered() {
        if (api.getRegisteredPlaceholders().isEmpty())
            return false;

        for (String key : globalPlaceholders.keySet())
            if (!api.isRegisteredPlaceholder(key))
                return false;

        for (String key : individualPlaceholders.keySet())
            if (!api.isRegisteredPlaceholder(key))
                return false;

        return true;
    }

    @Override
    public void register() {
        this.refreshIntervalTicks = config.getInt("hooks.hdapi.refresh-interval", 100);

        globalPlaceholders.forEach(api::registerGlobalPlaceholder);
        individualPlaceholders.forEach(api::registerIndividualPlaceholder);

        plugin.getLogger().info("Registered custom placeholders for HolographicDisplays.");
    }

    @Override
    public void unregister() {
        api.unregisterPlaceholders();
    }

    private void createDefaultPlaceholders() {
        createIndividualPlaceholder("peco_balance", this::getBalance);
        createGlobalPlaceholder("peco_top", this::getBalanceTopPlace);
        createIndividualPlaceholder("peco_has", this::hasAmount);
        createIndividualPlaceholder("peco_toppn", this::getPlayerBalanceTopPlacePosition);
        createIndividualPlaceholder("peco_toppf", this::getPlayerBalanceTopPlaceFormatted);
    }

    private void createGlobalPlaceholder(String key, Function<String[], Object> replacer) {
        globalPlaceholders.put(key, new GlobalPlaceholder() {
            @Override
            public @Nullable String getReplacement(@Nullable String argument) {
                return String.valueOf(replacer.apply(argument != null ? argument.split(", ") : new String[0]));
            }

            @Override
            public int getRefreshIntervalTicks() {
                return refreshIntervalTicks;
            }
        });
    }

    private void createIndividualPlaceholder(String key, BiFunction<Player, String[], Object> replacer) {
        individualPlaceholders.put(key, new IndividualPlaceholder() {
            @Override
            public @Nullable String getReplacement(@NotNull Player player, @Nullable String argument) {
                return String.valueOf(replacer.apply(player, argument != null ? argument.split(", ") : new String[0]));
            }

            @Override
            public int getRefreshIntervalTicks() {
                return refreshIntervalTicks;
            }
        });
    }

}
