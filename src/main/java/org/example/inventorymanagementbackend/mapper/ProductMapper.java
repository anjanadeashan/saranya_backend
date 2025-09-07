package org.example.inventorymanagementbackend.mapper;

import org.example.inventorymanagementbackend.dto.request.ProductRequest;
import org.example.inventorymanagementbackend.dto.response.ProductResponse;
import org.example.inventorymanagementbackend.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "discountedPrice", expression = "java(product.getDiscountedPrice())")
    @Mapping(target = "isLowStock", expression = "java(product.isLowStock())")
    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentStock", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "inventoryMovements", ignore = true)
    @Mapping(target = "saleItems", ignore = true)
    Product toEntity(ProductRequest request);
}