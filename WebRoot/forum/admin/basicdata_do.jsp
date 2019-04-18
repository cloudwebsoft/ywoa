<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "java.util.Enumeration"%>
<%@ page import = "java.util.Iterator"%>
<%@ page import = "org.jdom.*"%>
<%@ page import = "com.redmoon.forum.util.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>基础数据维护</title>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
	if (!privilege.isMasterLogin(request)) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	String itemCode = ParamUtil.get(request, "itemCode");
	String optionValue = "";
	String op = ParamUtil.get(request, "op");
	
	BasicDataMgr bdm = new BasicDataMgr();
	boolean re = false;
	
	if (op.equals("addOption")) {	
	    optionValue = ParamUtil.get(request, "optionValue");
		re = bdm.addOption(optionValue,itemCode);		
		if (re) {
		    out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"info_op_success"), "basicdata_list.jsp"));
		}else{
		    out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request,"info_op_fail")));
		}
	} else if (op.equals("delOption")) {
	    optionValue = ParamUtil.get(request, itemCode);	
		re = bdm.delOption(optionValue,itemCode);
		if (re) {
		    out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"info_op_success"), "basicdata_list.jsp"));
		}else{
		    out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request,"info_op_fail")));
		}
	} else if (op.equals("setDefaultValue")) {	
	    optionValue = ParamUtil.get(request, itemCode);
		re = bdm.setDefaultValue(optionValue,itemCode);
		if (re) {
		    out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"info_op_success"), "basicdata_list.jsp"));
		}else{
		    out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_success")));
		}
	}
%>
</body>
</html>
