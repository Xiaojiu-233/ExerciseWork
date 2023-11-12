package com.example.demo.controller;

import com.example.demo.entity.UrlInfo;
import com.example.demo.service.HamanService;
import com.example.demo.util.WaterMarkUtil;
import jakarta.annotation.Resource;
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

    //服务层
    @Resource
    HamanService hamanService;
    //储存链接
    @Value("${snapshot.path}")
    private String storePath;
    //返回的图片链接路径
    @Value("${snapshot.web}")
    private String webPath;

    //拉取破价链接
    @GetMapping("/breakPriceUrls")
    public synchronized Map<String,Object> GetCheckUrl(@RequestParam(value = "platform",required = true)String platform,
                                          @RequestParam(value = "count",defaultValue = "10")int count,
                                          @RequestParam(value = "batchNo",required = true)String batchNo){
        //准备数据
        HashMap<String,Object> ret = new HashMap<>();
        List<UrlInfo> list = hamanService.returnPullResult(platform, count, batchNo);
        boolean status = list != null;
        //返回数据
        ret.put("data",list);
        ret.put("status",status);
        return ret;
    }

    //服务端获取、处理与保存截图
    @PostMapping("/uploadImg")
    public synchronized Map<String,Object> ManageSnapshot(@RequestParam("img") String ImgBase64,@RequestParam("id")String Id){
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
            //将对应id的记录删除
            hamanService.deletePullInfo(Id);
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
