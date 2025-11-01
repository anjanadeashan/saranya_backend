package org.example.inventorymanagementbackend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.example.inventorymanagementbackend.dto.request.CustomerRequest;
import org.example.inventorymanagementbackend.dto.response.CustomerResponse;
import org.example.inventorymanagementbackend.entity.Customer;
import org.example.inventorymanagementbackend.mapper.CustomerMapper;
import org.example.inventorymanagementbackend.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        List<Customer> customers = customerRepository.findByIsActiveTrueOrderByNameAsc();
        return customers.stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .filter(c -> c.getIsActive())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        return customerMapper.toResponse(customer);
    }

    public CustomerResponse createCustomer(CustomerRequest request) {
        Customer customer = customerMapper.toEntity(request);
        customer.setIsActive(true);

        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Customer created successfully with id: {}", savedCustomer.getId());

        return customerMapper.toResponse(savedCustomer);
    }

    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer existingCustomer = customerRepository.findById(id)
                .filter(c -> c.getIsActive())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));

        existingCustomer.setName(request.getName());
        existingCustomer.setEmail(request.getEmail());
        existingCustomer.setPhone(request.getPhone());
        existingCustomer.setAddress(request.getAddress());
        existingCustomer.setCity(request.getCity());
        existingCustomer.setCountry(request.getCountry());
        existingCustomer.setCreditLimit(request.getCreditLimit());
        existingCustomer.setOutstandingBalance(request.getOutstandingBalance());

        Customer savedCustomer = customerRepository.save(existingCustomer);
        return customerMapper.toResponse(savedCustomer);
    }

    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .filter(c -> c.getIsActive())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));

        customer.setIsActive(false);
        customerRepository.save(customer);
        logger.info("Customer deleted successfully with id: {}", id);
    }

    @Transactional(readOnly = true)
    public Customer getCustomerEntityById(Long id) {
        return customerRepository.findById(id)
                .filter(c -> c.getIsActive())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    public void updateCustomer(Customer customer) {
    customerRepository.save(customer);
}
}
