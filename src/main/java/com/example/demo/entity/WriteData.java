package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class WriteData {

    List<String> inputString;

    int sortNum = 0;
}
