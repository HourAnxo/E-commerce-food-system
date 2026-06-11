package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.CategoryDTO;    // ← check this import
import com.example.E_commerce_food_system.Entity.Category;
import com.example.E_commerce_food_system.Repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
    }

    @Override
    public Category create(CategoryDTO dto) {
        Category category = new Category();
        category.setCategoryName(dto.getCategoryName());  // ← fix here
        return categoryRepository.save(category);
    }

    @Override
    public Category update(Integer id, CategoryDTO dto) {
        Category category = getById(id);
        category.setCategoryName(dto.getCategoryName());  // ← fix here
        return categoryRepository.save(category);
    }

    @Override
    public void delete(Integer id) {
        categoryRepository.deleteById(id);
    }
}