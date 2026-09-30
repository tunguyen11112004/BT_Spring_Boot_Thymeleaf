package org.fp.bt_qlsach.service;

import org.fp.bt_qlsach.entity.FinePolicy;
import org.fp.bt_qlsach.dto.FineQuote;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class FineCalculationService {

    /**
     * lateDays tính phí = max(0, ngày trả - hạn trả - graceDays).
     * fine = lateDays tính phí × số lượng trả lần này × phí/ngày.
     * Trần áp trên tổng phí của dòng, không viết đè phí đã ghi nếu đã vượt trần.
     */
    public FineQuote calculate(LocalDate dueDate,
                               LocalDate actualReturnDate,
                               int returnedQuantity,
                               BigDecimal currentLineFine,
                               FinePolicy policy) {
        if (returnedQuantity < 0) {
            throw new IllegalArgumentException("Số lượng trả không được âm.");
        }
        long rawLateDays = Math.max(0, ChronoUnit.DAYS.between(dueDate, actualReturnDate));
        int graceDays = Math.max(0, policy.getGraceDays());
        int billableLateDays = (int) Math.max(0, rawLateDays - graceDays);
        BigDecimal current = scale(currentLineFine == null ? BigDecimal.ZERO : currentLineFine);
        BigDecimal added = scale(policy.getDailyFineAmount()
                .multiply(BigDecimal.valueOf(billableLateDays))
                .multiply(BigDecimal.valueOf(returnedQuantity)));
        BigDecimal lineTotal = scale(current.add(added));
        BigDecimal max = policy.getMaxFineAmount();
        if (max != null && lineTotal.compareTo(max) > 0) {
            if (current.compareTo(max) >= 0) {
                added = scale(BigDecimal.ZERO);
                lineTotal = current;
            } else {
                lineTotal = scale(max);
                added = scale(lineTotal.subtract(current));
            }
        }
        return new FineQuote(billableLateDays, added, lineTotal);
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
