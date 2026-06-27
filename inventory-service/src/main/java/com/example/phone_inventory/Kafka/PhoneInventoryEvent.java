package com.example.phone_inventory.Kafka;

import com.example.phone_inventory.dto.PhoneOrderRequest;
import lombok.Data;

import java.util.List;

@Data
public class PhoneInventoryEvent {
    private String action;
    private String storeId;
    private List<PhoneOrderRequest> payload;
}
