package ru.soknight.peconomy.format;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.peconomy.format.amount.AmountFormatter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Formatter {

    private static final String AMOUNT_FORMAT_KEY = "amount-format";
    private static final String DATE_FORMAT_KEY = "transactions-history.date-format";

    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy - HH:mm:ss");

    private final Plugin plugin;
    private final Configuration config;
    private final Messages messages;

    private final AmountFormatter amountFormatter;
    private DateTimeFormatter dateTimeFormatter;

    public Formatter(Plugin plugin, Configuration config, Messages messages) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;

        this.amountFormatter = AmountFormatter.createCustomizable();
    }

    public void reload() {
        // --- amount format
        if(!amountFormatter.updateFormat(config, AMOUNT_FORMAT_KEY))
            plugin.getLogger().warning("You use invalid amount format! Check your config.yml!");

        // --- date format
        if(!updateDateTimeFormat())
            plugin.getLogger().warning("You use invalid date format! Check your config.yml!");
    }

    private boolean updateDateTimeFormat() {
        String configFormat = config.getString(DATE_FORMAT_KEY);
        if(configFormat != null) {
            try {
                this.dateTimeFormatter = DateTimeFormatter.ofPattern(configFormat);
                return true;
            } catch (Exception ignored) {
                this.dateTimeFormatter = DEFAULT_DATE_FORMATTER;
            }
        } else {
            this.dateTimeFormatter = DEFAULT_DATE_FORMATTER;
        }

        return false;
    }

    public @NotNull String formatAmount(double value) {
        return amountFormatter.formatAmount(value);
    }

    public @NotNull String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTimeFormatter.format(dateTime) : "null";
    }

    public @NotNull String formatOperator(String operator, CommandSender sender) {
        if(operator == null || operator.isEmpty())
            return messages.get("console-operator");

        if(!(sender instanceof Player))
            return operator;

        if(!config.getBoolean("transaction-source-hiding.enabled"))
            return operator;

        if(sender.hasPermission("peco.transaction.sourcespy"))
            return operator;

        if(!config.getList("transaction-source-hiding.staffs").contains(operator))
            return operator;

        return config.getColoredString("transaction-source-hiding.value");
    }

}
