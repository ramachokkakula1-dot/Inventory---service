package com.example.demo.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.entity.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
}
