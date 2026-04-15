package com.alertbotspring.ollamaconsumer.mongo;

import com.alertbotspring.ollamaconsumer.model.ProductRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRequestRepository extends MongoRepository<ProductRequest, String> {
}
