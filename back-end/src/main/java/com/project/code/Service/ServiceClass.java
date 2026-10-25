package com.project.code.Service;

import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ServiceClass {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public ServiceClass(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    // false if an inventory record already exists for this product + store
    public boolean validateInventory(Inventory inventory) {
        Inventory existing = inventoryRepository.findByProductIdandStoreId(
                inventory.getProduct().getId(), inventory.getStore().getId());
        return existing == null;
    }

    // false if a product with the same name already exists
    public boolean validateProduct(Product product) {
        Product existing = productRepository.findByName(product.getName());
        return existing == null;
    }

    // false if no product exists with this id
    public boolean ValidateProductId(long id) {
        Product product = productRepository.findByid(id);
        return product != null;
    }

    public Inventory getInventoryId(Inventory inventory) {
        return inventoryRepository.findByProductIdandStoreId(
                inventory.getProduct().getId(), inventory.getStore().getId());
    }
}