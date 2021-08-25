package ru.soknight.peconomy.balancetop.function;

import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.balancetop.BalanceTop;
import ru.soknight.peconomy.balancetop.BalanceTopPlace;
import ru.soknight.peconomy.database.model.WalletModel;

import java.util.Comparator;
import java.util.Optional;

@FunctionalInterface
public interface BalanceTopPlaceFinder {

    Optional<BalanceTopPlace> findPlace(@NotNull WalletModel wallet, @NotNull BalanceTop balanceTop, @NotNull Comparator<WalletModel> comparator);

}
