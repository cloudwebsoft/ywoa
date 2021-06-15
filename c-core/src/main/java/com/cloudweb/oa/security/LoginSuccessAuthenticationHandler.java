package com.cloudweb.oa.security;

import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.service.LoginService;
import com.cloudweb.oa.utils.SpringUtil;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author qiumin
 * @create 2019/1/13 12:59
 * @desc
 **/
@Component
public class LoginSuccessAuthenticationHandler implements AuthenticationSuccessHandler {

    @Autowired
    LoginService loginService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String str = null;
        try {
            if (loginService==null) {
                loginService = SpringUtil.getBean(LoginService.class);
            }
            str = loginService.login(request, response);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject json = JSONObject.parseObject(str);
        json.put("code", "400");
        //设置返回请求头
        response.setContentType("application/json;charset=utf-8");
        //写出流
        PrintWriter out = response.getWriter();
        out.write(json.toJSONString());
        out.flush();
        out.close();
    }
}
