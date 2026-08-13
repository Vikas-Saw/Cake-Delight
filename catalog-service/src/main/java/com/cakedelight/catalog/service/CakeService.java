package com.cakedelight.catalog.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cakedelight.catalog.entity.Cake;
import com.cakedelight.catalog.repository.CakeRepository;

@Service
public class CakeService {

    @Autowired
    private CakeRepository cakeRepository;

    // Save Cake
    public Cake saveCake(Cake cake) {
        return cakeRepository.save(cake);
    }

    // Get All Cakes
    public List<Cake> getAllCakes() {
        return cakeRepository.findAll();
    }

    // Get Cake By Id
    public Cake getCakeById(Long id) {
        return cakeRepository.findById(id).orElse(null);
    }

    // Update Cake
    public Cake updateCake(Long id, Cake cake) {

        Cake existingCake = cakeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cake not found"));

        existingCake.setName(cake.getName());
        existingCake.setCategory(cake.getCategory());
        existingCake.setPrice(cake.getPrice());
        existingCake.setAvailable(cake.getAvailable());	

        return cakeRepository.save(existingCake);
    }

    // Delete Cake
    public void deleteCake(Long id) {
        cakeRepository.deleteById(id);
    }
    
    // Filter by Name
    public List<Cake> filterByName(String name) {
        return cakeRepository.findByNameContainingIgnoreCase(name);
    }

    // Filter by Category
    public List<Cake> filterByCategory(String category) {
        return cakeRepository.findByCategoryIgnoreCase(category);
    }

    // Filter by Price Range
    public List<Cake> filterByPriceRange(Double minPrice, Double maxPrice) {
        return cakeRepository.findByPriceBetween(minPrice, maxPrice);
    }
}