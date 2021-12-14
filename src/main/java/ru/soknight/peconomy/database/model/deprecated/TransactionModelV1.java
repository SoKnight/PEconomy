package ru.soknight.peconomy.database.model.deprecated;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.soknight.lib.database.migration.annotation.MigrationPath;
import ru.soknight.lib.database.migration.runtime.MigrationDataConverter;
import ru.soknight.peconomy.database.model.TransactionModel;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "transactions")
@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public final class TransactionModelV1 {

    private static final MigrationDataConverter<TransactionModelV1, TransactionModel> CONVERTER = new ConverterV2();

    public static @NotNull MigrationDataConverter<TransactionModelV1, TransactionModel> getConverter() {
        return CONVERTER;
    }

    @DatabaseField(columnName = "id", generatedId = true)
    private int id;
    @DatabaseField(columnName = "owner", canBeNull = false)
    private String walletHolder;
    @DatabaseField(columnName = "source")
    private String operator;
    @DatabaseField(columnName = "currency", canBeNull = false)
    private String currency;
    @DatabaseField(columnName = "type", canBeNull = false)
    private String cause;
    @DatabaseField(columnName = "preBalance", canBeNull = false)
    private float balanceBefore;
    @DatabaseField(columnName = "postBalance", canBeNull = false)
    private float balanceAfter;
    @DatabaseField(columnName = "date", canBeNull = false)
    private LocalDateTime passedAt;

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        TransactionModelV1 that = (TransactionModelV1) o;
        return id == that.id &&
                Float.compare(that.balanceBefore, balanceBefore) == 0 &&
                Float.compare(that.balanceAfter, balanceAfter) == 0 &&
                Objects.equals(walletHolder, that.walletHolder) &&
                Objects.equals(operator, that.operator) &&
                Objects.equals(currency, that.currency) &&
                Objects.equals(cause, that.cause) &&
                Objects.equals(passedAt, that.passedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, walletHolder, operator, currency, cause, balanceBefore, balanceAfter, passedAt);
    }

    @Override
    public @NotNull String toString() {
        return "Transaction{" +
                "id=" + id +
                ", holder='" + walletHolder + '\'' +
                ", subject='" + operator + '\'' +
                ", currency='" + currency + '\'' +
                ", cause='" + cause + '\'' +
                ", balanceBefore=" + balanceBefore +
                ", balanceAfter=" + balanceAfter +
                ", passedAt=" + passedAt +
                '}';
    }

    @MigrationPath("v2/transactions-table-schema-patch.dbsp")
    private static final class ConverterV2 implements MigrationDataConverter<TransactionModelV1, TransactionModel> {

        @Override
        public @NotNull TransactionModel convert(@NotNull TransactionModelV1 model) {
            return new TransactionModel(
                    model.getId(),
                    model.getWalletHolder(),
                    model.getOperator(),
                    model.getCurrency(),
                    model.getCause(),
                    model.getBalanceBefore(),
                    model.getBalanceAfter(),
                    model.getPassedAt()
            );
        }

    }

}
