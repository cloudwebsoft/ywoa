package com.cloudweb.oa.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public interface IDocService {

    String getDirName(String dirCode);

    JSONObject listDoc(String dirCode, int page, int pageSize);

    JSONArray listImage(String dirCode, int rowCount);
}
