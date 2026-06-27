package com.example.chain_store.Kafka;


import com.example.chain_store.dto.PhoneOrderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhoneStoreProducer {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendToGetTopic(PhoneInventoryEvent message) {
        kafkaTemplate.send(KafkaTopics.GET, message);
    }
    public void sendToDeleteTopic(PhoneInventoryEvent message) {
        kafkaTemplate.send(KafkaTopics.DELETE, message);
    }
    public void sendToUpdateTopic(PhoneInventoryEvent message){
        kafkaTemplate.send(KafkaTopics.UPDATE, message);
    }

}
