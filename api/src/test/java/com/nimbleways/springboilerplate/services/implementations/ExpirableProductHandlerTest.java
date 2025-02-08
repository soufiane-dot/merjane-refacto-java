package com.nimbleways.springboilerplate.services.implementations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@ExtendWith(SpringExtension.class)
class ExpirableProductHandlerTest {
    @Mock private ProductRepository productRepository;
    @Mock private NotificationService notificationService;
    private ExpirableProductHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ExpirableProductHandler(productRepository, notificationService);
        when(productRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
    }

    @Test
    void handleNonExpiredProduct() {
        Product product = new Product(1L, 15, 5, "EXPIRABLE", "Test Product",
                LocalDate.now().plusDays(10), null, null);

        handler.handle(product);

        verify(productRepository).save(product);
        verify(notificationService, never()).sendExpirationNotification(anyString(), any());
    }

    @Test
    void handleExpiredProduct() {
        Product product = new Product(1L, 15, 5, "EXPIRABLE", "Test Product",
                LocalDate.now().minusDays(1), null, null);

        handler.handle(product);

        verify(productRepository).save(product);
        verify(notificationService).sendExpirationNotification(product.getName(), product.getExpiryDate());
    }
}
