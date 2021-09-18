package ru.soknight.peconomy.event.initiator;

import org.jetbrains.annotations.NotNull;

public final class UnimplementedFeatureException extends RuntimeException {

    public UnimplementedFeatureException(@NotNull String message) {
        this(message, null);
    }

    public UnimplementedFeatureException(@NotNull Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public UnimplementedFeatureException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }

}
