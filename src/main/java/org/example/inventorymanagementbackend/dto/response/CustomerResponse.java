package org.example.inventorymanagementbackend.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private BigDecimal creditLimit;
    private BigDecimal outstandingBalance;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal availableCredit;
    private BigDecimal creditUtilizationPercentage;
    private Boolean hasCreditRisk;
    private Boolean hasHighCreditUsage;
    private String fullContactInfo;
    private String fullAddress;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
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

    public BigDecimal getAvailableCredit() {
        return availableCredit;
    }

    public void setAvailableCredit(BigDecimal availableCredit) {
        this.availableCredit = availableCredit;
    }

    public BigDecimal getCreditUtilizationPercentage() {
        return creditUtilizationPercentage;
    }

    public void setCreditUtilizationPercentage(BigDecimal creditUtilizationPercentage) {
        this.creditUtilizationPercentage = creditUtilizationPercentage;
    }

    public Boolean getHasCreditRisk() {
        return hasCreditRisk;
    }

    public void setHasCreditRisk(Boolean hasCreditRisk) {
        this.hasCreditRisk = hasCreditRisk;
    }

    public Boolean getHasHighCreditUsage() {
        return hasHighCreditUsage;
    }

    public void setHasHighCreditUsage(Boolean hasHighCreditUsage) {
        this.hasHighCreditUsage = hasHighCreditUsage;
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
}
