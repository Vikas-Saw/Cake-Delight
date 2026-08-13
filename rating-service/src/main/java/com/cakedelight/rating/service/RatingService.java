package com.cakedelight.rating.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cakedelight.rating.entity.Rating;
import com.cakedelight.rating.repository.RatingRepository;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;

    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    // Submit a rating
    public Rating addRating(Rating rating) {
        return ratingRepository.save(rating);
    }

    // Get all ratings
    public List<Rating> getAllRatings() {
        return ratingRepository.findAll();
    }

    // Get ratings for a particular cake
    public List<Rating> getRatingsByCakeId(Long cakeId) {
        return ratingRepository.findByCakeId(cakeId);
    }

    // Calculate average rating for a cake
    public double getAverageRating(Long cakeId) {

        List<Rating> ratings = ratingRepository.findByCakeId(cakeId);

        if (ratings.isEmpty()) {
            return 0.0;
        }

        return ratings.stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0);
    }
}