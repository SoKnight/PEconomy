package ru.soknight.peconomy.database.model.deprecated;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.soknight.lib.database.migration.annotation.MigrationPath;
import ru.soknight.lib.database.migration.runtime.MigrationDataConverter;
import ru.soknight.peconomy.database.model.WalletModel;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;

@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "walletowners")
@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public final class WalletModelV1 {

    private static final MigrationDataConverter<WalletModelV1, WalletModel> CONVERTER = new ConverterV2();

    public static @NotNull MigrationDataConverter<WalletModelV1, WalletModel> getConverter() {
        return CONVERTER;
    }

    @DatabaseField(columnName = "owner", id = true)
    private String walletHolder;
    @DatabaseField(columnName = "wallets", canBeNull = false, dataType = DataType.SERIALIZABLE)
    private HashMap<String, Float> wallets;

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        WalletModelV1 that = (WalletModelV1) o;
        return Objects.equals(walletHolder, that.walletHolder) &&
                Objects.equals(wallets, that.wallets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(walletHolder, wallets);
    }

    @Override
    public @NotNull String toString() {
        return "Wallet{" +
                "holder='" + walletHolder + '\'' +
                ", wallets=" + wallets +
                '}';
    }

    @MigrationPath("v2/wallet-owners-table-schema-patch.dbsp")
    private static final class ConverterV2 implements MigrationDataConverter<WalletModelV1, WalletModel> {

        @Override
        public @NotNull WalletModel convert(@NotNull WalletModelV1 model) {
            return new WalletModel(model.getWalletHolder(), new LinkedHashMap<>(model.getWallets()));
        }

    }

}
