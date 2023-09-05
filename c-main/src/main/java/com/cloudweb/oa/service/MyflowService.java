package com.cloudweb.oa.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

public interface MyflowService {

    String replaceSubstitution(String val);

    JSONObject generateFlowString(String flowJson, String serverName);
}
