package com.laptrinh.k8s;

import com.laptrinh.k8s.entities.Product;
import com.laptrinh.k8s.repositories.ProductRepository;
import com.laptrinh.k8s.services.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class DataInitializer {
    private final ProductService productService;

    public DataInitializer(ProductService productService) {
        this.productService = productService;
    }


    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.saveAll(Arrays.asList(
                        Product.builder()
                                .name("Laptop")
                                .price(999.99)
                                .title("High-end Laptop")
                                .shortDescription("Powerful laptop for professionals")
                                .description("This laptop features a high-performance processor, ample storage, and a sleek design.")
                                .build(),

                        Product.builder()
                                .name("Smartphone")
                                .price(499.99)
                                .title("Flagship Phone")
                                .shortDescription("Premium smartphone with a great camera")
                                .description("This smartphone comes with an OLED display, a powerful chipset, and a long-lasting battery.")
                                .build(),

                        Product.builder()
                                .name("Headphones")
                                .price(199.99)
                                .title("Noise Cancelling Headphones")
                                .shortDescription("Immersive sound experience")
                                .description("These headphones provide active noise cancellation and high-fidelity audio quality.")
                                .build(),

                        Product.builder()
                                .name("Smartwatch")
                                .price(149.99)
                                .title("Fitness Tracker")
                                .shortDescription("Track your health and notifications")
                                .description("This smartwatch helps you monitor your fitness activities, heart rate, and smartphone notifications.")
                                .build()
                ));

                productService.syncDatabaseToElasticsearch();

            } else {
                System.out.println("Database already contains data. Skipping initialization.");
            }
        };
    }
}
