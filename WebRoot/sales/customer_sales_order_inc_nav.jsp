<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<script src="../inc/nav.js"></script>
<%
long idTop = ParamUtil.getLong(request, "customerId", -1);
if (idTop==-1)
	idTop = ParamUtil.getLong(request, "id");

long id = ParamUtil.getLong(request, "id", -1);
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="<%=request.getContextPath()%>/sales/customer_sales_order_show.jsp?customerId=<%=idTop%>&id=<%=id%>&formCode=sales_customer"><span>订单</span></a></li>
  </ul>
</div>