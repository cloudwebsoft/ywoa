<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="com.alibaba.fastjson.JSONException" %>
<%@ page import="com.alibaba.fastjson.JSONArray" %>
<%@ page import="com.cloudweb.oa.utils.SysUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%
    response.setContentType("text/javascript;charset=utf-8");

    String fieldName = ParamUtil.get(request, "fieldName");
    String formCode = ParamUtil.get(request, "formCode");
%>
<script>
    var mapPrompt = new MyMap();
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
    SysUtil sysUtil = SpringUtil.getBean(SysUtil.class);
    for (int i = 0; i < jsonArr.size(); i++) {
        JSONObject json = jsonArr.getJSONObject(i);
        String icon = json.getString("icon");
        String value = json.getString("value");
%>
    mapPrompt.put('<%=value%>', '<%=sysUtil.getRootPath()%>/showImgInJar.do?path=/static/images/symbol/<%=icon%>');
<%
    }
%>
(async function() {
    // 加载图标
    var ary = mapPrompt.getElements();
    var len = ary.length;
    for (var k=len-1; k>=0; k--) {
        let key = ary[k].key;
        let val = ary[k].value;
        if (typeof loadImgInJar == 'function') {
            // mapPrompt.remove(key);
            // 在项目编辑页面await后就会执行formatStatePrompt，结果mapPrompt因为被remove了，长度就变为了2，致取不到绿灯，故改写了mymap.js，在put的时候检查如果存在key则删除
            var buf = await loadImgInJar(val);
            mapPrompt.put(key, buf);
        } else {
            console.error('loadImgInJar is not function.');
        }
    }
    // console.log('mapPrompt', mapPrompt);
})();

function formatStatePrompt(state) {
    if (!state.id) { return state.text; }
    if (mapPrompt.get(state.id)) {
        console.log('formatStatePrompt', mapPrompt.get(state.id).value);
        // mapPrompt中键值对应的为state.id
        var $state = $(
            '<span><img src="' + mapPrompt.get(state.id).value + '" class="img-flag" /> ' + state.text + '</span>'
        );
        /*if (typeof loadImgByJQueryObj == 'function') {
            loadImgByJQueryObj($state);
        }*/
        return $state;
    } else {
        console.error('mapPrompt has not key: ', state.id, 'mapPrompt', mapPrompt);
    }
}
</script>