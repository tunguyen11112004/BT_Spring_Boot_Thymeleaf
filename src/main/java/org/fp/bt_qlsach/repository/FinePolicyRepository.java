package org.fp.bt_qlsach.repository;

import org.fp.bt_qlsach.entity.FinePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FinePolicyRepository extends JpaRepository<FinePolicy, Long> {

    Optional<FinePolicy> findFirstByActiveTrue();

    List<FinePolicy> findAllByOrderByIdDesc();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update FinePolicy p set p.active = false where p.active = true")
    int deactivateAll();
}
