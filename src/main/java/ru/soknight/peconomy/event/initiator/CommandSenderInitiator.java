package ru.soknight.peconomy.event.initiator;

import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public final class CommandSenderInitiator implements Initiator {

    private final CommandSender commandSender;

    @Override
    public @NotNull CommandSender asCommandSender() {
        return commandSender;
    }

    @Override
    public @NotNull Player asBukkitPlayer() {
        if(!isBukkitPlayer())
            throw new IllegalStateException("this initiator is not a bukkit player!");

        return (Player) commandSender;
    }

    @Override
    public boolean isCommandSender() {
        return true;
    }

    @Override
    public boolean isBukkitPlayer() {
        return commandSender instanceof Player;
    }

}
