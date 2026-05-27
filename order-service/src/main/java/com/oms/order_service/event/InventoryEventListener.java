package com.oms.order_service.event;

import com.oms.order_service.service.OrderFulfillmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final OrderFulfillmentService fulfillmentService;

    @KafkaListener(
            topics = "inventory-processed-events",
            groupId = "order-service-group"
    )
    public void listenToInventoryProcessed(InventoryProcessedEvent event) {
        log.info("Received InventoryProcessedEvent from Kafka for Order No: {}", event.getOrderNo());

        // Pass the event directly to our transactional fulfillment service
        fulfillmentService.processInventoryResponse(event);
    }
}