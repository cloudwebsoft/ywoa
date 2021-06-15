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

System.out.println(getClass() + " op=" + op);
if (op.equals("list")) {		
	StringBuffer sb = new StringBuffer();
	sb.append("<ul>");
	
	int providerId = ParamUtil.getInt(request, "providerId", -1);
	
	if (providerId==-1) {
		FormDAO fdao = new FormDAO();
		String formCode = "sales_provider_info";
		String sql = "select id from form_table_sales_provider_info order by id desc";
		Iterator ir = fdao.list(formCode, sql).iterator();
		while (ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			sb.append("<li id='li_" + fdao.getId() + "'><span id='" + fdao.getId() + "'>" + fdao.getFieldValue("provide_name") + "</span>");
			sb.append("<ul class='ajax'><li>{url:sales_purchase_product_add_ajax.jsp?op=list&providerId=" + fdao.getId() + "}</li>");
			sb.append("</ul>");
			sb.append("</li>");
		}
	}
	else {
		// 取得产品
		FormDb fd = new FormDb();
		fd = fd.getFormDb("sales_product_info");
		FormDAO fdaoPro = new FormDAO();
	
		String sql = "select id from form_table_sales_provider_pro where cws_id=" + providerId + " order by id asc";
		FormDAO fdao = new FormDAO();
		Iterator irp = fdao.list("sales_provider_pro", sql).iterator();
		while (irp.hasNext()) {
			fdao = (FormDAO)irp.next();
			fdaoPro = fdaoPro.getFormDAO(StrUtil.toLong(fdao.getFieldValue("product")), fd);
			sb.append("<li id='pro_" + fdaoPro.getId() + "'><span productId='" + fdaoPro.getId() + "' id='" + fdaoPro.getId() + "'>" + fdaoPro.getFieldValue("product_name") + "</span></li>");
		}
	}
	sb.append("</ul>");
	out.print(sb);
}
%>