package com.marketplace.services;

import com.marketplace.dto.ProductResponse;
import com.marketplace.entities.Product;
import com.marketplace.exceptions.ProductNotFoundException;
import com.marketplace.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Page<ProductResponse> findAll(String search, Pageable pageable) {
        Page<Product> products;
        if (search == null || search.isBlank()) {
            products = productRepository.findAll(pageable);
        } else {
            products = productRepository.findByNameContainingIgnoreCase(search.strip(), pageable);
        }

        return products.map(ProductResponse::from);
    }

    public ProductResponse findById(Long id) {
        if (id == null) {
            throw new ProductNotFoundException(id);
        }

        return productRepository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
