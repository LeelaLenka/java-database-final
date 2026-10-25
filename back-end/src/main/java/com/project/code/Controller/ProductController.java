package com.project.code.Controller;

import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Service.ServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ServiceClass serviceClass;

    @Autowired
    private InventoryRepository inventoryRepository;

    @PostMapping
    public Map<String, String> addProduct(@RequestBody Product product) {
        Map<String, String> map = new HashMap<>();
        if (!serviceClass.validateProduct(product)) {
            map.put("message", "Product already present in database");
            return map;
        }
        try {
            productRepository.save(product);
            map.put("message", "Product added successfully");
        } catch (DataIntegrityViolationException e) {
            map.put("message", "SKU should be unique");
        } catch (Exception e) {
            map.put("message", "Error: " + e.getMessage());
        }
        return map;
    }

    @GetMapping("/product/{id}")
    public Map<String, Object> getProductbyId(@PathVariable("id") Long id) {
        Map<String, Object> map = new HashMap<>();
        Product product = productRepository.findById(id).orElse(null);
        map.put("products", product);
        return map;
    }

    @PutMapping
    public Map<String, String> updateProduct(@RequestBody Product product) {
        Map<String, String> map = new HashMap<>();
        try {
            productRepository.save(product);
            map.put("message", "Data updated successfully");
        } catch (Exception e) {
            map.put("message", "Error: " + e.getMessage());
        }
        return map;
    }

    @GetMapping("/category/{name}/{category}")
    public Map<String, Object> filterbyCategoryProduct(@PathVariable("name") String name,
                                                       @PathVariable("category") String category) {
        Map<String, Object> map = new HashMap<>();
        List<Product> products;

        if (name.equals("null") && category.equals("null")) {
            products = productRepository.findAll();
        } else if (name.equals("null")) {
            products = productRepository.findByCategory(category);
        } else if (category.equals("null")) {
            products = productRepository.findProductBySubName(name);
        } else {
            products = productRepository.findProductBySubNameAndCategory(name, category);
        }

        map.put("products", products);
        return map;
    }

    @GetMapping
    public Map<String, Object> listProduct() {
        Map<String, Object> map = new HashMap<>();
        map.put("products", productRepository.findAll());
        return map;
    }

    @GetMapping("filter/{category}/{storeid}")
    public Map<String, Object> getProductbyCategoryAndStoreId(@PathVariable("category") String category,
                                                              @PathVariable("storeid") Long storeid) {
        Map<String, Object> map = new HashMap<>();
        map.put("product", productRepository.findProductByCategory(category, storeid));
        return map;
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteProduct(@PathVariable("id") Long id) {
        Map<String, String> map = new HashMap<>();
        if (!serviceClass.ValidateProductId(id)) {
            map.put("message", "Product not present in database");
            return map;
        }
        inventoryRepository.deleteByProductId(id);
        productRepository.deleteById(id);
        map.put("message", "Deleted product successfully with id: " + id);
        return map;
    }

    @GetMapping("/searchProduct/{name}")
    public Map<String, Object> searchProduct(@PathVariable("name") String name) {
        Map<String, Object> map = new HashMap<>();
        map.put("products", productRepository.findProductBySubName(name));
        return map;
    }
}