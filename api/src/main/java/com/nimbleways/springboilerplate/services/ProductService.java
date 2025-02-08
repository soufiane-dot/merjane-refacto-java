package com.nimbleways.springboilerplate.services;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.nimbleways.springboilerplate.entities.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;

@Service
public class ProductService {

    private final Map<String, ProductTypeHandler> handlers;

    @Autowired
    public ProductService(List<ProductTypeHandler> handlers) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(ProductTypeHandler::getType, Function.identity()));
    }

    public void processProductsForOrder(Order order) {
        order.getItems().forEach(this::processProduct);
    }

    private void processProduct(Product product) {
        ProductTypeHandler handler = handlers.get(product.getType());
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported product type: " + product.getType());
        }
        handler.handle(product);
    }
}
