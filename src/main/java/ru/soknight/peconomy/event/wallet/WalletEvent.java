package ru.soknight.peconomy.event.wallet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.event.BukkitEvent;

@Getter
@AllArgsConstructor
public abstract class WalletEvent extends BukkitEvent {

    protected final @NotNull WalletModel wallet;

}
