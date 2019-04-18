<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
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

String userName = privilege.getUserName(skey);
privilege.doLogin(request, skey);

String op = ParamUtil.get(request, "op");

String formCode = ParamUtil.get(request, "formCode");
// formCode = "contract";
if (formCode.equals("")) {
	jsonRet.put("res", "-1");
	jsonRet.put("msg", "编码不能为空！");
	out.print(jsonRet.toString());	
	return;
}

String formCodeRelated = ParamUtil.get(request, "formCodeRelated");
String menuItem = ParamUtil.get(request, "menuItem");

// 用于查询选择宏控件
request.setAttribute("formCode", formCodeRelated);

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCodeRelated);
if (fd==null || !fd.isLoaded()) {
	jsonRet.put("res", "-1");
	jsonRet.put("msg", "表单不存在！");
	out.print(jsonRet.toString());		
	return;
}

FormDb flowFd = new FormDb();
flowFd = flowFd.getFormDb(formCode);
long actionId = ParamUtil.getLong(request, "actionId", -1);

String relateFieldValue = "" + com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID;
int parentId = ParamUtil.getInt(request, "parentId", -1); // 父模块的ID
if (parentId==-1) {
	// out.print(SkinUtil.makeErrMsg(request, "缺少父模块记录的ID！"));
	// return;
	
	if (actionId!=-1 && actionId!=0) {
		WorkflowActionDb wa = new WorkflowActionDb();
		wa = wa.getWorkflowActionDb((int)actionId);
        com.redmoon.oa.flow.FormDAO fdaoFlow = new com.redmoon.oa.flow.FormDAO();
        fdaoFlow = fdaoFlow.getFormDAO(wa.getFlowId(), flowFd);
		parentId = (int)fdaoFlow.getId();		
	}
	
	ModuleRelateDb mrd = new ModuleRelateDb();
	mrd = mrd.getModuleRelateDb(formCode, formCodeRelated);
	if (mrd==null) {
		jsonRet.put("res", "-1");
		jsonRet.put("msg", "请检查模块是否相关联！");
		out.print(jsonRet.toString());				
		return;
	}
}
else {
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);
	relateFieldValue = fdm.getRelateFieldValue(parentId, formCodeRelated);
	if (relateFieldValue==null) {
		jsonRet.put("res", "-1");
		jsonRet.put("msg", "请检查模块是否相关联！");
		out.print(jsonRet.toString());			
		return;
	}
}

ModuleSetupDb msd = new ModuleSetupDb();
// System.out.println(getClass() + " formCodeRelated=" + formCodeRelated);

String moduleCode = ParamUtil.get(request, "moduleCode");
if (moduleCode.equals(""))
	moduleCode = formCodeRelated;
msd = msd.getModuleSetupDbOrInit(moduleCode);
if (msd==null) {
	jsonRet.put("res", "-1");
	jsonRet.put("msg", "模块不存在！");
	out.print(jsonRet.toString());			
	return;
}

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
boolean isNestSheetCheckPrivilege = cfg.getBooleanProperty("isNestSheetCheckPrivilege");
ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
if (isNestSheetCheckPrivilege && !mpd.canUserAppend(userName)) {
	jsonRet.put("res", "-1");
	jsonRet.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
	out.print(jsonRet.toString());	
	return;
}


// 用于com.redmoon.oa.visual.Render
request.setAttribute("pageKind", "nest_sheet_relate");
request.setAttribute("actionId", String.valueOf(actionId));

if (op.equals("saveformvalue")) {
	re = false;
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	try {
		if (formCode.equals("project") && formCodeRelated.equals("project_members")) {
			re = fdm.createPrjMember(application, request);
		} else {	
			re = fdm.create(application, request, msd);
		}
	}
	catch (ErrMsgException e) {
		e.printStackTrace();
		jsonRet.put("res", "-1");
		jsonRet.put("msg", e.getMessage());
		out.print(jsonRet.toString());		
		return;	
	}
	if (re) {
		jsonRet.put("res", "0");
		jsonRet.put("msg", "操作成功！");
		
		String cwsId = String.valueOf(parentId);				
		jsonRet.put("sums", FormUtil.getSums(fd, flowFd, cwsId));			
		
		out.print(jsonRet.toString());
	}
	else {
		jsonRet.put("res", "-1");
		jsonRet.put("msg", "操作失败！");
		out.print(jsonRet.toString());			
	}
	return;
}
%>