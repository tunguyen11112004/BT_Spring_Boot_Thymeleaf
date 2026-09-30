package org.fp.bt_qlsach.repository;

import org.fp.bt_qlsach.entity.Borrowing;
import org.fp.bt_qlsach.entity.BorrowingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long>, JpaSpecificationExecutor<Borrowing> {

    List<Borrowing> findByStatusIn(Collection<BorrowingStatus> statuses);

    @Query("""
            select distinct b from Borrowing b
            join fetch b.member
            join fetch b.details d
            join fetch d.book
            where b.id = :id
            """)
    Optional<Borrowing> findDetailedById(@Param("id") Long id);

    @Query("""
            select coalesce(sum(d.quantity - d.returnedQuantity), 0)
            from BorrowingDetail d
            where d.borrowing.member.id = :memberId
              and d.borrowing.status in :openStatuses
            """)
    long sumOutstanding(@Param("memberId") Long memberId,
                        @Param("openStatuses") Collection<BorrowingStatus> openStatuses);

    @Query("""
            select count(b) from Borrowing b
            where b.member.id = :memberId
              and b.unpaidFineAmount > 0
              and b.status <> :cancelled
            """)
    long countUnpaidFine(@Param("memberId") Long memberId, @Param("cancelled") BorrowingStatus cancelled);

    @Query("""
            select count(b) from Borrowing b
            where b.member.id = :memberId
              and b.dueDate < :today
              and b.status in :openStatuses
            """)
    long countOverdue(@Param("memberId") Long memberId,
                      @Param("today") LocalDate today,
                      @Param("openStatuses") Collection<BorrowingStatus> openStatuses);
}
