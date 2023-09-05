package com.cloudweb.oa.security;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.web.SkinUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.IPUtil;
import com.redmoon.oa.LogDb;
import com.redmoon.oa.LogUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
        String name = "";
        try {
            name = URLDecoder.decode(ParamUtil.get(request, "name", true), "utf-8");
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
        }

        I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);
        LogUtil.log(name, IPUtil.getRemoteAddr(request), LogDb.TYPE_LOGIN, i18nUtil.get("warn_login_fail"));

        // LogUtil.log(name, request.getRemoteAddr(), LogDb.TYPE_LOGIN, LogUtil.get(request, "warn_login_fail"));

        // 登录失败信息返回
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("ret", 0);
        paramMap.put("code", 401);
        if (e.getMessage().startsWith("auth_")) {
            // paramMap.put("msg", SkinUtil.LoadString(request, e.getMessage()));
            paramMap.put("msg", i18nUtil.get(e.getMessage()));
        }
        else {
            paramMap.put("msg", e.getMessage());
        }
        // 设置返回请求头
        response.setContentType("application/json;charset=utf-8");
        //写出流
        PrintWriter out = response.getWriter();
        out.write(JSONObject.toJSONString(paramMap));
        out.flush();
        out.close();
    }
}
