package com.example.phone_inventory.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhoneInventoryResponse {
    private String Id;
    private String type;
    private String model;
    private int quantity;
    private BigDecimal price;
    private Boolean isAvailable;
    private Date dateAdded;
}
