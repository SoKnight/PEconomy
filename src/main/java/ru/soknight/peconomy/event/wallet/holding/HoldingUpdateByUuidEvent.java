package ru.soknight.peconomy.event.wallet.holding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.soknight.peconomy.database.model.WalletModel;

import java.util.UUID;

public final class HoldingUpdateByUuidEvent extends HoldingUpdateEvent<UUID> {

    public HoldingUpdateByUuidEvent(@NotNull WalletModel wallet, @Nullable UUID previous, @NotNull UUID current) {
        super(wallet, previous, current);
    }

    @Override
    public @NotNull String toString() {
        return "HoldingUpdateByUuidEvent{" +
                "wallet=" + wallet +
                ", previous=" + previous +
                ", current=" + current +
                ", cancelled=" + cancelled +
                '}';
    }

}
