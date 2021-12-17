package ru.soknight.peconomy.event.wallet.holding;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.soknight.lib.tool.Validate;
import ru.soknight.peconomy.database.model.WalletModel;
import ru.soknight.peconomy.event.wallet.WalletEvent;

import java.util.Objects;
import java.util.Optional;

@Getter
@Setter
public abstract class HoldingUpdateEvent<I> extends WalletEvent implements Cancellable {

    protected @Nullable I previous;
    protected @NotNull I current;
    protected boolean cancelled;

    public HoldingUpdateEvent(@NotNull WalletModel wallet, @Nullable I previous, @NotNull I current) {
        super(wallet);
        this.previous = previous;
        this.current = current;
    }

    public @NotNull Optional<I> getPrevious() {
        return Optional.ofNullable(previous);
    }

    public boolean isSomethingChanged() {
        return !Objects.equals(previous, current);
    }

    public void setCurrent(@NotNull I current) {
        Validate.notNull(current, "current");
        this.current = current;
    }

}
