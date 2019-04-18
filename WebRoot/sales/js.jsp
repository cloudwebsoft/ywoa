<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%
String rootPath = request.getContextPath();
%>
function openWinSalesStockProductAdd(pageType) {
	openWin("<%=rootPath%>/sales/sales_stock_product_add.jsp?pageType=" + pageType, 800, 600);
}

function openWinSalesPurchaseProductAdd(pageType) {
	openWin("<%=rootPath%>/sales/sales_purchase_product_add.jsp?pageType=" + pageType, 800, 600);
}
