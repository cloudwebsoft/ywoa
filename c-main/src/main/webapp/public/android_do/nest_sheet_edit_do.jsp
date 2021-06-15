<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.android.Privilege"/>
<%
String skey = ParamUtil.get(request, "skey");
JSONObject jsonRet = new JSONObject();
boolean re = privilege.Auth(skey);
if (re) {
	try {
		jsonRet.put("res", "-2");
		jsonRet.put("msg", "时间过期");
		out.print(jsonRet.toString());
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return;
}

privilege.doLogin(request, skey);

String myname = privilege.getUserName( skey );
String op = ParamUtil.get(request, "op");

String formCode = ParamUtil.get(request, "formCode"); // 主模块编码
if (formCode.equals("")) {
	jsonRet.put("res", "-1");
	jsonRet.put("msg", "编码不能为空！");
	out.print(jsonRet.toString());		
	return;
}

String formCodeRelated = ParamUtil.get(request, "formCodeRelated"); // 从模块编码
String menuItem = ParamUtil.get(request, "menuItem");

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
boolean isNestSheetCheckPrivilege = cfg.getBooleanProperty("isNestSheetCheckPrivilege");

ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
if (isNestSheetCheckPrivilege && !mpd.canUserManage(myname)) {
	jsonRet.put("res", "-1");
	jsonRet.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
	out.print(jsonRet.toString());	
	return;
}

int id = ParamUtil.getInt(request, "id");
// 置嵌套表需要用到的cwsId
request.setAttribute("cwsId", "" + id);
// 置页面类型
request.setAttribute("pageType", "edit");

// 这里是为了使嵌套表格2表单中又存在嵌套表格2宏控件时，在getNestSheet方法中，传递给当前编辑的表单中的嵌套表格2宏控件
// 同时也用于查询选择宏控件
request.setAttribute("formCode", formCodeRelated);

long actionId = ParamUtil.getLong(request, "actionId", -1);
// 用于com.redmoon.oa.visual.Render
request.setAttribute("pageKind", "nest_sheet_relate");
request.setAttribute("actionId", String.valueOf(actionId));

int parentId = ParamUtil.getInt(request, "parentId", -1); // 父模块的ID，仅用于导航，如果导航不显示，则不用传递该参数，用例：module_show_realte.jsp编辑按钮

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCodeRelated);

com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);

String moduleCode = ParamUtil.get(request, "moduleCode");
// System.out.println(getClass() + " moduleCode=" + moduleCode);
if (moduleCode.equals(""))
	moduleCode = formCodeRelated;
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(moduleCode);

if (op.equals("saveformvalue")) {
	re = false;
	try {
		re = fdm.update(application, request, msd);
	}
	catch (ErrMsgException e) {
		jsonRet.put("res", "-1");
		jsonRet.put("msg", e.getMessage());
		out.print(jsonRet.toString());	
		return;
	}
	if (re) {
		jsonRet.put("res", "0");
		jsonRet.put("msg", "操作成功！");
		
		String cwsId = String.valueOf(parentId);	
		FormDb pForm = new FormDb();
		pForm = pForm.getFormDb(formCode);
		jsonRet.put("sums", FormUtil.getSums(fd, pForm, cwsId));			
		
		out.print(jsonRet.toString());
	}
	else {
		jsonRet.put("res", "-1");
		jsonRet.put("msg", "操作失败！");
		out.print(jsonRet.toString());			
	}
	return;
}%>