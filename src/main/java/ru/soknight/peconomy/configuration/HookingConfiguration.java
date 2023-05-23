package ru.soknight.peconomy.configuration;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.soknight.lib.configuration.Configuration;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 *  A {@link Configuration} that makes it possible to subscribe to
 * configuration updates throughout the plugin's life-cycle.
 */
public final class HookingConfiguration extends Configuration {
    private final Collection<Consumer<Configuration>> onRefreshedListeners;

    public HookingConfiguration(final @NotNull JavaPlugin plugin, final @NotNull String fileName) {
        super(plugin, fileName);
        onRefreshedListeners = new ConcurrentLinkedQueue<>(); /* preserve order and keep it thread-safe */
    }

    /**
     *  The given listener will be called when this configuration
     * object gets fully refreshed.
     *
     * @see #refresh()
     */
    public void subscribeOnRefreshed(final @NotNull Consumer<Configuration> listener) {
        onRefreshedListeners.add(listener);
    }

    @Override public void refresh() {
        super.refresh();
        for (final Consumer<Configuration> listener : onRefreshedListeners)
            listener.accept(this);
    }
}
