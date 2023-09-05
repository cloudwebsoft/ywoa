package com.cloudweb.oa.controller;

import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.pojo.AppErrorResponseEntity;
import com.redmoon.oa.pvg.Privilege;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * web错误 全局配置
 */
@Controller
@Slf4j
public class AppErrorController implements ErrorController {
    private static final String ERROR_PATH = "/error";
    private static final String ERROR_PATH_REST = "/error/rest";

    // 如果没有MyErrorAttributes，则默认将会autowired DefaultErrorAttributes，但在外部Tomcat中如果没有MyErrorAttributes则会报错
    @Autowired
    private ErrorAttributes errorAttributes;

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    /**
     * Web页面错误处理
     */
    @RequestMapping(value = ERROR_PATH, produces = MediaType.TEXT_HTML_VALUE)
    public String handlePathError(HttpServletRequest request, HttpServletResponse response) {
        int status = response.getStatus();
        ServletWebRequest servletWebRequest = new ServletWebRequest(request);
        Map<String, Object> map = this.errorAttributes.getErrorAttributes(servletWebRequest, true);
        log.error((String)map.get("trace"));
        switch (status) {
            case 403:
                return "403";
            default:
                request.setAttribute("code", status);
                request.setAttribute("msg", status + ": " + map.get("message"));
                return "th/error/error";
        }
    }

    /**
     * Json/XML等错误的处理
     */
    @RequestMapping(value = ERROR_PATH_REST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    // @ExceptionHandler(value = {Exception.class})
    public AppErrorResponseEntity handleError(HttpServletRequest request) {
        ServletWebRequest servletWebRequest = new ServletWebRequest(request);
        Map<String, Object> map = this.errorAttributes.getErrorAttributes(servletWebRequest, true);
        int status = getStatus(request);
        log.error((String)map.get("trace"));

        String type = (String)request.getAttribute("type");
        // 来自于ProtectFilter
        if ("protect".equals(type)) {
            String kind = (String)request.getAttribute("kind");
            String param = (String)request.getAttribute("param");
            String sourceUrl = (String)request.getAttribute("sourceUrl");
            String value = (String)request.getAttribute("value");

            Privilege privilege = new Privilege();
            String info = "";
            if ("XSS".equals(kind)) {
                com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "XSS " + sourceUrl + " " + param + "=" + value);
                info = "XSS攻击：参数 " + param + "，已记录！";
            } else if ("CSRF".equals(kind)) {
                com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "CSRF " + sourceUrl + " " + param + "=" + value);
                info = "CSRF攻击：参数 " + param + "，已记录！";
            } else if ("SQLInject".equals(kind)) {
                com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ " + sourceUrl + " " + param + "=" + value);
                info = "SQL注入：参数 " + param + "，已记录！";
            }
            return AppErrorResponseEntity.init(AppErrorResponseEntity.Status.PROTECT.getCode(), info);
        }
        else {
            return AppErrorResponseEntity.init(status, String.valueOf(map.getOrDefault("message", "Unknown error.")));
        }
    }

    private int getStatus(HttpServletRequest request) {
        Integer status = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (status != null) {
            return status;
        }

        return 500;
    }
}