package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ExposeMsg {

    private String id;

    private String batch_no;

    private String platform_name;

    private String page_url;

    private String sku_id;

    public ExposeMsg(String batch_no,String platform_name,StoreUrl st){
        this.batch_no = batch_no;
        this.platform_name = platform_name;
        this.id = st.getId();
        this.page_url = st.getPage_url();
        this.sku_id = st.getSku_id();
    }
}
