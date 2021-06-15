<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="cn.js.fan.util.DateUtil"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="java.sql.SQLException"%>
<%@page import="cn.js.fan.db.ResultIterator"%>
<%@page import="cn.js.fan.db.ResultRecord"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<%@page import="cn.js.fan.util.NumberUtil"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<%@page import="com.redmoon.oa.pvg.Privilege"%>
<%@page import="com.redmoon.oa.monitor.RunStatusUtil"%>

<%!
	private RunStatusUtil statusUtil = new RunStatusUtil();
	%>
<%
	String userName = "admin";
	String sso = ParamUtil.get(request, RunStatusUtil.ssoParm);
	com.redmoon.oa.sso.Config cfg = new com.redmoon.oa.sso.Config();
	String key = cfg.get("key");
	JSONObject returnJson = null;
	Map<String,Date> returnValue = statusUtil.getQueryDateCondition(request);
	Date startDate = returnValue.get(RunStatusUtil.beginDateParm);
	Date endDate = returnValue.get(RunStatusUtil.endDateParm);
	
	String errorMessageValue = "";
	boolean errorValue = false;
	if (sso == null || !sso.equals(key)) {
		errorMessageValue = "SSO KEY ERROR";
		errorValue = true;
	}
	if (!errorValue) {
		returnJson = statusUtil.statUsedStatusTOJSONObject(startDate,endDate,userName);
	} else {
		returnJson = statusUtil.createDefaultJSONObject();
		returnJson.put(RunStatusUtil.error,errorValue);
		returnJson.put(RunStatusUtil.errorMessage,errorMessageValue);
	}
	
	out.println(returnJson);
%>