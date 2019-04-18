<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
long workplanId = ParamUtil.getLong(request, "workplanId"); 
String reason = ParamUtil.get(request, "reason");
String data = ParamUtil.get(request, "data");
try {
   
    
    com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
    com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "workplanId", String.valueOf(workplanId), getClass().getName());
    com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "reason", reason, getClass().getName());
    com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "data", data, getClass().getName());
}
catch (ErrMsgException e) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
    return;
}


if (op.equals("gantt")) {
	WorkPlanTaskMgr wptm = new WorkPlanTaskMgr();
	
	boolean re = false;
	JSONObject jsonRet = new JSONObject();
	try {
		re = wptm.modify(request, workplanId);
	}
	catch (ErrMsgException e) {
		jsonRet.put("ret", "0");
		jsonRet.put("msg", e.getMessage());
		out.print(jsonRet);		
		return;
	}
	if (re) {
		jsonRet.put("ret", "1");
		jsonRet.put("msg", "操作成功！");
	}
	else {
		jsonRet.put("ret", "0");
		jsonRet.put("msg", "操作失败！");
	}
	out.clear();
	out.print(jsonRet);		
	return;
}
%>