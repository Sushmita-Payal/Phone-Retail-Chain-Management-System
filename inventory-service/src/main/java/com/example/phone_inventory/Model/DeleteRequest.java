package com.example.phone_inventory.Model;
import lombok.Data;

@Data
public class DeleteRequest {

    private String id;
    private int quantity;

}
