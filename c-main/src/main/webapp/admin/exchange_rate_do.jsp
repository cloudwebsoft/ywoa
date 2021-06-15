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
<%@page import="com.redmoon.oa.util.ExchangeRateMgr"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
if(op.equals("add")){
	try{
		ExchangeRateMgr wm = new ExchangeRateMgr();
		boolean re  = wm.create(request);
		if(re){
			out.print(StrUtil.Alert_Redirect("操作成功！", "exchange_rate_list.jsp"));
		}else{
		    out.print(StrUtil.Alert_Back("操作失败！"));
		}
	}catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
}else if(op.equals("edit")){
  try{
		ExchangeRateMgr wm = new ExchangeRateMgr();
		boolean re  = wm.save(request);
		if(re){
			out.print(StrUtil.Alert_Redirect("操作成功！", "exchange_rate_list.jsp"));
		}else
		{
		  out.print(StrUtil.Alert_Redirect("操作失败！", "exchange_rate_list.jsp"));
		}
	}catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
}
%>