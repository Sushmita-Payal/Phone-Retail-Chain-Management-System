package com.example.phone_inventory.Controller;

import com.example.phone_inventory.Mapper.PhoneInventoryMapper;
import com.example.phone_inventory.Model.DeleteRequest;
import com.example.phone_inventory.Model.ErrorResponse;
import com.example.phone_inventory.Model.PhoneInventoryItems;
import com.example.phone_inventory.Model.PhoneInventoryResponse;
import com.example.phone_inventory.Repository.PhoneInventoryRepo;
import com.example.phone_inventory.service.PhoneInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
//this is where we will define our Api path
@RequestMapping("/phone-inventory")
public class PhoneInventoryController {
@Autowired
private PhoneInventoryRepo phoneInventoryRepo;
    // POST /inventory - Add one or more items
    @PostMapping("/inventory")

    @ResponseStatus(HttpStatus.CREATED)

    //Adding by single or multiple items
    public ResponseEntity <String> addPhone(@RequestBody List<PhoneInventoryItems> phoneInventoryItems) {
          try {
              phoneInventoryRepo.saveAll(phoneInventoryItems);
              return ResponseEntity.ok("Phones added successfully!");
          }
          catch (Exception e){
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occured while adding Phones: " + e.getMessage());
          }
    }

    //Get item by ID
    @GetMapping("/inventory/id/{id}")
    public ResponseEntity<?> getItemById(@PathVariable String id) {
        Optional<PhoneInventoryItems> optionalItem = phoneInventoryRepo.findById(id);

        if (optionalItem.isPresent()) {
            PhoneInventoryResponse response = PhoneInventoryMapper.toResponse(optionalItem.get());
            return ResponseEntity.ok(response);
        }

        String message = "Phone item with ID " + id + " not found";
        ErrorResponse error = new ErrorResponse("NOT_FOUND", message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }





    // Get item by model
    @GetMapping("/inventory/model/{model}")
    public ResponseEntity<?> getItemByModel(@PathVariable String model) {
        List<PhoneInventoryItems> items = phoneInventoryRepo.findByModel(model);

        if (!items.isEmpty()) {
            List<PhoneInventoryResponse> responseList = items.stream()
                    .map(PhoneInventoryMapper::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseList);
        }

        String message = "No phone items found for model: " + model;
        ErrorResponse error = new ErrorResponse("NOT_FOUND", message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }


    // Get items by multiple IDs
    @GetMapping("/inventory/ids")
    public ResponseEntity<?> getItemByIds(@RequestParam List<String> ids) {
        List<PhoneInventoryItems> items = phoneInventoryRepo.findAllById(ids);

        if (!items.isEmpty()) {
            List<PhoneInventoryResponse> responseList = items.stream()
                    .map(PhoneInventoryMapper::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseList);
        }

        String message = "No phone items found for the provided IDs: " + ids;
        ErrorResponse error = new ErrorResponse("NOT_FOUND", message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }


    // Get items by multiple models
    @GetMapping("/inventory/models")
    public ResponseEntity<?> getItemByModels(@RequestParam List<String> models) {
        List<PhoneInventoryItems> items = phoneInventoryRepo.findAllByModelIn(models);

        if (!items.isEmpty()) {
            List<PhoneInventoryResponse> responseList = items.stream()
                    .map(PhoneInventoryMapper::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseList);
        }

        String message = "No phone items found for the provided models: " + models;
        ErrorResponse error = new ErrorResponse("NOT_FOUND", message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }



    //deleting a single item
    @DeleteMapping("/inventory/id/{id}")
    public ResponseEntity<?> deleteItemById(@PathVariable String id) {
        if (phoneInventoryRepo.existsById(id)) {
            phoneInventoryRepo.deleteById(id);
            String message = "Phone item with ID " + id + " deleted successfully";
            Map<String, String> response = Map.of(
                    "code", "DELETED",
                    "message", message
            );
            return ResponseEntity.ok(response);
        }

        String message = "Phone item with ID " + id + " not found";
        ErrorResponse error = new ErrorResponse("NOT_FOUND", message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }



    //deleting my multiple ids
    @DeleteMapping("/inventory/ids")
    public ResponseEntity<?> deleteItemsByIds(@RequestParam List<String> ids) {
        List<PhoneInventoryItems> itemsToDelete = phoneInventoryRepo.findAllById(ids);

        List<String> foundIds = itemsToDelete.stream()
                .map(PhoneInventoryItems::getId)
                .toList();

        List<String> missingIds = ids.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            String message = "Items not found for IDs: " + missingIds;
            ErrorResponse error = new ErrorResponse("NOT_FOUND", message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        phoneInventoryRepo.deleteAllById(ids);

        Map<String, Object> response = Map.of(
                "code", "DELETED",
                "message", "Deleted items successfully",
                "deletedIds", foundIds
        );

        return ResponseEntity.ok(response);
    }

    @Autowired
    private PhoneInventoryService phoneInventoryService;
    //Increaing Qaunity
    @PutMapping("/inventory/{id}/increase")
    public ResponseEntity<?> increaseInventory(
            @PathVariable String id,
            @RequestHeader("Quantity") int quantity) {

        PhoneInventoryItems updatedItem = phoneInventoryService.increaseInventory(id, quantity);

        if (updatedItem != null) {
            Map<String, Object> response = Map.of(
                    "code", "UPDATED",
                    "message", "Inventory increased successfully",
                    "updatedItem", updatedItem
            );
            return ResponseEntity.ok(response);
        } else {
            String message = "Phone item with ID " + id + " not found";
            ErrorResponse error = new ErrorResponse("NOT_FOUND", message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    //Decrease Quantity
    @PutMapping("/inventory/decrease")
    public ResponseEntity<?> decreaseInventory(@RequestBody DeleteRequest request) {
        PhoneInventoryItems updatedItem = phoneInventoryService.decreaseInventory(request.getId(), request.getQuantity());

        if (updatedItem != null) {
            Map<String, Object> response = Map.of(
                    "code", "UPDATED",
                    "message", "Inventory decreased successfully",
                    "updatedItem", updatedItem
            );
            return ResponseEntity.ok(response);
        } else {
            String message = "Phone item with ID " + request.getId() + " not found";
            ErrorResponse error = new ErrorResponse("NOT_FOUND", message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }








}
