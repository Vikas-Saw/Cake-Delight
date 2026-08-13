package com.cakedelight.rating.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cakedelight.rating.entity.Rating;
import com.cakedelight.rating.service.RatingService;

@RestController
@RequestMapping("/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    // Submit a rating
    @PostMapping
    public Rating addRating(@RequestBody Rating rating) {
        return ratingService.addRating(rating);
    }

    // Get all ratings
    @GetMapping
    public List<Rating> getAllRatings() {
        return ratingService.getAllRatings();
    }

    // Get ratings for a specific cake
    @GetMapping("/cake/{cakeId}")
    public List<Rating> getRatingsByCakeId(
            @PathVariable Long cakeId) {

        return ratingService.getRatingsByCakeId(cakeId);
    }

    // Get average rating for a specific cake
    @GetMapping("/cake/{cakeId}/average")
    public ResponseEntity<Double> getAverageRating(
            @PathVariable Long cakeId) {

        return ResponseEntity.ok(
                ratingService.getAverageRating(cakeId)
        );
    }
}