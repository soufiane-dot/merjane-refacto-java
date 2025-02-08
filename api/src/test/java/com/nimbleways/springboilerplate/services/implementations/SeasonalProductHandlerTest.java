package com.nimbleways.springboilerplate.services.implementations;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
class SeasonalProductHandlerTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private NotificationService notificationService;
    private SeasonalProductHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SeasonalProductHandler(productRepository, notificationService);
        when(productRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
    }

    @Test
    void handleInSeasonProduct() {
        LocalDate now = LocalDate.now();
        Product product = new Product(1L, 15, 5, "SEASONAL", "Test Product", null,
                now.minusDays(5), now.plusDays(10));

        handler.handle(product);

        assertEquals(4, product.getAvailable());
        verify(productRepository).save(product);
        verify(notificationService, never()).sendOutOfStockNotification(anyString());
    }

    @Test
    void handleOutOfSeasonProduct() {
        LocalDate now = LocalDate.now();
        Product product = new Product(1L, 15, 5, "SEASONAL", "Test Product", null,
                now.plusDays(10), now.plusDays(20));

        handler.handle(product);

        assertEquals(5, product.getAvailable());
        verify(productRepository).save(product);
        verify(notificationService).sendOutOfStockNotification("Test Product");
    }

    @Test
    void handleNoStockProduct() {
        LocalDate now = LocalDate.now();
        Product product = new Product(1L, 15, 0, "SEASONAL", "Test Product", null,
                now.minusDays(5), now.plusDays(10));

        handler.handle(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository).save(product);
        verify(notificationService).sendOutOfStockNotification("Test Product");
    }
}
