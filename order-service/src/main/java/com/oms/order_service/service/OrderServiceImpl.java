package com.oms.order_service.service;

import com.oms.order_service.client.CatalogServiceClient;
import com.oms.order_service.dto.CustomerDetailsDto;
import com.oms.order_service.dto.OrderRequestDto;
import com.oms.order_service.entity.OrderLine;
import com.oms.order_service.entity.Orders;
import com.oms.order_service.event.OrderCreatedEvent;
import com.oms.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CatalogServiceClient catalogServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // The Kafka topic we will publish to
    private static final String ORDER_CREATED_TOPIC = "order-created-events";

    @Override
    @Transactional
    public String placeOrder(OrderRequestDto orderRequest) {
        log.info("Initiating order placement for Order No: {}", orderRequest.getOrderNo());

        // 1. Synchronous Lookup: Get Warehouse and Pricebook
        CustomerDetailsDto customerDetails = catalogServiceClient.getCustomerDetails(orderRequest.getCustomerCode());

        // 2. Build the Order Entity (Header)
        Orders order = Orders.builder()
                .orderNo(orderRequest.getOrderNo())
                .customer(orderRequest.getCustomerCode())
                .pricebook(customerDetails.getPricebook())
                .warehouse(customerDetails.getWarehouse())
                .orderStatus(Orders.OrderStatus.PENDING)
                .orderType(Orders.OrderType.ACTIVE)
                .totalPrice(BigDecimal.ZERO) // Will be calculated later after pricing checks
                .build();

        // 3. Build the Order Lines (Children)
        List<OrderLine> lines = orderRequest.getOrderLines().stream()
                .map(dto -> OrderLine.builder()
                        .product(dto.getProductCode())
                        .qty(dto.getQty())
                        .price(BigDecimal.ZERO) // Pricing logic can be added here or via Catalog Service
                        .order(order)
                        .build())
                .collect(Collectors.toList());

        order.setOrderLines(lines);

        // 4. Save to PostgreSQL
        // Because of @Transactional and CascadeType.ALL, this saves both Orders and OrderLines atomically
        orderRepository.save(order);
        log.info("Order {} successfully saved to database with PENDING status", order.getOrderNo());

        // 5. Fire Asynchronous Event to Kafka for the Inventory Service
        OrderCreatedEvent event = buildKafkaEvent(order, lines);
        kafkaTemplate.send(ORDER_CREATED_TOPIC, order.getOrderNo(), event);
        log.info("Published OrderCreated event to Kafka for Order No: {}", order.getOrderNo());

        return "Order received and is pending inventory allocation.";
    }

    private OrderCreatedEvent buildKafkaEvent(Orders order, List<OrderLine> lines) {
        List<OrderCreatedEvent.OrderLineEvent> lineEvents = lines.stream()
                .map(line -> OrderCreatedEvent.OrderLineEvent.builder()
                        .productCode(line.getProduct())
                        .qty(line.getQty())
                        .build())
                .collect(Collectors.toList());

        return OrderCreatedEvent.builder()
                .orderNo(order.getOrderNo())
                .customerCode(order.getCustomer())
                .warehouseCode(order.getWarehouse())
                .orderLines(lineEvents)
                .build();
    }
}