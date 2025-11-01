package org.example.inventorymanagementbackend.mapper;

import org.example.inventorymanagementbackend.dto.request.ProductRequest;
import org.example.inventorymanagementbackend.dto.response.ProductResponse;
import org.example.inventorymanagementbackend.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface ProductMapper {

    // Map ProductRequest to Product entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "inventoryMovements", ignore = true)
    @Mapping(target = "saleItems", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(source = "currentStock", target = "currentStock")
    @Mapping(source = "lowStockThreshold", target = "lowStockThreshold")
    @Mapping(source = "fixedPrice", target = "fixedPrice")
    @Mapping(source = "discount", target = "discount")
    @Mapping(source = "code", target = "code")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    Product toEntity(ProductRequest request);

    // Map Product entity to ProductResponse
    @Mapping(source = "isActive", target = "isActive")
    ProductResponse toResponse(Product product);

    // Update existing product entity from request
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "inventoryMovements", ignore = true)
    @Mapping(target = "saleItems", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntity(@MappingTarget Product product, ProductRequest request);
}