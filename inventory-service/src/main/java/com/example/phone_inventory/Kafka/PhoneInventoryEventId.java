package com.example.phone_inventory.Kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhoneInventoryEventId {
    private String action;
    private String storeId;
    private List<PayloadItem> payload;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PayloadItem {
        private Map<String, String> data;
    }
}

