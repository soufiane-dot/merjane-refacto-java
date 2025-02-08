package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.entities.Product;

public interface ProductTypeHandler {
    String getType();
    void handle(Product product);
}
