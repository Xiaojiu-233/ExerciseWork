package com.example.demo.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public enum Platform {
    京东,淘宝,天猫;

    public static HashSet<String> returnNames(){
        HashSet<String> names = new HashSet<>();
        for(Platform p : Platform.values()){
            names.add(p.name());
        }
        return names;
    }

}
