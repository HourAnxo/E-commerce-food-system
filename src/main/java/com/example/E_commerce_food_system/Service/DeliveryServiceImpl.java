package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.DeliveryDTO;
import com.example.E_commerce_food_system.Entity.Delivery;
import com.example.E_commerce_food_system.Entity.DeliveryDecline;
import com.example.E_commerce_food_system.Entity.Orders;
import com.example.E_commerce_food_system.Repository.DeliveryDeclineRepository;
import com.example.E_commerce_food_system.Repository.DeliveryRepository;
import com.example.E_commerce_food_system.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderRepository orderRepository;

    // ===== NEW =====
    @Autowired
    private DeliveryDeclineRepository declineRepository;
    // ===============

    /**
     * Constructor-injected, unlike the @Autowired fields above — those predate the
     * project's constructor-injection convention; new dependencies follow it.
     */
    private final ApplicationEventPublisher events;

    public DeliveryServiceImpl(ApplicationEventPublisher events) {
        this.events = events;
    }

    @Override
    public List<DeliveryDTO> getAllDeliveries() {
        return deliveryRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryDTO getDeliveryById(Integer id) {
        return toDTO(findOrThrow(id));
    }

    @Override
    public DeliveryDTO getDeliveryByOrderId(Integer orderId) {
        Delivery delivery = deliveryRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Delivery not found for order " + orderId));
        return toDTO(delivery);
    }

    @Override
    public DeliveryDTO createDelivery(DeliveryDTO dto) {
        Orders order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setDeliveryPerson(dto.getDeliveryPerson());
        delivery.setDeliveryPhone(dto.getDeliveryPhone());
        delivery.setDeliveryAddress(dto.getDeliveryAddress());
        delivery.setEstimatedDelivery(dto.getEstimatedDelivery());
        if (dto.getDeliveryStatus() != null) {
            delivery.setDeliveryStatus(dto.getDeliveryStatus());
        }

        // ===== CHANGED: naming a courier now sends an OFFER, not an order =====
        // The admin modal fills in the courier at creation time. That should put
        // the delivery in Assigned so the driver can accept or decline, rather
        // than assuming they said yes.
        if (delivery.getDeliveryStatus() == Delivery.DeliveryStatus.Shipped) {
            // explicitly created as already on the way -> still needs a code
            delivery.setDeliveryCode(generateCode());
        } else if (isFilled(dto.getDeliveryPerson())) {
            delivery.setDeliveryStatus(Delivery.DeliveryStatus.Assigned);
            delivery.setAssignedAt(LocalDateTime.now());
            delivery.setAcceptToken(generateToken());
        } else {
            delivery.setDeliveryStatus(Delivery.DeliveryStatus.Preparing);
        }
        // ===============

        return toDTO(deliveryRepository.save(delivery));
    }

    @Override
    public DeliveryDTO updateDelivery(Integer id, DeliveryDTO dto) {
        Delivery delivery = findOrThrow(id);

        // MUST be here — before setDeliveryStatus below
        if (dto.getDeliveryStatus() == Delivery.DeliveryStatus.Shipped
                && delivery.getDeliveryStatus() != Delivery.DeliveryStatus.Shipped) {
            delivery.setDeliveryCode(generateCode());
        }

        // ===== CHANGED: do not wipe the courier when the admin only edits the
        // status dropdown. A blank field in the form should not null out a
        // driver who has already accepted. =====
        if (isFilled(dto.getDeliveryPerson())) {
            delivery.setDeliveryPerson(dto.getDeliveryPerson());
        }
        if (isFilled(dto.getDeliveryPhone())) {
            delivery.setDeliveryPhone(dto.getDeliveryPhone());
        }
        if (isFilled(dto.getDeliveryAddress())) {
            delivery.setDeliveryAddress(dto.getDeliveryAddress());
        }
        if (dto.getEstimatedDelivery() != null) {
            delivery.setEstimatedDelivery(dto.getEstimatedDelivery());
        }
        if (dto.getDeliveryStatus() != null) {
            delivery.setDeliveryStatus(dto.getDeliveryStatus());
        }
        // ===============

        return toDTO(deliveryRepository.save(delivery));
    }

    @Override
    public void deleteDelivery(Integer id) {
        deliveryRepository.deleteById(id);
    }

    // ===== driver enters the customer's code -> Delivered =====
    @Override
    @Transactional
    public DeliveryDTO completeDelivery(Integer deliveryId, String code) {
        Delivery delivery = findOrThrow(deliveryId);

        if (delivery.getDeliveryStatus() != Delivery.DeliveryStatus.Shipped) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Delivery is not in Shipped status");
        }
        if (delivery.getDeliveryCode() == null
                || !delivery.getDeliveryCode().equals(code)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid delivery code");
        }

        delivery.setDeliveryStatus(Delivery.DeliveryStatus.Delivered);
        delivery.setDeliveredAt(LocalDateTime.now());
        return toDTO(deliveryRepository.save(delivery));
    }

    // ===== customer confirms they received the product -> Completed =====
    @Override
    @Transactional
    public DeliveryDTO confirmDelivery(Integer deliveryId) {
        Delivery delivery = findOrThrow(deliveryId);

        if (delivery.getDeliveryStatus() != Delivery.DeliveryStatus.Delivered) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Delivery is not in Delivered status");
        }

        delivery.setDeliveryStatus(Delivery.DeliveryStatus.Completed);
        delivery.setConfirmedAt(LocalDateTime.now());
        return toDTO(deliveryRepository.save(delivery));
    }

    // ===== customer reports a problem -> Disputed =====
    @Override
    @Transactional
    public DeliveryDTO reportProblem(Integer deliveryId) {
        Delivery delivery = findOrThrow(deliveryId);

        if (delivery.getDeliveryStatus() != Delivery.DeliveryStatus.Delivered) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Delivery is not in Delivered status");
        }

        delivery.setDeliveryStatus(Delivery.DeliveryStatus.Disputed);
        return toDTO(deliveryRepository.save(delivery));
    }

    // ================================================================
    // NEW: assignment flow
    //
    //   Preparing --assign--> Assigned --accept--> Shipped
    //                            |
    //                         decline
    //                            |
    //                            v
    //                        Preparing   (driver logged, never re-offered)
    // ================================================================

    /** Admin offers the delivery to a driver. Does not start the delivery. */
    @Override
    @Transactional
    public DeliveryDTO assign(Integer deliveryId, String person, String phone) {
        Delivery delivery = findOrThrow(deliveryId);

        if (delivery.getDeliveryStatus() != Delivery.DeliveryStatus.Preparing) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only a delivery that is still preparing can be assigned");
        }
        if (!isFilled(person)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Delivery person is required");
        }
        if (declineRepository.existsByDeliveryIdAndDeliveryPerson(deliveryId, person)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    person + " already declined this delivery. Choose someone else.");
        }

        delivery.setDeliveryPerson(person);
        delivery.setDeliveryPhone(phone);
        delivery.setDeliveryStatus(Delivery.DeliveryStatus.Assigned);
        delivery.setAssignedAt(LocalDateTime.now());
        delivery.setAcceptToken(generateToken());

        return toDTO(deliveryRepository.save(delivery));
    }

    /** Driver takes the job. This is what actually starts the delivery. */
    @Override
    @Transactional
    public DeliveryDTO accept(Integer deliveryId) {
        Delivery delivery = requireAwaitingResponse(deliveryId);

        delivery.setDeliveryStatus(Delivery.DeliveryStatus.Shipped);
        delivery.setDeliveryCode(generateCode());
        delivery.setAcceptToken(null); // the link is single use

        Delivery saved = deliveryRepository.save(delivery);

        // Listeners fire after this transaction commits, so nobody is told the
        // delivery started if the save is rolled back.
        events.publishEvent(new DeliveryAcceptedEvent(
                saved.getOrder().getCustomer().getCustomerId(),
                saved.getOrder().getOrderId(),
                saved.getDeliveryPerson(),
                saved.getDeliveryCode()));

        return toDTO(saved);
    }

    /** Driver is busy. Back to the pool, and this driver is remembered. */
    @Override
    @Transactional
    public DeliveryDTO decline(Integer deliveryId) {
        Delivery delivery = requireAwaitingResponse(deliveryId);

        declineRepository.save(new DeliveryDecline(
                deliveryId, delivery.getDeliveryPerson(), LocalDateTime.now()));

        delivery.setDeliveryPerson(null);
        delivery.setDeliveryPhone(null);
        delivery.setDeliveryStatus(Delivery.DeliveryStatus.Preparing);
        delivery.setAssignedAt(null);
        delivery.setAcceptToken(null);

        return toDTO(deliveryRepository.save(delivery));
    }

    /** For the admin picker: who should no longer be offered this delivery. */
    @Override
    @Transactional(readOnly = true)
    public List<String> declinedBy(Integer deliveryId) {
        return declineRepository.findDeclinedPersons(deliveryId);
    }

    /** Driver opens the offer link instead of logging in. */
    @Override
    @Transactional(readOnly = true)
    public DeliveryDTO getByAcceptToken(String token) {
        Delivery delivery = deliveryRepository.findByAcceptToken(token)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "This link is no longer valid"));
        return toDTO(delivery);
    }

    // ===== auto-confirm Delivered deliveries older than 3 days =====
    @Scheduled(fixedRate = 3600000) // every hour
    @Transactional
    public void autoConfirmDeliveries() {
        List<Delivery> stale = deliveryRepository
                .findByDeliveryStatusAndDeliveredAtBefore(
                        Delivery.DeliveryStatus.Delivered,
                        LocalDateTime.now().minusDays(3));
        for (Delivery d : stale) {
            d.setDeliveryStatus(Delivery.DeliveryStatus.Completed);
            d.setConfirmedAt(LocalDateTime.now());
        }
        deliveryRepository.saveAll(stale);
    }

    // ---------------- helpers ----------------

    private Delivery findOrThrow(Integer id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Delivery not found with id: " + id));
    }

    private Delivery requireAwaitingResponse(Integer deliveryId) {
        Delivery delivery = findOrThrow(deliveryId);
        if (delivery.getDeliveryStatus() != Delivery.DeliveryStatus.Assigned) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This delivery is not waiting for a driver to respond");
        }
        return delivery;
    }

    private boolean isFilled(String value) {
        return value != null && !value.isBlank();
    }

    /** 6-digit code the customer reads out to the driver. */
    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    /** Unguessable single-use token for the driver's offer link. */
    private String generateToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private DeliveryDTO toDTO(Delivery delivery) {
        DeliveryDTO dto = new DeliveryDTO();
        dto.setDeliveryId(delivery.getDeliveryId());
        dto.setOrderId(delivery.getOrder().getOrderId());
        dto.setDeliveryPerson(delivery.getDeliveryPerson());
        dto.setDeliveryPhone(delivery.getDeliveryPhone());
        dto.setDeliveryAddress(delivery.getDeliveryAddress());
        dto.setDeliveryStatus(delivery.getDeliveryStatus());
        dto.setEstimatedDelivery(delivery.getEstimatedDelivery());
        dto.setDeliveryCode(delivery.getDeliveryCode());
        dto.setDeliveredAt(delivery.getDeliveredAt());
        dto.setConfirmedAt(delivery.getConfirmedAt());
        // ===== NEW =====
        dto.setAssignedAt(delivery.getAssignedAt());
        dto.setAcceptToken(delivery.getAcceptToken());
        // ===============
        return dto;
    }
}
