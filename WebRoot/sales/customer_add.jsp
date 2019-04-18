<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=8">
<meta name="renderer" content="ie-stand">
<title>添加客户</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<%@ include file="customer_inc_menu_top.jsp"%>
<script>
document.getElementById("menu2").className="current"; 
</script>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="sales.user";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

RequestAttributeElement rae = new RequestAttributeElement();
RequestAttributeMgr ram = new RequestAttributeMgr();
String userName = ParamUtil.get(request, "userName");
ram.addAttribute(request, rae.createHidden("userName", userName));

ram.addAttribute(request, rae.createHidden(RequestAttributeElement.NAME_FORWARD, "sales/customer_list.jsp?userName=" + StrUtil.UrlEncode(userName)));
%>
<div class="spacerH"></div>
<%@ include file="../visual_add.jsp"%>
</body>
<script>
$(function(){
	$("input[name='customer']").change(function(){
		checkName();
	})
});

function checkName(){
	var name = $("input[name='customer']").val();
	ajaxCheckName(name);
}

function ajaxCheckName(name) {
	$.ajax({
		type: "post",
		url: "customer_check_name_ajax.jsp",
		data : {
			op: "checkName",
        	name:name
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			var re = $.parseJSON(data);
			if (re.ret=="0") {
			    $("#errMsg").remove();
				$("input[name='customer']").after("<span id='errMsg' style='color:#CC0000;font-weight:bolder;'>客户名称 已存在</span>");
				$("input[name='customer']").focus();
				$("input[name='Submit']").attr('disabled',true);
			}else{
			    $("#errMsg").remove();
				$("input[name='Submit']").attr('disabled',false);
			}		
		},
		complete: function(XMLHttpRequest, status){
			//HideLoading();
		},
		error: function(){
			//请求出错处理
		}
	});	
} 
</script>
</html>
