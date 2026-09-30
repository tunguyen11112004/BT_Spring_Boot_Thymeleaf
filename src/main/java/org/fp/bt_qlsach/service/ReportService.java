package org.fp.bt_qlsach.service;

import org.fp.bt_qlsach.entity.Borrowing;
import org.fp.bt_qlsach.entity.BorrowingDetail;
import org.fp.bt_qlsach.entity.BorrowingStatus;
import org.fp.bt_qlsach.entity.FinePolicy;
import org.fp.bt_qlsach.repository.BorrowingRepository;
import org.fp.bt_qlsach.dto.FineQuote;
import org.fp.bt_qlsach.dto.OverdueRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReportService {

    private final BorrowingService borrowingService;
    private final BorrowingRepository borrowingRepository;
    private final FinePolicyService finePolicyService;
    private final FineCalculationService fineCalculationService;

    public ReportService(BorrowingService borrowingService,
                         BorrowingRepository borrowingRepository,
                         FinePolicyService finePolicyService,
                         FineCalculationService fineCalculationService) {
        this.borrowingService = borrowingService;
        this.borrowingRepository = borrowingRepository;
        this.finePolicyService = finePolicyService;
        this.fineCalculationService = fineCalculationService;
    }

    @Transactional
    public List<OverdueRow> overdueReport(LocalDate today) {
        borrowingService.refreshOverdueStatuses(today);
        FinePolicy policy = finePolicyService.requireActive();
        List<OverdueRow> rows = new ArrayList<>();
        for (Borrowing borrowing : borrowingRepository.findByStatusIn(List.of(BorrowingStatus.OVERDUE))) {
            int rawLateDays = (int) Math.max(0, ChronoUnit.DAYS.between(borrowing.getDueDate(), today));
            for (BorrowingDetail detail : borrowing.getDetails()) {
                int remaining = detail.remaining();
                if (remaining <= 0) {
                    continue;
                }
                FineQuote quote = fineCalculationService.calculate(
                        borrowing.getDueDate(), today, remaining, detail.getFineAmount(), policy);
                rows.add(new OverdueRow(
                        borrowing.getId(),
                        borrowing.getMember().getMemberCode(),
                        borrowing.getMember().getFullName(),
                        detail.getBook().getTitle(),
                        borrowing.getDueDate(),
                        rawLateDays,
                        quote.billableLateDays(),
                        remaining,
                        quote.addedFine()));
            }
        }
        rows.sort(Comparator.comparing(OverdueRow::lateDays).reversed());
        return rows;
    }
}
