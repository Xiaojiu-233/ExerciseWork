package com.example.demo.service;

import com.example.demo.entity.UrlInfo;

import java.util.List;

public interface HamanService {

    //返回拉取结果
    List<UrlInfo> returnPullResult(String platform, int count, String batchNo);

    //删除拉取截图
    void deletePullInfo(String id);
}
