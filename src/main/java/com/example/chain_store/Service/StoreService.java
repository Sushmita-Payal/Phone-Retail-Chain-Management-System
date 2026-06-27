package com.example.chain_store.Service;

import com.example.chain_store.Kafka.PhoneInventoryEvent;
import com.example.chain_store.Model.Store;
import com.example.chain_store.Repository.StoreRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoreService {
    @Autowired
    private final StoreRepo storeRepo;

    public StoreService(StoreRepo storeRepo) {
        this.storeRepo = storeRepo;
    }


    public boolean deleteItemById(String id) {
        if (storeRepo.existsById(id)) {
            storeRepo.deleteById(id);
            return true;
        }
        return false;
    }


//    public void addPhones(List<Phone> phones, String storeId ){
//        kafkaProducer.sendToUpdateTopics(new PhoneInventoryEvent("ADD_PHONES", storeId, phones));
//    }
}

