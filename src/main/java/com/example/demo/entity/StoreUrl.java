package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StoreUrl {

    private String id;

    private String page_url;

    private String sku_id;

    private boolean tomb;
}
