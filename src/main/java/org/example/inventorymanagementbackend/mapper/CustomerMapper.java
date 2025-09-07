package org.example.inventorymanagementbackend.mapper;

import org.example.inventorymanagementbackend.dto.request.CustomerRequest;
import org.example.inventorymanagementbackend.dto.response.CustomerResponse;
import org.example.inventorymanagementbackend.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    @Mapping(target = "availableCredit", expression = "java(customer.getAvailableCredit())")
    @Mapping(target = "creditUtilizationPercentage", expression = "java(customer.getCreditUtilizationPercentage())")
    @Mapping(target = "hasCreditRisk", expression = "java(customer.hasCreditRisk())")
    @Mapping(target = "hasHighCreditUsage", expression = "java(customer.hasHighCreditUsage())")
    @Mapping(target = "fullContactInfo", expression = "java(customer.getFullContactInfo())")
    @Mapping(target = "fullAddress", expression = "java(customer.getFullAddress())")
    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "sales", ignore = true)
    Customer toEntity(CustomerRequest request);
}