<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%
String userNameTop = ParamUtil.get(request, "userName");
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="sales_desktop.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>"><span>销售工作</span></a></li>
    <li id="menu2"><a href="sales_user_chance_list.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>"><span>商机</span></a></li>
    <li id="menu3"><a href="sales_user_action_list.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>"><span>行动</span></a></li>
    <li id="menu4"><a href="sales_user_order_list.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>"><span>订单</span></a></li>
    <li id="menu5"><a href="sales_user_huikuan_list.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>"><span>回款</span></a></li>
    <li id="menu6"><a href="sales_user_pay_plan_list.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>"><span>应收帐款</span></a></li>
    <li id="menu7"><a href="sales_user_invoice_list.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>"><span>发票</span></a></li>
    <li id="menu8"><a href="sales_user_service_list.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>"><span>服务</span></a></li>
  </ul>
</div>
