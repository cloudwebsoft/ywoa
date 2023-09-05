package com.cloudweb.oa.security;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.vo.CommonConstant;
import com.cloudwebsoft.framework.util.IPUtil;
import com.redmoon.oa.LogDb;
import com.redmoon.oa.LogUtil;
import com.redmoon.oa.pvg.Privilege;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class MyLogoutSuccessHandler implements LogoutSuccessHandler {
    @Autowired
    AuthUtil authUtil;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Privilege privilege = new Privilege();
        try {
            privilege.logout(request, response);
        } catch (ErrMsgException e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error(e);
        }

        I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);

        // authUtil.getUserName()为null
        // LogUtil.log(authUtil.getUserName(), IPUtil.getRemoteAddr(request), LogDb.TYPE_LOGOUT, i18nUtil.get("action_logout"));
        // 记录退出登录日志
        LogUtil.log((String)authentication.getPrincipal(), IPUtil.getRemoteAddr(request), LogDb.TYPE_LOGOUT, i18nUtil.get("action_logout"));

        // 前端退出登录会传参数from为front，与后端有所区别，如果是后端退出登录，则重定向至/index
        String from = ParamUtil.get(request, "from");
        if (!"front".equals(from)) {
            String skincode = ParamUtil.get(request, "skincode");
            String redirectUrl = ParamUtil.get(request, "redirectUrl");
            if ("".equals(redirectUrl)) {
                redirectUrl = request.getContextPath() + "/index?skincode=" + skincode;
            }
            response.sendRedirect(redirectUrl);
        } else {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            PrintWriter out = response.getWriter();
            JSONObject json = new JSONObject();
            json.put("code", CommonConstant.SC_OK_200);
            json.put("message", "success");
            out.append(json.toString());
        }
    }
}
