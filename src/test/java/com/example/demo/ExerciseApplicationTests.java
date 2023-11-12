package com.example.demo;

import com.example.demo.entity.DirTag;
import com.example.demo.entity.WriteData;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

    //写容器数组量
    private final int MSG = 4;

    public List<List<String>> ManageFile(String filePath,boolean isSelect) throws IOException {
        List<List<String>> list  = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8));

        String line = br.readLine();
        while((line = br.readLine()) != null){
            List<String> Msg = Arrays.asList(line.split(","));
            if(isSelect){
                List<String> msg = new ArrayList<>();
                msg.add(Msg.get(1));
                msg.add(line.substring(line.indexOf('{'),line.lastIndexOf('}')+1));
                list.add(msg);
            }else{
                list.add(Msg);
            }
        }

        br.close();

        return list;
    }

    @Test
    void TakeAndMatchShopName() throws Exception {
        //准备写入的数据
        Set<String> names = new HashSet<>();//用于去重的店名列表
        Set<WriteData>[] WriteMessage = new HashSet[MSG];
        for(int i = 0;i < MSG;i++){
            WriteMessage[i] = new HashSet<>();
        }
        //读文件
        List<List<String>> ShopMessage = ManageFile(filePath+"/sample.csv",true);
        List<List<String>> KeywordMessage = ManageFile(filePath+"/标签词库1026.csv",false);
        //处理数据
        HashMap<String,HashMap<String,HashMap<String, DirTag>>> KeywordHash = new HashMap<>();
        //处理标签
        int index = 0;
        for (List<String> list1: KeywordMessage){
            String key1 = list1.get(0);
            String key2 = list1.get(1);
            String key3 = list1.get(2);
            String value = list1.get(4);
            if(KeywordHash.containsKey(key1)){
                HashMap<String,HashMap<String,DirTag>> hash = KeywordHash.get(key1);
                if(hash.containsKey(key2)){
                    HashMap<String,DirTag> hash0 = hash.get(key2);
                    hash0.put(key3,new DirTag(index,value));
                }else{
                    HashMap<String,DirTag> hash0 = new HashMap<>();
                    hash0.put(key3,new DirTag(index,value));
                    hash.put(key2,hash0);
                }
            }else{
                HashMap<String,HashMap<String,DirTag>> hash = new HashMap<>();
                HashMap<String,DirTag> hash0 = new HashMap<>();
                hash0.put(key2,new DirTag(index,value));
                hash.put(key2,hash0);
                KeywordHash.put(key1,hash);
            }
            index++;
        }
        //处理sample
        for (List<String> list1: ShopMessage) {
            String t_id = list1.get(0);
            JSONObject json = new JSONObject(list1.get(1).replace("\"\"","\""));
            if(json.getString("success").equals("false"))break;
            JSONArray json_data =  json.getJSONObject("result").getJSONArray("data");
            for (int i = 0;i < json_data.length();i++) {
                JSONObject jo = json_data.getJSONObject(i).getJSONObject("data");
                //添加筛选数据
                if(!jo.has("storeId") || !jo.has("storeName"))
                    break;
                String s_Id = jo.getString("storeId");
                String s_Name = jo.getString("storeName");
                //添加tag数据
                String tag = "";
                int pos = 3;
                int id = 0;
                for(String key: KeywordHash.keySet()){
                    if(s_Name.contains(key)){
                        pos = 2;
                        HashMap<String,HashMap<String,DirTag>> hash = KeywordHash.get(key);
                        for(String key1: hash.keySet()){
                            if(s_Name.contains(key1)){
                                if(key1.isEmpty()){
                                    DirTag dt = hash.get(key1).get("");
                                    tag = dt.getTag();
                                    id = dt.getId();
                                    break;
                                }else{
                                    pos = 1;
                                    HashMap<String,DirTag> hash0 = hash.get(key1);
                                    for(String key2: hash0.keySet()){
                                        if(s_Name.contains(key2)){
                                            if(!key2.isEmpty()){
                                                pos = 0;
                                            }
                                            DirTag dt = hash.get(key1).get("");
                                            tag = dt.getTag();
                                            id = dt.getId();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //装填
                if(!names.contains(s_Name)){
                    WriteData wd = new WriteData(List.of(new String[]{t_id, s_Id, s_Name, tag}),id);
                    WriteMessage[pos].add(wd);
                    names.add(s_Name);
                }
            }
        }
        //写文件
        CsvWriter writer = new CsvWriter(new FileWriter("./src/main/resources/result.csv"),new CsvWriterSettings());
        writer.writeRow(new String[]{"task_id","storeId","storeName","tag"});
        for(int i = 0;i < MSG;i++){
            //提取
            List<WriteData> list = new ArrayList<>(WriteMessage[i].stream().toList());
            //排序
            list.sort(new Comparator<>() {
                @Override
                public int compare(WriteData o1, WriteData o2) {
                    if(o1.getSortNum() ==0 && o2.getSortNum() != 0)
                        return 1;
                    if(o1.getSortNum() !=0 && o2.getSortNum() == 0)
                        return -1;
                    return o1.getSortNum() - o2.getSortNum();
                }
            });
            //书写
            for(WriteData wd : list){
                writer.writeRow(wd.getInputString());
            }
        }

        writer.close();

    }



}
