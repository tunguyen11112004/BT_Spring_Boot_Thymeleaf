package org.fp.bt_qlsach.util;

import org.fp.bt_qlsach.entity.BorrowingStatus;
import org.fp.bt_qlsach.entity.PaymentMethod;
import org.springframework.stereotype.Component;

@Component("labels")
public class DisplayLabels {

    public String status(BorrowingStatus status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case BORROWING -> "Đang mượn";
            case PARTIALLY_RETURNED -> "Trả một phần";
            case RETURNED -> "Đã trả";
            case OVERDUE -> "Quá hạn";
            case FINE_PENDING -> "Còn nợ phí";
            case FINE_PAID -> "Đã thanh toán phí";
            case CANCELLED -> "Đã hủy";
        };
    }

    public String badge(BorrowingStatus status) {
        if (status == null) {
            return "text-bg-secondary";
        }
        return switch (status) {
            case BORROWING -> "text-bg-primary";
            case PARTIALLY_RETURNED -> "text-bg-info";
            case RETURNED -> "text-bg-success";
            case OVERDUE -> "text-bg-danger";
            case FINE_PENDING -> "text-bg-warning";
            case FINE_PAID -> "text-bg-success";
            case CANCELLED -> "text-bg-secondary";
        };
    }

    public String method(PaymentMethod method) {
        if (method == null) {
            return "";
        }
        return switch (method) {
            case CASH -> "Tiền mặt";
            case TRANSFER -> "Chuyển khoản";
        };
    }
}
