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
<%@ page import="com.redmoon.oa.android.system.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String op = ParamUtil.get(request, "op");
	if (op.equals("add")) {
		try {
			MobileAppIconConfigMgr mr = new MobileAppIconConfigMgr();
			if (!mr.isExist(request)) {
				out.print(StrUtil.Alert_Back("已经存在,不能重复添加！"));
				return;
			}

			boolean re = mr.create(request);
			if (re) {
				out.print(StrUtil.Alert_Redirect("操作成功！",
						"mobile_applist_config_list.jsp"));
			} else {
				out.print(StrUtil.Alert_Back("操作失败！"));
			}
		} catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}
	} else if (op.equals("edit")) {
		try {
			String id = ParamUtil.get(request, "id");	
			
			MobileAppIconConfigMgr mr = new MobileAppIconConfigMgr();
			
			boolean re = mr.save(request);
			if (re) {
				out.print(StrUtil.Alert_Redirect("操作成功！","mobile_applist_config_edit.jsp?id=" + id));
			} else {
				out.print(StrUtil.Alert_Back("操作失败！"));
			}
		} catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}
	}
%>