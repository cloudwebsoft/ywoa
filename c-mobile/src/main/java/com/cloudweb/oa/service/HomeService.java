package com.cloudweb.oa.service;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HomeService {

    String login(HttpServletRequest request,
                 String name,
                 String password,
                 String deviceId,
                 String client,
                 String openId,
                 HttpServletResponse response
    );


    String loginByMiniLoginInfo(HttpServletRequest request, JSONObject userInfoJson, String deviceId, String client, HttpServletResponse response);

    String loginByLoginInfo(HttpServletRequest request, String userName, String deviceId, String client, HttpServletResponse response);
}

