<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>模块选择</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    // 当从模式对话框打开本窗口时，因为分属于不同的IE进程，SESSION会丢失，可以用cookie中置sessionId来解决这个问题
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        // out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        // return;
    }
%>
<table class="tabStyle_1" style="padding:0px; margin:0px;" width="100%" cellPadding="0" cellSpacing="0">
    <tbody>
    <tr>
        <td height="28" class="tabStyle_1_title">&nbsp;请选择：</td>
    </tr>
    <tr>
        <td height="42" align="center"><%
            ModuleSetupDb msd = new ModuleSetupDb();
            Iterator mir = msd.listUsed().iterator();
            String opts = "";
            FormDb fd = new FormDb();
            while (mir.hasNext()) {
                msd = (ModuleSetupDb) mir.next();
                fd = fd.getFormDb(msd.getString("code"));
                if (fd.isLoaded()) {
                    opts += "<option value='" + msd.getString("code") + "'>" + fd.getName() + "</option>";
                }
            }
        %>
            <select id="sel" name="sel" style="width:200px">
                <%=opts%>
            </select>
            &nbsp;&nbsp;<input type="button" value="确定" onClick="doSel()"></td>
    </tr>
    </tbody>
</table>
</body>
<script language="javascript">
    function doSel() {
        window.opener.setSequence(o("sel").options[o("sel").selectedIndex].value, o("sel").options[o("sel").selectedIndex].text);
        window.close();
    }
</script>
</html>