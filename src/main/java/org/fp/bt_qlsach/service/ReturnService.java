package org.fp.bt_qlsach.service;

import org.fp.bt_qlsach.entity.Borrowing;
import org.fp.bt_qlsach.entity.BorrowingDetail;
import org.fp.bt_qlsach.entity.BorrowingStatus;
import org.fp.bt_qlsach.entity.FinePolicy;
import org.fp.bt_qlsach.util.BusinessException;
import org.fp.bt_qlsach.repository.BookRepository;
import org.fp.bt_qlsach.repository.BorrowingRepository;
import org.fp.bt_qlsach.dto.FineQuote;
import org.fp.bt_qlsach.dto.ReturnLineCommand;
import org.fp.bt_qlsach.dto.ReturnQuote;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReturnService {

    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final FinePolicyService finePolicyService;
    private final FineCalculationService fineCalculationService;

    public ReturnService(BorrowingRepository borrowingRepository,
                         BookRepository bookRepository,
                         FinePolicyService finePolicyService,
                         FineCalculationService fineCalculationService) {
        this.borrowingRepository = borrowingRepository;
        this.bookRepository = bookRepository;
        this.finePolicyService = finePolicyService;
        this.fineCalculationService = fineCalculationService;
    }

    @Transactional(readOnly = true)
    public List<ReturnQuote> preview(Long borrowingId, List<ReturnLineCommand> lines, LocalDate returnDate) {
        return quote(requireOpen(borrowingId), lines, returnDate, finePolicyService.requireActive());
    }

    @Transactional(readOnly = true)
    public List<ReturnQuote> quoteRemaining(Long borrowingId, LocalDate returnDate) {
        Borrowing borrowing = requireOpen(borrowingId);
        List<ReturnLineCommand> lines = new ArrayList<>();
        for (BorrowingDetail detail : borrowing.getDetails()) {
            if (detail.remaining() > 0) {
                lines.add(new ReturnLineCommand(detail.getId(), detail.remaining()));
            }
        }
        if (lines.isEmpty()) {
            return List.of();
        }
        return quote(borrowing, lines, returnDate, finePolicyService.requireActive());
    }

    @Transactional
    public List<ReturnQuote> confirm(Long borrowingId, List<ReturnLineCommand> lines, LocalDate returnDate) {
        Borrowing borrowing = requireOpen(borrowingId);
        FinePolicy policy = finePolicyService.requireActive();
        List<ReturnQuote> quotes = quote(borrowing, lines, returnDate, policy);

        for (ReturnQuote quote : quotes) {
            BorrowingDetail detail = findDetail(borrowing, quote.detailId());
            detail.setReturnedQuantity(detail.getReturnedQuantity() + quote.quantity());
            detail.setLastReturnedDate(returnDate);
            detail.setLateDays(quote.billableLateDays());
            detail.setFineAmount(quote.newLineFine());
        }
        borrowing.recalculateTotals();
        borrowing.refreshStatus(LocalDate.now());
        borrowingRepository.saveAndFlush(borrowing);

        for (ReturnQuote quote : quotes) {
            int updated = bookRepository.increaseStock(quote.bookId(), quote.quantity());
            if (updated != 1) {
                throw new BusinessException("Không thể cộng lại tồn kho. Giao dịch trả sách được hoàn tác.");
            }
        }
        return quotes;
    }

    private List<ReturnQuote> quote(Borrowing borrowing,
                                    List<ReturnLineCommand> lines,
                                    LocalDate returnDate,
                                    FinePolicy policy) {
        if (returnDate == null) {
            throw new BusinessException("Thiếu ngày trả.");
        }
        if (borrowing.getStatus() == BorrowingStatus.RETURNED || borrowing.getStatus() == BorrowingStatus.CANCELLED) {
            throw new BusinessException("Không trả sách cho phiếu đã trả hết hoặc đã hủy.");
        }
        List<ReturnLineCommand> requested = lines == null ? List.of() : lines;
        List<ReturnQuote> quotes = new ArrayList<>();
        for (ReturnLineCommand line : requested) {
            if (line.quantity() < 0) {
                throw new BusinessException("Số lượng trả không được âm.");
            }
            if (line.quantity() == 0) {
                continue;
            }
            BorrowingDetail detail = findDetail(borrowing, line.detailId());
            if (line.quantity() > detail.remaining()) {
                throw new BusinessException("Không được trả nhiều hơn số lượng còn đang mượn của \""
                        + detail.getBook().getTitle() + "\".");
            }
            FineQuote fine = fineCalculationService.calculate(
                    borrowing.getDueDate(),
                    returnDate,
                    line.quantity(),
                    detail.getFineAmount(),
                    policy);
            quotes.add(new ReturnQuote(
                    detail.getId(),
                    detail.getBook().getId(),
                    detail.getBook().getTitle(),
                    line.quantity(),
                    fine.billableLateDays(),
                    fine.addedFine(),
                    fine.newLineFine()));
        }
        if (quotes.isEmpty()) {
            throw new BusinessException("Nhập số lượng trả cho ít nhất một đầu sách.");
        }
        return quotes;
    }

    private Borrowing requireOpen(Long id) {
        Borrowing borrowing = borrowingRepository.findDetailedById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy phiếu mượn."));
        if (!borrowing.canReturn()) {
            throw new BusinessException("Không trả sách cho phiếu đã trả hết hoặc đã hủy.");
        }
        return borrowing;
    }

    private BorrowingDetail findDetail(Borrowing borrowing, Long detailId) {
        return borrowing.getDetails().stream()
                .filter(detail -> detail.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Không tìm thấy dòng sách trong phiếu."));
    }

    public static BigDecimal totalAdded(List<ReturnQuote> quotes) {
        BigDecimal total = BigDecimal.ZERO;
        for (ReturnQuote quote : quotes) {
            total = total.add(quote.addedFine());
        }
        return total;
    }
}
