package org.example.inventorymanagementbackend.dto.response;

import lombok.Data;
import org.example.inventorymanagementbackend.entity.Supplier;

import java.time.LocalDateTime;

@Data
public class SupplierResponse extends Supplier {

    private Long id;
    private String uniqueSupplierCode;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields from entity business logic methods
    private String fullContactInfo;
    private String fullAddress;
    private boolean hasCompleteContactInfo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueSupplierCode() {
        return uniqueSupplierCode;
    }

    public void setUniqueSupplierCode(String uniqueSupplierCode) {
        this.uniqueSupplierCode = uniqueSupplierCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFullContactInfo() {
        return fullContactInfo;
    }

    public void setFullContactInfo(String fullContactInfo) {
        this.fullContactInfo = fullContactInfo;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public boolean isHasCompleteContactInfo() {
        return hasCompleteContactInfo;
    }

    public void setHasCompleteContactInfo(boolean hasCompleteContactInfo) {
        this.hasCompleteContactInfo = hasCompleteContactInfo;
    }
}