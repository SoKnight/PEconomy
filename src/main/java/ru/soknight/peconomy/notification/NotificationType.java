package ru.soknight.peconomy.notification;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public enum NotificationType {

    PAYMENT_INCOMING,
    PAYMENT_OUTCOMING,
    STAFF_ADD("add"),
    STAFF_SET("set"),
    STAFF_RESET("reset"),
    STAFF_TAKE("take"),
    UNKNOWN;

    private final String id;

    NotificationType() {
        this.id = name().toLowerCase();
    }

    NotificationType(String id) {
        this.id = id;
    }

    public static @NotNull NotificationType getById(String id) {
        if(id == null || id.isEmpty())
            return UNKNOWN;

        for(NotificationType type : values())
            if(type.getId().equals(id))
                return type;

        return UNKNOWN;
    }

}
