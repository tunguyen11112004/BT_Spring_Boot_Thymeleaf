package org.fp.bt_qlsach.repository;

import org.fp.bt_qlsach.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);

    List<Category> findAllByOrderByNameAsc();
}
