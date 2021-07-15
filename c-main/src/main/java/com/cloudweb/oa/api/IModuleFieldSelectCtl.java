package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

public interface IModuleFieldSelectCtl {

    JSONArray getAjaxOpts(HttpServletRequest request, FormField ff) throws ErrMsgException, JSONException;

    JSONArray getOnSel(HttpServletRequest request, FormField ff, FormDb fd) throws ErrMsgException, JSONException, SQLException;

    String formatJSONString(String defaultVal);

    JSONObject getCtlDescription(FormField ff);

    void autoMap(HttpServletRequest request, int flowId, String value, FormField ff);
}
