package com.cakedelight.rating.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cakedelight.rating.entity.Rating;
import com.cakedelight.rating.security.JwtService;
import com.cakedelight.rating.service.RatingService;

@RestController
@RequestMapping("/ratings")
public class RatingController {

    private final RatingService ratingService;
    private final JwtService jwtService;

    public RatingController(
            RatingService ratingService,
            JwtService jwtService) {

        this.ratingService = ratingService;
        this.jwtService = jwtService;
    }

    // =========================
    // SUBMIT RATING
    // =========================
    @PostMapping
    public ResponseEntity<?> addRating(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Rating rating) {

        try {

            if (authorization == null ||
                    !authorization.startsWith("Bearer ")) {

                return ResponseEntity.badRequest()
                        .body("Missing JWT token");
            }

            String token = authorization.substring(7);

            if (!jwtService.isValidToken(token)) {

                return ResponseEntity.status(401)
                        .body("Invalid JWT token");
            }

            Long userId = jwtService.extractUserId(token);

            rating.setUserId(userId);

            return ResponseEntity.ok(
                    ratingService.addRating(
                            rating,
                            authorization
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }


    // =========================
    // GET ALL RATINGS
    // =========================
    @GetMapping
    public List<Rating> getAllRatings() {

        return ratingService.getAllRatings();
    }


    // =========================
    // GET MY RATINGS
    // =========================
    @GetMapping("/my")
    public ResponseEntity<?> getMyRatings(
            @RequestHeader("Authorization") String authorization) {

        try {

            if (authorization == null ||
                    !authorization.startsWith("Bearer ")) {

                return ResponseEntity.badRequest()
                        .body("Missing JWT token");
            }

            String token = authorization.substring(7);

            if (!jwtService.isValidToken(token)) {

                return ResponseEntity.status(401)
                        .body("Invalid JWT token");
            }

            Long userId = jwtService.extractUserId(token);

            List<Rating> ratings =
                    ratingService.getRatingsByUserId(userId);

            return ResponseEntity.ok(ratings);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }


    // =========================
    // GET RATINGS FOR CAKE
    // =========================
    @GetMapping("/cake/{cakeId}")
    public List<Rating> getRatingsByCakeId(
            @PathVariable Long cakeId) {

        return ratingService.getRatingsByCakeId(cakeId);
    }


    // =========================
    // GET AVERAGE RATING
    // =========================
    @GetMapping("/cake/{cakeId}/average")
    public ResponseEntity<Double> getAverageRating(
            @PathVariable Long cakeId) {

        return ResponseEntity.ok(
                ratingService.getAverageRating(cakeId)
        );
    }
}