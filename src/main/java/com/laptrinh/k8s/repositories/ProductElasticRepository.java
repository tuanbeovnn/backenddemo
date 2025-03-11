package com.laptrinh.k8s.repositories;

import com.laptrinh.k8s.documents.ProductElastic;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductElasticRepository extends ElasticsearchRepository<ProductElastic, Long> {
}
