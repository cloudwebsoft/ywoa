<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String formCode = ParamUtil.get(request, "formCode");

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	out.print("表单不存在！");
	return;
}

ModulePrivDb mpd = new ModulePrivDb(formCode);
if (!mpd.canUserSee(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%
/**
* 用于显示嵌套表格于父表单中，由NestTableCtl.converToHTML通过url连接调用，注意需在用到此文件的页面中，置request属性cwsId、pageType
* 当用于流程中时，根据可写表单域，如果指定的嵌套表单字段不可写，则全部嵌套表单的域都不可写
**/
String op = ParamUtil.get(request, "op");
// out.print("op=" + op);
if (op.equals("add")) {
	com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, fd);
	out.print(rd.rendForAdd());
}
else if (op.equals("view")) {
	int cwsId = ParamUtil.getInt(request, "cwsId", -1);
	com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(fd);
	long id = fdao.getIDByCwsId(cwsId);
	com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
	out.print(rd.report(true));
}
else if (op.equals("edit")) {
	int cwsId = ParamUtil.getInt(request, "cwsId", -1);
	com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(fd);
	long id = fdao.getIDByCwsId(cwsId);
	com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
	out.print(rd.rend());	
}
else if (op.equals("flow")) {
	int flowId = ParamUtil.getInt(request, "cwsId", -1);
	WorkflowMgr wfm = new WorkflowMgr();
	WorkflowDb wf = wfm.getWorkflowDb(flowId);
	
	int doc_id = wf.getDocId();
	DocumentMgr dm = new DocumentMgr();
	Document doc = dm.getDocument(doc_id);

	int workflowActionId = ParamUtil.getInt(request, "workflowActionId", -1);
	com.redmoon.oa.flow.Render rd = new com.redmoon.oa.flow.Render(request, wf, doc);
	WorkflowActionDb wad = new WorkflowActionDb();
	wad = wad.getWorkflowActionDb(workflowActionId);
	
	out.print(rd.rendForNestCtl(request, formCode, wad));
	
	//-----------------------------
	/*
	com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(fd);
	long id = fdao.getIDByCwsId(flowId);
	com.redmoon.oa.visual.Render render = new com.redmoon.oa.visual.Render(request, id, fd);
	out.print(render.rend(com.redmoon.oa.flow.Render.FORM_FLEMENT_ID));
	*/
	
}

%>