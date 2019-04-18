<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*" %>
<%
String action_inc = cn.js.fan.util.ParamUtil.get(request, "action");
String userNameTop = ParamUtil.get(request, "userName");
String isShowVisitTag = StrUtil.getNullStr((String)request.getAttribute("isShowVisitTag"));
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="linkman_list.jsp?userName=<%=cn.js.fan.util.StrUtil.UrlEncode(userNameTop)%>&action=<%=cn.js.fan.util.StrUtil.UrlEncode(action_inc)%>"><span>联系人管理</span></a></li>
    <li id="menu2"><a href="linkman_add.jsp?userName=<%=cn.js.fan.util.StrUtil.UrlEncode(userNameTop)%>&formCode=sales_linkman&action=<%=cn.js.fan.util.StrUtil.UrlEncode(action_inc)%>"><span>添加联系人</span></a></li>
    <li id="menu3"><a href="linkman_query.jsp?userName=<%=cn.js.fan.util.StrUtil.UrlEncode(userNameTop)%>&formCode=sales_linkman&action=<%=cn.js.fan.util.StrUtil.UrlEncode(action_inc)%>"><span>高级查询</span></a></li>
    <%if (isShowVisitTag.equals("true")) {
		long idTop = ParamUtil.getLong(request, "linkmanId", -1);
		if (idTop==-1) {
		%>
            <li id="menu4"><a href="#" class="black"><span>行动</span></a></li>
		<%
		}
		else {
		%>
            <li id="menu4"><a href="linkman_visit_list.jsp?linkmanId=<%=idTop%>&action=<%=cn.js.fan.util.StrUtil.UrlEncode(action_inc)%>" class="black"><span>行动</span></a></li>
		<%
		}
	}%>	
  </ul>
</div>