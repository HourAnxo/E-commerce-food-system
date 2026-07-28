package com.example.E_commerce_food_system.Repository;


import com.example.E_commerce_food_system.Entity.DeliveryDecline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryDeclineRepository extends JpaRepository<DeliveryDecline, Integer> {

    boolean existsByDeliveryIdAndDeliveryPerson(Integer deliveryId, String deliveryPerson);

    /** Names of everyone who has already turned this delivery down. */
    @Query("select d.deliveryPerson from DeliveryDecline d where d.deliveryId = :deliveryId")
    List<String> findDeclinedPersons(@Param("deliveryId") Integer deliveryId);
}
