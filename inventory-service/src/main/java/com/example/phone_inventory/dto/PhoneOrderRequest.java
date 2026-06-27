package com.example.phone_inventory.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PhoneOrderRequest {
    private String type;
    private String model;
    private int quantity;
    private double price;
    private boolean available;
    private LocalDateTime dateAdded;
}
