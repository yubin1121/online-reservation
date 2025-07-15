package kr.co.module.core.status;

import java.util.EnumSet;

public enum ReservationStatus {

    PENDING , CONFIRMED, COMPLETED, CANCELED, REJECTED;

    public boolean canTransitionTo(ReservationStatus newStatus) {
        switch (this) {
            case PENDING:
                // PENDING은 CONFIRMED, CANCELED, REJECTED로 전환 가능
                return EnumSet.of(CONFIRMED, CANCELED, REJECTED).contains(newStatus);
            case CONFIRMED:
                // CONFIRMED는 CANCELED 또는 COMPLETED로만 전환 가능
                return EnumSet.of(CANCELED, COMPLETED).contains(newStatus);
            case COMPLETED:
            case CANCELED:
            case REJECTED:
                return false;
            default:
                return false;
        }
    }
}
