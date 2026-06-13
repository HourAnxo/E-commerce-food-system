package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.ReviewDTO;
import com.example.E_commerce_food_system.Entity.Customer;
import com.example.E_commerce_food_system.Entity.Product;
import com.example.E_commerce_food_system.Entity.Review;
import com.example.E_commerce_food_system.Repository.CustomerRepository;
import com.example.E_commerce_food_system.Repository.ProductRepository;
import com.example.E_commerce_food_system.Repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDTO getReviewById(Integer id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return toDTO(review);
    }

    @Override
    public List<ReviewDTO> getReviewsByProduct(Integer productId) {
        return reviewRepository.findAll()
                .stream()
                .filter(r -> r.getProduct().getProductId().equals(productId))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDTO createReview(ReviewDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Review review = new Review();
        review.setCustomer(customer);
        review.setProduct(product);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setCreatedAt(LocalDateTime.now());

        return toDTO(reviewRepository.save(review));
    }

    @Override
    public ReviewDTO updateReview(Integer id, ReviewDTO dto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        return toDTO(reviewRepository.save(review));
    }

    @Override
    public void deleteReview(Integer id) {
        reviewRepository.deleteById(id);
    }

    private ReviewDTO toDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setReviewId(review.getReviewId());
        dto.setCustomerId(review.getCustomer().getCustomerId());
        dto.setProductId(review.getProduct().getProductId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}