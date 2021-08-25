package ru.soknight.peconomy.notification;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.peconomy.database.model.TransactionModel;

public final class NotificationManager {

    public static final String BUNGEECORD_CHANNEL = "BungeeCord";
    public static final String PLUGIN_CHANNEL = "PEconomyNotifications";

    private final Plugin plugin;
    private final Configuration config;
    private final NotificationListener listener;

    public NotificationManager(Plugin plugin, Configuration config) {
        this.plugin = plugin;
        this.config = config;
        this.listener = new NotificationListener(plugin);
    }

    public void register() {
        Messenger messenger = plugin.getServer().getMessenger();
        messenger.registerIncomingPluginChannel(plugin, BUNGEECORD_CHANNEL, listener);
        messenger.registerOutgoingPluginChannel(plugin, BUNGEECORD_CHANNEL);
    }

    public void unregister() {
        Messenger messenger = plugin.getServer().getMessenger();
        messenger.unregisterIncomingPluginChannel(plugin, BUNGEECORD_CHANNEL, listener);
        messenger.unregisterOutgoingPluginChannel(plugin, BUNGEECORD_CHANNEL);
    }

    public boolean notify(TransactionModel transaction, NotificationType notificationType) {
        if(!config.getBoolean("bungeecord-notifications", false))
            return false;

        return notify(Notification.fromTransaction(transaction, notificationType));
    }

    public boolean notify(Notification notification) {
        if(!config.getBoolean("bungeecord-notifications", false))
            return false;

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("Forward");
        output.writeUTF("ALL");
        output.writeUTF(PLUGIN_CHANNEL);

        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(notification.getType().getId());
        data.writeUTF(notification.getWalletHolder());
        data.writeUTF(notification.getCurrencyId());
        data.writeFloat(notification.getBalanceBefore());
        data.writeFloat(notification.getBalanceAfter());
        data.writeUTF(notification.getOperator());

        byte[] asByteArray = data.toByteArray();
        data.writeShort(asByteArray.length);
        data.write(asByteArray);

        Player transferer = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if(transferer == null)
            return false;

        transferer.sendPluginMessage(plugin, BUNGEECORD_CHANNEL, output.toByteArray());
        plugin.getLogger().info("Sent notification " + notification + " using " + transferer.getName() + " as transferer!");
        return true;
    }

}
