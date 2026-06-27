package com.example.phone_inventory.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;

@Document
//indication that this will be a document or table name in our mongoDb Compass

@Data
@AllArgsConstructor
@NoArgsConstructor
//all these annotation from lombok to reduce getters, setters and constructors instialization
public class PhoneInventoryItems {
    @Id
    private String id;
    private String type;
    private String model;
    private int quantity;
    private BigDecimal price;
    private Boolean isAvailable;
    private String storeId;
    private Date dateAdded;


}


