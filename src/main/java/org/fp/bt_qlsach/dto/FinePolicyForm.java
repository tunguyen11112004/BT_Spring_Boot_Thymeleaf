package org.fp.bt_qlsach.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class FinePolicyForm {

    @NotNull(message = "Nhập phí mỗi ngày")
    @DecimalMin(value = "0.01", message = "Phí mỗi ngày phải lớn hơn 0")
    private BigDecimal dailyFineAmount;

    @DecimalMin(value = "0.00", message = "Trần phí không được âm")
    private BigDecimal maxFineAmount;

    @NotNull(message = "Nhập số ngày miễn phạt")
    @Min(value = 0, message = "Số ngày miễn phạt không được âm")
    private Integer graceDays;

    public BigDecimal getDailyFineAmount() {
        return dailyFineAmount;
    }

    public void setDailyFineAmount(BigDecimal dailyFineAmount) {
        this.dailyFineAmount = dailyFineAmount;
    }

    public BigDecimal getMaxFineAmount() {
        return maxFineAmount;
    }

    public void setMaxFineAmount(BigDecimal maxFineAmount) {
        this.maxFineAmount = maxFineAmount;
    }

    public Integer getGraceDays() {
        return graceDays;
    }

    public void setGraceDays(Integer graceDays) {
        this.graceDays = graceDays;
    }
}
