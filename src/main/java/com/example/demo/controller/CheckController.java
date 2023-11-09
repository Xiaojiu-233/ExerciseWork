package com.example.demo.controller;

import com.example.demo.entity.ExposeMsg;
import com.example.demo.entity.StoreUrl;
import com.example.demo.util.Platform;
import com.example.demo.util.WaterMarkUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@RestController
public class CheckController {

    //用于存储的数据
    HashMap<String, List<StoreUrl>>[] hashMap = new HashMap[Platform.values().length];
    //读取的索引的数据库存长度
    HashMap<String, Integer>[] lengths = new HashMap[Platform.values().length];
    //读取过的数据容器
    HashMap<String,StoreUrl> tombMap = new HashMap<>();
    //储存链接
    @Value("${snapshot.path}")
    private String storePath;
    //返回的图片链接路径
    @Value("${snapshot.web}")
    private String webPath;

    //构造器
    public CheckController() throws IOException {
        //初始化hashmap数组，索引代表平台enum的值
        for(int i = 0;i < Platform.values().length;i++){
            hashMap[i] = new HashMap<>();
            lengths[i] = new HashMap<>();
        }
        //读取文件 处理后装入容器
        BufferedReader br = new BufferedReader(new FileReader(  "src/main/resources/haman_2023102801.csv"));
        String line = br.readLine();
        while((line = br.readLine()) != null){
            List<String> Msg = Arrays.asList(line.split(","));
            //判定
            if(!Platform.returnNames().contains(Msg.get(2)))
                continue;;
            //数据准备
            int index = Platform.valueOf(Msg.get(2)).ordinal();
            List<StoreUrl> listUrl = null;
            String batchId = Msg.get(1);
            StoreUrl  su = new StoreUrl(Msg.get(0),Msg.get(3),Msg.get(4),false);
            //装填
            if(hashMap[index].containsKey(batchId)){
                listUrl = hashMap[index].get(Msg.get(1));
            }else{
                listUrl = new ArrayList<>();
            }
            listUrl.add(su);
            hashMap[index].put(batchId,listUrl);
            lengths[index].put(batchId,0);
        }
        //关闭文件读取
        br.close();
    }

    //拉取破价链接
    @GetMapping("/breakPriceUrls")
    public synchronized Map<String,Object> GetCheckUrl(@RequestParam(value = "platform",required = true)String platform,
                                          @RequestParam(value = "count",defaultValue = "10")int count,
                                          @RequestParam(value = "batchNo",required = true)String batchNo){
        //准备数据
        HashMap<String,Object> ret = new HashMap<>();
        List<ExposeMsg> list = new ArrayList<>();
        boolean status = true;
        int index = 0;
        //数据判定
        if(!Platform.returnNames().contains(platform)){
            status = false;
        }else{
            index = Platform.valueOf(platform).ordinal();
            if(!hashMap[index].containsKey(batchNo)){
                status = false;
            }else{
                List<StoreUrl> list_url = hashMap[index].get(batchNo);
                int max_len = list_url.size();
                int point = lengths[index].get(batchNo)% max_len;
                int start = point;
                //读取list内容并返回
                for(int i = 0;i < count ;i++){
                    StoreUrl st = list_url.get(point);
                    if(st.isTomb()){
                        i--;point= (point +1)% max_len;continue;
                    }
                    list.add(new ExposeMsg(batchNo,platform,st));
                    tombMap.put(st.getId(),st);
                    point= (point +1)% max_len;
                    if(point == start)break;
                }
                lengths[index].put(batchNo,point);
            }
        }
        //返回数据
        ret.put("data",list);
        ret.put("status",status);
        return ret;
    }

    //服务端获取、处理与保存截图
    @PostMapping("/uploadImg")
    public Map<String,Object> ManageSnapshot(@RequestParam("img") String ImgBase64,@RequestParam("id")String Id){
        //数据准备
        HashMap<String,Object> ret = new HashMap<>();
        boolean status = false;
        Date nowTime = new Date();
        SimpleDateFormat dateFormat= new SimpleDateFormat("yyMMddhhmmss");
        String fileName = dateFormat.format(nowTime);
        //解析base64获得图片
        byte[] decodedBytes = Base64.getDecoder().decode(ImgBase64);
        try{
            String filePath =storePath+fileName+".png";
            WaterMarkUtil.SetWaterMark(filePath,decodedBytes,nowTime);
            //将对应id的记录设置为tomb=true
            if(tombMap.containsKey(Id))
                tombMap.get(Id).setTomb(true);
            //完成
            status = true;
        }catch (Exception ignored){}
        //保存至服务器
        ret.put("url",webPath+fileName+".png");
        ret.put("status",status);
        return ret;
    }

    @GetMapping("snapshots/get/{name}")
    public void returnShot(@PathVariable("name") String fileName, HttpServletResponse response){
        try{
            FileInputStream fileInputStream = new FileInputStream(storePath+fileName);
            ServletOutputStream servletOutputStream = response.getOutputStream();
            response.setContentType("image/png");
            //读取文件
            int len = 0;
            byte[] bytes = new byte[1024];
            while((len = fileInputStream.read(bytes)) != -1){
                servletOutputStream.write(bytes,0,len);
                servletOutputStream.flush();
            }
            //关闭输出输入流
            servletOutputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            System.out.println("出现异常！" + e);
        }
    }

}
