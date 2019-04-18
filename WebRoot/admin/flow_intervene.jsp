<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.task.TaskDb"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.message.MessageDb"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.oacalendar.*"%>
<%@ page import="com.cloudwebsoft.framework.util.*"%>
<%@ page import="com.redmoon.oa.sms.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv="read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int flow_id = ParamUtil.getInt(request, "flowId");
WorkflowMgr wfm = new WorkflowMgr();
WorkflowDb wf = wfm.getWorkflowDb(flow_id);

if (wf.getTypeCode()==null) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<%
	String str = LocalUtil.LoadString(request,"res.flow.Flow","processNotExist");
	out.print(SkinUtil.makeErrMsg(request, str, true));
	return;
}

// 检查表单是否存在
Leaf lf = new Leaf();
lf = lf.getLeaf(wf.getTypeCode());
FormDb fd = new FormDb();

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>查看/修改流程</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<script src="../inc/map.js"></script>

<style type="text/css"> 
@import url("../util/jscalendar/calendar-win2k-2.css"); 
</style>
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../inc/flow_dispose.jsp"></script>

<script type="text/javascript" src="../js/goToTop/goToTop.js"></script>
<link type="text/css" rel="stylesheet" href="../js/goToTop/goToTop.css" />

<script src="../js/jquery-ui/jquery-ui.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" /> 
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />  

<script tyle="text/javascript" language="javascript" src="../spwhitepad/createShapes.js"></script>
</head>
<body>
<%
fd = fd.getFormDb(lf.getFormCode());
if (!fd.isLoaded()) {
	String str = LocalUtil.LoadString(request,"res.flow.Flow","form");
	String str1 = LocalUtil.LoadString(request,"res.flow.Flow","noLongerExist");
	out.print(StrUtil.jAlert_Back(str + lf.getFormCode() + str1,"提示"));
	return;
}

// 置嵌套表需要用到的cwsId
request.setAttribute("cwsId", "" + flow_id);
// 置嵌套表需要用到的curOperate
request.setAttribute("pageType", "flowShow");
// 置NestSheetCtl需要用到的formCode
request.setAttribute("formCode", lf.getFormCode());

String myUserName = privilege.getUser(request);

Directory dir = new Directory();
Leaf ft = dir.getLeaf(wf.getTypeCode());
boolean isFree = false;
boolean isReactive = false;
boolean isRecall = false;
if (ft!=null) {
	isFree = ft.getType()!=Leaf.TYPE_LIST;
	WorkflowPredefineDb wfp = new WorkflowPredefineDb();
	wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());
	isReactive = wfp.isReactive();
	isRecall = wfp.isRecall();
}

WorkflowRuler wr = new WorkflowRuler();

boolean isFlowManager = false;
LeafPriv lp = new LeafPriv(wf.getTypeCode());
if (privilege.isUserPrivValid(request, "admin.flow")) {
	if (lp.canUserExamine(privilege.getUser(request))) {
		isFlowManager = true;
	}
}

// 判断是否拥有查看流程过程的权限
if (!isFlowManager) {
	// 判断是否参与了流程
	if (!privilege.isUserPrivValid(request, "admin")) {
		%>
		<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<%
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid"), true));
		return;
	}
}

boolean isStarted = wf.isStarted();
String op = ParamUtil.get(request, "op");
if (op.equals("delMyAction")) {
	boolean re = false;
	try {
		long delMyActionId = ParamUtil.getLong(request, "delMyActionId");
		MyActionDb mad = new MyActionDb();
		mad = mad.getMyActionDb(delMyActionId);
		// 删除日程中的待办事宜
		PlanDb pd = new PlanDb();
		pd = pd.getPlanDb(mad.getUserName(), PlanDb.ACTION_TYPE_FLOW, String.valueOf(mad.getId()));
		if (pd!=null) {
			pd.del();
		}		
		
		re = mad.del();
		if (re) {
			// 检查是否节点上是否有其它的办理记录，如果没有，则更改节点状态为未处理
        	String sql = "select id from " + mad.getTableName() + " where action_id=" + mad.getActionId();
			if (mad.list(sql).size()==0) {
				WorkflowActionDb wa = new WorkflowActionDb();
				wa = wa.getWorkflowActionDb((int)mad.getActionId());
				wa.setStatus(WorkflowActionDb.STATE_NOTDO);
				wa.save();
			}
		}
	}
	catch (Exception e) {
		e.printStackTrace();
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re) {
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		out.print(StrUtil.jAlert_Redirect(str,"提示", "flow_intervene.jsp?flowId=" + flow_id));
		//out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flow_intervene.jsp?flowId=" + flow_id));
	}
	else {
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		out.print(StrUtil.jAlert_Back(str,"提示"));
		//out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	}
	return;	
}
else if (op.equals("submitTo")) {
	long myActionId = ParamUtil.getLong(request, "myActionId");
	String internalName = ParamUtil.get(request, "internalName");
	WorkflowActionDb nextwa = new WorkflowActionDb();
	nextwa = nextwa.getWorkflowActionDbByInternalName(internalName, flow_id);
	
	MyActionDb mad = new MyActionDb();
	mad = mad.getMyActionDb(myActionId);
	
	WorkflowActionDb wa = new WorkflowActionDb();
	wa = wa.getWorkflowActionDb((int)mad.getActionId());
	
	String users = ParamUtil.get(request, "users");
	String userRealNames = ParamUtil.get(request, "userRealNames");
	nextwa.setUserName(users);
	nextwa.setUserRealName(userRealNames);
	nextwa.save();
					
	String deptOfUserWithMultiDept = null;
	wa.initTmpUserNameActived();
	wa.deliverToNextAction(request, wf, nextwa, myActionId, deptOfUserWithMultiDept);
	
	mad.setCheckStatus(MyActionDb.CHECK_STATUS_CHECKED);
	mad.save();
	
	boolean isUseMsg = true;
	boolean isToMobile = com.redmoon.oa.sms.SMSFactory.isUseSMS();

	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
	cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail();
	String senderName = StrUtil.GBToUnicode(Global.AppName);
	senderName += "<" + Global.getEmail() + ">";
	if (flowNotifyByEmail) {
		String mailserver = Global.getSmtpServer();
		int smtp_port = Global.getSmtpPort();
		String name = Global.getSmtpUser();
		String pwd_raw = Global.getSmtpPwd();
		try {
			sendmail.initSession(mailserver, smtp_port, name,
								 pwd_raw);
		} catch (Exception ex) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
		}
	}

	com.redmoon.forum.Config forumCfg = com.redmoon.forum.Config.getInstance();
	String t = SkinUtil.LoadString(request,
								   "res.module.flow",
								   "msg_user_actived_title");
	String c = SkinUtil.LoadString(request,
								   "res.module.flow",
										   "msg_user_actived_content");
	String tail = WorkflowMgr.getFormAbstractTable(wf);
	
	UserMgr um = new UserMgr();

	MessageDb md = new MessageDb();
	Iterator ir = wa.getTmpUserNameActived().iterator();
	while (ir.hasNext()) {
		MyActionDb mad2 = (MyActionDb) ir.next();
		t = t.replaceFirst("\\$flowTitle", wf.getTitle());
		String fc = c.replaceFirst("\\$flowTitle", wf.getTitle());
		fc = fc.replaceFirst("\\$fromUser", wa.getUserRealName());

		if (isToMobile) {
			IMsgUtil imu = SMSFactory.getMsgUtil();
			if (imu != null) {
				UserDb ud = um.getUserDb(mad2.getUserName());
				imu.send(ud, fc, MessageDb.SENDER_SYSTEM);
			}
		}

		fc += tail;
		if (isUseMsg) {
			// 发送信息
			String action = "action=" + MessageDb.ACTION_FLOW_DISPOSE + "|myActionId=" + mad2.getId();
			md.sendSysMsg(mad2.getUserName(), t, fc, action);
		}

		if (flowNotifyByEmail) {
			UserDb user = um.getUserDb(mad2.getUserName());
			if (user.getEmail()!=null && !user.getEmail().equals("")) {
				String action = "userName=" + user.getName() + "|" +
								"myActionId=" + mad2.getId();
				action = cn.js.fan.security.ThreeDesUtil.encrypt2hex(
						forumCfg.getKey(), action);
				fc += "<BR />>>&nbsp;<a href='" +
						Global.getFullRootPath(request) +
						"/public/flow_dispose.jsp?action=" + action +
						"' target='_blank'><lt:Label res='res.flow.Flow' key='clickHere'/></a>";
				sendmail.initMsg(user.getEmail(),
								 senderName,
								 t, fc, true);
				sendmail.send();
				sendmail.clear();
			}
		}
	}	
	String str = LocalUtil.LoadString(request,"res.common","info_op_success");
	out.print(StrUtil.jAlert_Redirect(str,"提示", "flow_intervene.jsp?flowId=" + flow_id));
	//out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flow_intervene.jsp?flowId=" + flow_id));
	return;
}
else if (op.equals("changeStatus")) {
	int status = ParamUtil.getInt(request, "status");
	// wf.setStatus(status);
	// boolean re = wf.save();
	MyActionDb mad = new MyActionDb();
	mad = mad.getLastMyActionDbOfFlow(wf.getId());
	long actionId = mad.getActionId();
	WorkflowActionDb lastAction = new WorkflowActionDb();
	lastAction = lastAction.getWorkflowActionDb((int)actionId);
	boolean re = wf.changeStatus(request, status, lastAction);
	if (re) {
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		out.print(StrUtil.jAlert_Redirect(str,"提示", "flow_intervene.jsp?flowId=" + flow_id));
		//out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flow_intervene.jsp?flowId=" + flow_id));
	}
	else {
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		out.print(StrUtil.jAlert_Back(str,"提示"));
		//out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	}
	return;	
}
else if (op.equals("changeActionStatus")) {
	int actionId = ParamUtil.getInt(request, "actionId");
	int status = ParamUtil.getInt(request, "status");
	WorkflowActionDb wa = new WorkflowActionDb();
	wa = wa.getWorkflowActionDb(actionId);
	wa.setStatus(status);
	boolean re = wa.save();
	if (re) {
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		out.print(StrUtil.jAlert_Redirect(str,"提示", "flow_intervene.jsp?flowId=" + flow_id));
		//out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flow_intervene.jsp?flowId=" + flow_id));
	}
	else {
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		out.print(StrUtil.jAlert_Back(str,"提示"));
		//out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	}
	return;	
}

com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();
	
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String canUserModifyFlow = cfg.get("canUserModifyFlow");
String mode = "user";
if (canUserModifyFlow.equals("true"))
	mode = "user";
else
	mode = "view";
	
String flowExpireUnit = cfg.get("flowExpireUnit");
boolean isHour = !flowExpireUnit.equals("day");	
if (flowExpireUnit.equals("day")){
	String str = LocalUtil.LoadString(request,"res.flow.Flow","day");
	flowExpireUnit = str;
}
else{
	String str = LocalUtil.LoadString(request,"res.flow.Flow","hour");
	flowExpireUnit = str;	
}
%>
<%@ include file="../flow_modify_inc_menu_top.jsp"%>
<script>
o("menu8").className="current"; 
</script>
<div class="spacerH"></div>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td align="center" >
		<%=WorkflowMgr.getLevelImg(request, wf)%><strong style="font-size:14px"><%=wf.getTitle()%></strong>
      </td>
    </tr>
  </tbody>
</table>
<table id="mainTable" width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td colspan="2" style="padding-left:5px;">
	<table width="100%"  border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td height="35" align="center" >
            <%
			int doc_id = wf.getDocId();
			DocumentMgr dm = new DocumentMgr();
			Document doc = dm.getDocument(doc_id);
			%>
            <lt:Label res="res.flow.Flow" key="organ"/>：<%=um.getUserDb(wf.getUserName()).getRealName()%>&nbsp;&nbsp;<lt:Label res="res.flow.Flow" key="state"/>：<%=wf.getStatusDesc()%>
            &nbsp;&nbsp;
            <span id="projectName">
            <%if (wf.getProjectId()!=-1) {
                com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
                FormDb prjFd = new FormDb();
                prjFd = prjFd.getFormDb("project");
                fdao = fdao.getFormDAO((int)wf.getProjectId(), prjFd);
                %>
                <lt:Label res="res.flow.Flow" key="project"/>：<a href="javascript:;" onClick="addTab('<%=fdao.getFieldValue("name")%>', 'project/project_show.jsp?projectId=<%=wf.getProjectId()%>&formCode=project')"><%=fdao.getFieldValue("name")%></a>&nbsp;&nbsp;<a title="<lt:Label res='res.flow.Flow' key='disassociate'/>" href="javascript:;" onClick="unlinkProject()" style='font-size:16px; font-color:red'>×</a>
                <%
            }
            %>
            </span>
            </td>
        </tr>
        <tr>
          <td><table width="100%" border="0" cellpadding="0" cellspacing="0">
              <tr>
                <td align="center">
                <div id="formAllDiv">
                <table width="100%" align="center" class="tabStyle_1 percent98">
                	
                      <tr>
                        <td class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="changeStatus"/></td>
                      </tr>
              
                      <tr>
                        <td align="center">
                    <form id="flowForm" name="flowForm" action="flow_intervene.jsp">
                      <input name="op" value="changeStatus" type="hidden" />
                        <input name="flowId" value="<%=flow_id%>" type="hidden" />
                        <select id="status" name="status">
                        <option value="<%=WorkflowDb.STATUS_NOT_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NOT_STARTED)%></option>
                        <option value="<%=WorkflowDb.STATUS_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_STARTED)%></option>
                        <option value="<%=WorkflowDb.STATUS_FINISHED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_FINISHED)%></option>
                        <option value="<%=WorkflowDb.STATUS_DISCARDED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DISCARDED)%></option>
                        <option value="<%=WorkflowDb.STATUS_REFUSED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_REFUSED)%></option>
                        <option value="<%=WorkflowDb.STATUS_NONE%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NONE)%></option>
                        </select>
                        <input type="submit" class = "btn" value='<lt:Label res="res.flow.Flow" key="sure"/>' />
                    </form>
                        <script>
						o("status").value = "<%=wf.getStatus()%>";
						</script>
                        </td>
                      </tr>
                  </table>
                <%
String sql = "select id from flow_my_action where flow_id=" + flow_id + " order by receive_date asc";
MyActionDb mad = new MyActionDb();
Vector v = mad.list(sql);
%>
                  <table width="100%" border="0" cellspacing="0" cellpadding="0" class="percent98">
                    <tr>
                      <td height="30" align="left"><strong>&nbsp;<lt:Label res="res.flow.Flow" key="cprocess"/>：</strong></td>
                    </tr>
                  </table>
                  <table width="98%" align="center" class="tabStyle_1 percent98">
                    <tbody>
                      <tr>
                        <td class="tabStyle_1_title" width="10%" align="center"><lt:Label res="res.flow.Flow" key="handler"/></td>
                        <td class="tabStyle_1_title" width="9%" align="center"><lt:Label res="res.flow.Flow" key="bearer"/></td>
                        <td class="tabStyle_1_title" width="7%" align="center"><lt:Label res="res.flow.Flow" key="agent"/></td>
                        <td class="tabStyle_1_title" width="12%" align="center"><lt:Label res="res.flow.Flow" key="task"/></td>
                        <td class="tabStyle_1_title" width="9%" align="center"><lt:Label res="res.flow.Flow" key="reachState"/></td>
                        <td class="tabStyle_1_title" width="11%" align="center"><lt:Label res="res.flow.Flow" key="startTime"/></td>
                        <td class="tabStyle_1_title" width="10%" align="center"><lt:Label res="res.flow.Flow" key="handleTime"/></td>
                        <td class="tabStyle_1_title" width="7%" align="center"><lt:Label res="res.flow.Flow" key="processor"/></td>
                        <td class="tabStyle_1_title" width="9%" align="center"><lt:Label res="res.flow.Flow" key="processeStatus"/></td>
                        <td class="tabStyle_1_title" width="16%" align="center"><lt:Label res="res.flow.Flow" key="operate"/></td>
                      </tr>
                      <%
java.util.Iterator ir = v.iterator();
DeptMgr deptMgr = new DeptMgr();
OACalendarDb oad = new OACalendarDb();
int m=0;
while (ir.hasNext()) {
 	mad = (MyActionDb)ir.next();
	WorkflowDb wfd = new WorkflowDb();
	wfd = wfd.getWorkflowDb((int)mad.getFlowId());
	String userName = wfd.getUserName();
	String userRealName = "";
	if (userName!=null) {
		UserDb user = um.getUserDb(mad.getUserName());
		userRealName = user.getRealName();
	}
	WorkflowActionDb wad = new WorkflowActionDb();	
	wad = wad.getWorkflowActionDb((int)mad.getActionId());
	m++;
	%>
                      <tr class="highlight" id=<%=m %>> 
                        <td  ><%
						  String deptCodes = mad.getDeptCodes();
						  String[] depts = StrUtil.split(deptCodes, ",");
						  if (depts!=null) {
							String dts = "";
							int deptLen = depts.length;
							for (int n=0; n<deptLen; n++) {
								DeptDb dd = deptMgr.getDeptDb(depts[n]);
								if (dd!=null) {
									if (dts.equals(""))
										dts = dd.getName();
									else
										dts += "," + dd.getName();
								}
							}
							if (!dts.equals(""))
								out.print(dts + "：");
						  }
						  
						  boolean isExpired = false;
						  java.util.Date chkDate = mad.getCheckDate();
						  if (chkDate==null)
							chkDate = new java.util.Date();
						  if (DateUtil.compare(chkDate, mad.getExpireDate())==1) {
							isExpired = true;
						  }
						  if (isExpired) {%>
							  <img src="../images/expired.gif" align="absmiddle" alt="<lt:Label res='res.flow.Flow' key='timeOut'/>" />
						  <%}%>
                      		<%=userRealName%>
                        </td>
                        <td><%
					  if (mad.getPrivMyActionId()!=-1) {
					  	MyActionDb mad2 = mad.getMyActionDb(mad.getPrivMyActionId());
						out.print(um.getUserDb(mad2.getUserName()).getRealName());
					  }
					  else
					  	out.print("&nbsp;");
					  %>
                        </td>
                        <td>
                        <%
						if (!mad.getProxyUserName().equals("")) {
							out.print(um.getUserDb(mad.getProxyUserName()).getRealName());
						}
						%>
                        </td>
                        <td><%=StrUtil.getNullStr(wad.getTitle())%></td>
                        <td align="center">
						<%if (wad.getDateDelayed()!=null) {%>
                        	<lt:Label res="res.flow.Flow" key="delay"/>
                        <%}else{%>
							<%=WorkflowActionDb.getStatusName(mad.getActionStatus())%>
                            <%
							String reas = wad.getReason();
							if (reas!=null && !"".equals(reas.trim())) {%>
                            <BR />
                            (<%=reas%>)
                            <%}%>
                        <%}%>
                        </td>
                        <td align="center"><%=DateUtil.format(mad.getReceiveDate(), "yy-MM-dd HH:mm")%> </td>
                        <td align="center"><%=DateUtil.format(mad.getCheckDate(), "yy-MM-dd HH:mm")%></td>
                        <td align="center">
                          <%if (mad.isChecked()) {%>
                          <%if (mad.getChecker().equals(UserDb.SYSTEM)) {%>
                          <lt:Label res="res.flow.Flow" key="system"/>
                          <%}else{
								if (!mad.getChecker().equals("")) {
								%>
                          <%=um.getUserDb(mad.getChecker()).getRealName()%>
                          <%}
							}
						}%>
                        </td>
                        <td align="center" id="tdMad<%=mad.getId()%>" class="<%=MyActionDb.getCheckStatusClass(mad.getCheckStatus())%>">
                        <%
						if (mad.getChecker().equals(UserDb.SYSTEM)) {
							String str = LocalUtil.LoadString(request,"res.flow.Flow","skipOverTime");
							out.print(str);
						}else{						
						%>
                          <%=mad.getCheckStatusName()%>
                        <%}
						if (mad.getCheckStatus()!=0 && mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_TRANSFER && mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {
							if (mad.getResultValue()!=WorkflowActionDb.RESULT_VALUE_RETURN) {
								if (mad.getSubMyActionId()==MyActionDb.SUB_MYACTION_ID_NONE)
								  out.print("<BR>(" + WorkflowActionDb.getResultValueDesc(mad.getResultValue()) + ")");
							}
						}
						%>
                        </td>
                        <td align="center">
						  <%
                            // 检查是否存在后续节点
                            boolean canRecall = false;
                            WorkflowActionDb wa = new WorkflowActionDb();
                            wa = wa.getWorkflowActionDb((int)mad.getActionId());
                            if (v.size()>1 && m==v.size()-1)
                                canRecall = true;
                            if (wa.getLinkToActions().size()==0)
                                canRecall = false;	
                                              
                          if (isFree) {%>
                              <%if (canRecall) {%>
                              <a href='javascript:;' onClick="recallFree(<%=m %>,<%=flow_id%>,<%=mad.getId()%>)"><lt:Label res="res.flow.Flow" key="forcedWithdraw"/></a>
                              <%}
                          }else{
                              if (canRecall) {
                              %>
                              <a href='javascript:;' onClick="recall(<%=m %>,<%=flow_id%>,<%=mad.getId()%>)"><lt:Label res="res.flow.Flow" key="forcedWithdraw"/></a>
                              <%
                              }
                          }%>
                          <%
                          // 发起记录不能被删除
                          if (m!=1) {%>
                          &nbsp;&nbsp;
                          <a href="javascript:;" onClick="delMyAction('<%=mad.getId()%>')"><lt:Label res="res.flow.Flow" key="delete"/></a>
                          <%}%>
                          <%if (!isFree) {%>
                          &nbsp;&nbsp;
                          <a href="javascript:;" onClick="submitTo('<%=mad.getId()%>')"><lt:Label res="res.flow.Flow" key="forwarded"/></a>
                          <%}%>
                          &nbsp;&nbsp;                          
                          <a href="javascript:;" onClick="changeCheckStatus('<%=mad.getId()%>')">状态</a>
                        </td>
                      </tr>
                      <%}%>
                    </tbody>
                  </table>
                  </div>
                <br>
                <%if (!isFree) {%>
                <table width="555" align="center" class="tabStyle_1 percent98">
                <thead>
                    <tr>
                      <td width="216" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="node"/></td>
                      <td width="204" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="state"/></td>
                      <td width="119" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="operate"/></td>
                    </tr>
                </thead>
                <%
                    Iterator irwa = wf.getActions().iterator();
                    while (irwa.hasNext()) {
                        WorkflowActionDb wa = (WorkflowActionDb)irwa.next();
                        %>
                        <form id="formStauts<%=wa.getId()%>" action="flow_intervene.jsp">
                        <tr>
                            <td align="center">
                            <input name="inName" type="hidden" value="<%=wa.getInternalName()%>">
                            <%=wa.getJobName() + "：" + wa.getTitle()%></td>
                            <td align="center">
                            <select id="waStatus<%=wa.getId()%>" name="status">
                            <option value="<%=WorkflowActionDb.STATE_NOTDO%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_NOTDO)%></option>
                            <option value="<%=WorkflowActionDb.STATE_IGNORED%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_IGNORED)%></option>
                            <option value="<%=WorkflowActionDb.STATE_DOING%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_DOING)%></option>
                            <option value="<%=WorkflowActionDb.STATE_RETURN%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_RETURN)%></option>
                            <option value="<%=WorkflowActionDb.STATE_FINISHED%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_FINISHED)%></option>
                            <option value="<%=WorkflowActionDb.STATE_DISCARDED%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_DISCARDED)%></option>
                            <option value="<%=WorkflowActionDb.STATE_TRANSFERED%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_TRANSFERED)%></option>
                            <option value="<%=WorkflowActionDb.STATE_HANDOVER%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_HANDOVER)%></option>
                            <option value="<%=WorkflowActionDb.STATE_PLUS%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_PLUS)%></option>
                            <option value="<%=WorkflowActionDb.STATE_SUSPEND_OVER%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_SUSPEND_OVER)%></option>
                            </select>
                            
                            <input id="flowId" name="flowId" type="hidden" value="<%=flow_id%>" />
                            <input id="op" name="op" type="hidden" value="changeActionStatus" />
                            <input id="actionId" name="actionId" type="hidden" value="<%=wa.getId()%>" />
                            
                            <script>
                            o("waStatus<%=wa.getId()%>").value = "<%=wa.getStatus()%>";
                            </script>
                            
                          </td>
                          <td align="center">
                            <input type="submit" class="btn" value='<lt:Label res="res.flow.Flow" key="sure"/>' />
                          </td>
                        </tr>
                        </form>
                        <%
                    }
                %>
                </table>
                <%}%>
                </td>
              </tr>
            </table></td>
        </tr>
      </table></td>
  </tr>
</table>
<div id="dlg" style="display:none">
<form id="formSubmitTo" action="flow_intervene.jsp" method="post">
	<%
    String flowString = wf.getFlowString();
    String options = "";
    Vector vt = null;
	if (isFree) {
		vt = wf.getActions();		
	}
	else {
		vt = wf.getActionsFromString(flowString);
	}
    Iterator irwa = vt.iterator();
    while (irwa.hasNext()) {
        WorkflowActionDb wa = (WorkflowActionDb)irwa.next();
        // 过滤掉当前节点
        options += "<option value='" + wa.getInternalName() + "'>" + wa.getJobName() + "：" + wa.getTitle() + "</option>";
    }
    
    %>
    <lt:Label res="res.flow.Flow" key="node"/>：
    <select id="internalName" name="internalName">
    <option value=""><lt:Label res="res.flow.Flow" key="pleaseSelect"/></option>
    <%=options%>
    </select>
    <br />
    <lt:Label res="res.flow.Flow" key="user"/>：
    <input id="userRealNames" name="userRealNames" />
    <input class="btn" onClick="openWinUsers()" type="button" value='<lt:Label res="res.flow.Flow" key="choose"/>' name="button">
    
    <input id="users" name="users" type="hidden" />
    <input id="flowId" name="flowId" type="hidden" value="<%=flow_id%>" />
    <input id="myActionId" name="myActionId" type="hidden" />
    <input id="op" name="op" type="hidden" value="submitTo" />
</form>    
</div>

<div id="dlgCheckStatus" style="display:none">
	待办记录状态：<select id="checkStatus" name="checkStatus">
    <option value="<%=MyActionDb.CHECK_STATUS_NOT%>"><%=MyActionDb.getCheckStatusName(MyActionDb.CHECK_STATUS_NOT)%></option>
    <option value="<%=MyActionDb.CHECK_STATUS_CHECKED%>"><%=MyActionDb.getCheckStatusName(MyActionDb.CHECK_STATUS_CHECKED)%></option>
    </select>
</div>
</body>
<script>
var selUserNames = "";
var selUserRealNames = "";

function getSelUserNames() {
	return selUserNames;
}

function getSelUserRealNames() {
	return selUserRealNames;
}

function openWinUsers() {
	selUserNames = o("users").value;
	selUserRealNames = o("userRealNames").value;
	showModalDialog('../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')
}

function setUsers(users, userRealNames) {
	o("users").value = users;
	o("userRealNames").value = userRealNames;
}

function delMyAction(delMyActionId) {
	jConfirm('<lt:Label res="res.flow.Flow" key="isDelete"/>','<lt:Label res="res.flow.Flow" key="prompt"/>',function(r){
		if(!r){return;}
		else{
			window.location.href = "flow_intervene.jsp?op=delMyAction&flowId=<%=flow_id%>&delMyActionId=" + delMyActionId;
		}
	})
}

function recall(id,flow_id,action_id){
	jConfirm('<lt:Label res="res.flow.Flow" key="toForcedWithdraw"/>','<lt:Label res="res.flow.Flow" key="prompt"/>',function(r){
		if(!r){
			return;
		}else{
			$.ajax({
				type:"get",
				url:"../flow_dispose_do.jsp",
				data:{"action":"recall","flow_id":flow_id,"action_id":action_id},
		 		success:function(data,status){
					data = $.parseJSON(data);
		 			jAlert(data.msg,'<lt:Label res="res.flow.Flow" key="prompt"/>');
		 			if (data.ret=="1") {
		 				$("#"+id).remove();
					}
		 		},
		 		error:function(XMLHttpRequest, textStatus){
		 			alert(XMLHttpRequest.responseText);
		 		}
			})
		}
	})
}

function recallFree(id,flow_id,action_id){
	jConfirm('<lt:Label res="res.flow.Flow" key="toForcedWithdraw"/>','<lt:Label res="res.flow.Flow" key="prompt"/>',function(r){
		if(!r){
			return;
		}else{
			$.ajax({
				type:"get",
				url:"../flow_dispose_free_do.jsp",
				data:{"action":"recall","flow_id":flow_id,"action_id":action_id},
		 		success:function(data,status){
		 			$("#"+id).remove();
		 			data = $.parseJSON(data);
		 			
		 			jAlert(data.msg,'<lt:Label res="res.flow.Flow" key="prompt"/>');
		 		},
		 		error:function(XMLHttpRequest, textStatus){
		 			//alert(XMLHttpRequest.responseText);
		 		}
			})
		}
	})
}

function submitTo(myActionId) {
	$("#dlg").dialog({
		title:'<lt:Label res="res.flow.Flow" key="selectNodeUser"/>',
		modal: true,
		width: 350,
		height: 160,
		// bgiframe:true,
		buttons: {
			'<lt:Label res="res.flow.Flow" key="cancel"/>': function() {
				$(this).dialog("close");
			},
			'<lt:Label res="res.flow.Flow" key="sure"/>': function() {
				if (o("users").value=="") {
					jAlert('<lt:Label res="res.flow.Flow" key="selectUser"/>','提示');
					return;
				}
				if (o("internalName").value=="") {
					jAlert('<lt:Label res="res.flow.Flow" key="selectNode"/>','提示');
					return;
				}
				
				$("#myActionId").val(myActionId);
				$("#formSubmitTo").submit();
				
				$(this).dialog("close");
			}
		},
		closeOnEscape: true,
		draggable: true,
		resizable:true
		});	
}

function changeCheckStatus(myActionId, curStatus) {
	$('#checkStatus').val(curStatus);
	$("#dlgCheckStatus").dialog({
		title:'<lt:Label res="res.flow.Flow" key="selChangeStatus"/>',
		modal: true,
		width: 100,
		height: 30,
			// bgiframe:true,
			buttons : {
				'<lt:Label res="res.flow.Flow" key="cancel"/>' : function() {
					$(this).dialog("close");
				},
				'<lt:Label res="res.flow.Flow" key="sure"/>' : function() {
					$.ajax({
						type : "get",
						url : "../public/flow/setMyActionStatus.do",
						data : {
							"myActionId" : myActionId,
							"checkStatus" : $('#checkStatus').val()
						},
						success : function(data, status) {
							data = $.parseJSON(data);
							jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
							if (data.ret == "1") {
								$("#tdMad" + myActionId).html($("#checkStatus").find("option:selected").text());
							}
						},
						error : function(XMLHttpRequest, textStatus) {
							alert(XMLHttpRequest.responseText);
						}
					})
					$(this).dialog("close");
				}
			},
			closeOnEscape: true,
		draggable: true,
		resizable:true
		});		
}
</script>
</html>