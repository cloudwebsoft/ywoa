package com.cloudweb.oa.security;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import com.redmoon.oa.pvg.Privilege;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class MyLogoutSuccessHandler implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Privilege privilege = new Privilege();
        try {
            privilege.logout(request, response);
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }

        String skincode = ParamUtil.get(request, "skincode");
        String redirectUrl = ParamUtil.get(request, "redirectUrl");
        if ("".equals(redirectUrl)) {
            redirectUrl = request.getContextPath() + "/index?skincode=" + skincode;
        }
        response.sendRedirect(redirectUrl);
    }
}
