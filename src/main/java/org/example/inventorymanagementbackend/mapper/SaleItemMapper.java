package org.example.inventorymanagementbackend.mapper;

import org.example.inventorymanagementbackend.dto.response.SaleItemResponse;
import org.example.inventorymanagementbackend.entity.Inventory;
import org.example.inventorymanagementbackend.entity.SaleItem;
import org.example.inventorymanagementbackend.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SaleItemMapper {

    @Autowired
    private InventoryRepository inventoryRepository;

    public SaleItemResponse toResponse(SaleItem saleItem) {
        if (saleItem == null) {
            return null;
        }

        SaleItemResponse response = new SaleItemResponse();
        response.setId(saleItem.getId());
        response.setSaleId(saleItem.getSale().getId());
        response.setProductId(saleItem.getProduct().getId());
        response.setProductCode(saleItem.getProduct().getCode());
        response.setProductName(saleItem.getProduct().getName());
        response.setQuantity(saleItem.getQuantity());
        response.setUnitPrice(saleItem.getUnitPrice());
        response.setDiscount(saleItem.getDiscount());
        response.setLineTotal(saleItem.getLineTotal());
        response.setInventoryId(saleItem.getInventoryId());
        response.setCreatedAt(saleItem.getCreatedAt());
        response.setUpdatedAt(saleItem.getUpdatedAt());

        // Fetch inventory batch details if inventory ID exists
        if (saleItem.getInventoryId() != null) {
            inventoryRepository.findById(saleItem.getInventoryId())
                    .ifPresent(inventory -> {
                        response.setInventoryUnitPrice(inventory.getUnitPrice());
                        response.setInventoryDate(inventory.getDate());
                    });
        }

        return response;
    }

    public SaleItemResponse toResponseWithInventory(SaleItem saleItem, Inventory inventory) {
        SaleItemResponse response = toResponse(saleItem);
        
        if (inventory != null) {
            response.setInventoryUnitPrice(inventory.getUnitPrice());
            response.setInventoryDate(inventory.getDate());
        }
        
        return response;
    }
}