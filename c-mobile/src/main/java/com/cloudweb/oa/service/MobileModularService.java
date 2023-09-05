package com.cloudweb.oa.service;

import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;

public interface MobileModularService {
    String create(HttpServletRequest request) throws ErrMsgException;
    String update(HttpServletRequest request) throws ErrMsgException;
}
