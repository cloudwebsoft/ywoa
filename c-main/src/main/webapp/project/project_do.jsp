<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "com.cloudwebsoft.framework.base.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ page import = "com.redmoon.oa.project.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String action = ParamUtil.get(request,"action");
String priv="read";
if (privilege.isUserPrivValid(request, priv))
	;
else {
	// 防止因未分配计划权限，而ajax处理时在parseJSON的时候报异常，如workplan_task中评价时
	// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}

boolean re = false;

String op = ParamUtil.get(request, "op");
if (op.equals("favorite")) {
	JSONObject json = new JSONObject();
	ProjectFavoriteDb pfd = new ProjectFavoriteDb();
	try {
		long projectId = ParamUtil.getLong(request, "projectId");
		if (pfd.isExist(privilege.getUser(request), projectId)) {
			throw new ErrMsgException("该项目已被关注！");
		}
		re = pfd.create(new JdbcTemplate(), new Object[]{new Long(projectId),privilege.getUser(request),new java.util.Date(),new Integer(0)});
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage().replace("\\r", "<BR />"));
		out.print(json);
		// e.printStackTrace();
		return;
	}
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
	}
	else {
		json.put("ret", "0");
		json.put("msg", "操作失败！");
	}
	out.print(json);
	return;
}
else if (op.equals("unfavorite")) {
	JSONObject json = new JSONObject();
	ProjectFavoriteDb pfd = new ProjectFavoriteDb();
	try {
		long projectId = ParamUtil.getLong(request, "projectId");		
		pfd = pfd.getProjectFavoriteDb(privilege.getUser(request), new Long(projectId));
		if (pfd!=null)
			re = pfd.del();
		else
			throw new ErrMsgException("该记录已不存在！");
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", com.cloudwebsoft.framework.security.AntiXSS.clean(e.getMessage()).replace("\\r", "<BR />"));
		out.print(json);
		// e.printStackTrace();
		return;
	}
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
	}
	else {
		json.put("ret", "0");
		json.put("msg", "操作失败！");
	}	
	out.print(json);
	return;
}
%>