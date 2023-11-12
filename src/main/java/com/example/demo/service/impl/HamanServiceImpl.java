package com.example.demo.service.impl;

import com.example.demo.entity.UrlInfo;
import com.example.demo.mapper.UrlInfoMapper;
import com.example.demo.service.HamanService;
import com.example.demo.util.Platform;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class HamanServiceImpl implements HamanService {

    //数据库与对象的映射
    @Resource
    UrlInfoMapper mapper;
    //运营商和时间的拉取位置记录
    HashMap<String,Integer>[] posRecords = new HashMap[Platform.values().length];

    //构造器
    public HamanServiceImpl(){
        //初始化
        if(posRecords[0] == null)
            for(int i = 0;i < Platform.values().length;i++){
                posRecords[i] = new HashMap<>();
            }
    }

    //返回拉取结果
    @Override
    public List<UrlInfo> returnPullResult(String platform, int count, String batchNo) {
        //初始化与简单的数据判定
        List<UrlInfo> infos = null;
        int platformNum = Platform.returnPos(platform);
        if(platformNum == -1) return null;
        int start = 0;
        if(posRecords[platformNum].containsKey(batchNo))
            start = posRecords[platformNum].get(batchNo);
        //获取内容
        infos = mapper.getHamanList(platform,batchNo,start,count);
        //获取数据总长度
        int dataLen = mapper.getHamanCount(platform,batchNo);
        //处理记录
        start += count;
        if(start >= dataLen)start = 0;
        posRecords[platformNum].put(batchNo,start);
        //返回
        return infos;
    }

    //删除拉取截图
    @Override
    public void deletePullInfo(String id) {
        //删除数据
        mapper.deleteHaman(id);
    }
}
