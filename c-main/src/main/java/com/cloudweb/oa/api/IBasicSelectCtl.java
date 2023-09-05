package com.cloudweb.oa.api;

import com.redmoon.oa.flow.FormField;

import javax.servlet.http.HttpServletRequest;

public interface IBasicSelectCtl {
    String convertToHtmlCtl(HttpServletRequest request, String fieldName, String code);

    String getCode(FormField ff);
}
