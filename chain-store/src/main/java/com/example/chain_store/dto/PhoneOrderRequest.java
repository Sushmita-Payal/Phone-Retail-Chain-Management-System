package com.example.chain_store.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PhoneOrderRequest {
    private String type;
    private String model;
    private int quantity;
    private double price;
    private boolean isAvailable;
    private LocalDateTime dateAdded;
}
