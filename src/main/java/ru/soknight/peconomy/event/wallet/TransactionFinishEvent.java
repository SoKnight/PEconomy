package ru.soknight.peconomy.event.wallet;

import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.event.initiator.Initiator;

public final class TransactionFinishEvent extends TransactionEvent {

    public TransactionFinishEvent(
            @NotNull WalletModel wallet,
            @NotNull Initiator initiator,
            @NotNull TransactionModel transaction
    ) {
        super(wallet, initiator, transaction);
    }

    @Override
    public @NotNull String toString() {
        return "WalletTransactionFinishEvent{" +
                "wallet=" + wallet +
                ", initiator=" + initiator +
                ", transaction=" + transaction +
                '}';
    }

}
