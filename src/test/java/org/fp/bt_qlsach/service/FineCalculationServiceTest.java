package org.fp.bt_qlsach.service;

import org.fp.bt_qlsach.entity.FinePolicy;
import org.fp.bt_qlsach.dto.FineQuote;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FineCalculationServiceTest {

    private final FineCalculationService service = new FineCalculationService();

    @Test
    void onTimeReturnHasNoFine() {
        FineQuote quote = service.calculate(
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 10),
                1,
                BigDecimal.ZERO,
                policy("5000", 0, null));

        assertThat(quote.billableLateDays()).isZero();
        assertThat(quote.addedFine()).isEqualByComparingTo("0");
    }

    @Test
    void threeDaysLateForOneCopy() {
        FineQuote quote = service.calculate(
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 13),
                1,
                BigDecimal.ZERO,
                policy("5000", 0, null));

        assertThat(quote.billableLateDays()).isEqualTo(3);
        assertThat(quote.addedFine()).isEqualByComparingTo("15000");
    }

    @Test
    void threeDaysLateForTwoCopies() {
        FineQuote quote = service.calculate(
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 13),
                2,
                BigDecimal.ZERO,
                policy("5000", 0, null));

        assertThat(quote.addedFine()).isEqualByComparingTo("30000");
    }

    @Test
    void graceDaysReduceBillableDays() {
        FineQuote quote = service.calculate(
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12),
                1,
                BigDecimal.ZERO,
                policy("5000", 1, null));

        assertThat(quote.billableLateDays()).isEqualTo(1);
        assertThat(quote.addedFine()).isEqualByComparingTo("5000");
    }

    @Test
    void maxFineCapsTheLineTotal() {
        FineQuote quote = service.calculate(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 30),
                1,
                BigDecimal.ZERO,
                policy("10000", 0, "50000"));

        assertThat(quote.addedFine()).isEqualByComparingTo("50000");
        assertThat(quote.newLineFine()).isEqualByComparingTo("50000");
    }

    @Test
    void capAppliesToAccumulatedLineFine() {
        FineQuote quote = service.calculate(
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 13),
                1,
                new BigDecimal("40000.00"),
                policy("20000", 0, "50000"));

        assertThat(quote.addedFine()).isEqualByComparingTo("10000.00");
        assertThat(quote.newLineFine()).isEqualByComparingTo("50000.00");
    }

    private FinePolicy policy(String daily, int graceDays, String max) {
        FinePolicy policy = new FinePolicy();
        policy.setDailyFineAmount(new BigDecimal(daily));
        policy.setGraceDays(graceDays);
        policy.setMaxFineAmount(max == null ? null : new BigDecimal(max));
        policy.setActive(true);
        return policy;
    }
}
