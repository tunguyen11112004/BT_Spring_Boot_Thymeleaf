package org.fp.bt_qlsach.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "borrowing")
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate borrowedDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate returnedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BorrowingStatus status = BorrowingStatus.BORROWING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalFineAmount = money(BigDecimal.ZERO);

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal paidFineAmount = money(BigDecimal.ZERO);

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal waivedFineAmount = money(BigDecimal.ZERO);

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unpaidFineAmount = money(BigDecimal.ZERO);

    @OneToMany(mappedBy = "borrowing", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<BorrowingDetail> details = new ArrayList<>();

    public void addDetail(BorrowingDetail detail) {
        details.add(detail);
        detail.setBorrowing(this);
    }

    public void recalculateTotals() {
        BigDecimal total = BigDecimal.ZERO;
        for (BorrowingDetail detail : details) {
            total = total.add(detail.getFineAmount());
        }
        this.totalFineAmount = money(total);
        this.unpaidFineAmount = money(totalFineAmount.subtract(paidFineAmount).subtract(waivedFineAmount));
    }

    /**
     * Quá hạn được ưu tiên khi còn sách chưa trả, để lọc phiếu và chặn mượn mới.
     * FINE_PENDING / FINE_PAID chỉ khi đã trả hết sách.
     */
    public void refreshStatus(LocalDate today) {
        if (status == BorrowingStatus.CANCELLED) {
            return;
        }
        boolean fullyReturned = !details.isEmpty()
                && details.stream().allMatch(detail -> detail.getReturnedQuantity() >= detail.getQuantity());
        boolean anyReturned = details.stream().anyMatch(detail -> detail.getReturnedQuantity() > 0);
        if (!fullyReturned) {
            returnedDate = null;
            if (dueDate.isBefore(today)) {
                status = BorrowingStatus.OVERDUE;
            } else if (anyReturned) {
                status = BorrowingStatus.PARTIALLY_RETURNED;
            } else {
                status = BorrowingStatus.BORROWING;
            }
            return;
        }
        if (returnedDate == null) {
            returnedDate = details.stream()
                    .map(BorrowingDetail::getLastReturnedDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .orElse(today);
        }
        if (unpaidFineAmount.signum() > 0) {
            status = BorrowingStatus.FINE_PENDING;
        } else if (totalFineAmount.signum() > 0 || paidFineAmount.signum() > 0 || waivedFineAmount.signum() > 0) {
            status = BorrowingStatus.FINE_PAID;
        } else {
            status = BorrowingStatus.RETURNED;
        }
    }

    public boolean hasPartialReturn() {
        boolean anyReturned = details.stream().anyMatch(detail -> detail.getReturnedQuantity() > 0);
        boolean stillOut = details.stream().anyMatch(detail -> detail.getReturnedQuantity() < detail.getQuantity());
        return anyReturned && stillOut;
    }

    public boolean canCancel() {
        if (status == BorrowingStatus.CANCELLED
                || status == BorrowingStatus.RETURNED
                || status == BorrowingStatus.FINE_PENDING
                || status == BorrowingStatus.FINE_PAID
                || status == BorrowingStatus.PARTIALLY_RETURNED) {
            return false;
        }
        boolean anyReturned = details.stream().anyMatch(detail -> detail.getReturnedQuantity() > 0);
        return !anyReturned && totalFineAmount.signum() == 0 && paidFineAmount.signum() == 0 && waivedFineAmount.signum() == 0;
    }

    public boolean canReturn() {
        return status != null && status.isOpen();
    }

    public boolean canPay() {
        return status != BorrowingStatus.CANCELLED && unpaidFineAmount.signum() > 0;
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public LocalDate getBorrowedDate() {
        return borrowedDate;
    }

    public void setBorrowedDate(LocalDate borrowedDate) {
        this.borrowedDate = borrowedDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }

    public BorrowingStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowingStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalFineAmount() {
        return totalFineAmount;
    }

    public void setTotalFineAmount(BigDecimal totalFineAmount) {
        this.totalFineAmount = totalFineAmount;
    }

    public BigDecimal getPaidFineAmount() {
        return paidFineAmount;
    }

    public void setPaidFineAmount(BigDecimal paidFineAmount) {
        this.paidFineAmount = paidFineAmount;
    }

    public BigDecimal getWaivedFineAmount() {
        return waivedFineAmount;
    }

    public void setWaivedFineAmount(BigDecimal waivedFineAmount) {
        this.waivedFineAmount = waivedFineAmount;
    }

    public BigDecimal getUnpaidFineAmount() {
        return unpaidFineAmount;
    }

    public void setUnpaidFineAmount(BigDecimal unpaidFineAmount) {
        this.unpaidFineAmount = unpaidFineAmount;
    }

    public List<BorrowingDetail> getDetails() {
        return details;
    }

    public void setDetails(List<BorrowingDetail> details) {
        this.details = details;
    }
}
