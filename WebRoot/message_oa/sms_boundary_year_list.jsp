<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="com.redmoon.oa.ui.SkinMgr"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="com.redmoon.oa.sms.SMSFactory"%>
<%@page import="com.redmoon.oa.sms.SMSBoundaryYearMgr"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<%@page import="cn.js.fan.util.DateUtil"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    
    <title>设置短信年配额</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<script src="../inc/common.js"></script>
	<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
	<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
	<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
	<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />

  </head>
  <%
  	String priv="admin";
	if (!privilege.isUserPrivValid(request,priv)) {
	    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
   %>
  <% 
  	SMSBoundaryYearMgr sbyMgr = new SMSBoundaryYearMgr();
  	String op = ParamUtil.get(request,"op");
  	if(op.equals("modify")){
  		
  		/*if(!sbyMgr.save(request)){
  			out.print(StrUtil.Alert_Back("保存设置失败！"));
  		}*/
  		sbyMgr.save(request);
  		out.print(StrUtil.Alert_Redirect("保存设置成功！","sms_boundary_year_list.jsp"));
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
    <form action="sms_boundary_year_list.jsp" onSubmit="return check()">
    	<table width="80%" cellpadding="0" cellspacing="0" class="tabStyle_1 percent80" align="center">
    		<tr>
    			<td class="tabStyle_1_title" colspan="2">设置短信年配额<input type="hidden" id="op" name="op" value="modify"></td>
    		</tr>
    		<!-- <tr>
    			<td class="tabStyle_1_title">套餐名称</td>
    			<td>
    				<input type="text" id="name" name="name" value="sbyDb.getString("name")==null?"":sbyDb.getString("name")"/><font color="#FF0000">*</font>
    				
    			</td>
    		</tr> -->
    		<tr>
    			<td>开始日期</td>
    			<td>
    				<input readonly type="text" id="beginDate" name="beginDate" value="<%=sbyMgr.getBeginDate()==null?"":DateUtil.format(sbyMgr.getBeginDate(),"yyyy-MM-dd")%>" onChange="getEndDate()">
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "beginDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
</script><font color="#FF0000">*</font>
    			</td>
    		</tr>
    		<tr>
    			<td>结束日期</td>
    			<td>
    				<input readonly type="text" id="endDate" name="endDate" value="<%=sbyMgr.getEndDate()==null?"":DateUtil.format(sbyMgr.getEndDate(),"yyyy-MM-dd")%>">
    			</td>
    		</tr>
    		<tr>
    			<td>配额</td>
    			<td>
    				<input type="text" id="total" name="total" value="<%=sbyMgr.getTotal()%>"/><font color="#FF0000">*</font>
    			</td>
    		</tr>
    	</table>
    	<div align="center">
    		<input type="submit" name="submit" value="确定" class="btn">
    	</div>
    </form>
  </body>
  <script type="text/javascript">
  	function getEndDate(){
  		var value = document.getElementById("beginDate").value;
  		var temp = value.split('-');
  		var year = parseInt(temp[0])+1;
  		var value2 = year+"-"+temp[1]+"-"+temp[2];
  		document.getElementById("endDate").value=value2;
  	}
  	
  	function check(){
  		if(document.getElementById("beginDate").value==""){
  			alert("开始日期不能为空！");
  			return false;
  		}
  		var total = document.getElementById("total").value;
  		if(total==""){
  			alert("年短信条数不能为空！");
  			return false;
  		}
  		if(!checkNum(total)){
  			alert("年短信条数必须为正整数！");
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
