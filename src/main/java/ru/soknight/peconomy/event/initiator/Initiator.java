package ru.soknight.peconomy.event.initiator;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Initiator {

    Initiator VAULT = createAsVaultEconomyConsumer();

    static @NotNull Initiator createAsCommandSender(@NotNull CommandSender commandSender) {
        return new CommandSenderInitiator(commandSender);
    }

    static @NotNull Initiator createAsVaultEconomyConsumer() {
        return new VaultEconomyConsumerInitiator();
    }

    default @NotNull CommandSender asCommandSender() {
        throw new UnimplementedFeatureException("#asCommandSender is not implemented for the " + getClass().getSimpleName());
    }

    default @NotNull Player asBukkitPlayer() {
        throw new UnimplementedFeatureException("#asBukkitPlayer is not implemented for the " + getClass().getSimpleName());
    }

    default boolean isCommandSender() {
        return false;
    }

    default boolean isBukkitPlayer() {
        return false;
    }

    default boolean isVaultEconomyConsumer() {
        return false;
    }

}
