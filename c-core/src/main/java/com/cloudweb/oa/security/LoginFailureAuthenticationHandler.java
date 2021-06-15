package com.cloudweb.oa.security;

import cn.js.fan.web.SkinUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qiumin
 * @create 2019/1/13 13:06
 * @desc
 **/
@Component
public class LoginFailureAuthenticationHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        //登录失败信息返回
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("ret", "0");
        paramMap.put("code", "500");
        if (e.getMessage().startsWith("auth_")) {
            paramMap.put("msg", SkinUtil.LoadString(request, e.getMessage()));
        }
        else {
            paramMap.put("msg", e.getMessage());
        }
        //设置返回请求头
        response.setContentType("application/json;charset=utf-8");
        //写出流
        PrintWriter out = response.getWriter();
        out.write(JSONObject.toJSONString(paramMap));
        out.flush();
        out.close();
    }
}
