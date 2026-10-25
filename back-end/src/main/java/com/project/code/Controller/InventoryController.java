package com.project.code.Controller;

import com.project.code.Model.CombinedRequest;
import com.project.code.Model.Inventory;
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
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ServiceClass serviceClass;

    @PutMapping
    public Map<String, String> updateInventory(@RequestBody CombinedRequest request) {
        Map<String, String> map = new HashMap<>();
        Product product = request.getProduct();
        Inventory inventory = request.getInventory();

        if (!serviceClass.ValidateProductId(product.getId())) {
            map.put("message", "Id " + product.getId() + " not present in database");
            return map;
        }

        try {
            productRepository.save(product);
            map.put("message", "Successfully updated product with id: " + product.getId());

            if (inventory != null) {
                Inventory existing = serviceClass.getInventoryId(inventory);
                if (existing != null) {
                    inventory.setId(existing.getId());
                    inventoryRepository.save(inventory);
                    map.put("message", "Successfully updated product");
                } else {
                    map.put("message", "No data available");
                }
            }
        } catch (DataIntegrityViolationException e) {
            map.put("message", "Error: " + e.getMessage());
        } catch (Exception e) {
            map.put("message", "Error: " + e.getMessage());
        }
        return map;
    }

    @PostMapping
    public Map<String, String> saveInventory(@RequestBody Inventory inventory) {
        Map<String, String> map = new HashMap<>();
        try {
            if (!serviceClass.validateInventory(inventory)) {
                map.put("message", "Data is already present");
            } else {
                inventoryRepository.save(inventory);
                map.put("message", "Data saved successfully");
            }
        } catch (DataIntegrityViolationException e) {
            map.put("message", "Error: " + e.getMessage());
        } catch (Exception e) {
            map.put("message", "Error: " + e.getMessage());
        }
        return map;
    }

    @GetMapping("/{storeid}")
    public Map<String, Object> getAllProducts(@PathVariable("storeid") Long storeid) {
        Map<String, Object> map = new HashMap<>();
        List<Product> products = productRepository.findProductsByStoreId(storeid);
        map.put("products", products);
        return map;
    }

    @GetMapping("filter/{category}/{name}/{storeid}")
    public Map<String, Object> getProductName(@PathVariable("category") String category,
                                              @PathVariable("name") String name,
                                              @PathVariable("storeid") Long storeid) {
        Map<String, Object> map = new HashMap<>();
        List<Product> products;

        if (category.equals("null") && name.equals("null")) {
            products = productRepository.findProductsByStoreId(storeid);
        } else if (category.equals("null")) {
            products = productRepository.findByNameLike(storeid, name);
        } else if (name.equals("null")) {
            products = productRepository.findByCategoryAndStoreId(storeid, category);
        } else {
            products = productRepository.findByNameAndCategory(storeid, name, category);
        }

        map.put("product", products);
        return map;
    }

    @GetMapping("search/{name}/{storeId}")
    public Map<String, Object> searchProduct(@PathVariable("name") String name,
                                             @PathVariable("storeId") Long storeId) {
        Map<String, Object> map = new HashMap<>();
        map.put("product", productRepository.findByNameLike(storeId, name));
        return map;
    }

    @DeleteMapping("/{id}")
    public Map<String, String> removeProduct(@PathVariable("id") Long id) {
        Map<String, String> map = new HashMap<>();
        if (!serviceClass.ValidateProductId(id)) {
            map.put("message", "Product not present in database");
            return map;
        }
        inventoryRepository.deleteByProductId(id);
        map.put("message", "Deleted product successfully with id: " + id);
        return map;
    }

    @GetMapping("validate/{quantity}/{storeId}/{productId}")
    public boolean validateQuantity(@PathVariable("quantity") Integer quantity,
                                    @PathVariable("storeId") Long storeId,
                                    @PathVariable("productId") Long productId) {
        Inventory inventory = inventoryRepository.findByProductIdandStoreId(productId, storeId);
        if (inventory == null || inventory.getStockLevel() == null) {
            return false;
        }
        return inventory.getStockLevel() >= quantity;
    }
}