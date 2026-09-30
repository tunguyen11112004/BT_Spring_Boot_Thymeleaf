package org.fp.bt_qlsach.service;

import org.fp.bt_qlsach.entity.FinePolicy;
import org.fp.bt_qlsach.util.BusinessException;
import org.fp.bt_qlsach.repository.FinePolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FinePolicyService {

    private final FinePolicyRepository finePolicyRepository;

    public FinePolicyService(FinePolicyRepository finePolicyRepository) {
        this.finePolicyRepository = finePolicyRepository;
    }

    @Transactional(readOnly = true)
    public List<FinePolicy> findAll() {
        return finePolicyRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public FinePolicy requireActive() {
        return finePolicyRepository.findFirstByActiveTrue()
                .orElseThrow(() -> new BusinessException("Chưa có chính sách phí phạt đang áp dụng."));
    }

    @Transactional
    public FinePolicy create(BigDecimal dailyFineAmount, BigDecimal maxFineAmount, int graceDays) {
        if (dailyFineAmount == null || dailyFineAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Phí mỗi ngày phải lớn hơn 0.");
        }
        if (graceDays < 0) {
            throw new BusinessException("Số ngày miễn phạt không được âm.");
        }
        if (maxFineAmount != null && maxFineAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Trần phí không được âm.");
        }
        finePolicyRepository.deactivateAll();
        FinePolicy policy = new FinePolicy();
        policy.setDailyFineAmount(dailyFineAmount);
        policy.setMaxFineAmount(maxFineAmount);
        policy.setGraceDays(graceDays);
        policy.setActive(true);
        return finePolicyRepository.save(policy);
    }
}
