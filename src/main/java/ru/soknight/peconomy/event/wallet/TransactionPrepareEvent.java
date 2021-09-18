package ru.soknight.peconomy.event.wallet;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.event.initiator.Initiator;

@Getter
@Setter
public final class TransactionPrepareEvent extends TransactionEvent implements Cancellable {

    private boolean cancelled;
    private boolean quiet;

    public TransactionPrepareEvent(
            @NotNull WalletModel wallet,
            @NotNull Initiator initiator,
            @NotNull TransactionModel transaction
    ) {
        super(wallet, initiator, transaction);
    }

    public void makeFailed() {
        transaction.makeFailed();
    }

    @Override
    public @NotNull String toString() {
        return "WalletTransactionPrepareEvent{" +
                "wallet=" + wallet +
                ", initiator=" + initiator +
                ", transaction=" + transaction +
                ", cancelled=" + cancelled +
                ", quiet=" + quiet +
                '}';
    }

}
