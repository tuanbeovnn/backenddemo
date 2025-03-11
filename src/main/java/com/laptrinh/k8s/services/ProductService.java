package com.laptrinh.k8s.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.laptrinh.k8s.converter.Converter;
import com.laptrinh.k8s.documents.ProductElastic;
import com.laptrinh.k8s.dtos.ProductDto;
import com.laptrinh.k8s.entities.Product;
import com.laptrinh.k8s.repositories.ProductElasticRepository;
import com.laptrinh.k8s.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private static final Logger log = LogManager.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final ProductElasticRepository productElasticRepository;

    public Product saveProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        ProductElastic elasticProduct = new ProductElastic(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getPrice(),
                savedProduct.getTitle(),
                savedProduct.getShortDescription(),
                savedProduct.getDescription()
        );
        productElasticRepository.save(elasticProduct);
        return savedProduct;
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
            return List.of();
        } catch (IOException e) {
            log.error("IO Exception in Elasticsearch search: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public List<ProductDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return Converter.toList(products, ProductDto.class);
    }

    @Transactional
    public void syncDatabaseToElasticsearchV2() {
        long count = productElasticRepository.count();
        if (count > 0) {
            System.out.println("Elasticsearch already has " + count + " products. Skipping sync.");
            return;
        }
        List<Product> products = productRepository.findAll();
        List<ProductElastic> elasticProducts = products.stream()
                .map(product -> new ProductElastic(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getTitle(),
                        product.getShortDescription(),
                        product.getDescription()))
                .toList();

        productElasticRepository.saveAll(elasticProducts);
        System.out.println("Successfully synced " + elasticProducts.size() + " products to Elasticsearch.");
    }
}
