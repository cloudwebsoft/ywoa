<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.ui.menu.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

JSONObject json = new JSONObject();

WallpaperMgr wm = new WallpaperMgr();
boolean re = false;
try {
	re = wm.create(application, request);
}
catch (ErrMsgException e) {
	json.put("ret", "0");
	json.put("msg", e.getMessage());
	out.print(json);
	return;
}
if (re) {
	json.put("ret", "1");
	json.put("msg", "操作成功！");
	out.print(json);		
	return;
}
else {
	json.put("ret", "0");
	json.put("msg", "操作失败！");
	out.print(json);
	return;
}
%>