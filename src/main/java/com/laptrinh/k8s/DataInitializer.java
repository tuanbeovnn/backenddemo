package com.laptrinh.k8s;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) { // Insert only if the table is empty
                productRepository.saveAll(Arrays.asList(
                    new Product("Product A", 19.99),
                    new Product("Product B", 29.99),
                    new Product("Product C", 39.99),
                    new Product("Product D", 49.99),
                    new Product("Product E", 59.99),
                    new Product("Product F", 69.99),
                    new Product("Product G", 79.99),
                    new Product("Product H", 89.99),
                    new Product("Product I", 99.99),
                    new Product("Product J", 109.99)
                ));
                System.out.println("Sample data initialized!");
            } else {
                System.out.println("Database already contains data. Skipping initialization.");
            }
        };
    }
}
