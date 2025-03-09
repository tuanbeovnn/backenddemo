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
                        new Product("Laptop", 999.99, "High-end Laptop", "Powerful laptop for professionals", "This laptop features a high-performance processor, ample storage, and a sleek design."),
                        new Product("Smartphone", 499.99, "Flagship Phone", "Premium smartphone with a great camera", "This smartphone comes with an OLED display, a powerful chipset, and a long-lasting battery."),
                        new Product("Headphones", 199.99, "Noise Cancelling Headphones", "Immersive sound experience", "These headphones provide active noise cancellation and high-fidelity audio quality."),
                        new Product("Smartwatch", 149.99, "Fitness Tracker", "Track your health and notifications", "This smartwatch helps you monitor your fitness activities, heart rate, and smartphone notifications.")
                ));

                productService.syncDatabaseToElasticsearch();

            } else {
                System.out.println("Database already contains data. Skipping initialization.");
            }
        };
    }
}
