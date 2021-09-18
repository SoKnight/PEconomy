package ru.soknight.peconomy.event.payment;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;

@Getter
@Setter
public final class PaymentPrepareEvent extends PaymentEvent implements Cancellable {

    private boolean cancelled;
    private boolean quiet;

    public PaymentPrepareEvent(
            @NotNull Player sender,
            @NotNull OfflinePlayer receiver,
            @NotNull WalletModel senderWallet,
            @NotNull WalletModel receiverWallet,
            @NotNull TransactionModel senderTransaction,
            @NotNull TransactionModel receiverTransaction,
            float transferringAmount
    ) {
        super(sender, receiver, senderWallet, receiverWallet, senderTransaction, receiverTransaction, transferringAmount);
    }

    @Override
    public @NotNull String toString() {
        return "PaymentPrepareEvent{" +
                "sender=" + sender +
                ", receiver=" + receiver +
                ", senderWallet=" + senderWallet +
                ", receiverWallet=" + receiverWallet +
                ", senderTransaction=" + senderTransaction +
                ", receiverTransaction=" + receiverTransaction +
                ", transferringAmount=" + transferringAmount +
                ", cancelled=" + cancelled +
                ", quiet=" + quiet +
                '}';
    }

}
