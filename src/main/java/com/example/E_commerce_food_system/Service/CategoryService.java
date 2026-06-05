package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.CategoryDTO;    // ← check this import
import com.example.E_commerce_food_system.Entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAll();
    Category getById(Integer id);
    Category create(CategoryDTO dto);        // ← must be CategoryDTO, NOT Category
    Category update(Integer id, CategoryDTO dto);
    void delete(Integer id);
}