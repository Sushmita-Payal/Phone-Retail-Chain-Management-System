package com.example.phone_inventory.Repository;

import com.example.phone_inventory.Model.PhoneInventoryItems;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhoneInventoryRepo extends MongoRepository<PhoneInventoryItems, String> {

    List<PhoneInventoryItems> findByModel(String model);

    List<PhoneInventoryItems> findAllByModelIn(List<String> models);

    List<PhoneInventoryItems> findByIdIn(List<String> criteria);

    List<PhoneInventoryItems> findByModelIn(List<String> criteria);

    void deleteAllById(Iterable<? extends String> ids);

}
