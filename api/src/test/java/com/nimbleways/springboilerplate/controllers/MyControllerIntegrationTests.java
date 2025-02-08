package com.nimbleways.springboilerplate.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
// Specify the controller class you want to test
// This indicates to spring boot to only load UsersController into the context
// Which allows a better performance and needs to do less mocks
@SpringBootTest
@AutoConfigureMockMvc
public class MyControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;


    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    public void processOrder_Success() throws Exception {
        Product product = new Product(null, 10, 5, "NORMAL", "Test Product", null, null, null);
        product = productRepository.save(product);

        Set<Product> orderItems = new HashSet<>();
        orderItems.add(product);
        Order order = new Order();
        order.setItems(orderItems);
        order = orderRepository.save(order);

        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()));

        Product updatedProduct = productRepository.findById(product.getId()).get();
        assertEquals(4, updatedProduct.getAvailable());
    }

    @Test
    public void processOrder_NotFound() throws Exception {
        mockMvc.perform(post("/orders/{orderId}/processOrder", 999L)
                        .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void processOrder_ExpiredProduct() throws Exception {
        Product expiredProduct = new Product(null, 15, 5, "EXPIRABLE", "Expired Product",
                LocalDate.now().minusDays(1), null, null);
        expiredProduct = productRepository.save(expiredProduct);

        Set<Product> orderItems = new HashSet<>();
        orderItems.add(expiredProduct);
        Order order = new Order();
        order.setItems(orderItems);
        order = orderRepository.save(order);

        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        verify(notificationService).sendExpirationNotification(
                expiredProduct.getName(), expiredProduct.getExpiryDate());
        Product updatedProduct = productRepository.findById(expiredProduct.getId()).get();
        assertEquals(0, updatedProduct.getAvailable());
    }

    @Test
    public void processOrder_SeasonalProduct() throws Exception {
        Product seasonalProduct = new Product(null, 15, 5, "SEASONAL", "Seasonal Product",
                null, LocalDate.now().plusDays(10), LocalDate.now().plusDays(20));
        seasonalProduct = productRepository.save(seasonalProduct);

        Set<Product> orderItems = new HashSet<>();
        orderItems.add(seasonalProduct);
        Order order = new Order();
        order.setItems(orderItems);
        order = orderRepository.save(order);

        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        verify(notificationService).sendOutOfStockNotification(seasonalProduct.getName());
    }
}
