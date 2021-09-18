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
@DatabaseTable(tableName = "transactions")
public class TransactionModel {

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

    public boolean isSuccess() {
        return !cause.equalsIgnoreCase("failed");
    }

    public void makeFailed() {
        this.cause = TransactionCause.FAILED.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

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
    public String toString() {
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

}
