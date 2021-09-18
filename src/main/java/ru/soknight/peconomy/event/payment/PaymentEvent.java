package ru.soknight.peconomy.event.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.database.model.TransactionModel;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.event.BukkitEvent;

@Getter
@AllArgsConstructor
public abstract class PaymentEvent extends BukkitEvent {

    protected final @NotNull Player sender;
    protected final @NotNull OfflinePlayer receiver;

    protected final @NotNull
    WalletModel senderWallet;
    protected final @NotNull WalletModel receiverWallet;

    protected final @NotNull
    TransactionModel senderTransaction;
    protected final @NotNull TransactionModel receiverTransaction;

    protected final float transferringAmount;

}
