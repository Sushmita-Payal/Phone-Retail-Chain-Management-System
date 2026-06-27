package com.example.phone_inventory.service;

import com.example.phone_inventory.Model.PhoneInventoryItems;
import com.example.phone_inventory.Repository.PhoneInventoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PhoneInventoryService {

    @Autowired
    private PhoneInventoryRepo phoneInventory;

    public PhoneInventoryItems increaseInventory(String id, int quantity) {
        Optional<PhoneInventoryItems> optionalItem = phoneInventory.findById(id);
        if (optionalItem.isPresent()) {
            PhoneInventoryItems item = optionalItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return phoneInventory.save(item);
        } else {
            return null;
        }
    }


    public PhoneInventoryItems decreaseInventory(String id, int quantity) {
        Optional<PhoneInventoryItems> optionalItemd = phoneInventory.findById(id);
        if (optionalItemd.isPresent()) {
            PhoneInventoryItems item = optionalItemd.get();
            System.out.println(item.getQuantity());
            item.setQuantity(item.getQuantity() - quantity);
            return phoneInventory.save(item);
        } else {
            return null;
        }
    }
}

