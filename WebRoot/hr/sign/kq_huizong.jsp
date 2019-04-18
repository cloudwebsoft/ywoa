<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import = "com.redmoon.oa.hr.SignMgr"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.util.StrUtil"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="com.redmoon.oa.visual.FormDAO"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<% 
	if (!privilege.isUserPrivValid(request, "kaoqin.admin")) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	String op = ParamUtil.get(request,"op");
	SignMgr kq = new SignMgr();
	String[] preDate = kq.getPreMonDate();            //获取上一个月的开始时间和结束时间
	String begindate = preDate[0];                    //上个月的第一天
	String enddate = preDate[1];                      //上个月的最后一天
	if("add".equals(op)){

	}
	
	//kx.getInformation(); 
 %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../../inc/common.js"></script>
<script type="text/javascript" src="../../js/jquery.js"></script>
<script src="../../js/jquery.js"></script>
<script type="text/javascript" src="../../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../../js/datepicker/jquery.datetimepicker.css"/>
<script src="../../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<link href="../../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../../js/jquery-showLoading/jquery.showLoading.js"></script>
<title>考勤汇总</title>
</head>
<body id="showLoading">
	<table cellSpacing="0" cellPadding="0" width="100%" >
	  <tbody>
	    <tr>
	      <td class="tdStyle_1">考勤汇总</td>
	    </tr>
	  </tbody>
	</table>
    <br>
	<form name="form1" action="kq_huizong.jsp?op=add" method="post" >
		<table border="0" align="center" cellspacing="0" class="tabStyle_1 percent98" style="width:600px">
			<thead>
		    <tr>
		      <td class="tabStyle_1_title" style="width:200px">&nbsp;请选择开始日期</td>
		      <td class="tabStyle_1_title" style="width:200px">&nbsp;请选择结束日期</td>
		       <td class="tabStyle_1_title" style="width:200px">&nbsp;提交</td>
		    </tr>
		    </thead>
		    <tr>
		      <td  align="center">
		     	 <input id="begin_date" name="begin_date" readonly type="text" value="<%=begindate%>"/>
			  </td>
			  <td align="center">
			  <input id="end_date" name="end_date" readonly type="text" value="<%=enddate%>"/>
			  </td>
			  <td align="center"><input type="button" class="btn" value="确定" onclick="submitForm()"></td>
		    </tr>
		</table>
	</form>
	<div style="padding-top: 30px; width:600px; margin:0px auto"><span class="LV_presence">*</span>&nbsp;&nbsp;注：<br/>考勤汇总前请确保工作日历已初始化完成。<br/>考勤机中的考勤编号和用户的员工编号相对应。</div>
</body>
<script type="text/javascript">
$(function(){
	$('#begin_date').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d'
    });
    $('#end_date').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d'
    });
})
	function submitForm(){
		var beginDate = $("#begin_date").val();
		var endDate = $("#end_date").val();
		if (beginDate==""||endDate==""){
			jAlert("请选择开始日期及结束日期!", "提示");
			return ;
		}
		$.ajax({
			type: "post",
			url: "../../attendance/dataCollect.do",
			data: {
				begin_date: beginDate,
				end_date: endDate
			},
			dataType: "html",
			beforeSend: function (XMLHttpRequest) {
				$('body').showLoading();
			},
			success: function (data, status) {
				data = $.parseJSON(data);
				if (data.ret == 0) {
					jAlert(data.msg, "提示");
				} else {
					jAlert(data.msg, "提示", function () {
						window.location.href = "kq_arrange_list.jsp?begin_date=" + beginDate + "&end_date=" + endDate;
					});
				}
			},
			complete: function (XMLHttpRequest, status) {
				$('body').hideLoading();
			},
			error: function (XMLHttpRequest, textStatus) {
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
	}
</script>
</html>