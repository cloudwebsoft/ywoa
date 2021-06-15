<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="org.json.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.fileark.plugin.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@page import="com.redmoon.oa.exam.MajorView"%>
<%@page import="com.redmoon.oa.exam.SubjectDb"%>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.basic.TreeSelectMgr"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>目录维护</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
<%
	String root_code =MajorView.ROOT_CODE;
	String name = ParamUtil.get(request,"name");
	String code = ParamUtil.get(request,"code");
	String parentCode = ParamUtil.get(request,"parentCode");
	String op = ParamUtil.get(request,"op");
	TreeSelectDb tsd = new TreeSelectDb();
	tsd = tsd.getTreeSelectDb(code);
 %>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1"><span class="thead" style="PADDING-LEFT: 10px"><%=name %>修改</span></td>
    </tr>
  </tbody>
</table>
<form action="" method="post" name="form1" target="dirhidFrame" id="form1" onsubmit="return form1_onsubmit()">
	<table align="center" class="tabStyle_1 percent80" >
		<tr>
			<td width="120" rowspan="7" align="left" valign="top" style="word-break:break-all"><br />
			    当前节点：<br />
			<font color="blue" id="parent_name"><%=name %></font> </td>
				<td align="left" id='codeText'>编码：<span id="span_code"><%=code %></span></td>
				<input type="hidden" name="code" id="code" value="<%=code %>"/>
				<input type="hidden" name="parentCode" id="parentCode" value="<%=parentCode %>"/>
		</tr>
		<tr>
			<td align="left">名称：
				<input type="hidden" name="op" id="op" />
				<input name="name" id="name" size=12/>
				<input type="hidden" name="parent_code" id="parent_code" value="<%=code %>"/>
				<input type="hidden" name="root_code" id="root_code" /></td>
		</tr>
		<tr>
			<td >描述：
				<input type="text" id="description" name="description" value="<%=tsd.getDescription() %>"/>
			</td>
		</tr>
		<tr>
			<td align="center" colspan="2">
		  	<input type="button" class="btn" onclick="mysubmit()" value="提交" />&nbsp;&nbsp;&nbsp;<input type="button" class="btn" onclick="javascript :history.back(-1);" value="返回" />      </td>
		</tr>
	</table>
</form>
<script>
function ajaxPost(path,parameter,func){
	$.ajax({
		type: "post",
		url: path,
		data: parameter,
		dataType: "html",
		contentType:"application/x-www-form-urlencoded; charset=iso8859-1",			
		success: function(data, status){
			func(data);
		},
		error: function(XMLHttpRequest, textStatus){
			alert(XMLHttpRequest.responseText);
		}
	});
}
function mysubmit(){
	var code = "<%=code%>";
	var formData = $("#form1").serialize();
	ajaxPost('../question/questionKindEdit.do',formData,function(data){
		data = $.parseJSON(data);
		jAlert(data.msg,"提示");
	});
}
$(function(){
	o("name").value ="<%=name%>";
	o("subject").value ="<%=tsd.getDescription()%>";
})
  </script>
</body>
</html>
