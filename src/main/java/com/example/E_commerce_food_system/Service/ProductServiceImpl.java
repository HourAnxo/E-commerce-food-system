package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.ProductDTO;
import com.example.E_commerce_food_system.Entity.Product;
import com.example.E_commerce_food_system.Service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Override
    public List<Product> getAll() {
        return null;
    }

    @Override
    public Product getById(Integer id) {
        return null;
    }

    @Override
    public Product create(ProductDTO dto) {
        return null;
    }

    @Override
    public Product update(Integer id, ProductDTO dto) {
        return null;
    }

    @Override
    public void delete(Integer id) {

    }
}