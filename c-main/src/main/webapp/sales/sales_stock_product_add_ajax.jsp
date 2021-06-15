<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("dir")) {
	String dirCode = ParamUtil.get(request, "dirCode");
	if (dirCode.equals(""))
		return;
		
	TreeSelectChildrenCache tscc = new TreeSelectChildrenCache(dirCode);
	Iterator ir = tscc.getList().iterator();
	StringBuffer sb = new StringBuffer();
	sb.append("<ul>");
	while (ir.hasNext()) {
		TreeSelectDb tsd = (TreeSelectDb)ir.next();
		sb.append("<li id='li_" + tsd.getCode() + "'><span id='" + tsd.getCode() + "'>" + tsd.getName() + "</span>");
		sb.append("<ul class='ajax'><li>{url:sales_stock_product_add_ajax.jsp?op=dir&dirCode=" + StrUtil.UrlEncode(tsd.getCode()) + "}</li>");
		sb.append("</ul>");
		sb.append("</li>");
		
	}
	// 取得产品
	String sql = "select id from form_table_sales_product_info where product_mode=" + StrUtil.sqlstr(dirCode) + " order by id asc";
	FormDAO fdao = new FormDAO();
	Iterator irp = fdao.list("sales_product_info", sql).iterator();
	while (irp.hasNext()) {
		fdao = (FormDAO)irp.next();
		sb.append("<li id='pro_" + fdao.getId() + "'><span productId='" + fdao.getId() + "' id='" + fdao.getId() + "'>" + fdao.getFieldValue("product_name") + "</span></li>");
	}
	sb.append("</ul>");
	out.print(sb);
}
else if (op.equals("prop")) {
	long productId = ParamUtil.getLong(request, "productId");
	FormDb fd = new FormDb();
	fd = fd.getFormDb("sales_product_info");
	FormDAO fdao = new FormDAO();
	fdao = fdao.getFormDAO(productId, fd);
	
	JSONObject json = new JSONObject();
	json.put("ret", "1");
	json.put("productId", "" + productId);
	json.put("measure_unit", fdao.getFieldValue("measure_unit"));
	json.put("productName", fdao.getFieldValue("product_name"));
	out.print(json);
	// System.out.println(getClass() + " " + json);
}
%>