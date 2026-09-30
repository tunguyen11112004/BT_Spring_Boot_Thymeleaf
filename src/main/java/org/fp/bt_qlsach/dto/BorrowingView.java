package org.fp.bt_qlsach.dto;

import org.fp.bt_qlsach.entity.Borrowing;
import org.fp.bt_qlsach.entity.FinePayment;
import org.fp.bt_qlsach.entity.FineWaiver;

import java.util.List;

public record BorrowingView(Borrowing borrowing, List<FinePayment> payments, List<FineWaiver> waivers) {
}
