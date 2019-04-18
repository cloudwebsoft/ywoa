<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("getActions")) {
	String flowString = ParamUtil.get(request, "flowString");
	String internalName = ParamUtil.get(request, "internalName");
	String str = "";
	WorkflowDb wf = new WorkflowDb();
	Vector v = wf.getActionsFromString(flowString);
	Iterator ir = v.iterator();
	while (ir.hasNext()) {
		WorkflowActionDb wa = (WorkflowActionDb)ir.next();
		// 过滤掉当前节点
		if (!wa.getInternalName().equals(internalName))
			str += "<option value='" + wa.getInternalName() + "'>" + wa.getJobName() + "：" + wa.getTitle() + "</option>";
	}
	out.print(str);
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<TITLE>流程动作选择</TITLE>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="cfg" scope="page" class="com.redmoon.oa.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script language="JavaScript">
function sel() {
	dialogArguments.setAction(o("action").value, $("#action").find("option:selected").text());
	window.close();
}

function window_onload() {
	var flowStr = dialogArguments.getFlowString();
	var curInternalName = dialogArguments.getInternalName();
	$.ajax({
		type: "post",
		url: "flow_designer_action_sel.jsp",
		data : {
			op: "getActions",
			internalName: curInternalName,
			flowString: flowStr
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			$("#action").html(data);
		},
		complete: function(XMLHttpRequest, status){
			// HideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});	
}
</script>
</HEAD>
<BODY onLoad="window_onload()">
<table align="center" cellpadding="2" cellspacing="0" class="tabStyle_1" id="mainTable" style="padding:0px; margin:0px; margin-top:3px;">
  <thead>
  <tr>
    <td height="22" align="center">选择动作</td>
  </tr>
  </thead>
  <tr>
    <td height="22" align="center">
    <select id="action" name="action">
    </select>
    </td>
  </tr>
  <tr>
    <td height="22" align="center">
    <input type="button" value="确定" onclick="sel()" />
    </td>
  </tr>
</table>
</BODY>
</HTML>