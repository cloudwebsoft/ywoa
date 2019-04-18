<%@ page contentType="text/html;charset=utf-8"
import = "cn.js.fan.util.ErrMsgException"
%>
<%@ page import="java.util.Calendar" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege" />
<%
privilege.logout(request, response);
//out.print(StrUtil.p_center("您已安全退出"));
response.sendRedirect("forum/index.jsp");
%>
