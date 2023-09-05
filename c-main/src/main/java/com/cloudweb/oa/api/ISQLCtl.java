package com.cloudweb.oa.api;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormField;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;

public interface ISQLCtl {
    String getCtlHtml(HttpServletRequest request, int flowId, long mainId, FormField ff, String pageType);

    String[] getSqlByDesc(String defaultValue);

    com.alibaba.fastjson.JSONObject getCtl(HttpServletRequest request, int flowId,
                      FormField ff) throws ErrMsgException;

    ResultIterator getResultByDAO(IFormDAO fdao, FormField ff);
}
