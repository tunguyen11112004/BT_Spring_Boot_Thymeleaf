package org.fp.bt_qlsach.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OverdueRow(
        Long borrowingId,
        String memberCode,
        String memberName,
        String bookTitle,
        LocalDate dueDate,
        int lateDays,
        int billableLateDays,
        int remainingQuantity,
        BigDecimal expectedFine) {
}
