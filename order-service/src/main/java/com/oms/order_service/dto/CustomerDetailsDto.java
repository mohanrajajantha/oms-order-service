package com.oms.order_service.dto;

import lombok.Data;

@Data
public class CustomerDetailsDto {
    private String customerCode;
    private String pricebook;
    private String warehouse;
    private String email;
}