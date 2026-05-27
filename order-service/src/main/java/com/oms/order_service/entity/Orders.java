package com.oms.order_service.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

    @Entity
    @Table(name = "orders")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Orders {

        @Id
        @Column(name = "order_no", unique = true, nullable = false)
        private String orderNo;

        @Column(name = "customer", nullable = false)
        private String customer;

        @Column(name = "pricebook", nullable = false)
        private String pricebook;

        @Column(name = "warehouse", nullable = false)
        private String warehouse;

        @Column(name = "total_price")
        private BigDecimal totalPrice;

        @Column(name = "order_status", nullable = false)
        @Enumerated(EnumType.STRING)
        private OrderStatus orderStatus;

        @Column(name = "order_type", nullable = false)
        @Enumerated(EnumType.STRING)
        private OrderType orderType;

        @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<OrderLine> orderLines;

        public enum OrderStatus {
            PENDING, ORDERED, SHIPPED, DELIVERED, CANCELLED, FAILED
        }

        public enum OrderType {
            ACTIVE, BACKORDER
        }
    }

