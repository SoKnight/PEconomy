package ru.soknight.peconomy.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.soknight.lib.tool.Validate;
import ru.soknight.peconomy.balancetop.BalanceTop;
import ru.soknight.peconomy.convertation.ConvertationSetup;

import java.util.Objects;

@Getter
@AllArgsConstructor
public final class CurrencyInstance {

    private final @NotNull String id;
    private final @NotNull String name;
    private final @NotNull String symbol;
    private final float limit;
    private final float newbieAmount;
    private final boolean visible;
    private final boolean transferable;
    private final @NotNull ConvertationSetup convertationSetup;

    private final @Nullable BalanceTopSetup balanceTopSetup;
    private final @Nullable BalanceTop balanceTop;

    public static @NotNull Builder builder(@NotNull String id) {
        return new Builder(id);
    }
    
    public CurrencyInstance(@NotNull String id, @NotNull String name, @NotNull String symbol) {
        this(id, name, symbol, 0F, 0F, true, true, new ConvertationSetup(id), null, null);
    }

    public float convert(@NotNull CurrencyInstance currency, float value) {
        return convert(currency.getId(), value);
    }

    public float convert(@NotNull String currencyId, float value) {
        return convertationSetup.getRate(currencyId).convert(value);
    }

    public boolean isConvertableTo(@NotNull CurrencyInstance currency) {
        return isConvertableTo(currency.getId());
    }

    public boolean isConvertableTo(@NotNull String currencyId) {
        return convertationSetup.hasRate(currencyId);
    }

    public boolean useBalanceTop() {
        return balanceTop != null && balanceTopSetup != null && balanceTopSetup.isValid();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CurrencyInstance that = (CurrencyInstance) o;
        return Float.compare(that.limit, limit) == 0 &&
                Float.compare(that.newbieAmount, newbieAmount) == 0 &&
                visible == that.visible &&
                transferable == that.transferable &&
                id.equals(that.id) &&
                name.equals(that.name) &&
                symbol.equals(that.symbol) &&
                convertationSetup.equals(that.convertationSetup) &&
                Objects.equals(balanceTopSetup, that.balanceTopSetup) &&
                Objects.equals(balanceTop, that.balanceTop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id, name, symbol, limit, newbieAmount, visible, transferable,
                convertationSetup, balanceTopSetup, balanceTop
        );
    }

    @Override
    public @NotNull String toString() {
        return "CurrencyInstance{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", limit=" + limit +
                ", newbieAmount=" + newbieAmount +
                ", visible=" + visible +
                ", transferable=" + transferable +
                ", convertationSetup=" + convertationSetup +
                ", balanceTopSetup=" + balanceTopSetup +
                ", balanceTop=" + balanceTop +
                '}';
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static final class Builder {

        private final @NotNull String currencyId;
        private String name;
        private String symbol;
        private float limit;
        private float newbieAmount;
        private boolean visible;
        private boolean transferable;
        private ConvertationSetup convertationSetup;

        private BalanceTopSetup balanceTopSetup;
        private BalanceTop balanceTop;

        private Builder(@NotNull String currencyId) {
            this.currencyId = currencyId;
            this.name = currencyId;
            this.limit = 0F;
            this.newbieAmount = 0F;
            this.visible = true;
            this.transferable = true;
        }

        public @NotNull CurrencyInstance create() {
            Validate.notEmpty(currencyId, "currencyId");
            Validate.notNull(name, "name");
            Validate.notNull(symbol, "symbol");
            Validate.isTrue(limit >= 0F, "'limit' cannot be less than 0");
            Validate.isTrue(newbieAmount >= 0F, "'newbieAmount' cannot be less than 0");
            Validate.notNull(convertationSetup, "convertationSetup");

            return new CurrencyInstance(
                    currencyId,
                    name,
                    symbol,
                    limit,
                    newbieAmount,
                    visible,
                    transferable,
                    convertationSetup,
                    balanceTopSetup,
                    balanceTop
            );
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Builder builder = (Builder) o;
            return Float.compare(builder.limit, limit) == 0 &&
                    Float.compare(builder.newbieAmount, newbieAmount) == 0 &&
                    visible == builder.visible &&
                    transferable == builder.transferable &&
                    currencyId.equals(builder.currencyId) &&
                    Objects.equals(name, builder.name) &&
                    Objects.equals(symbol, builder.symbol) &&
                    Objects.equals(convertationSetup, builder.convertationSetup) &&
                    Objects.equals(balanceTopSetup, builder.balanceTopSetup) &&
                    Objects.equals(balanceTop, builder.balanceTop);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    currencyId, name, symbol, limit, newbieAmount, visible, transferable,
                    convertationSetup, balanceTopSetup, balanceTop
            );
        }

        @Override
        public @NotNull String toString() {
            return "Builder{" +
                    "currencyId='" + currencyId + '\'' +
                    ", name='" + name + '\'' +
                    ", symbol='" + symbol + '\'' +
                    ", limit=" + limit +
                    ", newbieAmount=" + newbieAmount +
                    ", visible=" + visible +
                    ", transferable=" + transferable +
                    ", convertationSetup=" + convertationSetup +
                    ", balanceTopSetup=" + balanceTopSetup +
                    ", balanceTop=" + balanceTop +
                    '}';
        }

    }

}
