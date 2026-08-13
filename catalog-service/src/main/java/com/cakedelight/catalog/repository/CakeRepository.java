package com.cakedelight.catalog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cakedelight.catalog.entity.Cake;

public interface CakeRepository extends JpaRepository<Cake, Long> {

    List<Cake> findByNameContainingIgnoreCase(String name);

    List<Cake> findByCategoryIgnoreCase(String category);

    List<Cake> findByPriceBetween(Double minPrice, Double maxPrice);
}