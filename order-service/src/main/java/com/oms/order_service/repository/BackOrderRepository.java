package com.oms.order_service.repository;

import com.oms.order_service.entity.BackOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackOrderRepository extends JpaRepository<BackOrder, Long> {
    // Find backorders by customer to help with your sync/notification requirements
    List<BackOrder> findByCustomer(String customer);
}