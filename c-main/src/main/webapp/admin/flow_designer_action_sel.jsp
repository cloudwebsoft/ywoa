<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.JSONObject" %>
<%
String op = ParamUtil.get(request, "op");
if ("getActions".equals(op)) {
	String flowJson = ParamUtil.get(request, "flowJson");
	WorkflowActionDb wad = new WorkflowActionDb();
	String internalName = ParamUtil.get(request, "internalName");
	String str = "";
    JSONObject flowJsonObject = new JSONObject(WorkflowActionDb.tran(flowJson));
    JSONObject stateJsonObject = flowJsonObject.getJSONObject("states");
    Iterator ir = stateJsonObject.keys();
    while (ir.hasNext()) {
        String key = (String) ir.next();
        JSONObject state = stateJsonObject.getJSONObject(key);
        if (!state.getString("ID").equals(internalName)) {
            JSONObject props = state.getJSONObject("props");
            String title = wad.tranReverseForFlowJson(props.getJSONObject("ActionTitle").getString("value"));
            String jobName = wad.tranReverseForFlowJson(props.getJSONObject("ActionJobName").getString("value"));
            str += "<option value='" + state.getString("ID") + "'>" + jobName + "：" + title + "</option>";
        }
    }
	out.print(str);
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>流程动作选择</title>
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
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script language="JavaScript">
function sel() {
	window.opener.setAction(o("action").value, $("#action").find("option:selected").text());
	window.close();
}

function window_onload() {
	var flowJson = window.opener.getFlowJson();
	var curInternalName = window.opener.getInternalName();
	$.ajax({
		type: "post",
		url: "flow_designer_action_sel.jsp",
		data : {
			op: "getActions",
			internalName: curInternalName,
            flowJson: flowJson
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
</head>
<body onLoad="window_onload()">
<table align="center" cellpadding="2" cellspacing="0" class="tabStyle_1 percent98" id="mainTable" style="margin-top:3px;">
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
</body>
</HTML>