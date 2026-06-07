package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.ProductDTO;
import java.util.List;

public interface ProductService {
    List<ProductDTO> getAllProducts();
    ProductDTO getProductById(Integer id);
    List<ProductDTO> getProductsByCategory(Integer categoryId);
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO updateProduct(Integer id, ProductDTO productDTO);
    void deleteProduct(Integer id);
}