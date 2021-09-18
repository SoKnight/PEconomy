package ru.soknight.peconomy.event.wallet;

import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.database.model.WalletModel;

public final class WalletCreateEvent extends WalletEvent {

    public WalletCreateEvent(@NotNull WalletModel wallet) {
        super(wallet);
    }

    @Override
    public @NotNull String toString() {
        return "WalletCreateEvent{" +
                "wallet=" + wallet +
                '}';
    }

}
