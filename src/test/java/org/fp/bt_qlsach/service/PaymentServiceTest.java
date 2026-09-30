package org.fp.bt_qlsach.service;

import org.fp.bt_qlsach.entity.Borrowing;
import org.fp.bt_qlsach.entity.BorrowingStatus;
import org.fp.bt_qlsach.entity.PaymentMethod;
import org.fp.bt_qlsach.util.BusinessException;
import org.fp.bt_qlsach.repository.BorrowingRepository;
import org.fp.bt_qlsach.repository.FinePaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private BorrowingRepository borrowingRepository;

    @Mock
    private FinePaymentRepository finePaymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void rejectsPaymentAboveOutstandingFine() {
        Borrowing borrowing = new Borrowing();
        borrowing.setId(1L);
        borrowing.setStatus(BorrowingStatus.FINE_PENDING);
        borrowing.setUnpaidFineAmount(new BigDecimal("10000.00"));
        borrowing.setPaidFineAmount(BigDecimal.ZERO);
        borrowing.setTotalFineAmount(new BigDecimal("10000.00"));
        borrowing.setWaivedFineAmount(BigDecimal.ZERO);
        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(borrowing));

        assertThatThrownBy(() -> paymentService.pay(
                1L, new BigDecimal("10001"), PaymentMethod.CASH, null, LocalDate.now()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vượt");

        verify(finePaymentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
