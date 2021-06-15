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
<%@ page import="java.sql.SQLException"%>
<%@ page import="com.redmoon.oa.android.SystemUpMgr"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<%
String op = ParamUtil.get(request, "op");
if(op.equals("add")){
	try{
		SystemUpMgr wm = new SystemUpMgr();
		boolean re  = wm.create(request);
		if(re){
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "mobile_version_list.jsp"));
		}else{
		    out.print(StrUtil.jAlert_Back("操作失败！","提示"));
		}
	}catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
}else if(op.equals("edit")){
  try{
		SystemUpMgr wm = new SystemUpMgr();
		boolean re  = wm.save(request);
		if(re){
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "mobile_version_list.jsp"));
		}else
		{
		  out.print(StrUtil.jAlert_Redirect("操作失败！","提示", "mobile_version_list.jsp"));
		}
	}catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
}
%>