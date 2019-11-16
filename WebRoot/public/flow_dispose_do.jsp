<%@ page contentType="text/html; charset=utf-8" %><%@ page import="com.redmoon.oa.fileark.*"%><%@ page import="cn.js.fan.util.*"%><%@ page import="cn.js.fan.web.*"%><%@ page import="com.redmoon.kit.util.*"%><%@ page import="com.redmoon.oa.flow.*"%><%@ page import = "org.json.*"%>
<%@ page import="java.util.*"%><%@ page import="com.redmoon.oa.person.*"%>
<%@page import="org.json.JSONObject"%><%@page import="org.json.JSONException"%>
<%@page import="com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.android.Constant"%>
<%@page import="com.redmoon.oa.db.SequenceManager"%>
<%
/*
- 功能描述：用于android客户端处理流程
- 访问规则：
- 过程描述：
- 注意事项：
- 创建者：wql
- 创建时间：
==================
- 修改者：fgf
- 修改时间：2013-2-28
- 修改原因：
- 修改点：getNextActionDoingWillBeCheckedByUserSelf增加参数userName
*/
session.setAttribute(Constant.OA_NAME,"admin");
session.setAttribute(Constant.OA_UNITCODE,"root");

com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
String myop = ParamUtil.get(request, "myop");
// 如果本节点是异或聚合，办理完毕，但不转交
if (myop.equals("setFinishAndNotDelive")) {
	long myActionId = ParamUtil.getLong(request, "myActionId");
	long actionId = ParamUtil.getLong(request, "actionId");
	WorkflowActionDb wa = new WorkflowActionDb();
	wa = wa.getWorkflowActionDb((int)actionId);
	wa.setStatus(wa.STATE_FINISHED);
	wa.save();
	MyActionDb mad = new MyActionDb();
	mad = mad.getMyActionDb(myActionId);
    mad.setCheckDate(new java.util.Date());
    mad.setChecked(true);
    mad.save();	
    WorkflowDb wfd = new WorkflowDb();
    wfd = wfd.getWorkflowDb(wa.getFlowId());
    // 检查流程中的节点是否都已完成
    if (wfd.checkStatusFinished()) {
    	wfd.changeStatus(request, wfd.STATUS_FINISHED, wa);
    }
	out.print(StrUtil.Alert_Redirect("操作成功！", "../flow/flow_list.jsp?displayMode=1"));
	return;
}
else if (myop.equals("writeDocument")) {
	// 拟文
	WorkflowMgr wfm = new WorkflowMgr();
	try {
		int ret = wfm.writeDocument(application, request);
		if (ret!=-1)
			out.print("" + ret);
		else {
			out.print("操作失败！");
		}
	}
	catch (ErrMsgException e) {
		out.print(e.getMessage());
	}	
	return;
}
else if (myop.equals("renameAtt")) {
	int attId = ParamUtil.getInt(request, "attId");
	String newName = ParamUtil.get(request, "newName");
	
	String result = "操作成功！";
	com.redmoon.oa.flow.Attachment att = new com.redmoon.oa.flow.Attachment(attId);
	String name = att.getName();
	if (name.equals("")) {
		result = "名称不能为空！";
	}
	if (!name.equals(newName)) {
		att.setName(newName);
		if (att.save())
			result = "操作成功！";
		else
			result = "操作失败！";
	}
	response.setContentType("text/xml;charset=UTF-8");
	String str = "";
	str += "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
	str += "<ret>\n";
	str += "<item>\n";
	str += "<attId>" + attId + "</attId>\n";
	str += "<result>" + result + "</result>\n";
	str += "<newName>" + newName + "</newName>\n";
	str += "</item>\n";
	str += "</ret>";
	out.print(str);
	return;
}

WorkflowMgr wfm = new WorkflowMgr();

String action = ParamUtil.get(request, "action");
if (action.equals("recall")) {
	int flowId = ParamUtil.getInt(request, "flowId", -1);
	long myActionId = ParamUtil.getLong(request, "myActionId", -1);
	String skey = ParamUtil.get(request, "skey");
	com.redmoon.oa.android.Privilege prl = new com.redmoon.oa.android.Privilege();
	// 再次赋值覆盖上面的固定赋值，原因是因为上面需要doUpload
	session.setAttribute(Constant.OA_NAME,prl.getUserName(skey));
	session.setAttribute(Constant.OA_UNITCODE,prl.getUserUnitCode(skey));	
	JSONObject json = new JSONObject();
	try {
		boolean re = wfm.recallMyAction(request, myActionId);
		if (re){
			String str = LocalUtil.LoadString(request,"res.common","info_op_success");
			json.put("ret", "1");
			json.put("msg", str);
		}
		else {
			String str = LocalUtil.LoadString(request,"res.flow.Flow","noNodeCanBeWithdrawn");
			json.put("ret", "0");
			json.put("msg", str);
		}
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage());
	}
	out.print(json);
	return;
}

try {
	wfm.doUpload(application, request);
}
catch (ErrMsgException e) {
	out.print(StrUtil.Alert_Back(e.getMessage()));
	return;
}

request.setAttribute("workflowParams", new WorkflowParams(request, wfm.getFileUpload()));

// WorkflowHelper.set(new WorkflowParams(request, wfm.getFileUpload()));
String skey = wfm.getFieldValue("skey");
com.redmoon.oa.android.Privilege prl = new com.redmoon.oa.android.Privilege();
// 再次赋值覆盖上面的固定赋值，原因是因为上面需要doUpload
session.setAttribute(Constant.OA_NAME,prl.getUserName(skey));
session.setAttribute(Constant.OA_UNITCODE,prl.getUserUnitCode(skey));

String op = wfm.getFieldValue("op");

String strFlowId = wfm.getFieldValue("flowId");
int flowId = Integer.parseInt(strFlowId);
String strActionId = wfm.getFieldValue("actionId");
int actionId = Integer.parseInt(strActionId);
String strMyActionId = wfm.getFieldValue("myActionId");
long myActionId = Long.parseLong(strMyActionId);

WorkflowDb wf = wfm.getWorkflowDb(flowId);

String myname = privilege.getUser( request );

WorkflowActionDb wa = new WorkflowActionDb();
wa = wa.getWorkflowActionDb(actionId);
if (!wa.isLoaded()) {
	// out.print(SkinUtil.makeErrMsg(request, "没有正在办理的节点！"));
	JSONObject json = new JSONObject();
	json.put("res", "-1");
	json.put("msg", "没有正在办理的节点！");
	out.print(json);
	return;
}

MyActionDb myActionDb = new MyActionDb();
myActionDb = myActionDb.getMyActionDb(myActionId);
String result = wfm.getFieldValue("cwsWorkflowResult");
myActionDb.setResult(result);
// myActionDb.setChecked(true);
myActionDb.save();
// lzm添加  审阅 判断
if(op.equals("finish")){
	int kind = wa.getKind();
	if(kind == WorkflowActionDb.KIND_READ){
		op = "read";
	}
}

if (op.equals("return")) {
	try {
		boolean re = wfm.ReturnAction(request, wf, wa, myActionId);
		if (re) {			
			JSONObject json = new JSONObject();
			json.put("res", "0");
			json.put("op", op);
			json.put("msg", "操作成功！");
			out.print(json);					
		}
		else {
			JSONObject json = new JSONObject();
			json.put("res", "-1");
			json.put("msg", "操作失败！");
			json.put("op", op);
			out.print(json);
		}
	}
	catch (ErrMsgException e) {
		// out.print(StrUtil.Alert_Redirect(e.getMessage(), "flow_dispose.jsp?myActionId=" + myActionId));
		JSONObject json = new JSONObject();
		json.put("res", "-1");
		json.put("msg", e.getMessage());
		json.put("op", op);
		out.print(json);
	}
	return;
}
else if(op.equals("read")){
	boolean re = false;
	try {
		re = wfm.read(request, actionId, myActionId);
	}
	catch (ErrMsgException e) {
		// out.print(StrUtil.Alert_Back(e.getMessage()));
		JSONObject json = new JSONObject();
		json.put("res", "-1");
		json.put("msg", e.getMessage());
		json.put("op", "finish");
		out.print(json);
		return;	
	}
	if (re) {
		JSONObject json = new JSONObject();
		json.put("res", "0");
		json.put("op", "finish");
		json.put("msg", "操作成功!");
		out.print(json);
	}
	else {
		JSONObject json = new JSONObject();
		json.put("res", "-1");
		json.put("msg", "操作失败!");
		json.put("op", "finish");
		out.print(json);
	}	
	return;
}
else if (op.equals("saveformvalue")) {
	WorkflowMgr wm = new WorkflowMgr();
	boolean re = false;
	JSONObject json = new JSONObject();
	try {
		re = wfm.saveFormValue(request, wf, wa);
	}
	catch (ErrMsgException e) {
		// out.print(StrUtil.Alert_Back(e.getMessage()));
		json.put("res", "-1");
		json.put("msg", e.getMessage());
		json.put("op", "finish");
		out.print(json);
		return;	
	}
	if (re) {
		json.put("res", "5");
		json.put("op", "saveformvalue");
		json.put("msg", "保存草稿成功！");
		out.print(json);
	}
	else {
		json.put("res", "-1");
		json.put("msg", "操作失败!");
		json.put("op", "saveformvalue");
		out.print(json);
	}	
	return;	
}
else if(op.equals("del")){
	boolean re = false;
	JSONObject json = new JSONObject();
	try {
		re = wfm.del(request, flowId);
	} catch (ErrMsgException e) {
		// e.printStackTrace();
		json.put("res", "-1");
		json.put("msg", e.getMessage());
		json.put("op", "del");
		out.print(json);
		return;
	}
	if (re) {
		json.put("res", "0");
		json.put("op", "del");
		json.put("msg", "操作成功!");
	}
	else {
		json.put("res", "-1");
		json.put("msg", "操作失败!");
		json.put("op", "del");
	}	
	out.print(json);
	return;
}
else if(op.equals("finish")){
	boolean flagXorRadiate = wa.isXorRadiate();
	Vector vMatched = null;
	StringBuffer condBuf = new StringBuffer();
	if (flagXorRadiate) {
	  try {
		  wfm.saveFormValue(request, wf, wa);
		  
		  request.setAttribute("myActionId", myActionId);
		  vMatched = wa.matchNextBranch(wa,myname,condBuf,myActionId);
		  
		  /*
		  if (vMatched!=null)
		  	System.out.println(getClass() + " vMatched:"+ vMatched + " size=" + vMatched.size());		  
		  else
		  	System.out.println(getClass() + " vMatched:"+ vMatched);
		  */
	  }
	  catch (ErrMsgException e) {
		  // out.print("<font color=red>" + e.getMessage() + "</font><BR>");
		  JSONObject json = new JSONObject();
		  json.put("res", "-1");
		  json.put("msg",e.getMessage());
		  out.print(json);
		  return;
	  }
	}
	boolean isCondSatisfied = (vMatched!=null && vMatched.size()>0)?true:false;
	// System.out.println(getClass() + " isCondSatisfied=" + isCondSatisfied + "   " + linkMatched.getFlowId());
	String conds = condBuf.toString();
	boolean hasCond = conds.equals("")?false:true; // 是否含有条件
  	boolean isAfterSaveformvalueBeforeXorCondSelect = wfm.getFieldValue("isAfterSaveformvalueBeforeXorCondSelect").equals("true");
	if (hasCond && !isAfterSaveformvalueBeforeXorCondSelect) {
		JSONArray users = new JSONArray();
		com.redmoon.oa.android.Privilege pri = new com.redmoon.oa.android.Privilege();
		Vector vto = wa.getLinkToActions();
		Iterator toir = vto.iterator();
		WorkflowLinkDb wld = new WorkflowLinkDb();
		Iterator userir = null;
		// 如果条件不满足，则让用户选择默认条件（默认条件可能有多个）
		if (!isCondSatisfied) {
			int q = 0;
			while (toir.hasNext()) {
				WorkflowActionDb towa = (WorkflowActionDb) toir.next();
				wld = wld.getWorkflowLinkDbForward(wa, towa);
				// @task:是否该改为condType为-1（不需要，因为title中存储的是条件，cond_desc中才是描述
				// 过滤掉非默认条件
				if (!wld.getTitle().trim().equals(""))
				  continue;
				q ++;
				
				boolean isSelectable = towa.isStrategySelectable();
				Vector vuser = towa.matchActionUser(request, towa, wa, false, null);
				//Vector vuser = towa.matchActionUser(pri.getUserUnitCode(skey), towa, wa);
				userir = vuser.iterator();
				if(vuser != null && vuser.size()>0){
					userir = vuser.iterator();
					while (userir != null && userir.hasNext()) {
						UserDb ud = (UserDb) userir.next();
						JSONObject user = new JSONObject();
						user.put("actionTitle", towa.getTitle());						
						user.put("internalname", towa.getInternalName());
						user.put("name", "WorkflowAction_"+towa.getId());
						user.put("value", ud.getName());
						user.put("realName", ud.getRealName());
						user.put("isSelectable", isSelectable);
						users.put(user);
					}
				}else{
					JSONObject user = new JSONObject();
					user.put("actionTitle", towa.getTitle());				
					user.put("internalname", towa.getInternalName());
					user.put("name", "WorkflowAction_"+towa.getId());
					user.put("value", "");
					user.put("realName", "");
					users.put(user);
				}
				
			}
			JSONObject json = new JSONObject();
			json.put("res", "3");
			json.put("users",users);
			out.print(json);
			return;
		}
		else{
			while (toir.hasNext()) {
				WorkflowActionDb towa = (WorkflowActionDb)toir.next();
				Iterator irMatched = vMatched.iterator();
				boolean isTowaMatched = false;
				while (irMatched.hasNext()) {
					WorkflowLinkDb linkMatched = (WorkflowLinkDb)irMatched.next();
					if (towa.getInternalName().equals(linkMatched.getTo())) {
						isTowaMatched = true;
						break;
					}
				}
				if (isTowaMatched) {
					boolean isSelectable = towa.isStrategySelectable();
					Vector vuser = towa.matchActionUser(request, towa, wa, false, null);
					//Vector vuser = towa.matchActionUser(pri.getUserUnitCode(skey), towa, wa);
					if(vuser != null && vuser.size()>0){
						userir = vuser.iterator();
						while (userir != null && userir.hasNext()) {
							UserDb ud = (UserDb) userir.next();
							JSONObject user = new JSONObject();
							user.put("actionTitle", towa.getTitle());				
							user.put("internalname", towa.getInternalName());
							user.put("name", "WorkflowAction_"+towa.getId());
							user.put("value", ud.getName());
							user.put("realName", ud.getRealName());
							user.put("isSelectable", isSelectable);							
							users.put(user);
						}
					}else{
						JSONObject user = new JSONObject();
						user.put("actionTitle", towa.getTitle());				
						user.put("internalname", towa.getInternalName());
						user.put("name", "WorkflowAction_"+towa.getId());
						user.put("value", "");
						user.put("realName", "");
						users.put(user);
					}
				}
			}	
			if(users != null && users.length()>0){
				JSONObject json = new JSONObject();
				json.put("res", "3");
				json.put("users",users);
				out.print(json);
				return;
			}
		}			
	}
	
	// 检查是否有表单中指定的用户，如果有，则返回匹配到的人员，还是利用流程中匹配条件分支的方式，手机端无需改动
	if (!isAfterSaveformvalueBeforeXorCondSelect) {
		boolean isFieldUser = false;
		Vector vto = wa.getLinkToActions();
		Iterator toir = vto.iterator();
		while (toir.hasNext()) {
			WorkflowActionDb towa = (WorkflowActionDb) toir.next();
			if (towa.getJobCode().startsWith(WorkflowActionDb.PRE_TYPE_FIELD_USER)) {
				isFieldUser = true;
				break;
			}
		}	
		if (isFieldUser) {
			JSONArray users = new JSONArray();
			// 保存表单
			wfm.saveFormValue(request, wf, wa);
			toir = vto.iterator();
			while (toir.hasNext()) {
				WorkflowActionDb towa = (WorkflowActionDb)toir.next();
				boolean isSelectable = towa.isStrategySelectable();
				Vector vuser = towa.matchActionUser(request, towa, wa, false, null);
				//Vector vuser = towa.matchActionUser(pri.getUserUnitCode(skey), towa, wa);
				if(vuser != null && vuser.size()>0){
					Iterator userir = vuser.iterator();
					while (userir != null && userir.hasNext()) {
						UserDb ud = (UserDb) userir.next();
						JSONObject user = new JSONObject();
						user.put("actionTitle", towa.getTitle());				
						user.put("internalname", towa.getInternalName());
						user.put("name", "WorkflowAction_"+towa.getId());
						user.put("value", ud.getName());
						user.put("realName", ud.getRealName());
						user.put("isSelectable", isSelectable);							
						users.put(user);
					}
				}else{
					JSONObject user = new JSONObject();
					user.put("actionTitle", towa.getTitle());				
					user.put("internalname", towa.getInternalName());
					user.put("name", "WorkflowAction_"+towa.getId());
					user.put("value", "");
					user.put("realName", "");
					users.put(user);
				}			
			}
			// 如果匹配到用户，则返回
			if(users.length()>0) {
				JSONObject json = new JSONObject();
				json.put("res", "3");
				json.put("users",users);
				out.print(json);
				return;
			}		
		}
	}
}
if (op.equals("finish") || op.equals("AutoSaveArchiveNodeCommit")) {
	try {
		boolean re = wfm.FinishAction(request, wf, wa, myActionId);
		if (re) {	    
			/*
			if (op.equals("finish")) {
				myActionDb.setResultValue(WorkflowActionDb.RESULT_VALUE_AGGREE);
				myActionDb.save();
			}
			*/		
			// 如果后继节点中有一个节点是由本人继续处理，且已处于激活状态，则继续处理这个节点
			MyActionDb mad = wa.getNextActionDoingWillBeCheckedByUserSelf(privilege.getUser(request));
			
			// System.out.println(getClass() + " mad=" + mad);
			
			if (mad!=null) {
				/*
				if (op.equals("AutoSaveArchiveNodeCommit")) {
					response.sendRedirect("flow_doc_archive_save_auto.jsp?actionId=" + actionId + "&myActionId=" + mad.getId());
				}
				else
					out.print(StrUtil.Alert_Redirect("操作成功！请点击确定，继续处理下一节点！", "flow_dispose.jsp?myActionId=" + mad.getId()));
				*/
				JSONObject json = new JSONObject();
				json.put("res", "0");
				json.put("op", op);
				json.put("nextMyActionId", "" + mad.getId());
				json.put("msg", "操作成功！请点击确定，继续处理下一节点！");
				out.print(json);					
			}
			else {
				JSONObject json = new JSONObject();
				json.put("res", "0");
				json.put("op", op);
				json.put("nextMyActionId", "");
				json.put("msg", "操作成功！");
				out.print(json);					
			}
		}
		else {
			// out.print(StrUtil.Alert_Back("操作失败！"));
			JSONObject json = new JSONObject();
			json.put("res", "-1");
			json.put("msg", "操作失败！");
			json.put("op", op);
			out.print(json);		
		}
	}
	catch (ErrMsgException e) {
		// out.print(StrUtil.Alert_Redirect(e.getMessage(), "flow_dispose.jsp?myActionId=" + myActionId));
		// out.print(StrUtil.Alert_Back("操作失败：" + e.getMessage()));		
		e.printStackTrace();
		JSONObject json = new JSONObject();
		json.put("res", "-1");
		json.put("msg", e.getMessage());
		json.put("op", op);
		out.print(json);		
		 System.out.println("msg:"+e.getMessage());
	}
	return;
}

if (op.equals("manualFinish") || op.equals("AutoSaveArchiveNodeManualFinish") || op.equals("manualFinishAgree")) {
	boolean re = false;
	try {
		re = wfm.saveFormValue(request, wf, wa);
		re = wfm.ManualFinish(request, flowId, myActionId);
	}
	catch (ErrMsgException e) {
		// out.print(StrUtil.Alert_Back(e.getMessage()));
		JSONObject json = new JSONObject();
		json.put("res", "-1");
		json.put("msg", e.getMessage());
		json.put("op", op);
		out.print(json);		
	}
	if (re) {
		JSONObject json = new JSONObject();
		json.put("res", "0");
		json.put("op", op);
		json.put("msg", "操作成功！");
		out.print(json);
	}
	else {
		// out.print(StrUtil.Alert_Back("操作失败！"));
		JSONObject json = new JSONObject();
		json.put("res", "-1");
		json.put("msg", "操作失败！");
		json.put("op", op);
		out.print(json);
	}
	return;
}

// 自动存档前先保存数据，然后获取flow_displose.jsp中iframe中的report表单数据在 办理完毕 时存档
if (op.equals("editFormValue") || op.equals("saveformvalue") || op.equals("saveformvalueBeforeXorCondSelect")) {
	boolean re = false;
	try {
		re = wfm.saveFormValue(request, wf, wa);
		out.print(re);
	}
	catch (ErrMsgException e) {
		JSONObject json = new JSONObject();
		json.put("res", "-1");
		json.put("msg", e.getMessage());
		json.put("op", op);
		out.print(json);

		e.printStackTrace();
		
		return;
	}
	if (re) {
		if (op.equals("saveformvalue")) {
			// out.print(StrUtil.Alert_Redirect("保存成功！", "flow_dispose.jsp?myActionId=" + myActionId));
		}
		else if (op.equals("editFormValue")) {
			// out.print(StrUtil.Alert_Redirect("保存成功！", "flow_form_edit.jsp?flowId=" + wf.getId()));
		}
		else if (op.equals("AutoSaveArchiveNodeCommit")) {
			// response.sendRedirect("flow_dispose.jsp?action=afterAutoSaveArchiveNodeCommit&myActionId=" + myActionId);
		}
		else if (op.equals("AutoSaveArchiveNodeManualFinish")) {
			// response.sendRedirect("flow_dispose.jsp?action=afterAutoSaveArchiveNodeManualFinish&myActionId=" + myActionId);
		}
		else if (op.equals("saveformvalueBeforeXorCondSelect")) {
		}
		JSONObject json = new JSONObject();
		json.put("res", "0");
		json.put("op", op);
		json.put("msg", "操作成功！");
		out.print(json);
	}
	else {
		JSONObject json = new JSONObject();
		json.put("res", "-1");
		json.put("msg", "操作失败！");
		json.put("op", op);
		out.print(json);
	}
}
%>