package org.example.inventorymanagementbackend.mapper;

import org.example.inventorymanagementbackend.dto.response.SaleItemResponse;
import org.example.inventorymanagementbackend.entity.SaleItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SaleItemMapper {

    @Mapping(target = "saleId", source = "sale.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productCode", source = "product.code")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "discountAmount", expression = "java(saleItem.getDiscountAmount())")
    @Mapping(target = "discountedUnitPrice", expression = "java(saleItem.getDiscountedUnitPrice())")
    @Mapping(target = "subtotalBeforeDiscount", expression = "java(saleItem.getSubtotalBeforeDiscount())")
    @Mapping(target = "saleItemDescription", expression = "java(saleItem.getSaleItemDescription())")
    SaleItemResponse toResponse(SaleItem saleItem);
}
