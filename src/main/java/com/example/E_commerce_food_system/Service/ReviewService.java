package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.ReviewDTO;
import java.util.List;

public interface ReviewService {
    List<ReviewDTO> getAllReviews();
    ReviewDTO getReviewById(Integer id);
    List<ReviewDTO> getReviewsByProduct(Integer productId);
    ReviewDTO createReview(ReviewDTO dto);
    ReviewDTO updateReview(Integer id, ReviewDTO dto);
    void deleteReview(Integer id);
}