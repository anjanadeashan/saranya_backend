package org.example.inventorymanagementbackend.mapper;

import org.example.inventorymanagementbackend.dto.request.SupplierRequest;
import org.example.inventorymanagementbackend.dto.response.SupplierResponse;
import org.example.inventorymanagementbackend.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupplierMapper {

    @Mapping(target = "fullContactInfo", expression = "java(supplier.getFullContactInfo())")
    @Mapping(target = "fullAddress", expression = "java(supplier.getFullAddress())")
    @Mapping(target = "hasCompleteContactInfo", expression = "java(supplier.hasCompleteContactInfo())")
    SupplierResponse toResponse(Supplier supplier);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "inventoryMovements", ignore = true)
    Supplier toEntity(SupplierRequest request);
}