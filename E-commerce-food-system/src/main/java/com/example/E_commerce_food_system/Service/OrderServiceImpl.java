package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.OrderDTO;
import com.example.E_commerce_food_system.Entity.Customer;
import com.example.E_commerce_food_system.Entity.Orders;
import com.example.E_commerce_food_system.Repository.CustomerRepository;
import com.example.E_commerce_food_system.Repository.OrderRepository;
import com.example.E_commerce_food_system.Service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public OrderServiceImpl(OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    // Map Entity -> DTO
    private OrderDTO toDTO(Orders order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setCustomerId(order.getCustomer().getCustomerId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setOrderStatus(order.getOrderStatus());
        return dto;
    }

    // Map DTO -> Entity
    private Orders toEntity(OrderDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found with id: " + dto.getCustomerId()));

        Orders order = new Orders();
        order.setCustomer(customer);
        order.setOrderDate(dto.getOrderDate());
        order.setTotalAmount(dto.getTotalAmount());
        order.setOrderStatus(dto.getOrderStatus() != null
                ? dto.getOrderStatus()
                : Orders.OrderStatus.Pending);
        return order;
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO getOrderById(Integer id) {
        Orders order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found with id: " + id));
        return toDTO(order);
    }

    @Override
    public List<OrderDTO> getOrdersByCustomerId(Integer customerId) {
        return orderRepository.findByCustomer_CustomerId(customerId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Orders order = toEntity(orderDTO);
        return toDTO(orderRepository.save(order));
    }

    @Override
    public OrderDTO updateOrder(Integer id, OrderDTO orderDTO) {
        Orders order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found with id: " + id));

        Customer customer = customerRepository.findById(orderDTO.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found with id: " + orderDTO.getCustomerId()));

        order.setCustomer(customer);
        order.setOrderDate(orderDTO.getOrderDate());
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setOrderStatus(orderDTO.getOrderStatus());

        return toDTO(orderRepository.save(order));
    }

    @Override
    public void deleteOrder(Integer id) {
        if (!orderRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }
}