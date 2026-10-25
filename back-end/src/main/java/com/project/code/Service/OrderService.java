package com.project.code.Service;

import com.project.code.Model.*;
import com.project.code.Repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    public void saveOrder(PlaceOrderRequestDTO placeOrderRequest) {

        // 1. Retrieve or create the customer
        Customer customer = customerRepository.findByEmail(placeOrderRequest.getCustomerEmail());
        if (customer == null) {
            customer = new Customer();
            customer.setName(placeOrderRequest.getCustomerName());
            customer.setEmail(placeOrderRequest.getCustomerEmail());
            customer.setPhone(placeOrderRequest.getCustomerPhone());
            customer = customerRepository.save(customer);
        }

        // 2. Retrieve the store
        Store store = storeRepository.findById(placeOrderRequest.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // 3. Create and save the order details
        OrderDetails orderDetails = new OrderDetails(
                customer,
                store,
                placeOrderRequest.getTotalPrice(),
                LocalDateTime.now()
        );
        orderDetails = orderDetailsRepository.save(orderDetails);

        // 4. For each purchased product: update inventory and save an order item
        for (PurchaseProductDTO purchase : placeOrderRequest.getPurchaseProduct()) {

            Inventory inventory = inventoryRepository.findByProductIdandStoreId(
                    purchase.getId(), placeOrderRequest.getStoreId());

            if (inventory == null) {
                throw new RuntimeException("Inventory not found for product " + purchase.getId());
            }
            if (inventory.getStockLevel() < purchase.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product " + purchase.getId());
            }

            inventory.setStockLevel(inventory.getStockLevel() - purchase.getQuantity());
            inventoryRepository.save(inventory);

            OrderItem orderItem = new OrderItem(
                    orderDetails,
                    inventory.getProduct(),
                    purchase.getQuantity(),
                    purchase.getPrice() * purchase.getQuantity()
            );
            orderItemRepository.save(orderItem);
        }
    }
}