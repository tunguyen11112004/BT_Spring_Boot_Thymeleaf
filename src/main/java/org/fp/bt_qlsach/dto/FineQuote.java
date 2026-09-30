package org.fp.bt_qlsach.dto;

import java.math.BigDecimal;

public record FineQuote(int billableLateDays, BigDecimal addedFine, BigDecimal newLineFine) {
}
