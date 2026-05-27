package com.oms.order_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private String orderNo;
    private String customerCode;
    private String warehouseCode;
    private List<OrderLineEvent> orderLines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderLineEvent {
        private String productCode;
        private Integer qty;
    }
}