package ru.soknight.peconomy.task;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.soknight.lib.task.async.AsyncPluginTask;
import ru.soknight.peconomy.balancetop.BalanceTop;
import ru.soknight.peconomy.configuration.CurrencyInstance;

@Getter
public final class BalanceTopUpdateTask extends AsyncPluginTask {

    private final CurrencyInstance currency;
    private final BalanceTop balanceTop;

    public BalanceTopUpdateTask(
            @NotNull Plugin plugin,
            @NotNull CurrencyInstance currency
    ) {
        super(plugin);
        this.currency = currency;
        this.balanceTop = currency.getBalanceTop();
    }

    @Override
    public long getPeriod() {
        return currency.getBalanceTopSetup().getUpdatePeriod();
    }

    @Override
    public synchronized void run() {
        balanceTop.refresh();
    }

}
