package ru.soknight.peconomy.event.wallet.holding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.soknight.peconomy.database.model.WalletModel;

public final class HoldingUpdateByUsernameEvent extends HoldingUpdateEvent<String> {

    public HoldingUpdateByUsernameEvent(@NotNull WalletModel wallet, @Nullable String previous, @NotNull String current) {
        super(wallet, previous, current);
    }

    @Override
    public @NotNull String toString() {
        return "HoldingUpdateByUsernameEvent{" +
                "wallet=" + wallet +
                ", previous=" + previous +
                ", current=" + current +
                ", cancelled=" + cancelled +
                '}';
    }

}
