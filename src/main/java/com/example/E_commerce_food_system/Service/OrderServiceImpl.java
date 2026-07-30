package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.OrderDTO;
import com.example.E_commerce_food_system.DTO.OrderItemDTO;
import com.example.E_commerce_food_system.Entity.Customer;
import com.example.E_commerce_food_system.Entity.OrderItem;
import com.example.E_commerce_food_system.Entity.Orders;
import com.example.E_commerce_food_system.Entity.Product;
import com.example.E_commerce_food_system.Repository.CustomerRepository;
import com.example.E_commerce_food_system.Repository.OrderItemRepository;
import com.example.E_commerce_food_system.Repository.OrderRepository;
import com.example.E_commerce_food_system.Repository.ProductRepository;
import com.example.E_commerce_food_system.Service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CustomerRepository customerRepository,
                            OrderItemRepository orderItemRepository,
                            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    // Map Entity -> DTO
    private OrderDTO toDTO(Orders order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setCustomerId(order.getCustomer().getCustomerId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setItems(orderItemRepository.findByOrder_OrderId(order.getOrderId())
                .stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    private OrderItemDTO toItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setOrderItemId(item.getOrderItemId());
        dto.setProductId(item.getProduct().getProductId());
        dto.setProductName(item.getProduct().getProductName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
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

    /**
     * Saves the order and its line items, deducting the bought quantity from each
     * product's stock. Transactional so a single out-of-stock item rolls the whole
     * order back instead of leaving earlier items already deducted.
     *
     * <p>An order sent without items is still accepted (nothing to deduct) — that is
     * how orders created straight through the API behave.
     */
    @Override
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Orders order = orderRepository.save(toEntity(orderDTO));

        if (orderDTO.getItems() != null) {
            for (OrderItemDTO itemDTO : orderDTO.getItems()) {
                orderItemRepository.save(toItemEntity(order, itemDTO));
            }
        }
        return toDTO(order);
    }

    /** Builds one line item and takes its quantity out of the product's stock. */
    private OrderItem toItemEntity(Orders order, OrderItemDTO dto) {
        if (dto.getProductId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Order item is missing a productId");
        }
        int quantity = dto.getQuantity() == null ? 0 : dto.getQuantity();
        if (quantity < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + dto.getProductId()));

        deductStock(product, quantity);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        // Trust the product's current price rather than whatever the client sent.
        item.setUnitPrice(product.getPrice());
        return item;
    }

    /**
     * Takes {@code quantity} off the product's stock. A null stock_quantity means the
     * product is not stock-tracked (the UI treats null as always available), so it is
     * left alone; anything else must cover the order or the whole order is refused.
     */
    private void deductStock(Product product, int quantity) {
        Integer stock = product.getStockQuantity();
        if (stock == null) {
            return;
        }
        if (stock < quantity) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Not enough stock for " + product.getProductName()
                            + " — only " + stock + " left");
        }
        product.setStockQuantity(stock - quantity);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public OrderDTO updateOrder(Integer id, OrderDTO orderDTO) {
        Orders order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found with id: " + id));

        Customer customer = customerRepository.findById(orderDTO.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found with id: " + orderDTO.getCustomerId()));

        Orders.OrderStatus previousStatus = order.getOrderStatus();

        order.setCustomer(customer);
        order.setOrderDate(orderDTO.getOrderDate());
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setOrderStatus(orderDTO.getOrderStatus());

        applyStockForStatusChange(order, previousStatus, orderDTO.getOrderStatus());

        return toDTO(orderRepository.save(order));
    }

    /**
     * Keeps stock in step with the order's status. An order holds its items' stock for
     * as long as it is alive; cancelling releases it back, and un-cancelling takes it
     * again (which can fail if the stock has since been sold to someone else).
     *
     * <p>Only the transitions across {@code Cancelled} move stock, so re-saving an order
     * that was already cancelled — as the admin form does on every edit — never double-
     * restores it.
     */
    private void applyStockForStatusChange(Orders order,
                                           Orders.OrderStatus previous,
                                           Orders.OrderStatus next) {
        if (next == null || previous == next) {
            return;
        }
        boolean cancelling = next == Orders.OrderStatus.Cancelled;
        boolean unCancelling = previous == Orders.OrderStatus.Cancelled;
        if (!cancelling && !unCancelling) {
            return;
        }

        for (OrderItem item : orderItemRepository.findByOrder_OrderId(order.getOrderId())) {
            if (cancelling) {
                restoreStock(item.getProduct(), item.getQuantity());
            } else {
                deductStock(item.getProduct(), item.getQuantity());
            }
        }
    }

    /** Puts {@code quantity} back on the product's stock; a null stock stays untracked. */
    private void restoreStock(Product product, int quantity) {
        Integer stock = product.getStockQuantity();
        if (stock == null) {
            return;
        }
        product.setStockQuantity(stock + quantity);
        productRepository.save(product);
    }

    /**
     * Deletes the order along with its line items and releases any stock it was still
     * holding, so a deleted order does not quietly cost inventory. An already-cancelled
     * order gave its stock back at cancellation time — crediting it again here would
     * invent stock that was never sold.
     *
     * <p>The items are deleted through the repository rather than left to the
     * order_item foreign key: reading them above puts them in the persistence context,
     * and flushing a managed child that still points at a deleted parent fails.
     */
    @Override
    @Transactional
    public void deleteOrder(Integer id) {
        Orders order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found with id: " + id));

        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(id);
        if (order.getOrderStatus() != Orders.OrderStatus.Cancelled) {
            for (OrderItem item : items) {
                restoreStock(item.getProduct(), item.getQuantity());
            }
        }
        orderItemRepository.deleteAll(items);
        orderRepository.delete(order);
    }
}