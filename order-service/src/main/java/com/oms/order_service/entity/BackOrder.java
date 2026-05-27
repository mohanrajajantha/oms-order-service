package com.oms.order_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "back_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer", nullable = false)
    private String customer;

    @Column(name = "pricebook", nullable = false)
    private String pricebook;

    @Column(name = "product", nullable = false)
    private String product;

    @Column(name = "warehouse", nullable = false)
    private String warehouse;

    @Column(name = "backorder_qty", nullable = false)
    private Integer backorderQty;

    @Column(name = "customer_response", nullable = false)
    @Enumerated(EnumType.STRING)
    private CustomerResponse customerResponse;

    @Column(name = "original_order_no", nullable = false)
    private String originalOrderNo; // To link back to the parent order

    public enum CustomerResponse {
        PENDING, ACCEPT, DECLINE
    }
}