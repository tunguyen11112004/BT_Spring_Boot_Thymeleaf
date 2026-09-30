package org.fp.bt_qlsach.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "fine_policy")
public class FinePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyFineAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxFineAmount;

    @Column(nullable = false)
    private int graceDays;

    @Column(nullable = false)
    private boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public int getGraceDays() {
        return graceDays;
    }

    public void setGraceDays(int graceDays) {
        this.graceDays = graceDays;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
