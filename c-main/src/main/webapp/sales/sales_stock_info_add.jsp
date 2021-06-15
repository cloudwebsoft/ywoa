<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.sale.*"%>
<%
int type = ParamUtil.getInt(request, "type", 1);
String typeName = "入库";
if (type==0)
	typeName = "出库";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta name="renderer" content="ie-stand">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=typeName%></title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/map.js"></script>

<style type="text/css"> 
@import url("<%=request.getContextPath()%>/util/jscalendar/calendar-win2k-2.css"); 
</style>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar-setup.js"></script>

</head> 
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1"><%=typeName%></td>
    </tr>
  </tbody>
</table>
<%
String priv="sales.stock";
if (!privilege.isUserPrivValid(request, priv)) {
	// out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}

RequestAttributeElement rae = new RequestAttributeElement();
RequestAttributeMgr ram = new RequestAttributeMgr();
ram.addAttribute(request, rae.createHidden(RequestAttributeElement.NAME_FORWARD, "sales/sales_stock_info_list.jsp?op=search&opType=" + type));
%>
<%@ include file="../visual_add.jsp"%>
</body>
</html>
