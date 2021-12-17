package ru.soknight.peconomy.event.wallet;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.event.initiator.Initiator;
import ru.soknight.peconomy.transaction.TransactionCause;

@Getter
public abstract class TransactionEvent extends WalletEvent {

    protected final @NotNull Initiator initiator;
    protected final @NotNull TransactionModel transaction;

    public TransactionEvent(
            @NotNull WalletModel wallet,
            @NotNull Initiator initiator,
            @NotNull TransactionModel transaction
    ) {
        super(wallet);
        this.initiator = initiator;
        this.transaction = transaction;
    }

    public @NotNull TransactionCause getCause() {
        return TransactionCause.getById(transaction.getCause());
    }

    public @NotNull String getCauseRaw() {
        return transaction.getCause();
    }

    public float getBalanceAfter() {
        return transaction.getBalanceAfter();
    }

    public float getBalanceBefore() {
        return transaction.getBalanceBefore();
    }

    public float getBalanceDelta() {
        return getBalanceAfter() - getBalanceBefore();
    }

    public boolean hasFailed() {
        return getCause().hasFailed();
    }

    public boolean isStaffOperation() {
        return getCause().isStaffOperation();
    }

    public boolean isInitiatedByCommandSender() {
        return initiator.isCommandSender();
    }

    public boolean isInitiatedByVault() {
        return initiator.isVaultEconomyConsumer();
    }

}
