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
public class InventoryProcessedEvent {
    private String orderNo;
    private String status; // e.g., "FULL", "PARTIAL", "FAILED"
    private List<ProcessedLine> processedLines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessedLine {
        private String productCode;
        private Integer requestedQty;
        private Integer availableQty;
    }
}