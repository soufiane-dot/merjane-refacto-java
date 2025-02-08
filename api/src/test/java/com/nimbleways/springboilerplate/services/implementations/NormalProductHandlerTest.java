package com.nimbleways.springboilerplate.services.implementations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.*;

@ExtendWith(SpringExtension.class)
class NormalProductHandlerTest {
    @Mock private ProductRepository productRepository;
    @Mock private NotificationService notificationService;
    private NormalProductHandler handler;

    @BeforeEach
    void setUp() {
        handler = new NormalProductHandler(productRepository, notificationService);
        when(productRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
    }

    @Test
    void handleAvailableProduct() {
        Product product = new Product(1L, 15, 5, "NORMAL", "Test Product", null, null, null);

        handler.handle(product);

        verify(productRepository).save(product);
        verify(notificationService, never()).sendDelayNotification(anyInt(), anyString());
    }

    @Test
    void handleUnavailableProductWithLeadTime() {
        Product product = new Product(1L, 15, 0, "NORMAL", "Test Product", null, null, null);

        handler.handle(product);

        verify(productRepository).save(product);
        verify(notificationService).sendDelayNotification(15, "Test Product");
    }
}
