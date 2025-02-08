package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductTypeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SeasonalProductHandler implements ProductTypeHandler {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Autowired
    public SeasonalProductHandler(ProductRepository productRepository, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }
    @Override
    public String getType() {
        return "SEASONAL";
    }

    @Override
    public void handle(Product product) {
        LocalDate now = LocalDate.now();
        if (now.isAfter(product.getSeasonStartDate())
                && now.isBefore(product.getSeasonEndDate())
                && product.getAvailable() > 0) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
        } else {
            if (now.plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate())) {
                notificationService.sendOutOfStockNotification(product.getName());
                product.setAvailable(0);
                productRepository.save(product);
            } else if (product.getSeasonStartDate().isAfter(now)) {
                notificationService.sendOutOfStockNotification(product.getName());
                productRepository.save(product);
            } else {
                product.setLeadTime(product.getLeadTime());
                productRepository.save(product);
                notificationService.sendDelayNotification(product.getLeadTime(), product.getName());
            }
        }
    }
}
