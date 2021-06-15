package com.cloudweb.oa.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * @author
 */
public class MyCommonsMultipartResolver extends CommonsMultipartResolver {

    private String excludeUrls;
    /**
     * 被排除的url数组
     */
    private String[] excludeUrlArray;

    private String exceptionUrls;
    /**
     * 被排除的数组中的例外情况的数组
     */
    private String[] exceptionArray;

    public String getExcludeUrls() {
        return excludeUrls;
    }

    public void setExcludeUrls(String excludeUrls) {
        this.excludeUrls = excludeUrls;
        this.excludeUrlArray = excludeUrls.split(",");
    }

    public void setExcludeUrlArray(String[] excludeUrlArray) {
        this.excludeUrlArray = excludeUrlArray;
    }

    @Override
    public boolean isMultipart(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String url: excludeUrlArray) {
            if (uri.contains(url)) {
                for (String url2 : exceptionArray) {
                    if (uri.contains(url2)) {
                        return true;
                    }
                }
                return false;
            }
        }

        return super.isMultipart(request);
    }

    public void setExceptionUrls(String exceptionUrls) {
        this.exceptionUrls = exceptionUrls;
        this.exceptionArray = exceptionUrls.split(",");
    }

    public void setExceptionArray(String[] exceptionArray) {
        this.exceptionArray = exceptionArray;
    }
}