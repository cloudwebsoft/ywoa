<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@page import="com.redmoon.oa.ui.SkinMgr" %>
<!DOCTYPE>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>序列号</title>
    <link href="../common.css" rel="stylesheet" type="text/css">
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
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
<table width="100%" height="68" cellPadding="0" cellSpacing="0">
    <tbody>
    <tr>
        <td height="28" class="tabStyle_1_title">&nbsp;工作流序列号种类选择</td>
    </tr>
    <tr>
        <td height="42" align="center" class="head"><%
            WorkflowSequenceDb wsd = new WorkflowSequenceDb();
            java.util.Iterator ir = wsd.list().iterator();
            String opts = "";
            while (ir.hasNext()) {
                wsd = (WorkflowSequenceDb) ir.next();
                opts += "<option value='" + wsd.getId() + "'>" + wsd.getName() + "</option>";
            }
        %>
            <select id="sel" name="sel" style="width:200px">
                <%=opts%>
            </select>
            &nbsp;&nbsp;<input type="button" class="btn" value="确定" onClick="doSel()"></td>
    </tr>
    </tbody>
</table>
</body>
<script language="javascript">
    <!--
    function doSel() {
        window.opener.setSequence(o('sel').options[o('sel').selectedIndex].value, o('sel').options[o('sel').selectedIndex].text);
        window.close();
    }
    //-->
</script>
</html>