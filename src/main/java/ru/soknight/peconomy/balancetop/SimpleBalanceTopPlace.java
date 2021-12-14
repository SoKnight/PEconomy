package ru.soknight.peconomy.balancetop;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.soknight.peconomy.database.model.WalletModel;

import java.util.Objects;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleBalanceTopPlace implements BalanceTopPlace {

    private final WalletModel wallet;
    private final String currencyId;
    private int positionIndex;

    public static @NotNull SimpleBalanceTopPlace create(@Nullable WalletModel wallet, @NotNull String currencyId, int positionIndex) {
        return new SimpleBalanceTopPlace(wallet, currencyId, positionIndex);
    }

    @Override
    public int getPosition() {
        return positionIndex + 1;
    }

    @Override
    public boolean isEmpty() {
        return wallet == null;
    }

    @Override
    public @NotNull String getWalletHolder() {
        if(isEmpty())
            throw new IllegalStateException("this balance top place is empty!");

        return wallet.getPlayerName();
    }

    @Override
    public float getWalletBalance() {
        if(isEmpty())
            throw new IllegalStateException("this balance top place is empty!");

        return wallet.getAmount(currencyId);
    }

    public void updatePosition(int positionIndex) {
        this.positionIndex = positionIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleBalanceTopPlace that = (SimpleBalanceTopPlace) o;
        return positionIndex == that.positionIndex &&
                Objects.equals(wallet, that.wallet) &&
                Objects.equals(currencyId, that.currencyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wallet, currencyId, positionIndex);
    }

    @Override
    public @NotNull String toString() {
        return "BalanceTopPlace{" +
                "wallet=" + wallet +
                ", empty=" + isEmpty() +
                ", currencyId='" + currencyId + '\'' +
                ", positionIndex=" + positionIndex +
                '}';
    }

}
