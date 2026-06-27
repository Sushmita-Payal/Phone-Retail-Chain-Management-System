package com.example.chain_store.Controller;

import com.example.chain_store.Model.Store;
import com.example.chain_store.Model.StoreError;
import com.example.chain_store.Model.StoreUpdateRequest;
import com.example.chain_store.Repository.StoreRepo;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/phone-store/store")
public class ChainStoreController {
    @Autowired
    private StoreRepo storeRepo;

    @PostMapping("/createStore")
    public ResponseEntity<?> addItems(@Valid @RequestBody List<@Valid Store> items) {
        try {
            long existingCount = storeRepo.count();

            for (int i = 0; i < items.size(); i++) {
                Store store = items.get(i);
                store.setStoreId("Store" + (existingCount + i + 1));
            }

            storeRepo.saveAll(items);
            return ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonMap("message", "Items added successfully"));

        } catch (Exception e) {
            StoreError error = new StoreError("500", "Error occurred while adding Stores: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }



    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable String id) {
        Optional<Store> optionalStore = storeRepo.findById(id);

        if (optionalStore.isEmpty()) {
            StoreError error = new StoreError("404", "Store not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        return ResponseEntity.ok(optionalStore.get());
    }





    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItemById(@PathVariable String id) {
        if (!storeRepo.existsById(id)) {
            StoreError error = new StoreError("404", "Store not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        storeRepo.deleteById(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Store deleted successfully"));
    }



    @PutMapping(value = "/updateStore", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateStore(
            @RequestParam("id") String id,
            @Valid @RequestBody Store storeDetails) {

        try {
            Optional<Store> optionalStore = storeRepo.findById(id);
            if (optionalStore.isEmpty()) {
                String message = "Store not found with id: " + id;
                StoreError error = new StoreError("404", message);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Store store = optionalStore.get();
            store.setStoreName(storeDetails.getStoreName());
            store.setAddress(storeDetails.getAddress());
            store.setManagerName(storeDetails.getManagerName());

            storeRepo.save(store);

            String successMsg = "Successfully changed data for id: " + id;
            return ResponseEntity.ok(Collections.singletonMap("message", successMsg));

        } catch (IllegalArgumentException e) {
            StoreError error = new StoreError("400", "Invalid input: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            StoreError error = new StoreError("500", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }





}
