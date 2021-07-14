<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>签名</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../../inc/common.js"></script>
</head>
<body onload="if (o('pwd')) o('pwd').focus();">
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

// 窗口不能做成模式对话框，因为form及location.href都会弹出新窗口
    String op = ParamUtil.get(request, "op");
    if (op.equals("sign")) {
        String pwd = ParamUtil.get(request, "pwd");
        UserDb ud = new UserDb();
        ud = ud.getUserDb(privilege.getUser(request));
        if (ud.getPwdRaw().equals(pwd)) {
%>
<script>
    window.opener.setIntpuObjValue("<%=ud.getRealName() + "   " + cn.js.fan.util.DateUtil.format(new java.util.Date(), "yyyy年MM月dd日")%>");
    window.close();
</script>
<%
            return;
        } else {
            out.print(StrUtil.Alert("密码错误！"));
        }
    }
%>
<form name="form1" action="?op=sign" method="post">
    <table class="tabStyle_1 percent98" width="100%" cellpadding="0" cellspacing="0">
        <thead>
        <tr>
            <td height="28" class="tabStyle_1_title">&nbsp;请输入密码</td>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td height="40" align="center" bgcolor="#FFFFFF" class="head">
                <input name="pwd" type="password" autocomplete="off"/>
                &nbsp;&nbsp;
                <input class="btn" type="submit" value="签名"/>
            </td>
        </tr>
        </tbody>
    </table>
</form>
</body>
</html>