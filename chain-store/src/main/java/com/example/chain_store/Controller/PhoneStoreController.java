
package com.example.chain_store.Controller;
import com.example.chain_store.Kafka.KafkaTopics;
import com.example.chain_store.Kafka.PhoneInventoryEvent;
import com.example.chain_store.Kafka.PhoneInventoryEventId;
import com.example.chain_store.Kafka.PhoneStoreProducer;
import com.example.chain_store.dto.PhoneOrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("phone-store/store")
public class PhoneStoreController {

    @Autowired
    private PhoneStoreProducer producer;

    @PostMapping("/orderPhones")
    public void orderPhones(@RequestBody PhoneInventoryEvent request) {
        producer.sendToUpdateTopic(request);
    }

    @PostMapping("/addPhones")
    public void addPhones(@RequestBody PhoneInventoryEvent request) {
        producer.sendToUpdateTopic(request);
    }

    @PutMapping("/increaseQuantity")
    public void increaseQuantity(@RequestBody PhoneInventoryEvent request) {
        producer.sendToUpdateTopic(request);
    }

    @PutMapping("/decreaseQuantity")
    public void decreaseQuantity(@RequestBody PhoneInventoryEvent request) {
        producer.sendToUpdateTopic(request);
    }

    @GetMapping("/id/{storeId}/{id}")
    public void getPhoneById(@PathVariable String storeId, @PathVariable String id) {
        PhoneInventoryEvent event = new PhoneInventoryEvent();
        event.setAction("GET_BY_ID");
        event.setStoreId(storeId);

        PhoneOrderRequest request = new PhoneOrderRequest();
        request.setType(id);

        event.setPayload(List.of(request));

        System.out.println("Sending Event: " + event);

        producer.sendToGetTopic(event);
    }



    @GetMapping("{storeId}/ids")
    public void getPhonesByIds(@RequestParam List<String> ids, @PathVariable String storeId) {
        PhoneInventoryEvent event = new PhoneInventoryEvent();
        event.setAction("GET_BY_IDS");
        event.setStoreId(storeId);

        List<PhoneOrderRequest> payload = ids.stream()
                .map(id -> {
                    PhoneOrderRequest request = new PhoneOrderRequest();
                    request.setType(id);
                    return request;
                })
                .toList();

        event.setPayload(payload);
        System.out.println("Sending Event: " + event);
        producer.sendToGetTopic(event);
    }


    @GetMapping("/model/{storeId}/{model}")
    public void getPhonesByModel(@PathVariable String storeId, @PathVariable String model) {
        PhoneInventoryEvent event = new PhoneInventoryEvent();
        event.setAction("GET_BY_MODEL");
        event.setStoreId(storeId);

        PhoneOrderRequest request = new PhoneOrderRequest();
        request.setModel(model);

        event.setPayload(List.of(request));
        producer.sendToGetTopic(event);
    }

    @GetMapping("/{storeId}/models")
    public void getPhonesByModels(@RequestParam List<String> models, @PathVariable String storeId) {
        PhoneInventoryEvent event = new PhoneInventoryEvent();
        event.setAction("GET_BY_MODELS");
        event.setStoreId(storeId);

        List<PhoneOrderRequest> payload = models.stream()
                .map(model -> {
                    PhoneOrderRequest request = new PhoneOrderRequest();
                    request.setModel(model); // setting model in payload
                    return request;
                })
                .toList();

        event.setPayload(payload);
        producer.sendToGetTopic(event);
    }

    @GetMapping("/{storeId}/catalog")
    public void getEntireCatalog(@PathVariable String storeId) {
        PhoneInventoryEvent event = new PhoneInventoryEvent();
        event.setAction("GET_ALL");
        event.setStoreId(storeId);
        event.setPayload(List.of());

        producer.sendToGetTopic(event);
    }



    @DeleteMapping("/{storeId}/id/{id}")
    public void deletePhoneById(@PathVariable String storeId, @PathVariable String id) {
        PhoneInventoryEvent event = new PhoneInventoryEvent();
        event.setAction("DELETE_BY_ID");
        event.setStoreId(storeId);

        PhoneOrderRequest request = new PhoneOrderRequest();
        request.setType(id);

        event.setPayload(List.of(request));

        System.out.println("Sending Delete Event: " + event);

        producer.sendToDeleteTopic(event);
    }





    @DeleteMapping("/{storeId}/ids")
    public void deletePhonesByIds(@PathVariable String storeId, @RequestBody List<String> ids) {
        List<PhoneOrderRequest> requests = ids.stream().map(id -> {
            PhoneOrderRequest req = new PhoneOrderRequest();
            req.setType(id);
            return req;
        }).toList();

        PhoneInventoryEvent event = new PhoneInventoryEvent();
        event.setAction("DELETE_BY_IDS");
        event.setStoreId(storeId);
        event.setPayload(requests);

        producer.sendToDeleteTopic(event);
    }



}
