package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.CustomerDTO;
import java.util.List;

public interface CustomerService {

    List<CustomerDTO> getAllCustomers();

    CustomerDTO getCustomerById(Integer id);

    CustomerDTO getCustomerByEmail(String email);

    CustomerDTO createCustomer(CustomerDTO customerDTO);

    CustomerDTO updateCustomer(Integer id, CustomerDTO customerDTO);

    void deleteCustomer(Integer id);
}