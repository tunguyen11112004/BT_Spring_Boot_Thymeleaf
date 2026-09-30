package org.fp.bt_qlsach.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class WaiverForm {

    @NotNull(message = "Nhập số tiền miễn giảm")
    @DecimalMin(value = "0.01", message = "Số tiền miễn giảm phải lớn hơn 0")
    private BigDecimal amount;

    @NotBlank(message = "Nhập lý do miễn giảm")
    @Size(max = 500, message = "Lý do tối đa 500 ký tự")
    private String reason;

    @NotBlank(message = "Nhập người duyệt")
    @Size(max = 120, message = "Người duyệt tối đa 120 ký tự")
    private String approvedBy;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
}
