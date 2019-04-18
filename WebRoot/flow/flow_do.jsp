<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.shell.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@page import="com.redmoon.oa.person.UserSetupDb"%>
<%@page import="com.redmoon.oa.ui.LocalUtil"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

boolean re = false;
String op = ParamUtil.get(request, "op");

if (op.equals("favorite")) {
	JSONObject json = new JSONObject();
	WorkflowFavoriteDb wfd = new WorkflowFavoriteDb();
	try {
		long flowId = ParamUtil.getLong(request, "flowId");
		if (wfd.isExist(privilege.getUser(request), flowId)) {
			String str = LocalUtil.LoadString(request,"res.flow.Flow","processBeConcerned");
			throw new ErrMsgException(str);
		}
		re = wfd.create(new JdbcTemplate(), new Object[]{new Long(flowId),privilege.getUser(request),new java.util.Date(),new Integer(0)});
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
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		json.put("msg", str);
	}
	else {
		json.put("ret", "0");
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		json.put("msg", str);
	}	
	out.print(json);
	return;
}
else if (op.equals("unfavorite")) {
	JSONObject json = new JSONObject();
	WorkflowFavoriteDb wfd = new WorkflowFavoriteDb();
	try {
		long flowId = ParamUtil.getLong(request, "flowId");		
		wfd = wfd.getWorkflowFavoriteDb(privilege.getUser(request), new Long(flowId));
		if (wfd!=null){
			re = wfd.del();
		}else{
			String str = LocalUtil.LoadString(request,"res.flow.Flow","notAlreadyExist");
			throw new ErrMsgException(str);
		}
			
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
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		json.put("msg", str);
	}
	else {
		json.put("ret", "0");
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		json.put("msg", str);
	}	
	out.print(json);
	return;
}
else if (op.equals("refreshFlow")) {
	int flowId = ParamUtil.getInt(request, "flowId", -1);
	
	WorkflowDb wf = new WorkflowDb();
	wf = wf.getWorkflowDb(flowId);	
	// Leaf lf = new Leaf();
	// Leaf lf = lf.getLeaf(wf.getTypeCode());
	wf.refreshFlow();
	
	JSONObject json = new JSONObject();	
	json.put("ret", "1");
	String str = LocalUtil.LoadString(request,"res.common","info_op_success");
	json.put("msg", str);
	out.print(json);
	return;
}
else if ("applyProps".equals(op)) {
	String fieldWrite = ParamUtil.get(request, "fieldWrite");
	String fieldHide = ParamUtil.get(request, "fieldHide");
	int flowId = ParamUtil.getInt(request, "flowId", -1);
	int actionId = ParamUtil.getInt(request, "actionId");
		
	WorkflowDb wf = new WorkflowDb();
	wf = wf.getWorkflowDb(flowId);	
		
	WorkflowPredefineDb wpd = new WorkflowPredefineDb();
    wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
    WorkflowActionDb wad = new WorkflowActionDb();
    wad = wad.getWorkflowActionDb(actionId);
    
    WorkflowDb wfDefault = new WorkflowDb();
    wfDefault.setFlowString(wpd.getFlowString());
    
    WorkflowActionDb waDefault = null;
    	
    java.util.Vector v = wfDefault.getActionsFromString(wfDefault.getFlowString());
    java.util.Iterator ir = v.iterator();
    while (ir.hasNext()) {
    	WorkflowActionDb wa = (WorkflowActionDb)ir.next();
    	if (wa.getInternalName().equals(wad.getInternalName())) {
    		wa.setFieldWrite(fieldWrite);
    		wa.setFieldHide(fieldHide);
    		
    		String item2 = wa.generateItem2();
    		wa.setItem2(item2);

	   		waDefault = wa;	   		
	   		break;
   		}
   	}

	if (waDefault!=null) {
		wfDefault.renewWorkflowString(waDefault, false);
		wpd.setFlowString(wfDefault.getFlowString());
		wpd.save();
	}
	
	// Leaf lf = new Leaf();
	// Leaf lf = lf.getLeaf(wf.getTypeCode());
	wf.refreshFlow();
	
	JSONObject json = new JSONObject();	
	json.put("ret", "1");
	String str = LocalUtil.LoadString(request, "info_op_success");
	json.put("msg", str);
	out.print(json);

	/*
	int doc_id = wf.getDocId();
	DocumentMgr dm = new DocumentMgr();
	Document doc = dm.getDocument(doc_id);
	Render rd = new Render(request, wf, doc);
	String content = rd.rend(wad);
	out.print(content);
	*/
}
else if ("runValidateScript".equals(op)) {
	int flowId = ParamUtil.getInt(request, "flowId", -1);
	int actionId = ParamUtil.getInt(request, "actionId");	
	JSONObject json = new JSONObject();		
	FormDAOMgr fdm = new FormDAOMgr();
	BSHShell shell = null;
	try {
		shell = fdm.runValidateScript(request, flowId, actionId);
				
		// out.print(logs);
		json.put("ret", "1");
		
		if (shell==null) {
			json.put("msg", "请检查脚本是否存在！");
		}
		else {
			String errDesc = shell.getConsole().getLogDesc();
			json.put("msg", StrUtil.toHtml(errDesc));
		}
	}
	catch (ErrMsgException e) {
		// json.put("ret", "1");
		// json.put("msg", e.getMessage());	
		
		// String errDesc = shell.getConsole().getLogDesc();
		
		// out.print(logs);
		json.put("ret", "0");
		shell = fdm.getBshShell();
		String errDesc = "";
		if (shell!=null) {
			errDesc = shell.getConsole().getLogDesc();
		}
		if (!"".equals(errDesc)) {
			errDesc += "\r\n" + e.getMessage();
		}
		else {
			errDesc = e.getMessage();
		}
		json.put("msg", StrUtil.toHtml(errDesc));			
	}
	out.print(json);			
}
else if ("runFinishScript".equals(op)) {
	int flowId = ParamUtil.getInt(request, "flowId", -1);
	int actionId = ParamUtil.getInt(request, "actionId");	
	JSONObject json = new JSONObject();		
	
	BSHShell shell = null;

	WorkflowDb wf = new WorkflowDb();
	wf = wf.getWorkflowDb(flowId);
	
	Leaf lf = new Leaf();
	lf = lf.getLeaf(wf.getTypeCode());
	FormDb fd = new FormDb();
	fd = fd.getFormDb(lf.getFormCode());
	
	FormDAO fdao = new FormDAO();
	fdao = fdao.getFormDAO(flowId, fd);
	
	WorkflowActionDb wa = new WorkflowActionDb();
	wa = wa.getWorkflowActionDb(actionId);
	
	WorkflowPredefineDb wpd = new WorkflowPredefineDb();
    wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
    WorkflowPredefineMgr wpm = new WorkflowPredefineMgr();
    String script = wpm.getOnFinishScript(wpd.getScripts());
   	       
   	if (script!=null && !"".equals(script.trim())) {
		shell = wf.runFinishScript(request, wf, fdao, wa, script, true);
	}
							
	// out.print(logs);
	json.put("ret", "1");
	
	if (shell==null) {
		json.put("msg", "请检查脚本是否存在！");
	}
	else {
		String errDesc = shell.getConsole().getLogDesc();
		json.put("msg", StrUtil.toHtml(errDesc));
	}

	out.print(json);			
}
else if ("runDeliverScript".equals(op)) {
	int flowId = ParamUtil.getInt(request, "flowId", -1);
	long myActionId = ParamUtil.getInt(request, "myActionId");	
	
	JSONObject json = new JSONObject();		
	
	BSHShell shell = null;

	WorkflowDb wf = new WorkflowDb();
	wf = wf.getWorkflowDb(flowId);
	
	Leaf lf = new Leaf();
	lf = lf.getLeaf(wf.getTypeCode());
	FormDb fd = new FormDb();
	fd = fd.getFormDb(lf.getFormCode());
	
	FormDAO fdao = new FormDAO();
	fdao = fdao.getFormDAO(flowId, fd);
	
	MyActionDb mad = new MyActionDb();
	mad = mad.getMyActionDb(myActionId);
	
	WorkflowActionDb wa = new WorkflowActionDb();
	wa = wa.getWorkflowActionDb((int)mad.getActionId());
	
	WorkflowPredefineDb wpd = new WorkflowPredefineDb();
    wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
    WorkflowPredefineMgr wpm = new WorkflowPredefineMgr();
    String script = wpm.getActionFinishScript(wpd.getScripts(), wa.getInternalName());
   	       
   	if (script!=null && !"".equals(script.trim())) {	
   		WorkflowMgr wm = new WorkflowMgr();	
		shell = wm.runDeliverScript(request, privilege.getUser(request), wf, fdao, mad, script, true);
	}

	// out.print(logs);
	json.put("ret", "1");
	
	if (shell==null) {
		json.put("msg", "请检查脚本是否存在！");
	}
	else {
		String errDesc = shell.getConsole().getLogDesc().trim();
		json.put("msg", StrUtil.toHtml(errDesc));
	}

	out.print(json);			
}
else if ("recover".equals(op)) {
	int flowId = ParamUtil.getInt(request, "flowId", -1);
	
	WorkflowDb wf = new WorkflowDb();
	wf = wf.getWorkflowDb(flowId);
	wf.setStatus(WorkflowDb.STATUS_STARTED);
	wf.save();
	
	JSONObject json = new JSONObject();	
	json.put("ret", "1");
	String str = LocalUtil.LoadString(request,"res.common","info_op_success");
	json.put("msg", str);
	out.print(json);
	return;
}
else if ("delAnnex".equals(op)) {
	long annexId = ParamUtil.getLong(request, "annexId");
	WorkflowAnnexDb wad = new WorkflowAnnexDb();
	wad = (WorkflowAnnexDb)wad.getQObjectDb(new Long(annexId));
	re = wad.del();

	JSONObject json = new JSONObject();	
	json.put("ret", "1");
	String str = LocalUtil.LoadString(request,"res.common","info_op_success");
	json.put("msg", str);
	out.print(json);
	return;
}
else if (op.equals("getTree")) {
	Leaf lf = new Leaf();
	lf = lf.getLeaf("root");
	DirectoryView dv = new DirectoryView(lf);
	dv.ShowDirectoryAsOptions(request, out, lf, 1);
	return;
}
%>