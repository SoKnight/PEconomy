package ru.soknight.peconomy.balancetop.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.soknight.peconomy.balancetop.BalanceTop;
import ru.soknight.peconomy.balancetop.BalanceTopPlace;
import ru.soknight.peconomy.database.model.WalletModel;

import java.util.Comparator;
import java.util.List;

@FunctionalInterface
public interface BalanceTopPlacesProvider {

    @Nullable List<BalanceTopPlace> queryPlaces(@NotNull BalanceTop balanceTop, @NotNull Comparator<WalletModel> comparator);

}
