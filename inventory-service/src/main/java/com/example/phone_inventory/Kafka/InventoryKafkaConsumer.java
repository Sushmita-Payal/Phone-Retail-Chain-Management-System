package com.example.phone_inventory.Kafka;

import com.example.phone_inventory.Model.PhoneInventoryItems;
import com.example.phone_inventory.Repository.PhoneInventoryRepo;
import com.example.phone_inventory.dto.PhoneOrderRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@Service
public class InventoryKafkaConsumer {


    @Autowired
    private PhoneInventoryRepo phoneInventoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.UPDATE, groupId = "inventory-group", autoStartup = "${kafka.listener.autoStartup:true}")
    public void consume(PhoneInventoryEvent event) {
        System.out.println("Received Event: " + event);


        switch (event.getAction()) {
            case "ADD_PHONES" -> {
                List<PhoneOrderRequest> phoneOrders = objectMapper.convertValue(
                        event.getPayload(), new TypeReference<List<PhoneOrderRequest>>() {}
                );
                String storeId = event.getStoreId();

                List<PhoneInventoryItems> inventoryItems = phoneOrders.stream().map(order -> {
                    PhoneInventoryItems item = new PhoneInventoryItems();
                    item.setType(order.getType());
                    item.setModel(order.getModel());
                    item.setQuantity(order.getQuantity());
                    item.setPrice(BigDecimal.valueOf(order.getPrice()));
                    item.setIsAvailable(order.isAvailable());
                    item.setStoreId(storeId);
                    item.setDateAdded(Timestamp.valueOf(order.getDateAdded()));
                    return item;
                }).toList();

                phoneInventoryRepository.saveAll(inventoryItems);
                System.out.println("Saved " + inventoryItems.size() + " items to MongoDB for storeId: " + storeId);
            }

            case "ORDER_PHONES" -> {
                List<PhoneOrderRequest> phoneOrders = objectMapper.convertValue(
                        event.getPayload(), new TypeReference<List<PhoneOrderRequest>>() {}
                );
                String storeId = event.getStoreId();


                for (PhoneOrderRequest order : phoneOrders) {
                    List<PhoneInventoryItems> items = phoneInventoryRepository.findByModel(order.getModel());

                    for (PhoneInventoryItems item : items) {
                        if (storeId.equals(item.getStoreId())) {
                            int currentQty = item.getQuantity();
                            int orderQty = order.getQuantity();

                            if (currentQty >= orderQty) {
                                item.setQuantity(currentQty - orderQty);
                                phoneInventoryRepository.save(item);
                                System.out.println("Processed order for model: " + order.getModel() + ", new quantity: " + item.getQuantity());
                            } else {
                                System.out.println("Not enough stock for model: " + order.getModel());
                            }
                        }
                    }
                }
            }

            case "INCREASE_QUANTITY" -> {
                List<PhoneOrderRequest> phoneOrders = objectMapper.convertValue(
                        event.getPayload(), new TypeReference<List<PhoneOrderRequest>>() {}
                );
                String storeId = event.getStoreId();

                for (PhoneOrderRequest order : phoneOrders) {
                    List<PhoneInventoryItems> items = phoneInventoryRepository.findByModel(order.getModel());

                    for (PhoneInventoryItems item : items) {
                        if (storeId.equals(item.getStoreId())) {
                            int currentQty = item.getQuantity();
                            int addedQty = order.getQuantity();
                            item.setQuantity(currentQty + addedQty);
                            phoneInventoryRepository.save(item);
                            System.out.println("Increased quantity for model: " + order.getModel() +
                                    ", new quantity: " + item.getQuantity());
                        }
                    }
                }
            }

            case "DECREASE_QUANTITY" -> {
                List<PhoneOrderRequest> phoneOrders = objectMapper.convertValue(
                        event.getPayload(), new TypeReference<List<PhoneOrderRequest>>() {}
                );
                String storeId = event.getStoreId();

                for (PhoneOrderRequest order : phoneOrders) {
                    List<PhoneInventoryItems> items = phoneInventoryRepository.findByModel(order.getModel());

                    for (PhoneInventoryItems item : items) {
                        if (storeId.equals(item.getStoreId())) {
                            int currentQty = item.getQuantity();
                            int decreaseQty = order.getQuantity();

                            if (currentQty >= decreaseQty) {
                                item.setQuantity(currentQty - decreaseQty);
                                phoneInventoryRepository.save(item);
                                System.out.println("Decreased quantity for model: " + order.getModel() +
                                        ", new quantity: " + item.getQuantity());
                            } else {
                                System.out.println("Not enough stock to decrease for model: " + order.getModel());
                            }
                        }
                    }
                }
            }


            default -> System.out.println("Unknown action: " + event.getAction());
        }

    }

    @KafkaListener(topics = KafkaTopics.GET, groupId = "inventory-group", autoStartup = "${kafka.listener.autoStartup:true}")
    public void consumeGet(PhoneInventoryEvent event) {
        System.out.println("Received Get Event: " + event);

        List<PhoneInventoryItems> result = new ArrayList<>();

        switch (event.getAction()) {
            case "GET_BY_ID" -> {
                String id = event.getPayload().get(0).getType();
                phoneInventoryRepository.findById(id).ifPresent(result::add);
            }

            case "GET_BY_IDS" -> {
                List<String> ids = event.getPayload().stream()
                        .map(PhoneOrderRequest::getType)
                        .toList();
                result = phoneInventoryRepository.findByIdIn(ids);
            }

            case "GET_BY_MODEL" -> {
                String model = event.getPayload().get(0).getModel();
                result = phoneInventoryRepository.findByModel(model);
            }

            case "GET_BY_MODELS" -> {
                List<String> models = event.getPayload().stream()
                        .map(PhoneOrderRequest::getModel)
                        .toList();
                result = phoneInventoryRepository.findByModelIn(models);
            }

            case "GET_ALL" -> {
                result = phoneInventoryRepository.findAll();
            }
            default -> System.out.println("Unsupported action: " + event.getAction());
        }
        System.out.println("Fetched " + result.size() + " items for action: " + event.getAction());
        System.out.println("Fetched item: " + result);
    }



    @KafkaListener(topics = KafkaTopics.DELETE, groupId = "inventory-group", autoStartup = "${kafka.listener.autoStartup:true}")
    public void consumeDelete(PhoneInventoryEvent event) {
        try {
            System.out.println("Received Delete Event: " + event);

            List<PhoneOrderRequest> payload = event.getPayload();

            switch (event.getAction()) {
                case "DELETE_BY_ID" -> {
                    String id = payload.get(0).getType();
                    phoneInventoryRepository.deleteById(id);
                    System.out.println("Deleted phone with ID: " + id);
                }

                case "DELETE_BY_IDS" -> {
                    List<String> ids = payload.stream()
                            .map(PhoneOrderRequest::getType)
                            .toList();
                    phoneInventoryRepository.deleteAllById(ids);
                    System.out.println("Deleted phones with IDs: " + ids);
                }

                default -> System.out.println("Unsupported delete action: " + event.getAction());
            }

            System.out.println("Completed deletion for action: " + event.getAction());
        } catch (Exception e) {
            System.err.println("Error processing delete event: " + e.getMessage());
            e.printStackTrace();
        }
    }




}
