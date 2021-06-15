<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@page import="java.sql.SQLException"%>
<%@page import="com.redmoon.oa.sms.SMSTemplateMgr"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");

if(op.equals("add")){
	try{
		SMSTemplateMgr stMgr = new SMSTemplateMgr();
		boolean re  = stMgr.create(request);
		if(re){
			out.print(StrUtil.Alert_Redirect("添加成功！", "sms_template_list.jsp"));
		}else
		{
		    out.print(StrUtil.Alert_Redirect("添加失败，请核查操作是否正常！", "wy_sms_add.jsp"));
		}
	}catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
}else if(op.equals("edit")){
  try{
		SMSTemplateMgr stMgr = new SMSTemplateMgr();
		boolean re  = stMgr.save(request);
		if(re){
			out.print(StrUtil.Alert_Redirect("修改成功！", "sms_template_list.jsp"));
		}
	}catch (ErrMsgException e) {  
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}

}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>短信模板管理</title>

</head>
<body>
</body>
</html>