package com.example.chain_store.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@Data
@Document
@NoArgsConstructor

public class Store {


   @Id
   private String storeId;



   @NotBlank(message = "Store Name is required")
   @Size(min = 10, max = 50, message = "Store Name must be between 10 and 50 characters")
   private String storeName;

   @Size(min = 10, max = 50, message = "Address must be between 10 and 50 characters")
   @NotBlank(message = "Address is required")
   private String address;

   @Size(min = 5, max = 15, message = "Manager Name must be between 5 and 15 characters")
   @NotBlank(message = "Manager Name is required")
   private String managerName;

}
