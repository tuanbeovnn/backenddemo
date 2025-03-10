package com.laptrinh.k8s.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.laptrinh.k8s.converter.Converter;
import com.laptrinh.k8s.dtos.ProductDto;
import com.laptrinh.k8s.entities.Product;
import com.laptrinh.k8s.repositories.ProductRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

@Service
public class ProductService {
    private static final Logger log = LogManager.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final ElasticsearchClient elasticsearchClient;


    public ProductService(ProductRepository productRepository, ElasticsearchClient elasticsearchClient) {
        this.productRepository = productRepository;
        this.elasticsearchClient = elasticsearchClient;
    }

    public List<ProductDto> getProductsWithSearch(String keyword) throws IOException {
        SearchResponse<ProductDto> searchResponse = elasticsearchClient.search(s -> {
                    s.index("products");
                    if (!StringUtils.hasText(keyword)) {
                        log.debug("No keyword provided, performing match all query.");
                        s.query(q -> q.matchAll(m -> m));
                    } else {
                        log.debug("Building query for keyword: {}", keyword);
                        s.query(q -> q.bool(b -> b
                                .should(m -> m.match(t -> t.field("name").query(keyword).boost(3.0F)))
                                .should(m -> m.match(t -> t.field("title").query(keyword).boost(2.0F)))
                                .should(m -> m.match(t -> t.field("shortDescription").query(keyword).boost(1.5F)))
                                .should(m -> m.matchPhrase(t -> t.field("description").query(keyword)))
                        ));
                    }
                    return s;
                },
                ProductDto.class
        );

        List<ProductDto> productDtos = searchResponse.hits().hits().stream()
                .map(Hit::source)
                .toList();
        log.info("Found {} products matching keyword: {}", productDtos.size(), keyword);

        return productDtos;
    }

    public List<ProductDto> getProductsWithSearchV2(String keyword) throws IOException {
        log.info("Searching products with fuzziness for keyword: {}", keyword);

        SearchResponse<ProductDto> searchResponse;
        try {
            searchResponse = elasticsearchClient.search(s -> {
                        s.index("products");
                        if (!StringUtils.hasText(keyword)) {
                            log.debug("No keyword provided, performing match all query.");
                            s.query(q -> q.matchAll(m -> m));
                        } else {
                            log.debug("Building fuzzy search query for keyword: {}", keyword);
                            s.query(q -> q
                                    .bool(b -> b
                                            .should(m -> m.match(t -> t.field("name").query(keyword).boost(3.0F).fuzziness("AUTO")))
                                            .should(m -> m.match(t -> t.field("title").query(keyword).boost(2.0F).fuzziness("AUTO")))
                                            .should(m -> m.match(t -> t.field("shortDescription").query(keyword).boost(1.5F).fuzziness("AUTO")))
                                            .should(m -> m.match(t -> t.field("description").query(keyword).fuzziness("AUTO")))
                                    )
                            );
                        }
                        return s;
                    },
                    ProductDto.class
            );

            List<ProductDto> productDtos = searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .toList();

            log.info("Found {} fuzzy search results for keyword: {}", productDtos.size(), keyword);
            return productDtos;
        } catch (ElasticsearchException e) {
            log.error("Elasticsearch query failed: {}", e.getMessage(), e);
            return List.of(); // Return an empty list in case of failure
        } catch (IOException e) {
            log.error("IO Exception in Elasticsearch search: {}", e.getMessage(), e);
            return List.of();
        }
    }


    public List<ProductDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return Converter.toList(products, ProductDto.class);
    }

    public void syncDatabaseToElasticsearch() {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            try {
                IndexResponse response = elasticsearchClient.index(i -> i
                        .index("products")
                        .id(String.valueOf(product.getId()))
                        .document(product)
                );
                System.out.println("Indexed product: " + product.getId() + " Status: " + response.result());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
