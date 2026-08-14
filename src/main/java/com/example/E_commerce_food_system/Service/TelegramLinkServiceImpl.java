package com.example.E_commerce_food_system.Service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.E_commerce_food_system.DTO.CustomerDTO;
import com.example.E_commerce_food_system.Entity.Customer;
import com.example.E_commerce_food_system.Repository.CustomerRepository;

@Service
public class TelegramLinkServiceImpl implements TelegramLinkService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public TelegramLinkServiceImpl(CustomerRepository customerRepository,
                                   PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public CustomerDTO link(Long chatId, String email, String password) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        // Same check as CustomerServiceImpl.login — never reveal which half was wrong.
        if (password == null || !passwordEncoder.matches(password, customer.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // telegram_chat_id is UNIQUE: if this chat was already bound to a different
        // account, release it first or the insert fails on the constraint.
        customerRepository.findByTelegramChatId(chatId)
                .filter(existing -> !existing.getCustomerId().equals(customer.getCustomerId()))
                .ifPresent(existing -> {
                    existing.setTelegramChatId(null);
                    customerRepository.save(existing);
                });

        customer.setTelegramChatId(chatId);
        return toDTO(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public boolean unlink(Long chatId) {
        return customerRepository.findByTelegramChatId(chatId)
                .map(customer -> {
                    customer.setTelegramChatId(null);
                    customerRepository.save(customer);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomerDTO> findByChatId(Long chatId) {
        return customerRepository.findByTelegramChatId(chatId).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findChatIdByCustomerId(Integer customerId) {
        return customerRepository.findById(customerId)
                .map(Customer::getTelegramChatId);
    }

    /** Same contract as CustomerServiceImpl.toDTO — the password hash never leaves. */
    private CustomerDTO toDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerId(customer.getCustomerId());
        dto.setFullName(customer.getFullName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        return dto;
    }
}
