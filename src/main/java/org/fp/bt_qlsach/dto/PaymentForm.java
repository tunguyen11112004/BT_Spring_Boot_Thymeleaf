package org.fp.bt_qlsach.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.fp.bt_qlsach.entity.PaymentMethod;

import java.math.BigDecimal;

public class PaymentForm {

    @NotNull(message = "Nhập số tiền")
    @DecimalMin(value = "0.01", message = "Số tiền thanh toán phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "Chọn phương thức thanh toán")
    private PaymentMethod method;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
