package com.oms.order_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDto {

    @NotBlank(message = "Order Number is required")
    private String orderNo;

    @NotBlank(message = "Customer code is required")
    private String customerCode;

    @NotEmpty(message = "Order must contain at least one product line")
    @Valid
    private List<OrderLineDto> orderLines;
}