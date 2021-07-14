package com.cloudweb.oa.controller;

import com.cloudweb.oa.pojo.AppErrorResponseEntity;
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
    @RequestMapping(value = ERROR_PATH, produces = "text/html")
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
    @RequestMapping(value = ERROR_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    // @ExceptionHandler(value = {Exception.class})
    public AppErrorResponseEntity handleError(HttpServletRequest request) {
        ServletWebRequest servletWebRequest = new ServletWebRequest(request);
        Map<String, Object> map = this.errorAttributes.getErrorAttributes(servletWebRequest, true);
        int status = getStatus(request);
        log.error((String)map.get("trace"));
        return AppErrorResponseEntity.init(status, String.valueOf(map.getOrDefault("message", "Unknown error.")));
    }

    private int getStatus(HttpServletRequest request) {
        Integer status = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (status != null) {
            return status;
        }

        return 500;
    }
}