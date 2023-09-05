<%@ page contentType="text/html;charset=utf-8"
         import="cn.js.fan.util.*,
         		com.redmoon.oa.ui.*,
         		cn.js.fan.web.*"
%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String info = ParamUtil.get(request, "info");
    if ("".equals(info)) {
        info = StrUtil.getNullStr((String) request.getAttribute("info"));
    }
    String url = ParamUtil.get(request, "url");
    String type = ParamUtil.get(request, "type");

    String callingPage = request.getHeader("Referer");
    if ("login".equals(type)) {
        // System.out.println(getClass() + " callingPage=" + callingPage);
        info = "您还没有登录或您的登录已过期，请关闭当前窗口重新登录！";
%>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
<%
    // out.print(SkinUtil.makeErrMsg(request, info));
%>
<script>
    alert("<%=info%>");
    window.top.location.href = "<%=request.getContextPath()%>/index.do";
</script>
<%
    } else if ("protect".equals(type)) {
        String kind = ParamUtil.get(request, "kind");
        String param = ParamUtil.get(request, "param");
        String value = ParamUtil.get(request, "value");
        String sourceUrl = ParamUtil.get(request, "sourceUrl");

        if ("XSS".equals(kind)) {
            com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "CSRF " + sourceUrl + " " + param + "=" + value);
            info = "XSS攻击：" + param + "=" + value + "，已记录！";
        } else {
            com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ " + sourceUrl + " " + param + "=" + value);
            info = "SQL注入：" + param + "=" + value + "，已记录！";
        }
        out.print(SkinUtil.makeErrMsg(request, info));
    }
    else {
%>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
<%
        out.print(SkinUtil.makeErrMsg(request, info));
    }
%>