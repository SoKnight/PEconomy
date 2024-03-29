package ru.soknight.peconomy.transaction;

import lombok.Getter;

@Getter
public enum TransactionCause {

    STAFF_ADD("add"),
    STAFF_SET("set"),
    STAFF_RESET("reset"),
    STAFF_TAKE("take"),
    PAYMENT_OUTCOMING,
    PAYMENT_INCOMING,
    CONVERTATION,
    FAILED,
    UNKNOWN;

    private final String id;

    TransactionCause() {
        this.id = name().toLowerCase();
    }

    TransactionCause(String id) {
        this.id = id;
    }

    public static TransactionCause getById(String id) {
        for(TransactionCause cause : values())
            if(cause.getId().equals(id))
                return cause;

        return UNKNOWN;
    }

    public boolean hasFailed() {
        return this == FAILED;
    }

    public boolean isStaffOperation() {
        switch (this) {
            case STAFF_ADD:
            case STAFF_SET:
            case STAFF_RESET:
            case STAFF_TAKE:
                return true;
            default:
                return false;
        }
    }

}
