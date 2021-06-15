<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<script src="../inc/nav.js"></script>
<%
String action_inc = cn.js.fan.util.ParamUtil.get(request, "action");
String userNameTop = ParamUtil.get(request, "userName");
String custom_inc_action = "";
if (action_inc.equals("")) {
	custom_inc_action = "我的";
}

%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="<%=request.getContextPath()%>/sales/customer_list.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>&action=<%=cn.js.fan.util.StrUtil.UrlEncode(action_inc)%>"><span><%=custom_inc_action%>客户</span></a></li>
    <li id="menu2"><a href="<%=request.getContextPath()%>/sales/customer_add.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>&formCode=sales_customer&action=<%=cn.js.fan.util.StrUtil.UrlEncode(action_inc)%>"><span>添加客户</span></a></li>
    <li id="menu3"><a href="<%=request.getContextPath()%>/sales/customer_query.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>&formCode=sales_customer&amp;action=<%=cn.js.fan.util.StrUtil.UrlEncode(action_inc)%>"><span>高级查询</span></a></li>
    <%if (!action_inc.equals("manage")) {%>
    <li id="menu4"><a href="<%=request.getContextPath()%>/sales/customer_myshare_list.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>&action=<%=cn.js.fan.util.StrUtil.UrlEncode(action_inc)%>" class="black"><span>共享客户</span></a></li>
    <li id="menu9"><a href="<%=request.getContextPath()%>/sales/customer_neglect_list.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>&action=<%=cn.js.fan.util.StrUtil.UrlEncode(action_inc)%>" class="black"><span>回落客户</span></a></li>
	<li id="menu5"><a href="<%=request.getContextPath()%>/sales/customer_my_find_list.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>&" class="black"><span>发现的客户</span></a></li>
	<li id="menu6"><a href="<%=request.getContextPath()%>/sales/customer_my_distributed_list.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>&" class="black"><span>分配的客户</span></a></li>
	<li id="menu7"><a href="<%=request.getContextPath()%>/crm/customer_stat.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>&" class="black"><span>月统计</span></a></li>
	<li id="menu8"><a href="<%=request.getContextPath()%>/crm/customer_stat_year.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>&" class="black"><span>年统计</span></a></li>
    <%}%>
	<li id="menu10"><a href="<%=request.getContextPath()%>/sales/customer_visit_import.jsp?action=<%=cn.js.fan.util.StrUtil.UrlEncode(action_inc)%>" class="black"><span>导入</span></a></li>
  </ul>
</div>