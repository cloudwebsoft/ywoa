<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="com.redmoon.oa.sms.SMSFactory"%>
<%@page import="com.redmoon.oa.ui.SkinMgr"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="com.redmoon.oa.sms.SMSRemindMgr"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

<%
	String priv="admin";
	if (!privilege.isUserPrivValid(request,priv)) {
	    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
 %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    
    <title>短信配额提醒设置</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<script src="../inc/common.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<%
		String op = ParamUtil.get(request,"op");
		if(op.equals("modify")){
			SMSRemindMgr srdMgr = new SMSRemindMgr();
			srdMgr.save(request);
			out.println(StrUtil.Alert_Redirect("设置成功！","sms_remind_m.jsp"));
			return;
		}
	 %>

  </head>
  <body>
  <%@ include file="sms_inc_menu_top_boundary.jsp"%>  
  <script>
$("menu3").className="current";
</script>
  <div class="spacerH"></div>
	<%
		SMSRemindMgr srdMgr = new SMSRemindMgr();
	 %>
	 <br/>
	 <form action="" method="post" name="myfrom" onsubmit="return check()">
	 <table cellpadding="0" cellspacing="0" width="80%" class="tabStyle_1 percent80" align="center">
	 	<tr>
	 		<td colspan="2" class="tabStyle_1_title">短信配额提醒设置</td>
	 	</tr>
	 	<tr>
	 		<td>
	 			短信预警
	 			<input type="hidden" name="boundaryType" id="boundaryType" value="<%=boundaryType%>" />
	 			<input type="hidden" name="op" value="modify" />
	 		</td>
	 		<td>
	 			<input type="text" name="<%=(boundaryType==com.redmoon.oa.sms.Config.SMS_BOUNDARY_YEAR)?"boundaryYear":"boundaryMonth"%>" id="remindBoundary" value="<%=srdMgr.getBoundary(boundaryType)%>" />（当剩余短信条数少于预警条数时提醒。）
	 		</td>
	 	</tr>
	 	<tr>
	 		<td>提醒标题</td>
	 		<td align="left">
	 			<input type="text" size="30" name="title" id="title" value="<%=srdMgr.getTitle(boundaryType)==null?"":srdMgr.getTitle(boundaryType) %>" />
	 		</td>
	 	</tr>
	 	<tr>
	 		<td>提醒内容</td>
	 		<td align="left">
	 			<textarea name="<%=(boundaryType==com.redmoon.oa.sms.Config.SMS_BOUNDARY_YEAR)?"boundaryYearContent":"boundaryMonthContent"%>" id="<%=(boundaryType==com.redmoon.oa.sms.Config.SMS_BOUNDARY_YEAR)?"boundaryYearContent":"boundaryMonthContent"%>" rows="3" cols="80"><%=srdMgr.getContent(boundaryType)==null?"":srdMgr.getContent(boundaryType)%></textarea>
	 		</td>
	 	</tr>
	 </table>
	 <div align="center">
	 	<input type="submit" class="btn" value="确定" />
	 </div>
	 </form>
  </body>
  <script>
  	function check(){
  		var remindBoundary = document.getElementById("remindBoundary").value;
  		if(!checkNum(remindBoundary)){
  			alert("短信预警条数必须为正整数！");
  			return false;
  		}
  		return true;
  	}
  	function checkNum(value)
	{
		var re = /^[1-9]\d*$/;
	     if (!re.test(value)){
	        return false;
	     }
	     return true;
	}
  </script>
</html>
