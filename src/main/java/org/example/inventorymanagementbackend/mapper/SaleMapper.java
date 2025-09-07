package org.example.inventorymanagementbackend.mapper;

import org.example.inventorymanagementbackend.dto.response.SaleResponse;
import org.example.inventorymanagementbackend.entity.Sale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {SaleItemMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SaleMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "isCheckPayment", expression = "java(sale.isCheckPayment())")
    @Mapping(target = "isCheckOverdue", expression = "java(sale.isCheckOverdue())")
    @Mapping(target = "isCheckDueSoon", expression = "java(sale.isCheckDueSoon())")
    @Mapping(target = "saleDescription", expression = "java(sale.getSaleDescription())")
    SaleResponse toResponse(Sale sale);
}
