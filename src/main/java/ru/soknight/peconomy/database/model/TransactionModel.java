package ru.soknight.peconomy.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.soknight.peconomy.transaction.TransactionCause;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "peco_transactions")
public final class TransactionModel {

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_WALLET_HOLDER = "wallet_holder";
    public static final String COLUMN_OPERATOR = "operator";
    public static final String COLUMN_CURRENCY = "currency";
    public static final String COLUMN_CAUSE = "cause";
    public static final String COLUMN_BALANCE_BEFORE = "balance_before";
    public static final String COLUMN_BALANCE_AFTER = "balance_after";
    public static final String COLUMN_PASSED_AT = "passed_at";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true, canBeNull = false)
    private int id;
    @DatabaseField(columnName = COLUMN_WALLET_HOLDER, canBeNull = false)
    private String walletHolder;
    @DatabaseField(columnName = COLUMN_OPERATOR)
    private String operator;
    @DatabaseField(columnName = COLUMN_CURRENCY, canBeNull = false)
    private String currency;
    @DatabaseField(columnName = COLUMN_CAUSE, canBeNull = false)
    private String cause;
    @DatabaseField(columnName = COLUMN_BALANCE_BEFORE, canBeNull = false)
    private float balanceBefore;
    @DatabaseField(columnName = COLUMN_BALANCE_AFTER, canBeNull = false)
    private float balanceAfter;
    @DatabaseField(columnName = COLUMN_PASSED_AT, canBeNull = false)
    private LocalDateTime passedAt;

    public TransactionModel(
            @NotNull String walletHolder,
            @NotNull String currency,
            float balanceBefore,
            float balanceAfter,
            @Nullable String operator,
            @NotNull TransactionCause cause
    ) {
        this(walletHolder, currency, balanceBefore, balanceAfter, operator, cause.getId());
    }

    public TransactionModel(
            @NotNull String walletHolder,
            @NotNull String currency,
            float balanceBefore,
            float balanceAfter,
            @Nullable String operator,
            @NotNull String cause
    ) {
        this.walletHolder = walletHolder;
        this.currency = currency;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.operator = operator;
        this.cause = cause;
        this.passedAt = LocalDateTime.now();
    }

    public boolean isSuccess() {
        return !cause.equalsIgnoreCase("failed");
    }

    public void makeFailed() {
        this.cause = TransactionCause.FAILED.getId();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        TransactionModel that = (TransactionModel) o;
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
        return "TransactionModel{" +
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

}
