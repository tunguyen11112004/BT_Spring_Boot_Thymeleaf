package org.fp.bt_qlsach.service;

import org.fp.bt_qlsach.entity.Category;
import org.fp.bt_qlsach.util.BusinessException;
import org.fp.bt_qlsach.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public Category create(String name, String description) {
        String normalized = name.trim();
        if (categoryRepository.findByNameIgnoreCase(normalized).isPresent()) {
            throw new BusinessException("Thể loại đã tồn tại.");
        }
        Category category = new Category();
        category.setName(normalized);
        category.setDescription(description == null ? null : description.trim());
        return categoryRepository.save(category);
    }
}
