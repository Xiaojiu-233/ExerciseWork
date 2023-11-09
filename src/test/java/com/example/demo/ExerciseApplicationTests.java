package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvWriter;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvWriterSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
class ExerciseApplicationTests {

    //读文件的文件夹路径
    @Value("${read.path}")
    private String filePath;

    public List<List<String>> ManageFile(String filePath) throws IOException {
        List<List<String>> list  = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8));

        String line = br.readLine();
        while((line = br.readLine()) != null){
            List<String> Msg = Arrays.asList(line.split(","));
            list.add(Msg);
        }

        br.close();

        return list;
    }

    @Test
    void TakeAndMatchShopName() throws Exception {
        //准备写入的数据
        Set<List<String>> WriteMessage = new HashSet<>();
        //读文件
        List<List<String>> ShopMessage = ManageFile(filePath+"/sample.csv");
        List<List<String>> KeywordMessage = ManageFile(filePath+"/标签词库1026.csv");
        //处理数据
        //处理标签
        HashMap<String,HashMap<String,String>> KeywordHash = new HashMap<>();
        for (List<String> list1: KeywordMessage){
            String key1 = list1.get(0);
            String key2 = list1.get(1);
            String value = list1.get(4);
            if(KeywordHash.containsKey(key1)){
                HashMap<String,String> hash = KeywordHash.get(key1);
                hash.put(key2,value);
            }else{
                HashMap<String,String> hash = new HashMap<>();
                hash.put(key2,value);
                KeywordHash.put(key1,hash);
            }
        }
        //处理sample
        for (List<String> list1: ShopMessage) {
            String t_id = list1.get(1);
            for(int i = 0 ;i < list1.size();i++){
                String s = list1.get(i);
                if(s.contains("storeName")){
                    int ii = i;
                    String ss = list1.get(ii);
                    while(!ss.contains("storeId")){
                        ii++;
                        ss = list1.get(ii);
                    }
                    //添加筛选数据
                    String s_Id = ss.split(":")[1].replace("\"","").replace("\\","");
                    String s_Name = s.split(":")[1].replace("\"","");
                    //添加tag数据
                    String tag = "";
                    for(String key: KeywordHash.keySet()){
                        if(s_Name.contains(key)){
                            HashMap<String,String> hash = KeywordHash.get(key);
                            if(hash.containsKey("")){
                                tag = hash.get("");
                            }
                            for(String key1: hash.keySet()){
                                if(s_Name.contains(key1)){
                                    tag = hash.get(key1);
                                    if(tag == null)
                                        System.out.println();
                                    break;
                                }
                            }
                        }
                    }
                    //装填
                    String[] strings = new String[]{t_id,s_Id,s_Name,tag};
                    WriteMessage.add(List.of(strings));
                }
            }
        }
        //写文件
        CsvWriter writer = new CsvWriter(new FileWriter("./src/main/resources/result.csv"),new CsvWriterSettings());
        writer.writeRow(new String[]{"task_id","storeId","storeName","tag"});
        for (List<String> list: WriteMessage) {
            writer.writeRow(list.toArray());
        }

        writer.close();

    }


}
