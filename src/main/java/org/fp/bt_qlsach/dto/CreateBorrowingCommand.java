package org.fp.bt_qlsach.dto;

import java.time.LocalDate;
import java.util.List;

public record CreateBorrowingCommand(
        Long memberId,
        LocalDate borrowedDate,
        LocalDate dueDate,
        List<BorrowLineCommand> lines) {
}
