package com.nimbleways.springboilerplate.services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.*;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

@ExtendWith(SpringExtension.class)
@UnitTest
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    private ProductService productService;
    private NormalProductHandler normalHandler;
    private ExpirableProductHandler expirableHandler;
    private SeasonalProductHandler seasonalHandler;

    @BeforeEach
    void setUp() {
        normalHandler = new NormalProductHandler(productRepository, notificationService);
        expirableHandler = new ExpirableProductHandler(productRepository, notificationService);
        seasonalHandler = new SeasonalProductHandler(productRepository, notificationService);

        productService = new ProductService(Arrays.asList(
                normalHandler,
                expirableHandler,
                seasonalHandler
        ));

        doNothing().when(notificationService).sendDelayNotification(anyInt(), anyString());
        doNothing().when(notificationService).sendExpirationNotification(anyString(), any(LocalDate.class));
        doNothing().when(notificationService).sendOutOfStockNotification(anyString());
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);
    }

    @Test
    void processNormalProduct() {
        Product product = new Product(1L, 15, 10, "NORMAL", "Test Product", null, null, null);
        Order order = createOrderWithProducts(product);
        when(productRepository.save(any())).thenReturn(product);

        productService.processProductsForOrder(order);

        verify(productRepository).save(product);
        assertEquals(9, product.getAvailable());
    }

    @Test
    void processExpiredProduct() {
        Product product = new Product(1L, 15, 10, "EXPIRABLE", "Expired Product",
                LocalDate.now().minusDays(1), null, null);
        Order order = createOrderWithProducts(product);
        when(productRepository.save(any())).thenReturn(product);

        productService.processProductsForOrder(order);

        verify(notificationService).sendExpirationNotification(product.getName(), product.getExpiryDate());
        verify(productRepository).save(product);
        assertEquals(0, product.getAvailable());
    }

    @Test
    void processSeasonalProduct() {
        Product product = new Product(1L, 15, 10, "SEASONAL", "Seasonal Product",
                null, LocalDate.now().plusDays(10), LocalDate.now().plusDays(20));
        Order order = createOrderWithProducts(product);
        when(productRepository.save(any())).thenReturn(product);

        productService.processProductsForOrder(order);

        verify(notificationService).sendOutOfStockNotification(product.getName());
        verify(productRepository).save(product);
        assertEquals(10, product.getAvailable());
    }

    @Test
    void processMixedProducts() {
        Product normal = new Product(1L, 15, 10, "NORMAL", "Normal Product", null, null, null);
        Product expired = new Product(2L, 15, 10, "EXPIRABLE", "Expired Product",
                LocalDate.now().minusDays(1), null, null);
        Product seasonal = new Product(3L, 15, 10, "SEASONAL", "Seasonal Product",
                null, LocalDate.now(), LocalDate.now().plusDays(10));

        Order order = createOrderWithProducts(normal, expired, seasonal);
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        productService.processProductsForOrder(order);

        verify(productRepository, times(3)).save(any());
        assertEquals(9, normal.getAvailable());
        assertEquals(0, expired.getAvailable());
        assertEquals(0, seasonal.getAvailable());
    }

    @Test
    void processEmptyOrder() {
        Order order = new Order();
        order.setItems(new HashSet<>());

        productService.processProductsForOrder(order);

        verify(productRepository, never()).save(any());
        verify(notificationService, never()).sendDelayNotification(anyInt(), anyString());
        verify(notificationService, never()).sendExpirationNotification(anyString(), any(LocalDate.class));
        verify(notificationService, never()).sendOutOfStockNotification(anyString());
    }

    private Order createOrderWithProducts(Product... products) {
        Order order = new Order();
        order.setItems(new HashSet<>(Arrays.asList(products)));
        return order;
    }
}
