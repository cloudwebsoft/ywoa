package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import org.json.JSONArray;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Vector;

public interface INestTableCtl {

    String getNestTable(HttpServletRequest request, FormField ff);

    Vector<FormField> parseFieldsByView(FormDb fd, String viewContent);

    String convertTextarea2InputAndClearCwsSpan(String html);

    int uploadExcel(ServletContext application, HttpServletRequest request) throws ErrMsgException;

    JSONArray getCellJsonArray();
}
