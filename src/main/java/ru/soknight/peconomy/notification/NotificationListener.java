package ru.soknight.peconomy.notification;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import ru.soknight.peconomy.event.notification.NotificationReceivedEvent;

@AllArgsConstructor
public final class NotificationListener implements PluginMessageListener {

    private final Plugin plugin;

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if(!channel.equals(NotificationManager.BUNGEECORD_CHANNEL))
            return;

        ByteArrayDataInput input = ByteStreams.newDataInput(message);
        String subchannel = input.readUTF();
        if(!NotificationManager.PLUGIN_CHANNEL.equals(subchannel))
            return;

        short length = input.readShort();
        byte[] asByteArray = new byte[length];
        input.readFully(asByteArray);

        ByteArrayDataInput data = ByteStreams.newDataInput(asByteArray);
        String rawType = data.readUTF();

        NotificationType type = NotificationType.getById(rawType);
        if(type == NotificationType.UNKNOWN) {
            plugin.getLogger().warning("A notification with unknown type was received: " + rawType);
            return;
        }

        String walletHolder = data.readUTF();
        Player receiver = plugin.getServer().getPlayer(walletHolder);
        if(receiver == null || !receiver.isOnline())
            return;

        String currencyId = data.readUTF();
        float balanceBefore = data.readFloat();
        float balanceAfter = data.readFloat();
        String operator = data.readUTF();

        Notification notification = new Notification(walletHolder, currencyId, balanceBefore, balanceAfter, operator, type);
        if(!notification.validate()) {
            plugin.getLogger().severe("Cannot validate this notification: " + notification);
            return;
        }

        plugin.getLogger().info("Received notification: " + notification);
        new NotificationReceivedEvent(receiver, notification).fire();
    }

}
