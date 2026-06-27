package com.example.chain_store.Model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreUpdateRequest {
    @NotBlank(message = "Store name must not be blank")
    @Size(min = 10, max = 50, message = "Store name must be between 10 and 50 characters")
    private String storeName;

    @NotBlank(message = "Address must not be blank")
    @Size(min = 10, max = 50, message = "Address must be between 10 and 50 characters")
    private String address;

    @NotBlank(message = "Manager name must not be blank")
    @Size(min = 5, max = 15, message = "Manager name must be between 5 and 15 characters")
    private String managerName;
}

