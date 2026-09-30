package org.fp.bt_qlsach.service;

import org.fp.bt_qlsach.entity.Borrowing;
import org.fp.bt_qlsach.entity.BorrowingStatus;
import org.fp.bt_qlsach.entity.FinePayment;
import org.fp.bt_qlsach.entity.PaymentMethod;
import org.fp.bt_qlsach.util.BusinessException;
import org.fp.bt_qlsach.repository.BorrowingRepository;
import org.fp.bt_qlsach.repository.FinePaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class PaymentService {

    private final BorrowingRepository borrowingRepository;
    private final FinePaymentRepository finePaymentRepository;

    public PaymentService(BorrowingRepository borrowingRepository, FinePaymentRepository finePaymentRepository) {
        this.borrowingRepository = borrowingRepository;
        this.finePaymentRepository = finePaymentRepository;
    }

    @Transactional
    public void pay(Long borrowingId, BigDecimal amount, PaymentMethod method, String note, LocalDate paymentDate) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số tiền thanh toán phải lớn hơn 0.");
        }
        if (method == null) {
            throw new BusinessException("Chọn phương thức thanh toán.");
        }
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy phiếu mượn."));
        if (borrowing.getStatus() == BorrowingStatus.CANCELLED) {
            throw new BusinessException("Không thanh toán cho phiếu đã hủy.");
        }
        if (borrowing.getUnpaidFineAmount().signum() <= 0) {
            throw new BusinessException("Phiếu này không còn phí phải thu.");
        }
        if (normalized.compareTo(borrowing.getUnpaidFineAmount()) > 0) {
            throw new BusinessException("Không được thanh toán vượt quá số phí còn phải thu.");
        }

        FinePayment payment = new FinePayment();
        payment.setBorrowing(borrowing);
        payment.setAmount(normalized);
        payment.setMethod(method);
        payment.setNote(note == null ? "" : note.trim());
        payment.setPaymentDate(paymentDate == null ? LocalDate.now() : paymentDate);
        finePaymentRepository.save(payment);

        borrowing.setPaidFineAmount(borrowing.getPaidFineAmount().add(normalized).setScale(2, RoundingMode.HALF_UP));
        borrowing.recalculateTotals();
        borrowing.refreshStatus(LocalDate.now());
        borrowingRepository.save(borrowing);
    }
}
