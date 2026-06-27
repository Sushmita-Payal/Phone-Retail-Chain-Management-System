package com.example.chain_store.Repository;

import com.example.chain_store.Model.Store;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepo extends MongoRepository<Store, String> {
    boolean deleteByStoreId(String storeId);
}
