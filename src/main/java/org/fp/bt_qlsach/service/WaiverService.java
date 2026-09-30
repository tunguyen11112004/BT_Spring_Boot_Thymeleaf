package org.fp.bt_qlsach.service;

import org.fp.bt_qlsach.entity.Borrowing;
import org.fp.bt_qlsach.entity.BorrowingStatus;
import org.fp.bt_qlsach.entity.FineWaiver;
import org.fp.bt_qlsach.util.BusinessException;
import org.fp.bt_qlsach.repository.BorrowingRepository;
import org.fp.bt_qlsach.repository.FineWaiverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class WaiverService {

    private final BorrowingRepository borrowingRepository;
    private final FineWaiverRepository fineWaiverRepository;

    public WaiverService(BorrowingRepository borrowingRepository, FineWaiverRepository fineWaiverRepository) {
        this.borrowingRepository = borrowingRepository;
        this.fineWaiverRepository = fineWaiverRepository;
    }

    @Transactional
    public void waive(Long borrowingId, BigDecimal amount, String reason, String approvedBy, LocalDate approvedDate) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số tiền miễn giảm phải lớn hơn 0.");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("Lý do miễn giảm là bắt buộc.");
        }
        if (approvedBy == null || approvedBy.isBlank()) {
            throw new BusinessException("Người duyệt miễn giảm là bắt buộc.");
        }
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy phiếu mượn."));
        if (borrowing.getStatus() == BorrowingStatus.CANCELLED) {
            throw new BusinessException("Không miễn giảm cho phiếu đã hủy.");
        }
        if (normalized.compareTo(borrowing.getUnpaidFineAmount()) > 0) {
            throw new BusinessException("Không được miễn giảm vượt quá số phí còn phải thu.");
        }

        FineWaiver waiver = new FineWaiver();
        waiver.setBorrowing(borrowing);
        waiver.setAmount(normalized);
        waiver.setReason(reason.trim());
        waiver.setApprovedBy(approvedBy.trim());
        waiver.setApprovedDate(approvedDate == null ? LocalDate.now() : approvedDate);
        fineWaiverRepository.save(waiver);

        borrowing.setWaivedFineAmount(borrowing.getWaivedFineAmount().add(normalized).setScale(2, RoundingMode.HALF_UP));
        borrowing.recalculateTotals();
        borrowing.refreshStatus(LocalDate.now());
        borrowingRepository.save(borrowing);
    }
}
