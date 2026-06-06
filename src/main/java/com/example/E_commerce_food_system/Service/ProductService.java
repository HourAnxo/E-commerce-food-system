package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.ProductDTO;
import com.example.E_commerce_food_system.Entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> getAll();
    Product getById(Integer id);
    Product create(ProductDTO dto);
    Product update(Integer id, ProductDTO dto);
    void delete(Integer id);


}
