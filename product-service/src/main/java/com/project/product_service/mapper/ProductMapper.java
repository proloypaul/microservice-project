package com.project.product_service.mapper;

import com.project.product_service.dto.ProductRequestDto;
import com.project.product_service.dto.ProductResponseDto;
import com.project.product_service.model.Product;

public class ProductMapper {
    public static ProductResponseDto toDto(Product product){
        ProductResponseDto productResponseDto = new ProductResponseDto();
        productResponseDto.setId(product.getId());
        productResponseDto.setName(product.getName());
        productResponseDto.setDescription(product.getDescription());
        productResponseDto.setSkuCode(product.getSkuCode());
        productResponseDto.setPrice(product.getPrice());


        return productResponseDto;
    }

    public static Product toModel(ProductRequestDto productRequestDto){
        Product product = new Product();

        product.setName(productRequestDto.getName());
        product.setDescription(productRequestDto.getDescription());
        product.setSkuCode(productRequestDto.getSkuCode());
        product.setPrice(productRequestDto.getPrice());

        return product;
    }
}
