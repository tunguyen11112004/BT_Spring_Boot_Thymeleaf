package org.fp.bt_qlsach.service;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.fp.bt_qlsach.entity.Borrowing;
import org.fp.bt_qlsach.entity.BorrowingStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

final class BorrowingSpecs {

    private BorrowingSpecs() {
    }

    static Specification<Borrowing> filter(Long memberId,
                                           BorrowingStatus status,
                                           LocalDate from,
                                           LocalDate to,
                                           boolean overdueOnly) {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("member", JoinType.LEFT);
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (memberId != null) {
                predicates.add(cb.equal(root.get("member").get("id"), memberId));
            }
            if (overdueOnly) {
                predicates.add(cb.equal(root.get("status"), BorrowingStatus.OVERDUE));
            } else if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("borrowedDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("borrowedDate"), to));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
