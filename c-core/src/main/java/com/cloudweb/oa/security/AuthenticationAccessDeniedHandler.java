package com.cloudweb.oa.security;

import com.alibaba.fastjson.JSONObject;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @create
 * @desc
 **/
@Component
public class AuthenticationAccessDeniedHandler implements AccessDeniedHandler {

    private String errorPage = "/403";

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
        //判断是否为ajax请求
        if (httpServletRequest.getHeader("accept").indexOf("application/json") > -1
                || (httpServletRequest.getHeader("X-Requested-With") != null && httpServletRequest.getHeader("X-Requested-With").equals(
                "XMLHttpRequest"))) {
            //设置状态为403，无权限状态
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            //设置格式以及返回json数据 方便前台使用reponseJSON接取
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentType("application/json; charset=utf-8");
            PrintWriter out = httpServletResponse.getWriter();
            JSONObject json = new JSONObject();
            json.put("message", e.getMessage());
            out.append(json.toString());
        } else if (!httpServletResponse.isCommitted()) { //非ajax请求
            if (errorPage != null) {
                // Put exception into request scope (perhaps of use to a view)
                httpServletRequest.setAttribute(WebAttributes.ACCESS_DENIED_403, e);

                // Set the 403 status code.
                httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);

                // forward to error page.
                RequestDispatcher dispatcher = httpServletRequest.getRequestDispatcher(errorPage);
                httpServletRequest.setAttribute("message", e.getMessage());
                dispatcher.forward(httpServletRequest, httpServletResponse);
            } else {
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            }
        }
    }

}
