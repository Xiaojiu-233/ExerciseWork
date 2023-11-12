package com.example.demo.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public enum Platform {
    京东,淘宝,天猫;

    //返回平台在数据Hash表中的位置，如果没找到返回-1
    public static int returnPos(String platform){
        HashSet<String> names = new HashSet<>();
        int index = -1;
        for(Platform p : Platform.values()){
            if(platform.equals(p.name())){
                index = Platform.valueOf(platform).ordinal();
                break;
            }
        }
        return index;
    }

}
