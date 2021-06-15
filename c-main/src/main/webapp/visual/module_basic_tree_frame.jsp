<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.redmoon.oa.basic.SelectDb" %>
<%@ page import="com.redmoon.oa.basic.SelectKindPriv" %>
<%@ page import="com.redmoon.oa.pvg.PrivDb" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "read")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String basicCode = ParamUtil.get(request, "basicCode");
    SelectDb selectDb = new SelectDb();
    selectDb = selectDb.getSelectDb(basicCode);
    if (!selectDb.isLoaded()) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, SkinUtil.ERR_ID)));
        return;
    }

	String userName = privilege.getUser(request);
	int kind = selectDb.getKind();
    SelectKindPriv skp = new SelectKindPriv();
    if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
        if (skp.canUserAppend(userName, kind) || skp.canUserModify(userName, kind) || skp.canUserDel(userName, kind)) {
        } else {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title><%=selectDb.getName()%>列表</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <script src="../inc/common.js"></script>
</head>
<frameset id="frm" cols="250,*" framespacing="3" frameborder="1">
    <noframes>
        <body></body>
    </noframes>
    <frame src="module_basic_tree.jsp?basicCode=<%=StrUtil.UrlEncode(basicCode)%>" id="leftModuleFrame" name="leftModuleFrame" marginwidth="0" marginheight="0" scrolling="auto" frameborder="1"/>
    <frame src="" id="mainModuleFrame" name="mainModuleFrame" marginwidth="0" marginheight="0" scrolling="auto" frameborder="1"/>
</frameset>
</html>
