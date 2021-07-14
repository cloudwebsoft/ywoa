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
<%@ page import="com.redmoon.oa.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
if(op.equals("add")){
	try{
		RegionMgr rm = new RegionMgr();
		boolean re  = rm.create(request);
		int parentId = ParamUtil.getInt(request, "parent_id");
		if(re){
			out.print(StrUtil.Alert_Redirect("添加成功！", "region_list.jsp?parentId=" + parentId));
		}
	}catch(ErrMsgException e){
		 out.print(StrUtil.Alert_Back(e.getMessage()));
	}
}else if(op.equals("edit")){
  try{
		RegionMgr rm = new RegionMgr();
		boolean re  = rm.save(request);
		if(re){
			out.print(StrUtil.Alert_Back("修改成功！"));
			// out.print(StrUtil.Alert_Redirect("修改成功！", "region_list.jsp"));
		}
	}catch(ErrMsgException e){
		 out.print(StrUtil.Alert_Back(e.getMessage()));
	}
}
%>