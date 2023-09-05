package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import com.alibaba.fastjson.JSONObject;

public interface IMyflowUtil {
    String toMyflow(String flowString) throws ErrMsgException;

    boolean generateMyflowForFree(int flowId) throws ErrMsgException;
}
