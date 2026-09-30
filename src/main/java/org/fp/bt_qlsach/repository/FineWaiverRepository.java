package org.fp.bt_qlsach.repository;

import org.fp.bt_qlsach.entity.FineWaiver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FineWaiverRepository extends JpaRepository<FineWaiver, Long> {

    List<FineWaiver> findByBorrowingIdOrderByIdDesc(Long borrowingId);
}
