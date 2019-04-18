<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%
Privilege pvgTop = new Privilege();
%>
<div id="tabs1">
  <ul>
	<%
		String id1 = ParamUtil.get(request,"id");
		String title1 = ParamUtil.get(request,"title");
		try {
			com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvgTop, "id1", id1, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvgTop, "title1", title1, getClass().getName());
		}
		catch (ErrMsgException e) {
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
			return;
		}
	 %>
    <li id="menu1"><a href="doc_log.jsp?id=<%=id1 %>&title=<%=title1 %>&type=chakan"><span>查看日志</span></a></li>
    <li id="menu2"><a href="doc_log.jsp?id=<%=id1 %>&title=<%=title1 %>&type=xiazai"><span>下载日志</span></a></li>
    <li id="menu3"><a href="doc_sign.jsp?id=<%=id1 %>&title=<%=title1 %>&type=xiazai"><span>已读</span></a></li>
    <li id="menu4"><a href="doc_sign_not.jsp?id=<%=id1 %>&title=<%=title1 %>&type=xiazai"><span>未读</span></a></li>
  </ul>
</div>