package ru.soknight.peconomy.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.database.model.TransactionModel;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class Notification {

    private final String walletHolder;
    private final String currencyId;
    private final float balanceBefore;
    private final float balanceAfter;
    private final String operator;
    private final NotificationType type;

    public static @NotNull Notification fromTransaction(@NotNull TransactionModel transaction, @NotNull NotificationType notificationType) {
        String walletHolder = transaction.getWalletHolder();
        String currencyId = transaction.getCurrency();
        float balanceBefore = transaction.getBalanceBefore();
        float balanceAfter = transaction.getBalanceAfter();
        String operator = transaction.getOperator();
        return new Notification(walletHolder, currencyId, balanceBefore, balanceAfter, operator, notificationType);
    }

    public Player getHolderAsPlayer() {
        return Bukkit.getPlayer(walletHolder);
    }

    public boolean validate() {
        return notNull(walletHolder) && notEmpty(currencyId) && balanceBefore >= 0F && balanceAfter >= 0F && notNull(type);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Notification that = (Notification) o;
        return Float.compare(that.balanceBefore, balanceBefore) == 0 &&
                Float.compare(that.balanceAfter, balanceAfter) == 0 &&
                Objects.equals(walletHolder, that.walletHolder) &&
                Objects.equals(currencyId, that.currencyId) &&
                Objects.equals(operator, that.operator) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(walletHolder, currencyId, balanceBefore, balanceAfter, operator, type);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "walletHolder=" + walletHolder +
                ", currencyId='" + currencyId + '\'' +
                ", balanceBefore=" + balanceBefore +
                ", balanceAfter=" + balanceAfter +
                ", operator='" + operator + '\'' +
                ", type=" + type +
                '}';
    }

    private boolean notNull(Object object) {
        return object != null;
    }

    private boolean notEmpty(String string) {
        return string != null && !string.isEmpty();
    }

}
