package com.cakedelight.rating.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cakedelight.rating.entity.Rating;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByCakeId(Long cakeId);
}