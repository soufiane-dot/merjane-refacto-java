package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductTypeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NormalProductHandler implements ProductTypeHandler {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Autowired
    public NormalProductHandler(ProductRepository productRepository, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    @Override
    public String getType() {
        return "NORMAL";
    }

    @Override
    public void handle(Product product) {
        if (product.getAvailable() > 0) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
        } else {
            int leadTime = product.getLeadTime();
            if (leadTime > 0) {
                product.setLeadTime(leadTime);
                productRepository.save(product);
                notificationService.sendDelayNotification(leadTime, product.getName());
            }
        }
    }
}
