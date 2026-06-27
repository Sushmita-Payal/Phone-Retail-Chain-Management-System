package com.example.chain_store.Kafka;

import com.example.chain_store.dto.PhoneOrderRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhoneInventoryEvent {
    private String action;
    private String storeId;
    private List<PhoneOrderRequest> payload;
}
