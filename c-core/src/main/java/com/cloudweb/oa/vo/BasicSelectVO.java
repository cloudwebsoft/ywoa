package com.cloudweb.oa.vo;

import com.alibaba.fastjson.JSONArray;
import com.redmoon.oa.basic.SelectOptionDb;
import lombok.Data;

import java.util.List;

@Data
public class BasicSelectVO {

    private String code;

    private String name;

    private Integer orders;

    private int type;

    private int kind;

    private String typeName;

    private String kindName;

    private List<SelectOptionDb> options;

    private JSONArray treeData;

    private String defaultValue;
}
