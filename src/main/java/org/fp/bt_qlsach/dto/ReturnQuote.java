package org.fp.bt_qlsach.dto;

import java.math.BigDecimal;

public record ReturnQuote(
        Long detailId,
        Long bookId,
        String bookTitle,
        int quantity,
        int billableLateDays,
        BigDecimal addedFine,
        BigDecimal newLineFine) {
}
