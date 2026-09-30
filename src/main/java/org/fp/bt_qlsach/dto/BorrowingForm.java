package org.fp.bt_qlsach.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowingForm {

    @NotNull(message = "Chọn độc giả")
    private Long memberId;

    @NotNull(message = "Nhập ngày mượn")
    private LocalDate borrowedDate;

    @NotNull(message = "Nhập hạn trả")
    private LocalDate dueDate;

    private List<BorrowLineForm> lines = new ArrayList<>();

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
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

    public List<BorrowLineForm> getLines() {
        return lines;
    }

    public void setLines(List<BorrowLineForm> lines) {
        this.lines = lines;
    }
}
