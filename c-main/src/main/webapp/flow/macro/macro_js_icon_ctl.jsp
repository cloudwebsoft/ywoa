<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="com.alibaba.fastjson.JSONException" %>
<%@ page import="com.alibaba.fastjson.JSONArray" %>
<%
    response.setContentType("text/javascript;charset=utf-8");

    String fieldName = ParamUtil.get(request, "fieldName");
    String formCode = ParamUtil.get(request, "formCode");
%>
    var mapPrompt = new Map();
<%
    FormDb fd = new FormDb();
    fd = fd.getFormDb(formCode);
    if (!fd.isLoaded()) {
        DebugUtil.e(getClass(), "表单", formCode + " 不存在");
        return;
    }

    FormField ff = null;
    ff = fd.getFormField(fieldName);
    String props = ff.getDescription();
    JSONObject jsonProps = null;
    try {
        jsonProps = JSONObject.parseObject(props);
    }
    catch (JSONException e) {
        DebugUtil.e(getClass(), "错误", "控件描述格式非法");
        return;
    }

    JSONArray jsonArr = jsonProps.getJSONArray("options");
    if (jsonArr == null) {
        DebugUtil.e(getClass(), "错误", "控件描述中选项格式非法");
        return;
    }
    for (int i = 0; i < jsonArr.size(); i++) {
        JSONObject json = jsonArr.getJSONObject(i);
        String icon = json.getString("icon");
        String value = json.getString("value");
%>
mapPrompt.put('<%=value%>', '<%=request.getContextPath()%>/images/icons/<%=icon%>');
<%
    }
%>
function formatStatePrompt(state) {
    if (!state.id) { return state.text; }
    // mapPrompt中键值对应的为state.id
    var $state = $(
      '<span><img src="' + mapPrompt.get(state.id).value + '" class="img-flag" /> ' + state.text + '</span>'
    );
    return $state;
}