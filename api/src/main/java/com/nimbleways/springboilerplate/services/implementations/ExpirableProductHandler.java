package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductTypeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ExpirableProductHandler implements ProductTypeHandler {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Autowired
    public ExpirableProductHandler(ProductRepository productRepository, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    @Override
    public String getType() {
        return "EXPIRABLE";
    }

    @Override
    public void handle(Product product) {
        LocalDate now = LocalDate.now();
        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(now)) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
        } else {
            notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
            product.setAvailable(0);
            productRepository.save(product);
        }
    }
}
