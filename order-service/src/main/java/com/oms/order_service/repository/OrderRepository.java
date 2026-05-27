package com.oms.order_service.repository;

import com.oms.order_service.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Orders, String> {
    // Spring Data JPA automatically provides standard CRUD methods.
    // We can add custom queries here later if needed.
}