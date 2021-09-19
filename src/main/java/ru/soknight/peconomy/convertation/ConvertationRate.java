package ru.soknight.peconomy.convertation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Getter
@AllArgsConstructor
public final class ConvertationRate {

    public static final ConvertationRate DEFAULT = new ConvertationRate(1F, 1F);

    private final float fromRate;
    private final float toRate;

    public static boolean isParseableValue(@Nullable Object value) {
        return value instanceof Number || value instanceof String;
    }

    public static @NotNull ConvertationRate parse(@Nullable Object value) throws ConvertationRateParseException {
        if(value instanceof Number)
            return fromFloatValue(((Number) value).floatValue());

        if(value instanceof String) {
            String asString = (String) value;

            Float asFloat = parseFloat(asString);
            if(asFloat != null)
                return fromFloatValue(asFloat);

            if(asString.contains(":")) {
                if(asString.startsWith(":"))
                    throw new ConvertationRateParseException("Rate value cannot starts with ':'");

                if(asString.endsWith(":"))
                    throw new ConvertationRateParseException("Rate value cannot ends with ':'");

                String[] parts = asString.split(":");
                if(parts.length != 2)
                    throw new ConvertationRateParseException("Rate value can't have more than one colon as separator");

                Float from = parseFloat(parts[0]);
                if(from == null)
                    throw new ConvertationRateParseException("Cannot parse a first part of the conversation rate: '" + parts[0] + "'");
                if(from <= 0F)
                    throw new ConvertationRateParseException("Rate value cannot be less than or equal 1");

                Float to = parseFloat(parts[1]);
                if(to == null)
                    throw new ConvertationRateParseException("Cannot parse a second part of the conversation rate: '" + parts[1] + "'");
                if(to <= 0F)
                    throw new ConvertationRateParseException("Rate value cannot be less than or equal 1");

                return new ConvertationRate(from, to);
            }
        }

        throw new ConvertationRateParseException("Cannot parse this value as convertation rate: " + value);
    }

    private static @NotNull ConvertationRate fromFloatValue(float value) throws ConvertationRateParseException {
        if(value <= 0F)
            throw new ConvertationRateParseException("Rate value cannot be less than or equal 1");

        return new ConvertationRate(1F / value, 1F);
    }

    private static @Nullable Float parseFloat(@NotNull String str) {
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public float convert(float value) {
        return value * toRate / fromRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConvertationRate that = (ConvertationRate) o;
        return Float.compare(that.fromRate, fromRate) == 0 && Float.compare(that.toRate, toRate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromRate, toRate);
    }

    @Override
    public @NotNull String toString() {
        return "ConvertationRate{" +
                "fromRate=" + fromRate +
                ", toRate=" + toRate +
                '}';
    }

}
