package ru.soknight.peconomy.event.payment;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;

public final class PaymentFinishEvent extends PaymentEvent {

    public PaymentFinishEvent(
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
        return "PaymentFinishEvent{" +
                "sender=" + sender +
                ", receiver=" + receiver +
                ", senderWallet=" + senderWallet +
                ", receiverWallet=" + receiverWallet +
                ", senderTransaction=" + senderTransaction +
                ", receiverTransaction=" + receiverTransaction +
                ", transferringAmount=" + transferringAmount +
                '}';
    }

}
