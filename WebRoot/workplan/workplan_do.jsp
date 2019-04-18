<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "com.cloudwebsoft.framework.base.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String action = ParamUtil.get(request,"action");
String priv="workplan";
if (privilege.isUserPrivValid(request, priv))
	;
else {
	// 防止因未分配计划权限，而ajax处理时在parseJSON的时候报异常，如workplan_task中评价时
	// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}

WorkPlanMgr am = new WorkPlanMgr();
boolean re = false;
String op = ParamUtil.get(request, "op");

if (op.equals("unfavorite")) {
	JSONObject json = new JSONObject();
	WorkPlanFavoriteDb wpfd = new WorkPlanFavoriteDb();
	try {
		long workplanId = ParamUtil.getLong(request, "workplanId");		
		wpfd = wpfd.getWorkPlanFavoriteDb(privilege.getUser(request), new Long(workplanId));
		if (wpfd!=null)
			re = wpfd.del();
		else
			throw new ErrMsgException("该记录已不存在！");
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
else if (op.equals("checkAnnex")) {
	JSONObject json = new JSONObject();
	
	long id = ParamUtil.getLong(request, "id");
	WorkPlanAnnexDb wpad = new WorkPlanAnnexDb();
	wpad = (WorkPlanAnnexDb)wpad.getQObjectDb(new Long(id));
	
	com.redmoon.oa.workplan.Privilege pvg = new com.redmoon.oa.workplan.Privilege();
	int workplanId = wpad.getInt("workplan_id");
	if (!pvg.canUserManageWorkPlan(request, workplanId)) {
		json.put("ret", "0");
		json.put("msg", "权限非法！");
		out.print(json);
		return;
	}
	
	int checkStatus = ParamUtil.getInt(request, "check_status");
	String appraise = ParamUtil.get(request, "appraise");
	wpad.set("check_status", new Integer(checkStatus));
	wpad.set("appraise", appraise);
	wpad.set("checker", privilege.getUser(request));
	wpad.set("check_date", new java.util.Date());
	
	re = wpad.save();
	if (re) {
		if (checkStatus==WorkPlanAnnexDb.CHECK_STATUS_PASSED) {
			WorkPlanTaskDb wptd = new WorkPlanTaskDb();
			wptd = (WorkPlanTaskDb)wptd.getQObjectDb(new Long(wpad.getLong("task_id")));
			int progress = ParamUtil.getInt(request, "progress");
			wptd.set("progress", new Integer(progress));
			wptd.save();
		}
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
else if (op.equals("assessTask")) {
	JSONObject json = new JSONObject();
	
	long taskId = ParamUtil.getLong(request, "taskId");
	WorkPlanTaskDb wptd = new WorkPlanTaskDb();
	wptd = (WorkPlanTaskDb)wptd.getQObjectDb(new Long(taskId));
	
	com.redmoon.oa.workplan.Privilege pvg = new com.redmoon.oa.workplan.Privilege();
	int workplanId = wptd.getInt("work_plan_id");
	if (!pvg.canUserManageWorkPlan(request, workplanId)) {
		json.put("ret", "0");
		json.put("msg", "权限非法！");
		out.print(json);
		return;
	}
	
	wptd.set("assess", ParamUtil.getInt(request, "assess"));
	re = wptd.save();
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
else if (op.equals("addTask")) {
	JSONObject json = new JSONObject();
	
	WorkPlanTaskMgr wptm = new WorkPlanTaskMgr();
	try {
		re = wptm.create(request);
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage().replace("\\r", "<BR />"));
		out.print(json);
		e.printStackTrace();
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
else if (op.equals("delTask")) {
	JSONObject json = new JSONObject();
	
	WorkPlanTaskMgr wptm = new WorkPlanTaskMgr();
	try {
		re = wptm.del(request);
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage().replace("\\r", "<BR />"));
		out.print(json);
		e.printStackTrace();
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
else if (op.equals("getTask")) {
	JSONObject json = new JSONObject();
	long taskId = ParamUtil.getLong(request, "taskId");
	WorkPlanTaskDb wptd = new WorkPlanTaskDb();
	wptd = (WorkPlanTaskDb)wptd.getQObjectDb(new Long(taskId));
	if (wptd!=null) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
		json.put("name", wptd.getString("name"));
		json.put("progress", wptd.getString("progress"));
		json.put("start", DateUtil.format(wptd.getDate("start_date"), "yyyy-MM-dd"));
		json.put("end", DateUtil.format(wptd.getDate("end_date"), "yyyy-MM-dd"));
		json.put("resource", wptd.getString("task_resource"));
		json.put("startIsMilestone", wptd.getString("startIsMilestone"));
		json.put("endIsMilestone", wptd.getString("endIsMilestone"));
		json.put("workplan_related", wptd.getInt("workplan_related")==-1?"":wptd.getInt("workplan_related"));
		json.put("status", wptd.getInt("status"));
		json.put("reportFlowType", StrUtil.getNullStr(wptd.getString("report_flow_type")));
		json.put("depends", StrUtil.getNullStr(wptd.getString("depends")));
		WorkPlanTaskDb pwptd = wptd.getParentNode(taskId);
		if (pwptd != null) {
			json.put("parentTaskId", pwptd.getString("id"));
			json.put("parentTaskName", pwptd.getString("name"));
		} else {
			json.put("parentTaskId", 0);
			json.put("parentTaskName", "无");
		}
	}
	else {
		json.put("ret", "0");
		json.put("msg", "操作失败！");
	}	
	out.print(json);
	return;	
}
else if (op.equals("editTask")) {
	JSONObject json = new JSONObject();
	WorkPlanTaskMgr wptm = new WorkPlanTaskMgr();
	try {
		re = wptm.edit(request);
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage().replace("\\r", "<BR />"));
		out.print(json);
		e.printStackTrace();
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
else if (op.equals("move")) {
	JSONObject json = new JSONObject();
	WorkPlanTaskMgr wptm = new WorkPlanTaskMgr();
	try {
		re = wptm.move(request);
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage().replace("\\r", "<BR />"));
		out.print(json);
		e.printStackTrace();
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
else if (op.equals("favorite")) {
	JSONObject json = new JSONObject();
	WorkPlanFavoriteDb wpfd = new WorkPlanFavoriteDb();
	try {
		long workplanId = ParamUtil.getLong(request, "workplanId");
		if (wpfd.isExist(privilege.getUser(request), workplanId)) {
			throw new ErrMsgException("该计划已被关注！");
		}
		re = wpfd.create(new JdbcTemplate(), new Object[]{new Long(workplanId),privilege.getUser(request),new java.util.Date(),new Integer(0)});
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

else if (op.equals("addTaskUser")) {
	JSONObject json = new JSONObject();
	
	WorkPlanTaskUserMgr wptum = new WorkPlanTaskUserMgr();
	try {
		re = wptum.create(request);
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage().replace("\\r", "<BR />"));
		out.print(json);
		e.printStackTrace();
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
else if (op.equals("getTaskUser")) {
	JSONObject json = new JSONObject();
	long taskUserId = ParamUtil.getLong(request, "id");
	WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
	wptud = (WorkPlanTaskUserDb)wptud.getQObjectDb(new Long(taskUserId));
	if (wptud!=null) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
		json.put("user_name", wptud.getString("user_name"));
		json.put("percent", wptud.getInt("percent"));
		json.put("duration", NumberUtil.round(wptud.getDouble("duration"), 1));
	}
	else {
		json.put("ret", "0");
		json.put("msg", "操作失败！");
	}	
	out.print(json);
	return;	
}
else if (op.equals("editTaskUser")) {
	JSONObject json = new JSONObject();
	WorkPlanTaskUserMgr wptum = new WorkPlanTaskUserMgr();
	try {
		re = wptum.edit(request);
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage().replace("\\r", "<BR />"));
		out.print(json);
		e.printStackTrace();
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
else if (op.equals("delTaskUser")) {
	JSONObject json = new JSONObject();
	
	WorkPlanTaskUserMgr wptum = new WorkPlanTaskUserMgr();
	try {
		re = wptum.del(request);
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage().replace("\\r", "<BR />"));
		out.print(json);
		e.printStackTrace();
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
else if(op.equals("remind")){
	JSONObject json = new JSONObject();
	
	WorkPlanMgr wpMgr = new WorkPlanMgr();
	try {
		re = wpMgr.remind(request);
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage().replace("\\r", "<BR />"));
		out.print(json);
		e.printStackTrace();
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

if (op.equals("del")) {
    JSONObject json = new JSONObject();
    try {
        re = am.del(request);
    }
    catch (ErrMsgException e) {
        json.put("ret", "0");
        json.put("msg", e.getMessage().replace("\\r", "<BR />"));
       
        //out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
    }
    if (re) {
        String privurl = ParamUtil.get(request, "privurl");
        if (privurl.equals("")){
            privurl = "workplan_list.jsp";
        }
        json.put("ret", "1");
        json.put("msg", "删除成功！");
        json.put("url", privurl);
       
        //out.print(StrUtil.jAlert_Redirect("删除成功！","提示", privurl));
    }
     out.print(json);
     return;
}
%>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<%


if (op.equals("modify")) {
	int id = ParamUtil.getInt(request, "id");
	try {
		re = am.modify(application, request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re) {
		String strProjectId = am.getFileUpload().getFieldValue("projectId");
		int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);
		if (strProjectId!=null) {
			long projectId = StrUtil.toLong(strProjectId, -1);
			out.print(StrUtil.jAlert_Redirect("修改成功！","提示", "workplan_edit.jsp?id=" + id + "&isShowNav=" + isShowNav + "&projectId=" + projectId));			
		}
		else {
			out.print(StrUtil.jAlert_Redirect("修改成功！","提示", "workplan_edit.jsp?id=" + id + "&isShowNav=" + isShowNav));
		}
	}
	 out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
}
else if (op.equals("delattach")) {
	int planId = ParamUtil.getInt(request, "id");
	WorkPlanDb wpd = new WorkPlanDb();
	wpd = wpd.getWorkPlanDb(planId);
	try {
		re = am.delAttachment(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("删除附件成功！","提示", "workplan_edit.jsp?id=" + planId + "&projectId=" + wpd.getProjectId()));
	}
}

 if (op.equals("add")) {
    try {
        re = am.create(application, request);
    }
    catch (ErrMsgException e) {
        out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
        out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
    }
    if (re) {
        long projectId = StrUtil.toLong(am.getFileUpload().getFieldValue("projectId"), -1);
        if (projectId!=-1) {
            out.print(StrUtil.jAlert_Redirect("添加成功！","提示", "../project/project_workplan_list.jsp?projectId=" + projectId));            
        }
        else{
            if(action.equals("sel")) {
                out.print(StrUtil.jAlert_Redirect("添加成功！","提示", "workplan_list_sel.jsp"));
            }else{
                out.print(StrUtil.jAlert_Redirect("添加成功！","提示", "workplan_list.jsp?op=mine"));
            }
        }
    }
    out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
}
else if (op.equals("addJob")) {
	QObjectMgr qom = new QObjectMgr();
	JobUnitDb ju = new JobUnitDb();
	int id = ParamUtil.getInt(request, "id");
	
	WorkPlanDb wpd = new WorkPlanDb();
	wpd = wpd.getWorkPlanDb(id);
		
	try {
		if (qom.create(request, ju, "scheduler_add")) {
			int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);	
			out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "workplan_edit.jsp?id=" + id + "&isShowNav=" + isShowNav + "&projectId=" + wpd.getProjectId()));
		}
		else
			out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_fail"),"提示"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
}
else if (op.equals("editJob")) {
	QObjectMgr qom = new QObjectMgr();
	int planId = ParamUtil.getInt(request, "planId");
	WorkPlanDb wpd = new WorkPlanDb();
	wpd = wpd.getWorkPlanDb(planId);
	int jobId = ParamUtil.getInt(request, "id");
	JobUnitDb ju = new JobUnitDb();
	ju = (JobUnitDb)ju.getQObjectDb(new Integer(jobId));
	try {
	if (qom.save(request, ju, "scheduler_edit")) {
		int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);	
		out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "workplan_edit.jsp?id=" + planId + "&isShowNav=" + isShowNav + "&projectId=" + wpd.getProjectId()));
	}
	else
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_fail"),"提示"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
	}
	out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
}
else if (op.equals("delJob")) {
	JobUnitDb jud = new JobUnitDb();
	int delid = ParamUtil.getInt(request, "id");
	int planId = ParamUtil.getInt(request, "planId");
	WorkPlanDb wpd = new WorkPlanDb();
	wpd = wpd.getWorkPlanDb(planId);
	JobUnitDb ldb = (JobUnitDb)jud.getQObjectDb(new Integer(delid));
	if (ldb.del()) {
		int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);
		out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "workplan_edit.jsp?id=" + planId + "&isShowNav=" + isShowNav + "&projectId=" + wpd.getProjectId()));
	}
	else
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_fail"),"提示"));
}

%>