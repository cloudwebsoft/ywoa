<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="com.redmoon.oa.ui.SkinMgr"%>
<%@page import="com.redmoon.oa.sms.SMSFactory"%>
<%@page import="com.redmoon.oa.sms.Config"%>
<%@page import="com.redmoon.oa.sms.SMSBoundaryYearMgr"%>
<%@page import="cn.js.fan.util.DateUtil"%>
<%@page import="com.redmoon.oa.sms.SMSBoundaryMonthMgr"%>
<%@page import="cn.js.fan.web.SkinUtil"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

<%
	String priv="admin";
	if (!privilege.isUserPrivValid(request,priv)) {
	    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
 %>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("set")) {
	com.redmoon.oa.sms.Config cfg = new com.redmoon.oa.sms.Config();
	boolean isUsed = ParamUtil.get(request, "isUsed").equals("true");
	cfg.setIsUsed(isUsed);
	int defaultBoundary = ParamUtil.getInt(request, "defaultBoundary");
	cfg.setBoundary(defaultBoundary);
	//com.redmoon.oa.sms.SMSFactory.init();
	out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "sms_boundary_show.jsp"));
	return;
}/*else if(op.equals("setSmsBoundary")){
	com.redmoon.oa.sms.Config cfg = new com.redmoon.oa.sms.Config();
	int defaultBoundary = ParamUtil.getInt(request, "defaultBoundary");
	cfg.setBoundary(defaultBoundary);
	com.redmoon.oa.sms.SMSFactory.init();
	out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "sms_boundary_show.jsp"));
	return;
}*/
 %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    
    <title>短信配额信息</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<script src="../inc/common.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />

  </head>
  <body>
  <%@ include file="sms_inc_menu_top_boundary.jsp"%>
    <script>
$("menu1").className="current";
</script>
<%
  	int total = 0;
  	int used = 0;
  	int remain = 0;
  	if(boundaryType==Config.SMS_BOUNDARY_YEAR){
  		SMSBoundaryYearMgr sbyMgr = new SMSBoundaryYearMgr();
  		total = sbyMgr.getTotal();
  		used = sbyMgr.getUsedCount();
  		remain = sbyMgr.getRemainingCount();
  	}else if(boundaryType==Config.SMS_BOUNDARY_MONTH){
  		int month = DateUtil.getMonth(new Date());
  		SMSBoundaryMonthMgr sbmMgr = new SMSBoundaryMonthMgr();
  		total = sbmMgr.getTotal();
  		used = sbmMgr.getUsedCount(month);
  		remain = sbmMgr.getRemainingCount(month);
  	}
   %>
<div class="spacerH"></div><br/>
	<form method="post" id="form1" name="form1" action="sms_boundary_show.jsp?op=set">
	<table cellpadding="0" cellspacing="0" class="tabStyle_1 percent80" width="80%" align="center">
		<tr>
			<td class="tabStyle_1_title" colspan="2">短信配额信息</td>
		</tr>
          <tr class="highlight">
            <td>&nbsp;短信启用</td>
			<td>
				<select name="isUsed">
			<option value="true">是</option>
			<option value="false">否</option>
			</select>
			</td>
          </tr>
		  <script>
		  form1.isUsed.value = "<%=com.redmoon.oa.sms.SMSFactory.isUseSMS()?"true":"false"%>";
		  </script>
          <tr class="highlight">
            <td>&nbsp;当前配额类型</td>
			<td><select name="defaultBoundary">
			<option value="0">无</option>
			<option value="1">按年配额</option>
			<option value="2">按月配额</option>
			</select></td>
          </tr>
		  <script>
		  form1.defaultBoundary.value = "<%=com.redmoon.oa.sms.SMSFactory.getBoundary()%>";
		  </script>
		<tr class="highlight">
			<td>&nbsp;配额数量</td>
			<td><%=total%></td>
		</tr>
		<tr class="highlight">
			<td>&nbsp;已经使用数量</td>
			<td><%=used%></td>
		</tr>
		<tr class="highlight">
			<td>&nbsp;剩余短信</td>
			<td><%=remain%></td>
		</tr>
	</table>
	<div align="center">
		<input type="submit" name="submit" id="submit" class="btn" value="确定" />
	</div>
	</form>
  </body>
</html>
