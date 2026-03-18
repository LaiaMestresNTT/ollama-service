package com.alertbotspring.ollamaconsumer.mongo;

import com.alertbotspring.ollamaconsumer.model.ScrapedProduct;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapedProductRepository extends MongoRepository<ScrapedProduct, String> {

    List<ScrapedProduct> findTop3ByRequestIdOrderByScoreDesc(String requestId);
}