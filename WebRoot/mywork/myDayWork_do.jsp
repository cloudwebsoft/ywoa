<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="cn.js.fan.db.ResultIterator"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<%@page import="com.redmoon.oa.flow.FormDb"%>
<%@page import="com.redmoon.oa.visual.FormDAO"%>
<%@page import="com.redmoon.oa.worklog.WorkLogForModuleDb"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page import="cn.js.fan.util.ResKeyException"%>
<%@page import="cn.js.fan.db.ResultRecord"%>
<%@page import="com.redmoon.oa.worklog.*"%>
<%
	String code = ParamUtil.get(request,"code");
	int workLogId = ParamUtil.getInt(request,"workLog",0);
	int prjId = ParamUtil.getInt(request,"prjId",0);
	int process = ParamUtil.getInt(request,"process",0);
	boolean re = false;
	if(code.equals("prj")){
		WorkLogForModuleMgr wm = new WorkLogForModuleMgr();
		FormDb fd = new FormDb(code);
		FormDAO fdao = new FormDAO(prjId, fd);  //prjId 这里是项目任务单Id
		int processOld = StrUtil.toInt(fdao.getFieldValue("task_progress"), 0);
		// 如果进度为100%，则不再调用，以免反复触发延迟接收流程，如果可以调用，则逻辑不严谨
		if (processOld!=100) {				
			wm.updatePrj(code,workLogId,prjId,process, MyWorkManageAction.TYPE_DAY, true);
		}
		response.sendRedirect(request.getContextPath()+"/ymoa/myWorkManageInit.action?code="+code+"&id="+prjId);
	}else if(code.equals("prj_task")){
		FormDb fd = new FormDb(code);
		FormDAO fdao = new FormDAO(prjId,fd);  //prjId 这里是项目任务单Id
		int processOld = StrUtil.toInt(fdao.getFieldValue("task_progress"), 0);
		// 如果任务进度为100%，则不再调用，以免反复触发延迟接收流程，如果可以调用，则逻辑不严谨
		if (processOld<100) {
			WorkLogForModuleMgr wm = new WorkLogForModuleMgr();
			wm.updatePrjTask(code,workLogId,prjId,process, MyWorkManageAction.TYPE_DAY, true);
		}
				
		response.sendRedirect(request.getContextPath()+"/ymoa/myWorkManageInit.action?code="+code+"&id="+prjId);
	}
%>