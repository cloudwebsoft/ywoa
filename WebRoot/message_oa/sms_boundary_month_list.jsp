<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="com.redmoon.oa.sms.SMSFactory"%>
<%@page import="com.redmoon.oa.sms.SMSBoundaryMonthMgr"%>
<%@page import="com.redmoon.oa.ui.SkinMgr"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    
    <title>设置短信月配额</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<script src="../inc/common.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />

  </head>
  <%
  	String priv="admin";
	if (!privilege.isUserPrivValid(request,priv)) {
	    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
  	String op = ParamUtil.get(request,"op");
  	SMSBoundaryMonthMgr sbmMgr = new SMSBoundaryMonthMgr();
  	if(op.equals("modify")){
  		sbmMgr.save(request);
  		out.print(StrUtil.Alert_Redirect("保存设置成功！","sms_boundary_month_list.jsp"));
  		return;
  	}
   %>
  <body>
  <%@ include file="sms_inc_menu_top_boundary.jsp"%>
  <script>
$("menu2").className="current";
</script>
<br/>
<br/>
<br/>
    <form action="sms_boundary_month_list.jsp" method="post" onsubmit="return check()">
    	<table cellpadding="0" cellspacing="0" class="tabStyle_1 percent80" width="80%" align="center">
    		<tr>
    			<td class="tabStyle_1_title" colspan="4">设置短信月配额</td>
    		</tr>
    		<tr>
    			<td>配额</td>
    			<td colspan="3"> 
    				<input type="hidden" name="op" id="op" value="modify">
    				<input type="text"  name="total" id="total" value="<%=sbmMgr.getTotal()%>" /><font color="#FF0000">*</font>
    			</td>
    		</tr>
    	</table>
    	<div align="center">
    		<input type="submit" name="submit" id="submit" class="btn" value="确定">
    	</div>
    </form>
  </body>
  <script type="text/javascript">
  	function checkNum(value)
	{
		var re = /^[1-9]\d*$/;
	     if (!re.test(value)){
	        return false;
	     }
	     return true;
	}
  	function check(){
  		var total = document.getElementById("total").value;
  		if(total==""){
  			alert("配额不能为空！");
  			return false;
  		}
  		if(!checkNum(total)){
  			alert("配额必须为正整数！");
  			return false;
  		}
  		return true;
  	}
  </script>
</html>
