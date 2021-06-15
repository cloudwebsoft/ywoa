<%@ page contentType="text/html;charset=utf-8" %>
<%@ include file="../inc/nocache.jsp" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="cn.js.fan.util.ErrMsgException" %>
<html>
<head>
    <title>消息回复</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <LINK href="../common.css" type=text/css rel=stylesheet>
</head>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
    if (!privilege.isUserLogin(request)) { %>
<table width="320" border="0" cellspacing="0" cellpadding="0" align="center" class="9black">
    <tr>
        <td>
            <li>您的登录已过期，请重新登录，如果不是会员请先注册。
        </td>
    </tr>
</table>
<% } %>
<%
    // 防漏扫
    if (!request.getMethod().equalsIgnoreCase("POST")) {
        out.print(StrUtil.Alert_Back("请使用POST方式！"));
        return;
    }
    
    String name = privilege.getUser(request);
    String title, receiver, content, errmsg = "";
    title = StrUtil.getNullString(request.getParameter("title"));
    receiver = StrUtil.getNullString(request.getParameter("receiver"));
    content = StrUtil.getNullString(request.getParameter("content"));
    if (title.trim().length() == 0 || content.trim().length() == 0)
        errmsg += "标题和内容不能为空！\n";
    if (name.equals(receiver))
        errmsg += "请不要给自己发信息！\n";
    if (!errmsg.equals(""))
        StrUtil.Alert_Back(errmsg);
    
    if (!privilege.isUserLogin(request)) { %>
<table width="320" border="0" cellspacing="0" cellpadding="0" align="center" class="9black">
    <tr>
        <td>
            <li>您的登录已过期，请重新登录，如果不是会员请先注册。
        </td>
    </tr>
</table>
<% } %>
<table width="320" border="0" cellspacing="1" cellpadding="3" align="center" bgcolor="#99CCFF" class="9black"
       height="260">
    <tr>
        <td bgcolor="#CEE7FF" height="23">
            <div align="center"><b>撰 写 新 消 息</b></div>
        </td>
    </tr>
    <tr>
        <td bgcolor="#FFFFFF" height="50">
            <table width="300" border="0" cellspacing="0" cellpadding="0" align="center">
                <tr>
                    <td width="75">
                        <div align="center"><a href="message.jsp?page=1"><img src="images/inboxpm.gif" width="40"
                                                                              height="40" border="0"></a></div>
                    </td>
                    <td width="75">
                        <div align="center"><a href="mysend.jsp"><img src="images/m_outbox.gif" width="40" height="40"
                                                                      border="0"></a></div>
                    </td>
                    <td width="75">
                        <div align="center"><a href="send.jsp"><img src="images/newpm.gif" width="40" height="40"
                                                                    border="0"></a></div>
                    </td>
                    <td width="75">
                        <div align="center"><img src="images/m_delete.gif" width="40" height="40"></div>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td bgcolor="#FFFFFF" height="152" valign="top">
            <table width="300" border="0" cellspacing="0" cellpadding="0" align="center" class="9black" height="6">
                <tr>
                    <td></td>
                </tr>
            </table>
            <table width="300" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
                <tr>
                    
                    <td height="35">
                        <li>
                            <jsp:useBean id="Msg" scope="page" class="com.redmoon.forum.message.MessageMgr"/>
                                <%
boolean isSuccess = false;
try {
	isSuccess = Msg.AddMsg(request);
}
catch (ErrMsgException e) {
	out.println(StrUtil.makeErrMsg("消息发送失败："+e.getMessage()));
}
%>
                                <% if (isSuccess) { %>
                            消息回复成功！
                                <% } %>
                    </td>
                </tr>
                <tr>
                    <td height="35">
                        <div align="center"></div>
                    </td>
                </tr>
                <tr>
                    <td height="35">
                        <div align="center"></div>
                    </td>
                </tr>
                <tr>
                    <td height="35">
                        <div align="center"></div>
                    </td>
                </tr>
            </table>
            <table width="300" border="0" cellspacing="0" cellpadding="0" align="center" class="9black" height="6">
                <tr>
                    <td></td>
                </tr>
            </table>
    </tr>
    <tr>
        <td bgcolor="#CEE7FF" height="6"></td>
    </tr>
</table>
</body>
</html>
