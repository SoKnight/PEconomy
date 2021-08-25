package ru.soknight.peconomy.event.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.event.AsyncEvent;
import ru.soknight.peconomy.notification.Notification;

@Getter
@AllArgsConstructor
public final class NotificationReceivedEvent extends AsyncEvent {

    @Getter private static final HandlerList handlerList = new HandlerList();

    private final Player receiver;
    private final Notification notification;

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

}
