package org.fp.bt_qlsach.repository;

import org.fp.bt_qlsach.entity.FinePayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinePaymentRepository extends JpaRepository<FinePayment, Long> {

    List<FinePayment> findByBorrowingIdOrderByIdDesc(Long borrowingId);
}
