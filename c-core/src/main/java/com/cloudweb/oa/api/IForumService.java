package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IForumService {

    boolean JumpToCommunity(HttpServletRequest request, HttpServletResponse response, String userName) throws ErrMsgException;

    boolean isUserLogin(HttpServletRequest request);

    String getUser(HttpServletRequest request);
}
