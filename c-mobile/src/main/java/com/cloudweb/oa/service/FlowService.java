package com.cloudweb.oa.service;

import com.redmoon.oa.flow.MyActionDb;
import org.json.JSONObject;

public interface FlowService {

    JSONObject init(MyActionDb mad, String mutilDept);
}
