package com.oms.order_service.service;

import com.oms.order_service.client.CatalogServiceClient;
import com.oms.order_service.dto.CustomerDetailsDto;
import com.oms.order_service.entity.BackOrder;
import com.oms.order_service.entity.OrderLine;
import com.oms.order_service.entity.Orders;
import com.oms.order_service.event.InventoryProcessedEvent;
import com.oms.order_service.event.OrderFinalizedEvent;
import com.oms.order_service.repository.BackOrderRepository;
import com.oms.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderFulfillmentService {

    private final OrderRepository orderRepository;
    private final BackOrderRepository backOrderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CatalogServiceClient catalogServiceClient;

    // The Kafka topic for the Integration Service to listen to
    private static final String ORDER_FINALIZED_TOPIC = "order-finalized-events";

    @Transactional
    public void processInventoryResponse(InventoryProcessedEvent event) {
        log.info("Processing inventory response for Order No: {}", event.getOrderNo());

        // 1. Fetch the original pending order
        Orders order = orderRepository.findById(event.getOrderNo())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + event.getOrderNo()));


        // 2. Handle complete failure (e.g., all items out of stock)
        if ("FAILED".equals(event.getStatus())) {
            order.setOrderStatus(Orders.OrderStatus.FAILED);
            orderRepository.save(order);
            log.warn("Inventory check failed. Order {} marked as FAILED.", order.getOrderNo());

            // Notify Integration Service of failure so it can email the customer
            kafkaTemplate.send(ORDER_FINALIZED_TOPIC, order.getOrderNo(), "OrderFailed: " + order.getOrderNo());
            return;
        }

        BigDecimal newTotalPrice = BigDecimal.ZERO;

        // 3. Process each line item for partial fulfillments
        for (InventoryProcessedEvent.ProcessedLine processedLine : event.getProcessedLines()) {

            OrderLine orderLine = order.getOrderLines().stream()
                    .filter(line -> line.getProduct().equals(processedLine.getProductCode()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Product missing from order: " + processedLine.getProductCode()));

            BigDecimal linePrice = catalogServiceClient.getProductPrice(order.getPricebook(),orderLine.getProduct());
            orderLine.setPrice(linePrice);
            int shortage = processedLine.getRequestedQty() - processedLine.getAvailableQty();

            if (shortage > 0) {
                log.info("Partial fulfillment for {}. Shortage: {}", processedLine.getProductCode(), shortage);

                // Update order line to the quantity we can actually fulfill
                orderLine.setQty(processedLine.getAvailableQty());

                // Create the Backorder record
                BackOrder backOrder = BackOrder.builder()
                        .customer(order.getCustomer())
                        .pricebook(order.getPricebook())
                        .product(processedLine.getProductCode())
                        .warehouse(order.getWarehouse())
                        .backorderQty(shortage)
                        .customerResponse(BackOrder.CustomerResponse.PENDING)
                        .originalOrderNo(order.getOrderNo())
                        .build();
                backOrderRepository.save(backOrder);
            }

            // Calculate the new total price based on the fulfilled quantities
            BigDecimal lineTotal = orderLine.getPrice().multiply(BigDecimal.valueOf(orderLine.getQty()));
            newTotalPrice = newTotalPrice.add(lineTotal);
        }

        // 4. Update and finalize the Order Header
        order.setTotalPrice(newTotalPrice);
        order.setOrderStatus(Orders.OrderStatus.ORDERED);
        orderRepository.save(order);

        log.info("Order {} successfully updated. New Total Price: {}", order.getOrderNo(), newTotalPrice);

        // 5. Fire OrderFinalized event to trigger Salesforce Sync and Emails
        kafkaTemplate.send(ORDER_FINALIZED_TOPIC, order.getOrderNo(), new OrderFinalizedEvent(order.getOrderNo(),order.getCustomer()));
    }
}