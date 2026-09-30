package org.fp.bt_qlsach.entity;

public enum BorrowingStatus {
    BORROWING,
    PARTIALLY_RETURNED,
    RETURNED,
    OVERDUE,
    FINE_PENDING,
    FINE_PAID,
    CANCELLED;

    public boolean isOpen() {
        return this == BORROWING || this == PARTIALLY_RETURNED || this == OVERDUE;
    }
}
