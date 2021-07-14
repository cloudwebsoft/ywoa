package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;

public interface IMyflowUtil {
    String toMyflow(String flowString) throws ErrMsgException;
}
