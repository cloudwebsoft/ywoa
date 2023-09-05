package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Vector;

public interface IModuleFieldSelectCtl {

    com.alibaba.fastjson.JSONArray getAjaxOpts(HttpServletRequest request, FormField ff) throws ErrMsgException;

    com.alibaba.fastjson.JSONArray getOnSel(HttpServletRequest request, FormField ff, FormDb fd) throws ErrMsgException, SQLException;

    String formatJSONString(String defaultVal);

    com.alibaba.fastjson.JSONObject getCtlDescription(FormField ff);

    void autoMap(HttpServletRequest request, int flowId, String value, FormField ff);

    void autoMapOnAdd(HttpServletRequest request, Vector<FormField> v, String value, FormField ff);
}
