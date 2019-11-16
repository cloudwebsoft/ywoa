<%@ page contentType="text/html; charset=utf-8" %><%@ page import="java.io.*"%><%@ page import="com.cloudwebsoft.framework.db.*"%><%@ page import="com.redmoon.oa.fileark.*"%><%@ page import="com.redmoon.oa.db.*"%><%@ page import="com.redmoon.oa.pvg.*"%><%@ page import="com.redmoon.oa.message.*"%><%@ page import="com.redmoon.oa.person.*"%><%@ page import="com.redmoon.oa.dept.*"%><%@ page import="cn.js.fan.util.*"%><%@ page import="cn.js.fan.web.*"%><%@ page import="com.redmoon.kit.util.*"%><%@ page import="com.redmoon.oa.flow.*"%><%@ page import = "org.json.*"%><%@ page import="cn.js.fan.mail.SendMail"%><%@ page import="javax.mail.internet.MimeUtility"%><%@page import="com.redmoon.oa.ui.LocalUtil"%>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %><%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
String priv = "read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String myop = ParamUtil.get(request, "myop");
String myname = privilege.getUser( request );

// 如果本节点是异或聚合，办理完毕，但不转交
if (myop.equals("setFinishAndNotDelive")) {
	long myActionId = ParamUtil.getLong(request, "myActionId");
	long actionId = ParamUtil.getLong(request, "actionId");
	WorkflowActionDb wa = new WorkflowActionDb();
	wa = wa.getWorkflowActionDb((int)actionId);
	wa.setStatus(WorkflowActionDb.STATE_FINISHED);
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
    	wfd.changeStatus(request, WorkflowDb.STATUS_FINISHED, wa);
    }
    String str = LocalUtil.LoadString(request,"res.common","info_op_success");
	out.print(StrUtil.Alert_Redirect(str, "flow/flow_list.jsp?displayMode=1"));
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
			 String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
			out.print(str);
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
	//System.out.println("==========="+newName);
	
	String str1 = LocalUtil.LoadString(request,"res.common","info_op_success");
	String str_faile = LocalUtil.LoadString(request,"res.common","info_op_fail");
	String result = str1;
	com.redmoon.oa.flow.Attachment att = new com.redmoon.oa.flow.Attachment(attId);
	String name = att.getName();
	if (name.equals("")) {
		String str_name = LocalUtil.LoadString(request,"res.flow.Flow","nameNotBeEmpty");
		result = str_name;
	}
	if (!name.equals(newName)) {
		att.setName(newName);
		if (att.save())
			result = str1;
		else
			result = str_faile;
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
else if (myop.equals("linkProject")) {
	int flowId = ParamUtil.getInt(request, "flowId", -1);
	long projectId = ParamUtil.getLong(request, "projectId");
	WorkflowDb wf = new WorkflowDb();
	wf = wf.getWorkflowDb(flowId);
	wf.setProjectId(projectId);
	boolean re = wf.save();
	JSONObject json = new JSONObject();
	if (re) {
		json.put("ret", "1");
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		json.put("msg", str);
		out.print(json);
	}
	else {
		json.put("ret", "0");
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		json.put("msg", str);
		out.print(json);
	}
	return;
}
else if (myop.equals("unlinkProject")) {
	int flowId = ParamUtil.getInt(request, "flowId", -1);
	WorkflowDb wf = new WorkflowDb();
	wf = wf.getWorkflowDb(flowId);
	wf.setProjectId(-1);
	boolean re = wf.save();
	JSONObject json = new JSONObject();
	if (re) {
		json.put("ret", "1");
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		json.put("msg", str);
		out.print(json);
	}
	else {
		json.put("ret", "0");
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		json.put("msg", str);
		out.print(json);
	}
	return;	
}
else if (myop.equals("distribute")) {	
	UserDb user = new UserDb();
	user = user.getUserDb(myname);
	
	String depts = ParamUtil.get(request, "depts");
	int flowId = ParamUtil.getInt(request, "flowId");
	String paperTitle = ParamUtil.get(request, "title");
	int isFlowDisplay = ParamUtil.getInt(request, "isFlowDisplay", 0);
	String[] ary = StrUtil.split(depts, ",");
	int len = 0;
	if (ary!=null)
		len = ary.length;
	
	//发送邮件提醒
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
	SendMail sendmail = WorkflowDb.getSendMail();
	UserDb formUserDb = new UserDb();
	formUserDb = formUserDb.getUserDb(privilege.getUser(request));
	String fromNick = "";
	try {
		fromNick = MimeUtility.encodeText(formUserDb.getRealName());
	} catch (Exception e) {
		e.printStackTrace();
	}
	String fromEmail = Global.getEmail();
    fromNick = fromNick + "<" + fromEmail + ">";		
		
	com.redmoon.forum.Config forumCfg = com.redmoon.forum.Config.getInstance();
		
	boolean re = false;
	DeptDb dd = new DeptDb();
	String fromUnit = user.getUnitCode();
	dd = dd.getDeptDb(fromUnit);
	String unitName = dd.getName();
	PaperConfig pc = PaperConfig.getInstance();	
	// 从配置文件中得到收文角色
	String swRoles = pc.getProperty("swRoles");
	String[] aryRole = StrUtil.split(swRoles, ",");
	int aryRoleLen = 0;
	if (aryRole!=null)
		aryRoleLen = aryRole.length;
	RoleDb[] aryR = new RoleDb[aryRoleLen];
	RoleDb rd = new RoleDb();
	// 取出收文角色
	for (int i=0; i<aryRoleLen; i++) {
		aryR[i] = rd.getRoleDb(aryRole[i]);
	}
	for (int i=0; i<len; i++) {
		PaperDistributeDb pdd = new PaperDistributeDb();
		String toUnit = ary[i];
		// dd = dd.getDeptDb(toUnit);
		// dd = dd.getUnitOfDept(dd);
		// toUnit = dd.getCode();
		java.util.Date disDate = new java.util.Date();
		int isReaded = 0;
		int kind = PaperDistributeDb.KIND_UNIT;
        long paperId = SequenceManager.nextID(SequenceManager.OA_FLOW_PAPER_DISTRIBUTE);

		re = pdd.create(new JdbcTemplate(), new Object[]{new Long(paperId), paperTitle, new Integer(flowId), fromUnit, toUnit, disDate, myname, new Integer(isFlowDisplay), new Integer(isReaded), new Integer(kind)});
		if (re) {
			for (int j=0; j<aryRoleLen; j++) {
				// 取出角色中的全部用户
				java.util.Iterator ir = aryR[j].getAllUserOfRole().iterator();
				while (ir.hasNext()) {
					user = (UserDb)ir.next();
					// 如果用户属于收文单位
					if (user.getUnitCode().equals(toUnit)) {
						// 消息提醒
						String action;
						if (isFlowDisplay==0) {
							action = "action=" + MessageDb.ACTION_PAPER_DISTRIBUTE + "|paperId=" + paperId;
						}
						else {
                    		action = "action=" + MessageDb.ACTION_FLOW_SHOW + "|flowId=" + flowId;
                    	}
						
						try {
							String swNoticeTitle = pc.getProperty("swNoticeTitle");
							swNoticeTitle = StrUtil.format(swNoticeTitle, new Object[]{unitName, paperTitle});
							String swNoticeContent = pc.getProperty("swNoticeContent");
							swNoticeContent = StrUtil.format(swNoticeContent, new Object[]{unitName, paperTitle, DateUtil.format(disDate, "yyyy-MM-dd")});				
							MessageDb md = new MessageDb();
							md.sendSysMsg(user.getName(), swNoticeTitle, swNoticeContent, action);
							
					        if (flowNotifyByEmail) {
					        	if (isFlowDisplay==1) {
			                        String actionFlow = "op=show|userName=" + user.getName() + "|" +
			                                        "flowId=" + flowId;
			                        actionFlow = cn.js.fan.security.ThreeDesUtil.encrypt2hex(forumCfg.getKey(), actionFlow);
			                        UserSetupDb usd = new UserSetupDb(user.getName());
			                        swNoticeContent += "<BR />>>&nbsp;<a href='" +
			                                Global.getFullRootPath(request) +
			                                "/public/flow_dispose.jsp?action=" + actionFlow +
			                                "' target='_blank'>" + 
			                                (usd.getLocal().equals("en-US") ? "Click here to view" : "请点击此处查看") + "</a>";
		                        }					        		
								sendmail.initMsg(user.getEmail(), fromNick, swNoticeTitle, swNoticeContent, true);
								sendmail.send();
								sendmail.clear();
							}	
														
							String swPlanTitle = pc.getProperty("swPlanTitle");
							swPlanTitle = StrUtil.format(swPlanTitle, new Object[]{paperTitle});
							String swPlanContent = pc.getProperty("swPlanContent");
							swPlanContent = StrUtil.format(swPlanContent, new Object[]{unitName, paperTitle, DateUtil.format(disDate, "yyyy-MM-dd")});										
							
							// 创建日程安排
							PlanDb pd = new PlanDb();
							pd.setTitle(swPlanTitle);
							pd.setContent(swPlanContent);
							pd.setMyDate(new java.util.Date());
							pd.setEndDate(new java.util.Date());
							pd.setActionData(String.valueOf(paperId));
							pd.setActionType(PlanDb.ACTION_TYPE_PAPER_DISTRIBUTE);
							pd.setUserName(user.getName());
							pd.setRemind(false);
							pd.setRemindBySMS(false);
							pd.setRemindDate(new java.util.Date());
							pd.create();							
						} catch (ErrMsgException ex2) {
							ex2.printStackTrace();
						}
					}
				}
			}
		}
	}	
	
	UserDb toUserDb = new UserDb();
	String toNick = "";	
	String users = ParamUtil.get(request, "users");
	// System.out.println(getClass() + " users="+users);
	ary = StrUtil.split(users, ",");
	len = 0;
	if (ary!=null)
		len = ary.length;
	for (int i=0; i<len; i++) {
		PaperDistributeDb pdd = new PaperDistributeDb();
		java.util.Date disDate = new java.util.Date();
		int isReaded = 0;
		int kind = PaperDistributeDb.KIND_USER;
        long paperId = SequenceManager.nextID(SequenceManager.OA_FLOW_PAPER_DISTRIBUTE);		
		re = pdd.create(new JdbcTemplate(), new Object[]{new Long(paperId), paperTitle, new Integer(flowId), fromUnit, ary[i], disDate, myname, new Integer(isFlowDisplay), new Integer(isReaded), new Integer(kind)});
		if (re) {
			// 消息提醒
			String action;
			if (isFlowDisplay==0) {
				action = "action=" + MessageDb.ACTION_PAPER_DISTRIBUTE + "|paperId=" + paperId;
			}
			else {
            	action = "action=" + MessageDb.ACTION_FLOW_SHOW + "|flowId=" + flowId;
            }			
			try {
				String swNoticeTitle = pc.getProperty("swNoticeTitle");
				swNoticeTitle = StrUtil.format(swNoticeTitle, new Object[]{unitName, paperTitle});
				String swNoticeContent = pc.getProperty("swNoticeContent");
				swNoticeContent = StrUtil.format(swNoticeContent, new Object[]{unitName, paperTitle, DateUtil.format(disDate, "yyyy-MM-dd")});				
				MessageDb md = new MessageDb();
				md.sendSysMsg(ary[i], swNoticeTitle, swNoticeContent, action);
				
		        if (flowNotifyByEmail) {		
		        	toUserDb = toUserDb.getUserDb(ary[i]);	
					if (isFlowDisplay==1) {
			        	String actionFlow = "op=show|userName=" + toUserDb.getName() + "|" +
			                                        "flowId=" + flowId;
			            actionFlow = cn.js.fan.security.ThreeDesUtil.encrypt2hex(forumCfg.getKey(), actionFlow);
			                        UserSetupDb usd = new UserSetupDb(toUserDb.getName());
			                        swNoticeContent += "<BR />>>&nbsp;<a href='" +
			                                Global.getFullRootPath(request) +
			                                "/public/flow_dispose.jsp?action=" + actionFlow +
			                                "' target='_blank'>" + 
			                                (usd.getLocal().equals("en-US") ? "Click here to view" : "请点击此处查看") + "</a>";
		            }		        
					sendmail.initMsg(toUserDb.getEmail(), fromNick, swNoticeTitle, swNoticeContent, true);
					sendmail.send();
					sendmail.clear();
				}
					
				String swPlanTitle = pc.getProperty("swPlanTitle");
				swPlanTitle = StrUtil.format(swPlanTitle, new Object[]{paperTitle});
				String swPlanContent = pc.getProperty("swPlanContent");
				swPlanContent = StrUtil.format(swPlanContent, new Object[]{unitName, paperTitle, DateUtil.format(disDate, "yyyy-MM-dd")});				
				
				// 创建日程安排
				PlanDb pd = new PlanDb();
				pd.setTitle(swPlanTitle);
				pd.setContent(swPlanContent);
				pd.setMyDate(new java.util.Date());
				pd.setEndDate(new java.util.Date());
				pd.setActionData(String.valueOf(paperId));
				pd.setActionType(PlanDb.ACTION_TYPE_PAPER_DISTRIBUTE);
				pd.setUserName(ary[i]);
				pd.setRemind(false);
				pd.setRemindBySMS(false);
				pd.setRemindDate(new java.util.Date());
				pd.create();
			} catch (ErrMsgException ex2) {
				ex2.printStackTrace();
			}
		}
	}
	
	// 生成PDF
	WorkflowDb wf = new WorkflowDb();
	wf = wf.getWorkflowDb(flowId);
	int doc_id = wf.getDocId();
	com.redmoon.oa.flow.DocumentMgr dm = new com.redmoon.oa.flow.DocumentMgr();
	com.redmoon.oa.flow.Document doc = dm.getDocument(doc_id);
	java.util.Vector attachments = doc.getAttachments(1);
	java.util.Iterator ir = attachments.iterator();
	while (ir.hasNext()) {
		com.redmoon.oa.flow.Attachment att = (com.redmoon.oa.flow.Attachment) ir.next();
		String ext = StrUtil.getFileExt(att.getName());
		if (ext.equals("doc") || ext.equals("docx"))
			;
		else
			continue;
		
		String fileName = att.getDiskName();
		String fName = fileName.substring(0, fileName.lastIndexOf("."));
		fileName = fName + ".pdf";
		
		String fileDiskPath = cn.js.fan.web.Global.getRealPath() + att.getVisualPath() + "/" + att.getDiskName();
		String pdfPath = cn.js.fan.web.Global.getRealPath() + att.getVisualPath() + "/" + fileName;
		File f = new File(pdfPath);
		try {
			com.redmoon.oa.util.PDFConverter.convert2PDF(fileDiskPath, pdfPath);
		}
		catch(Exception e) {
			// UnsatisfiedLinkError: no jacob in java.library.path			
			e.printStackTrace();
		}		
	}
	
	if (re) {
		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("op", myop);
		String str = LocalUtil.LoadString(request,"res.flow.Flow","distributionSuccess");
		json.put("msg", str);
		out.print(json);
	}
	else {
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		String str = LocalUtil.LoadString(request,"res.flow.Flow","distributionFaile");
		json.put("msg", str);
		json.put("op", myop);
		out.print(json);
	}
	return;
}
else if (myop.equals("isArchiveGovDone")) {
	int flowId = ParamUtil.getInt(request, "flowId");
	boolean re = PaperMgr.isArchiveGovDone(flowId);
	if (re) {
		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("op", myop);
		out.print(json);
	}
	else {
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("op", myop);
		out.print(json);
	}
	return;
}
else if (myop.equals("clearLocker")) { // 当编辑完文件关闭窗口时解锁
	int fileId = ParamUtil.getInt(request, "fileId");
	boolean re = true;
	com.redmoon.oa.flow.Attachment at = new com.redmoon.oa.flow.Attachment(fileId);
	if (!"".equals(at.getLockUser())) {
		at.setLockUser("");
		re = at.save();
	}
	if (re) {
		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("op", myop);
		out.print(json);
	}
	else {
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("op", myop);
		out.print(json);
	}
	return;
}

WorkflowMgr wfm = new WorkflowMgr();

String action = ParamUtil.get(request, "action");
if (action.equals("recall")) {
	int flowId = ParamUtil.getInt(request, "flow_id");
	long myActionId = ParamUtil.getLong(request, "action_id");
	try {
		boolean re = wfm.recallMyAction(request, myActionId);
		JSONObject json = new JSONObject();
		if (re){
			String str = LocalUtil.LoadString(request,"res.common","info_op_success");
			json.put("ret", "1");
			json.put("msg", str);
			out.print(json);
		}
		else {
			String str = LocalUtil.LoadString(request,"res.flow.Flow","noNodeCanBeWithdrawn");
			json.put("ret", "0");
			json.put("msg", str);
			out.print(json);
		}
	}
	catch (ErrMsgException e) {
		//out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}

try {
	wfm.doUpload(application, request);
}
catch (ErrMsgException e) {
	// 可能会抛出验证非法的错误信息
	// e.printStackTrace();
	DebugUtil.e(getClass(), "doUpload", e.getMessage());

	JSONObject json = new JSONObject();
	json.put("ret", "0");
	json.put("msg", e.getMessage());
	json.put("op", "");
	out.print(json);
	return;
}

request.setAttribute("workflowParams", new WorkflowParams(request, wfm.getFileUpload()));

// WorkflowHelper.set(new WorkflowParams(request, wfm.getFileUpload()));

String op = wfm.getFieldValue("op");
String strFlowId = wfm.getFieldValue("flowId");
int flowId = Integer.parseInt(strFlowId);
String strActionId = wfm.getFieldValue("actionId");
int actionId = Integer.parseInt(strActionId);
String strMyActionId = wfm.getFieldValue("myActionId");
long myActionId = Long.parseLong(strMyActionId);

WorkflowDb wf = wfm.getWorkflowDb(flowId);

WorkflowActionDb wa = new WorkflowActionDb();
wa = wa.getWorkflowActionDb(actionId);
if (!wa.isLoaded()) {
	// out.print(SkinUtil.makeErrMsg(request, "没有正在办理的节点！"));
	String str = LocalUtil.LoadString(request,"res.flow.Flow","notBeingHandle");
	out.print(str);
	return;
}

MyActionDb myActionDb = new MyActionDb();
myActionDb = myActionDb.getMyActionDb(myActionId);
if (myActionDb.getCheckStatus()==MyActionDb.CHECK_STATUS_PASS) {
	JSONObject json = new JSONObject();
	json.put("ret", "0");
	json.put("op", op);
	String str = LocalUtil.LoadString(request,"res.flow.Flow","noNeedToDealWith");
	json.put("msg", str);
	out.print(json);
	return;
}
else if (myActionDb.getCheckStatus()==MyActionDb.CHECK_STATUS_PASS_BY_RETURN) {
	JSONObject json = new JSONObject();
	json.put("ret", "0");
	json.put("op", op);
	String str = LocalUtil.LoadString(request,"res.flow.Flow","upcomingProcess");
	json.put("msg", str);
	out.print(json);
	return;
}

String result = wfm.getFieldValue("cwsWorkflowResult");
myActionDb.setResult(result);
/*
// 如果FinishAction在处理时抛出了异常，则不能置状态为checked，否则回到待办记录列表后，会找不到此记录
if(op!= null && !op.trim().equals("saveformvalue") && !op.trim().equals("saveformvalueBeforeXorCondSelect")){
	 myActionDb.setChecked(true);
}
*/
myActionDb.save();

if (op.equals("return")) {
	try {
		boolean re = wfm.ReturnAction(request, wf, wa, myActionId);
		if (re) {			
			JSONObject json = new JSONObject();
			json.put("ret", "1");
			json.put("op", op);
			String str = LocalUtil.LoadString(request,"res.common","info_op_success");
			json.put("msg", str);
			out.print(json);
		}
		else {
			// out.print(StrUtil.Alert_Redirect("操作失败！", "flow_dispose.jsp?myActionId=" + myActionId));
			JSONObject json = new JSONObject();
			json.put("ret", "0");
			String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
			json.put("msg", str);
			json.put("op", op);
			out.print(json);
		}
	}
	catch (ErrMsgException e) {
		// out.print(StrUtil.Alert_Redirect(e.getMessage(), "flow_dispose.jsp?myActionId=" + myActionId));
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		json.put("op", op);
		out.print(json);
	}
	return;
}

// System.out.println(getClass() + " " + op);
// response.setContentType("application/x-json");

if (op.equals("finish") || op.equals("AutoSaveArchiveNodeCommit")) {
	try {
		try {
			wfm.checkLock(request, wf);
		} catch (ErrMsgException e1) {
			myActionDb.setChecked(false);
			myActionDb.save();
			JSONObject json = new JSONObject();
			json.put("ret", "0");
			json.put("msg", e1.getMessage());
			json.put("op", op);
			out.print(json);
			return;
		}
		boolean re = wfm.FinishAction(request, wf, wa, myActionId);
		if (re) {
			/*
			if (op.equals("finish")) {
				myActionDb.setResultValue(WorkflowActionDb.RESULT_VALUE_AGGREE);
				myActionDb.save();
			}
			*/
			// 自动存档
			if (op.equals("AutoSaveArchiveNodeCommit")) {
				re = wfm.autoSaveArchive(request, wf, wa); 
			}
			
			// 如果后继节点中有一个节点是由本人继续处理，且已处于激活状态，则继续处理这个节点
			MyActionDb mad = wa.getNextActionDoingWillBeCheckedByUserSelf(privilege.getUser(request));
			
			// System.out.println(getClass() + " mad=" + mad);
			
			op = "finish";

			if (mad!=null) {
				JSONObject json = new JSONObject();
				json.put("ret", "1");
				json.put("op", op);
				json.put("nextMyActionId", "" + mad.getId());
				String str = LocalUtil.LoadString(request,"res.flow.Flow","clickOk");
				json.put("msg", str);
				out.print(json);
			}
			else {
				JSONObject json = new JSONObject();
				json.put("ret", "1");
				json.put("op", op);
				json.put("nextMyActionId", "");
				String str = LocalUtil.LoadString(request,"res.common","info_op_success");
				json.put("msg", str);
				out.print(json);					
			}
		}
		else {
			// out.print(StrUtil.Alert_Back("操作失败！"));
			JSONObject json = new JSONObject();
			json.put("ret", "0");
			String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
			json.put("msg", str);
			json.put("op", op);
			out.print(json);
		}
	}
	catch (ErrMsgException e) {
		// out.print(StrUtil.Alert_Redirect(e.getMessage(), "flow_dispose.jsp?myActionId=" + myActionId));
		// out.print(StrUtil.Alert_Back("操作失败：" + e.getMessage()));		
		// e.printStackTrace();
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		json.put("op", op);
		out.print(json);
		e.printStackTrace();
	}
	catch (NullPointerException e) {
		// 为便于查错
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		json.put("op", op);
		out.print(json);		
		e.printStackTrace();
	}
	return;
}
if (op.equals("read")) {
	boolean re = false;
	try {
		re = wfm.read(request, actionId, myActionId);
	}
	catch (ErrMsgException e) {
		// out.print(StrUtil.Alert_Back(e.getMessage()));
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		json.put("op", op);
		out.print(json);
		return;	
	}
	if (re) {
		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("op", op);
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		json.put("msg", str);
		out.print(json);
	}
	else {
		// out.print(StrUtil.Alert_Back("操作失败！"));
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		json.put("msg", str);
		json.put("op", op);
		out.print(json);
	}	
	return;
}
if (op.equals("manualFinish") || op.equals("AutoSaveArchiveNodeManualFinish") || op.equals("manualFinishAgree")) {
	boolean re = false;
	try {
		re = wfm.saveFormValue(request, wf, wa);
		re = wfm.ManualFinish(request, flowId, myActionId);
		/*
		if (re) {
			if (op.equals("manualFinishAgree")) {
				myActionDb.setResultValue(WorkflowActionDb.RESULT_VALUE_AGGREE);
			}
			else {
				myActionDb.setResultValue(WorkflowActionDb.RESULT_VALUE_DISAGGREE);
			}
			myActionDb.save();
		}
		*/
	}
	catch (ErrMsgException e) {
		// out.print(StrUtil.Alert_Back(e.getMessage()));
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		json.put("op", op);
		out.print(json);		
	}
	if (re) {
		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("op", op);
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		json.put("msg", str);
		out.print(json);
	}
	else {
		// out.print(StrUtil.Alert_Back("操作失败！"));
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		json.put("msg", str);
		json.put("op", op);
		out.print(json);
	}
	return;
}

// 自动存档前先保存数据，然后获取flow_displose.jsp中iframe中的report表单数据在 办理完毕 时存档
if (op.equals("editFormValue") || op.equals("saveformvalue") || op.equals("saveformvalueBeforeXorCondSelect")) {
	boolean re = false;
	try {
		// 2013-06-29 fgf 注意保存草稿已经不再进行有效性验证		
		re = wfm.saveFormValue(request, wf, wa);
	}
	catch (ErrMsgException e) {
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		json.put("op", op);
		out.print(json);

		// e.printStackTrace();
		DebugUtil.e(getClass(), op, e.getMessage());
		// LogUtil.getLog(getClass()).error(StrUtil.trace(e));
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
		json.put("ret", "1");
		json.put("op", op);
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		json.put("msg", str);
		out.print(json);
	}
	else {
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		json.put("msg", str);
		json.put("op", op);
		out.print(json);
	}
}
%>