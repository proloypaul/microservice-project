package com.project.product_service.service;

import com.project.product_service.dto.ProductRequestDto;
import com.project.product_service.dto.ProductResponseDto;
import com.project.product_service.mapper.ProductMapper;
import com.project.product_service.model.Product;
import com.project.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponseDto createProduct(ProductRequestDto productRequestDto){

       Product product = productRepository.save(ProductMapper.toModel(productRequestDto));


       log.info("Product {} is saved", product.getId());
       return ProductMapper.toDto(product);
    }


    public List<ProductResponseDto> getAllProducts(){
        List<Product> products = productRepository.findAll();

        return products.stream().map(ProductMapper::toDto).toList();
    }

}
