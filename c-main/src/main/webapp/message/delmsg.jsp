<%@ page contentType="text/html;charset=utf-8" %>
<%@ include file="../inc/nocache.jsp" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="cn.js.fan.util.ErrMsgException" %>
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

<jsp:useBean id="Msg" scope="page" class="com.redmoon.forum.message.MessageMgr"/>
<%
    if (request.getMethod().equalsIgnoreCase("POST")) {
        out.print(StrUtil.Alert_Back("请使用POST方式！"));
        return;
    }
    
    boolean isSuccess = false;
    try {
        isSuccess = Msg.delMsg(request);
    } catch (ErrMsgException e) {
        out.print(StrUtil.makeErrMsg("消息删除失败：" + e.getMessage()));
        return;
    }
    if (isSuccess) {
        out.print(StrUtil.Alert_Redirect("消息删除成功！", "message.jsp"));
    } else {
        out.print(StrUtil.Alert_Back("消息删除失败！"));
    }
%>
