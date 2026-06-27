package com.example.phone_inventory.Mapper;

import com.example.phone_inventory.Model.PhoneInventoryItems;
import com.example.phone_inventory.Model.PhoneInventoryResponse;

public class PhoneInventoryMapper {
    public static PhoneInventoryResponse toResponse(PhoneInventoryItems item) {
        return new PhoneInventoryResponse(
                item.getId(),
                item.getType(),
                item.getModel(),
                item.getQuantity(),
                item.getPrice(),
                item.getIsAvailable(),
                item.getDateAdded()
        );
    }
}

