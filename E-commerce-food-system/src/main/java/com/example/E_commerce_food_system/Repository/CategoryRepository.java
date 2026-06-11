package com.example.E_commerce_food_system.Repository;
import com.example.E_commerce_food_system.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface CategoryRepository extends JpaRepository<Category,Integer>{
}
