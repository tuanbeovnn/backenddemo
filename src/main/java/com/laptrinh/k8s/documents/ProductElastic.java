package com.laptrinh.k8s.documents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
public class ProductElastic {

    @Id
    private Long id;
    private String name;
    private double price;
    private String title;
    private String shortDescription;
    private String description;

}
