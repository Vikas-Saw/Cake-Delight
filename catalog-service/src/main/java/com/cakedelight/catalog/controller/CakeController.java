package com.cakedelight.catalog.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cakedelight.catalog.entity.Cake;
import com.cakedelight.catalog.service.CakeService;

@RestController
@RequestMapping("/cakes")
public class CakeController {

    @Autowired
    private CakeService cakeService;

    // Add Cake
    @PostMapping
    public Cake addCake(@RequestBody Cake cake) {
        return cakeService.saveCake(cake);
    }

    // Get All Cakes
    @GetMapping
    public List<Cake> getAllCakes() {
        return cakeService.getAllCakes();
    }

    // Get Cake By ID
    @GetMapping("/{id}")
    public ResponseEntity<Cake> getCakeById(@PathVariable Long id) {

        Cake cake = cakeService.getCakeById(id);

        if (cake == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(cake);
    }

    // Update Cake
    @PutMapping("/{id}")
    public Cake updateCake(
            @PathVariable Long id,
            @RequestBody Cake cake) {

        return cakeService.updateCake(id, cake);
    }

    // Delete Cake
    @DeleteMapping("/{id}")
    public String deleteCake(@PathVariable Long id) {

        cakeService.deleteCake(id);

        return "Cake deleted successfully";
    }

    // Filter by Name
    @GetMapping("/filter/name")
    public List<Cake> filterByName(@RequestParam String name) {

        return cakeService.filterByName(name);
    }

    // Filter by Category
    @GetMapping("/filter/category")
    public List<Cake> filterByCategory(@RequestParam String category) {

        return cakeService.filterByCategory(category);
    }

    // Filter by Price Range
    @GetMapping("/filter/price")
    public List<Cake> filterByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {

        return cakeService.filterByPriceRange(minPrice, maxPrice);
    }
}