<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="com.redmoon.oa.flow.FormDb"%>
<%@page import="com.redmoon.oa.visual.FormDAO"%>
<%@page import="com.redmoon.oa.basic.SelectOptionDb"%>

<%

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>销售商机历史记录</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script>

</script>
</head>
<body>

<%	
	int hid = ParamUtil.getInt(request,"hid",-1);
String sql = "select * from form_table_sales_chance_bak where id="+hid;
JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = jt.executeQuery(sql);
ResultRecord rd = null;
if(ri.hasNext()){
	rd = (ResultRecord)ri.next();
%>
<table  class="tabStyle_1 percent98" style="width:80%" cellspacing="0" cellpadding="2">
<thead>

<tr>
</br>
<td align="center" colspan="4">销售商机</td></tr></thead>
<tbody>
<tr>
<td align="right" style="width:200px">编码：</td>
<td colspan="3"><%=rd.getString("code") %></td>
</tr>
<tr>
<td style="width:200px" align="right">商机名称：</td>
<td style="width:300px"><%=rd.getString("chanceName") %></td>
<td align="right" style="width:200px">预计销售金额：</td>
<td style="width:300px"><%=rd.getString("expectPrice") %></td></tr>
<tr>
<td align="right">商机客户：</td>
<td>
<%
	FormDb fdb = new FormDb();
	fdb = fdb.getFormDb("sales_customer");
	FormDAO fdo = new FormDAO();
	fdo = fdo.getFormDAO(rd.getLong("customer"),fdb);
%>
<%=fdo.getFieldValue("customer") %>
</td>
<td align="right">发现时间：</td>
<td><%=rd.getString("find_date") %></td></tr>
<tr>
<td align="right">商机提供人：</td>
<td><%=rd.getString("provider") %></td>
<td align="right">预计成交时间：</td>
<td><%=rd.getString("pre_date") %></td></tr>
<tr>
<td align="right">商机阶段：</td>
<td>
<%
	SelectOptionDb sod = new SelectOptionDb();
	System.out.println(rd.getString("sjzt"));
%>
<%=sod.getOptionName("sales_chance_state",rd.getString("state")) %>
</td>
<td align="right">商机状态：</td>
<td><%=sod.getOptionName("sales_chance_status",rd.getString("sjzt")) %></td></tr>
<tr>
<td align="right">商机来源：</td>
<td><%=rd.getString("sjly") %></td>
<td align="right">成交可能性：</td>
<td><%=rd.getString("possibility")+"%" %></td></tr>
<tr>
<td align="right" valign="middle" width="20%">描述：</td>
<td colspan="3"><textarea name="description" title="描述" rows="5" cols="80" disabled><%=rd.getString("description") %></textarea></td>
</tr></tbody></table>
<%} %>
</body>

</html>
