<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.cloudweb.oa.api.ICloudUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.redmoon.oa.Config" %>
<%
    String formCode = ParamUtil.get(request, "formCode");
    String cwsToken = ParamUtil.get(request, "cwsToken");
    com.redmoon.oa.Config cfg = Config.getInstance();
    String cloudUrl = "";
    if (cfg.getBooleanProperty("isCloud")) {
        ICloudUtil cloudUtil = SpringUtil.getBean(ICloudUtil.class);
        cloudUrl = cloudUtil.getLoginCheckUrl(cwsToken, formCode);
    }
%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>脚本设计器</title>
    <script>
        function setCols(cols) {
            frm.cols = cols;
        }

        function getCols() {
            return frm.cols;
        }
    </script>
</head>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    if (!cfg.getBooleanProperty("isCloud")) {
%>
<frameset id="frm" cols="250,*" framespacing="3" frameborder="1">
    <noframes>
        <body></body>
    </noframes>
    <frame src="script_left.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>" id="leftScriptFrame" name="leftScriptFrame" marginwidth="0" marginheight="0" scrolling="auto" frameborder="1"/>
    <frame src="script_main.jsp" id="mainScriptFrame" name="mainScriptFrame" marginwidth="0" marginheight="0" scrolling="auto" frameborder="1"/>
</frameset>
</frameset>
<%} else {%>
<frameset id="frm" cols="250,*" framespacing="3" frameborder="1">
    <noframes>
        <body></body>
    </noframes>
    <frame src="ide_left.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>" id="leftFrame" name="leftFrame" marginwidth="0" marginheight="0" scrolling="auto" frameborder="1"/>
    <frame src="<%=cloudUrl%>" id="mainFrame" name="mainFrame" marginwidth="0" marginheight="0" scrolling="auto" frameborder="1"/>
</frameset>
</frameset>
<%}%>
</html>
