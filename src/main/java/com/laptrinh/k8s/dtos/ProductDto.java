package com.laptrinh.k8s.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductDto {


    private Long id;

    private String name;

    private double price;

    private String title;

    private String shortDescription;

    private String description;

}
