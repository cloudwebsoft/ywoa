package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.flow.FormField;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public interface INestSheetCtl {

    String getNestSheet(HttpServletRequest request, FormField ff);

    boolean autoSelect(HttpServletRequest request, long parentId, FormField nestField) throws ErrMsgException;

    int uploadExcel(ServletContext application, HttpServletRequest request, long parentId) throws ErrMsgException;

    JSONObject getCtlDescription(FormField ff);
}
