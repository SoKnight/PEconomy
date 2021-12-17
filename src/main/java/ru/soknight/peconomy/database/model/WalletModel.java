package ru.soknight.peconomy.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.soknight.lib.tool.Validate;
import ru.soknight.peconomy.configuration.CurrencyInstance;
import ru.soknight.peconomy.database.persister.StringFloatMapJsonPersister;
import ru.soknight.peconomy.transaction.TransactionCause;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "peco_wallet_owners")
public final class WalletModel {

    public static final String COLUMN_PLAYER_NAME = "player_name";
    public static final String COLUMN_PLAYER_UUID = "player_uuid";
    public static final String COLUMN_WALLETS = "wallets";
    public static final String COLUMN_CREATED_AT = "created_at";

    @DatabaseField(columnName = COLUMN_PLAYER_NAME, id = true, canBeNull = false)
    private @NotNull String playerName;
    @DatabaseField(columnName = COLUMN_PLAYER_UUID)
    private @Nullable UUID playerUUID;
    @DatabaseField(columnName = COLUMN_WALLETS, canBeNull = false, persisterClass = StringFloatMapJsonPersister.class)
    private @NotNull Map<String, Float> wallets;
    @DatabaseField(columnName = COLUMN_CREATED_AT, canBeNull = false)
    private @NotNull LocalDateTime createdAt;

    public WalletModel(@NotNull OfflinePlayer bukkitPlayer) {
        this(bukkitPlayer.getName(), bukkitPlayer.getUniqueId());
    }
    
    public WalletModel(@NotNull String playerName, @Nullable UUID playerUUID) {
        this(playerName, playerUUID, new LinkedHashMap<>());
    }

    public WalletModel(@NotNull OfflinePlayer bukkitPlayer, @NotNull LinkedHashMap<String, Float> wallets) {
        this(bukkitPlayer.getName(), bukkitPlayer.getUniqueId(), wallets);
    }

    public WalletModel(@NotNull String playerName, @Nullable UUID playerUUID, @NotNull LinkedHashMap<String, Float> wallets) {
        Validate.notNull(playerName, "playerName");
        Validate.notNull(wallets, "wallets");

        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.wallets = wallets;
        this.createdAt = LocalDateTime.now();
    }

    public @NotNull Optional<UUID> getPlayerUUID() {
        return Optional.ofNullable(playerUUID);
    }

    public void loadCurrency(@NotNull CurrencyInstance currency) {
        if(!wallets.containsKey(currency.getId()))
            addAmount(currency.getId(), currency.getNewbieAmount());
    }

    public void updateUUID(@NotNull UUID uuid) {
        Validate.notNull(uuid, "uuid");
        this.playerUUID = uuid;
    }

    public void addAmount(@NotNull String currency, float amount) {
        synchronized (this) {
            float pre = getAmount(currency);
            float post = pre + amount;
            wallets.put(currency, post);
        }
    }
    
    public @NotNull TransactionModel addAmount(@NotNull String currency, float amount, @NotNull String operator) {
        synchronized (this) {
            float pre = getAmount(currency);
            float post = pre + amount;
            wallets.put(currency, post);

            return new TransactionModel(playerName, currency, pre, post, operator, TransactionCause.STAFF_ADD);
        }
    }
    
    public float getAmount(@NotNull String currency) {
        return hasWallet(currency) ? wallets.get(currency) : 0F;
    }
    
    public boolean hasAmount(@NotNull String currency, float amount) {
        return amount <= getAmount(currency);
    }
    
    public boolean hasWallet(@NotNull String currency) {
        return wallets.containsKey(currency);
    }

    public void resetWallet(@NotNull String currency) {
        synchronized (this) {
            float pre = getAmount(currency);
            wallets.put(currency, 0F);
        }
    }

    public @NotNull TransactionModel resetWallet(@NotNull String currency, @Nullable String operator) {
        synchronized (this) {
            float pre = getAmount(currency);
            wallets.put(currency, 0F);

            return new TransactionModel(playerName, currency, pre, 0F, operator, TransactionCause.STAFF_RESET);
        }
    }
    
    public void setAmount(@NotNull String currency, float amount) {
        synchronized (this) {
            wallets.put(currency, amount);
        }
    }

    public @NotNull TransactionModel setAmount(@NotNull String currency, float amount, @NotNull String operator) {
        synchronized (this) {
            float pre = getAmount(currency);
            wallets.put(currency, amount);

            return new TransactionModel(playerName, currency, pre, amount, operator, TransactionCause.STAFF_SET);
        }
    }

    public void takeAmount(@NotNull String currency, float amount) {
        takeAmount(currency, amount, false);
    }
    
    public void takeAmount(@NotNull String currency, float amount, boolean ignoreNegativeBalance) {
        synchronized (this) {
            float pre = getAmount(currency);
            float post = pre - amount;

            if(post >= 0F || ignoreNegativeBalance)
                wallets.put(currency, post);
        }
    }

    public @NotNull TransactionModel takeAmount(@NotNull String currency, float amount, @Nullable String operator) {
        return takeAmount(currency, amount, operator, false);
    }

    public @NotNull TransactionModel takeAmount(@NotNull String currency, float amount, @Nullable String operator, boolean ignoreNegativeBalance) {
        synchronized (this) {
            float pre = getAmount(currency);
            float post = pre - amount;

            if(post >= 0F || ignoreNegativeBalance)
                wallets.put(currency, post);

            return new TransactionModel(playerName, currency, pre, post, operator, TransactionCause.STAFF_TAKE);
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        WalletModel that = (WalletModel) o;
        return Objects.equals(playerName, that.playerName) &&
                Objects.equals(playerUUID, that.playerUUID) &&
                Objects.equals(wallets, that.wallets) &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, playerUUID, wallets, createdAt);
    }

    @Override
    public @NotNull String toString() {
        return "WalletModel{" +
                "playerName='" + playerName + '\'' +
                ", playerUUID=" + playerUUID +
                ", wallets=" + wallets +
                ", createdAt=" + createdAt +
                '}';
    }

    // --- backward compatibility methods

    /**
     * @deprecated Use {@link WalletModel#WalletModel(String, UUID)} instead.
     */
    @Deprecated
    public WalletModel(@NotNull String playerName) {
        this(playerName, null, new LinkedHashMap<>());
    }

    /**
     * @deprecated Use {@link WalletModel#WalletModel(String, UUID, LinkedHashMap)} instead.
     */
    @Deprecated
    public WalletModel(@NotNull String playerName, @NotNull LinkedHashMap<String, Float> wallets) {
        this(playerName, null, wallets);
    }

    /**
     * @deprecated Use {@link #getPlayerName()} instead.
     */
    @Deprecated
    public @NotNull String getWalletHolder() {
        return playerName;
    }

}
