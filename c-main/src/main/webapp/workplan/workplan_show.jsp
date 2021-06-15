<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "com.redmoon.kit.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.flow.WorkflowDb"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>工作计划-查看</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<SCRIPT LANGUAGE= "Javascript" SRC="../FusionCharts/FusionCharts.js"></SCRIPT>
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int id = ParamUtil.getInt(request, "id");

String op = ParamUtil.get(request, "op");
if (op.equals("addAnnex")) {
	boolean re = false;
	FileUpload fu = null;
	try {
		WorkPlanAnnexMgr wam = new WorkPlanAnnexMgr();
		re = wam.create(application, request);
		fu = wam.getFileUpload();
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
	if (re) {
		String privurl = fu.getFieldValue("privurl");
		if (!privurl.equals("")) {
			out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", privurl));
			return;
		}
		int annexType = StrUtil.toInt(fu.getFieldValue("annex_type"), -1);
		if (annexType==WorkPlanAnnexDb.TYPE_WEEK) {
			out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "workplan_annex_list_week.jsp?id=" + id));
		}
		else if (annexType==WorkPlanAnnexDb.TYPE_MONTH) {
			out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "workplan_annex_list_month.jsp?id=" + id));
		}
		else
			out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "workplan_annex_list.jsp?id=" + id));
	}
	else {
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_success"),"提示"));
	}
	return;
}
else if (op.equals("delAnnex")) {
	boolean re = false;
	try {
		WorkPlanAnnexMgr wam = new WorkPlanAnnexMgr();
		re = wam.del(request);
	}
	catch (Exception e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re) {
		String privurl = ParamUtil.get(request, "privurl");
		if (privurl.equals(""))
			privurl = "workplan_annex_list.jsp?id=" + id;
		out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", privurl));
	}
	else {
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_success"),"提示"));
	}
	return;
}

WorkPlanMgr wpm = new WorkPlanMgr();
WorkPlanDb wpd = null;
// 由这里来检查权限
try {
	wpd = wpm.getWorkPlanDb(request, id, "see");
	if (!wpd.isLoaded()) {
		out.print(SkinUtil.makeErrMsg(request, "计划不存在！"));
		return;
	}
}
catch (ErrMsgException e) {
	out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String beginDate = DateUtil.format(wpd.getBeginDate(), "yyyy-MM-dd");
String endDate = DateUtil.format(wpd.getEndDate(), "yyyy-MM-dd");

UserMgr um = new UserMgr();

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);
if (isShowNav==1) {
%>
<%@ include file="workplan_show_inc_menu_top.jsp"%>
<script>
o("menu1").className="gantt_tab_sel";
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<%}%>
<form action="workplan_do.jsp?op=modify" method="post" enctype="multipart/form-data" id="form1">
<table id="workplanTable" width="98%" border="0" align="center" cellpadding="2" cellspacing="0" class="tabStyle_1 percent80">
  <tbody>
    <tr>
      <td colspan="2" nowrap="nowrap" class="tabStyle_1_title"><%=wpd.getTitle()%></td>
    </tr>
    <tr height="40px">
      <td width="15%" nowrap="nowrap" class="TableContent">进度：</td>
      <td height="14" class="TableData">
        <table width="50%" border="0" cellpadding="0" cellspacing="0" style="border:0px"><tr><td style="border:0px; padding:0px">
          <div class="progressBar">
            <div class="progressBarFore" style="width:<%=wpd.getProgress()%>%;">
              </div>
            <div class="progressText">
              <%=wpd.getProgress()%>%
              </div>
            </div>
        </td></tr></table></td>
    </tr>
    <tr>
      <td class="TableContent" nowrap="nowrap">剩余天数：</td>
      <td class="TableData">
	  	<%   
		int nowDays = DateUtil.datediff(wpd.getEndDate(), new Date());
		if(nowDays<0){
			// nowDays = 0;
		}
		int sumDays = DateUtil.datediff(wpd.getEndDate(), wpd.getBeginDate());
		float progress =(float)nowDays/sumDays;

		float r23 = (float)2/3;
		if(progress>r23) {
		%>
	    	<img src="../images/green.jpg" width="16" height="18" border="0" />
		<%}else if(progress<r23 && progress>((float)1/3)){%>
			<img src="../images/yel.jpg" width="16" height="18" border="0" />
		<%}else if(progress<((float)1/3) && progress>=0){%>
			<img src="../images/red.jpg" width="16" height="18" border="0" />
        <%}else {%>
			<img src="../images/red_hot.jpg" width="16" height="18" border="0" />
        <%}%>
		<%if (progress<1 && nowDays<0) {%>
			<font color="red">过期<%=-nowDays%>天</font>
		<%} else {%>
        	剩余<%=nowDays%>天
        <%}%>
		&nbsp;(总天数：<%=sumDays%>天)
        </td>
    </tr>
    <tr>
      <td class="TableContent" nowrap="nowrap">内容：</td>
      <td class="TableData"><%=wpd.getContent()%></td>
    </tr>
    <tr>
      <td class="TableContent" nowrap="nowrap">关联流程ID</td>
      <td class="TableData">
	   <%
		 if(wpd.getFlowId()!=0){
			 WorkflowDb wf = new WorkflowDb();
			 wf = wf.getWorkflowDb(wpd.getFlowId());
	   %>
		<a href="javascript:;" onclick="addTab('查看流程', 'flow_modify.jsp?flowId=<%=wpd.getFlowId()%>')"><%=wf.getTitle()%></a>
	  <%}else{%>
	  	无关联
	  <%}%>
	  </td>
    </tr>
    <tr>
      <td class="TableContent" nowrap="nowrap">有效期：</td>
      <td class="TableData">开始日期：
           <%=beginDate%>&nbsp;&nbsp;&nbsp;&nbsp;结束日期： 
        <%=endDate%></td>
    </tr>
    <tr>
      <td class="TableContent" nowrap="nowrap">类型：</td>
      <td class="TableData">
	  <%
	  WorkPlanTypeDb wptd = new WorkPlanTypeDb();
	  wptd = wptd.getWorkPlanTypeDb(wpd.getTypeId());
	  %>
	  <%=wptd.getName()%>	  </td>
    </tr>
    <tr>
      <td class="TableContent" nowrap="nowrap">发布范围（部门）：</td>
      <td class="TableData">
	  <%
	  String[] arydepts = wpd.getDepts();
	  String[] aryusers = wpd.getUsers();
	  String depts = "";
	  String deptNames = "";
	  String users = "";
	  
	  int len = 0;
	  if (arydepts!=null) {
	  	len = arydepts.length;
		DeptDb dd = new DeptDb();
	  	for (int i=0; i<len; i++) {
			if (depts.equals("")) {
				depts = arydepts[i];
				dd = dd.getDeptDb(arydepts[i]);
				deptNames = dd.getName();
			}
			else {
				depts += "," + arydepts[i];
				dd = dd.getDeptDb(arydepts[i]);
				deptNames += "，" + dd.getName();
			}
		}
	  }
	  
	  if (aryusers!=null) {
	  	len = aryusers.length;
	  	for (int i=0; i<len; i++) {
			UserDb user = um.getUserDb(aryusers[i]);
			if (users.equals("")) {
				users = "<a href='javascript:;' onclick=\"addTab('用户信息', 'user_info.jsp?userName=" + StrUtil.UrlEncode(user.getName()) + "')\">" + user.getRealName() + "</a>";
			}
			else {
				users += "，" + "<a href='javascript:;' onclick=\"addTab('用户信息', 'user_info.jsp?userName=" + StrUtil.UrlEncode(user.getName()) + "')\">" + user.getRealName() + "</a>";
			}
		}
	  }
	  
	  String[] principalAry = wpd.getPrincipals();
	  len = principalAry.length;
	  String principals = "";
	  for (int i=0; i<len; i++) {
	  	if (principalAry[i].equals(""))
			continue;
	  	UserDb user = um.getUserDb(principalAry[i]);
		if (principals.equals(""))
			principals = "<a href='javascript:;' onclick=\"addTab('用户信息', 'user_info.jsp?userName=" + StrUtil.UrlEncode(user.getName()) + "')\">" + user.getRealName() + "</a>";
		else
			principals += "，" + "<a href='javascript:;' onclick=\"addTab('用户信息', 'user_info.jsp?userName=" + StrUtil.UrlEncode(user.getName()) + "')\">" + user.getRealName() + "</a>";
	  }
	  %>
	  <%=deptNames%>
        &nbsp;</td>
    </tr>
    <tr>
      <td class="TableContent" nowrap="nowrap">参与人：</td>
      <td class="TableData">
          <%=users%>
        &nbsp;</td>
    </tr>
    <tr>
      <td class="TableContent" nowrap="nowrap">负责人：</td>
      <td class="TableData">
          <%=principals%>
        &nbsp;</td>
    </tr>
    <tr>
      <td class="TableContent" nowrap="nowrap">创建者：</td>
      <td class="TableData"><a href="javascript:;" onclick="addTab('用户信息', 'user_info.jsp?userName=<%=StrUtil.UrlEncode(wpd.getAuthor())%>')"><%=um.getUserDb(wpd.getAuthor()).getRealName()%></a></td>
    </tr>
    <tr>
      <td class="TableContent" nowrap="nowrap">审核状态：</td>
      <td class="TableData">
      <%if (wpd.getCheckStatus()==WorkPlanDb.CHECK_STATUS_NOT) {%>
      未审
      <%}else{%>
      已审
      <%}%>
      </td>
    </tr>
    <tr>
      <td class="TableContent" nowrap="nowrap">备注：</td>
      <td class="TableData"><%=wpd.getRemark()%></td>
    </tr>
    
    <tr class="TableControl" align="middle">
      <td colspan="2" align="left" nowrap="nowrap">
    附件：
	  <%
      java.util.Iterator attir = wpd.getAttachments().iterator();
      while (attir.hasNext()) {
        Attachment att = (Attachment)attir.next();
      %>
        <div><img src="../images/attach2.gif" width="17" height="17" />&nbsp;<a target="_blank" href="workplan_getfile.jsp?workPlanId=<%=wpd.getId()%>&amp;attachId=<%=att.getId()%>"><%=att.getName()%></a>&nbsp;&nbsp;&nbsp;<a href="workplan_do.jsp?op=delattach&amp;workPlanId=<%=wpd.getId()%>&amp;attachId=<%=att.getId()%>"></a></div>
        <%}%>
      </td>
    </tr>
    <%
	com.redmoon.oa.workplan.Privilege pvg = new com.redmoon.oa.workplan.Privilege();
	if (pvg.canUserManageWorkPlan(request, id)) {
	%>
    <tr class="TableControl" align="middle">
      <td colspan="2" align="center" nowrap="nowrap">
      <input type="button" class="btn" value="修改" onclick="window.location.href='workplan_edit.jsp?id=<%=id%>'" />
      </td>
    </tr>
    <%}%>
  </tbody>
</table>
</form>
</body>
</html>
