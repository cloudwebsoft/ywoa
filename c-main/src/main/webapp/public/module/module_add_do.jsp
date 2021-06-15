<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="cn.js.fan.util.*"%>
<%@page import="cn.js.fan.web.Global"%>
<%@page import="com.redmoon.oa.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.net.*"%>
<%@page import="cn.js.fan.web.*" %>
<%@page import="cn.js.fan.security.ThreeDesUtil"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<%@page import="org.json.JSONObject"%>
<%@page import="com.redmoon.oa.visual.*"%>
<%@page import="com.redmoon.oa.flow.FormMgr"%>
<%@page import="com.redmoon.oa.flow.FormDb"%>
<%@page import="com.redmoon.oa.flow.FormField"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="fsecurity" scope="page" class="cn.js.fan.security.SecurityUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String skey = ParamUtil.get(request,"skey");
com.redmoon.oa.android.Privilege pri = new com.redmoon.oa.android.Privilege();
String userName = pri.getUserName(skey);
JSONObject json = new JSONObject();
if(userName.equals("")){
	json.put("res", "-1");
	json.put("msg", "skey不存在！");
	out.print(json.toString());
	return;
}
pri.doLogin(request,skey);

String moduleCode = ParamUtil.get(request, "moduleCode");
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDb(moduleCode);
String formCode = msd.getString("form_code");
try {
	FormMgr fm = new FormMgr();
	FormDb fdAdd = fm.getFormDb(formCode);
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fdAdd);
	boolean re = fdm.create(application,request);
	if(re){
		json.put("res","0");
		json.put("msg","操作成功");
	}else{
		json.put("res","-1");
		json.put("msg","操作失败");
	}
}catch (ErrMsgException e) {
	json.put("res","-1");
	json.put("msg",e.getMessage());
}finally{
	out.print(json.toString());
}
%>
