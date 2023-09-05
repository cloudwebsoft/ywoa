<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<HTML><HEAD><TITLE>流程连接属性</TITLE>
<link href="../common.css" rel="stylesheet" type="text/css">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String conditionType = ParamUtil.get(request, "conditionType");
%>
<script src="../inc/common.js"></script>
<script language="JavaScript">
function openWin(url,width,height)
{
	var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function ClearCond() {
	if (confirm("您确定要清除条件么？")) {
		window.opener.SetSelectedLinkProperty("title", "");	
		window.opener.SetSelectedLinkProperty("desc", "");	
		window.close();
	}
}

//@task: 注意在控件的link中不能存储"号
function encodeLinkStr(str) {
	return str.replaceAll("\"", "\\quot");
}

function decodeLinkStr(str) {
	return str.replaceAll("\\\\quot", "\"");
}

function ModifyLink() {
	if (expireHour.value.trim()!="") {
		if (!isNumeric(expireHour.value.trim())) {
			alert("到期时间必须为大于1的数字！");
			return;
		}
		if (expireHour.value<0) {
			alert("到期时间必须为大于或等于0的数字！");
			return;
		}
	}
	window.opener.SetSelectedLinkProperty("conditionType", conditionType.value);
	window.opener.SetSelectedLinkProperty("desc", desc.value);
	window.opener.SetSelectedLinkProperty("expireHour", expireHour.value);
	window.opener.SetSelectedLinkProperty("expireAction", expireAction.value);
	
	var t = "";
	if (conditionType.value=="<%=WorkflowLinkDb.COND_TYPE_FORM%>") {
		t = fields.value + compare.value + condValue.value;
	}
	else if (conditionType.value=="<%=WorkflowLinkDb.COND_TYPE_DEPT%>") {
		t = depts.value;
	}
	else if (conditionType.value=="<%=WorkflowLinkDb.COND_TYPE_ROLE%>") {
		t = depts.value;
	}
	else if (conditionType.value=="<%=WorkflowLinkDb.COND_TYPE_SCRIPT%>") {
		t = deptNames.value;
	}
	
	window.opener.SetSelectedLinkProperty("title", encodeLinkStr(t));	
	window.close();
}

function window_onload() {
	var t = decodeLinkStr(window.opener.GetSelectedLinkProperty("title"));
	rawTitle.innerHTML = t;
	desc.value = window.opener.GetSelectedLinkProperty("desc");
	conditionType.value = window.opener.GetSelectedLinkProperty("conditionType");
	expireHour.value = window.opener.GetSelectedLinkProperty("expireHour");
	expireAction.value = window.opener.GetSelectedLinkProperty("expireAction");	
	conditionType_onchange(true);
	if (conditionType.value=="<%=WorkflowLinkDb.COND_TYPE_FORM%>") {
		if (t=="")
			return;
		if (t.indexOf(">=")!=-1)
			compare.value = ">=";
		else if (t.indexOf("<=")!=-1)
			compare.value = "<=";
		else if (t.indexOf(">")!=-1)
			compare.value = ">";
		else if (t.indexOf("<")!=-1)
			compare.value = "<";
		else if (t.indexOf("=")!=-1)
			compare.value = "=";
		var ary = t.split(compare.value);
		fields.value = ary[0];
		condValue.value = ary[1];
	}
	else if (conditionType.value=="<%=WorkflowLinkDb.COND_TYPE_DEPT%>") {
		depts.value = t;
	}
	else if (conditionType.value=="<%=WorkflowLinkDb.COND_TYPE_ROLE%>") {
		depts.value = t;
	}
	else if (conditionType.value=="<%=WorkflowLinkDb.COND_TYPE_SCRIPT%>") {
		deptNames.value = t;
	}
}

function conditionType_onchange(isOnLoad) {
	if (conditionType.value=="<%=WorkflowLinkDb.COND_TYPE_FORM%>") {
		deptNames.disabled = true;
		deptAddBtn.disabled = true;
		deptClearBtn.disabled = true;
		roleAddBtn.disabled = true;
		compare.disabled = false;
		condValue.disabled = false;
		fields.disabled = false;
		deptNames.readOnly = true;
		deptNames.value = "";
	}
	else if (conditionType.value=="<%=WorkflowLinkDb.COND_TYPE_DEPT%>") {
		compare.disabled = true;
		condValue.disabled = true;
		fields.disabled = true;
		deptNames.disabled = false;		
		deptAddBtn.disabled = false;
		deptClearBtn.disabled = false;
		roleAddBtn.disabled = true;
		deptNames.readOnly = true;
		if (!isOnLoad)
			deptNames.value = "";
				
		$("condName").innerHTML = "部门";
	}
	else if (conditionType.value=="<%=WorkflowLinkDb.COND_TYPE_ROLE%>") {
		compare.disabled = true;
		condValue.disabled = true;
		fields.disabled = true;
		deptNames.disabled = false;
		deptAddBtn.disabled = true;
		deptClearBtn.disabled = false;
		roleAddBtn.disabled = false;
		deptNames.readOnly = true;
		
		if (!isOnLoad)
			deptNames.value = "";
		
		$("condName").innerHTML = "角色";
	}
	else if (conditionType.value=="<%=WorkflowLinkDb.COND_TYPE_SCRIPT%>") {
		compare.disabled = true;
		condValue.disabled = true;
		fields.disabled = true;
		deptAddBtn.disabled = true;
		deptClearBtn.disabled = true;
		roleAddBtn.disabled = true;
		
		deptNames.disabled = false;
		deptNames.value = "";
		deptNames.readOnly = false;
		
		$("condName").innerHTML = "脚本";
	}
}

function getDepts() {
	return depts.value;
}

function openWinDepts() {
	var ret = openWin('../dept_multi_sel.jsp', 520, 350);
	if (ret==null)
		return;
	deptNames.value = "";
	depts.value = "";
	for (var i=0; i<ret.length; i++) {
		if (deptNames.value=="") {
			depts.value += ret[i][0];
			deptNames.value += ret[i][1];
		}
		else {
			depts.value += "," + ret[i][0];
			deptNames.value += "," + ret[i][1];
		}
	}
	if (depts.value.indexOf("<%=DeptDb.ROOTCODE%>")!=-1) {
		depts.value = "<%=DeptDb.ROOTCODE%>";
		deptNames.value = "全部";
	}
}

function setRoles(roles, descs) {
	depts.value = roles;
	deptNames.value = descs
}
</script>
<META content="Microsoft FrontPage 4.0" name=GENERATOR><meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</HEAD>
<BODY leftMargin=4 topMargin=8 rightMargin=0 class=menubar onLoad="window_onload()">
<table width="100%"  border="0" align="center" cellpadding="2" cellspacing="1" bgcolor="#CCCCCC">
  <tr>
    <td height="23" colspan="2" class="right-title">&nbsp;&nbsp;流程连接属性</td>
  </tr>
  <tr>
    <td height="22" align="center" bgcolor="#FFFFFF">描述</td>
    <td height="22" bgcolor="#FFFFFF"><input type="text" name="desc" style="width: 260px"></td>
  </tr>
  <tr>
    <td height="22" align="center" bgcolor="#FFFFFF">到期</td>
    <td height="22" bgcolor="#FFFFFF"><input type="text" name="expireHour" style="width: 60px" value="0">
      <%
	  	Config cfg = new Config();
		String flowExpireUnit = cfg.get("flowExpireUnit");
		if (flowExpireUnit.equals("day"))
			out.print("天");
		else
			out.print("小时");
	  %>
	  (下一节点人员处理的到期时间，0表示不限时)
	  超期则
	  <select name="expireAction">
        <option value="">等待</option>
        <option value="next">交办至后续节点</option>
      </select></td>
  </tr>
  <tr>
    <td height="22" align="center" bgcolor="#FFFFFF">类型</td>
    <td height="22" align="left" bgcolor="#FFFFFF">
	<select name="conditionType" onChange="conditionType_onchange(false)">
	<option value="-1">无条件</option>
	<option value="">根据表单</option>
	<option value="dept">根据上一节点处理人员所在的部门</option>
	<option value="role">根据上一节用户角色</option>
	<option value="<%=WorkflowLinkDb.COND_TYPE_SCRIPT%>">脚本</option>
	</select>
	条件：&nbsp;(<span id="rawTitle"></span>)	</td>
  </tr>
  <tr>
    <td width="90" height="22" align="center" bgcolor="#FFFFFF">表单</td>
    <td height="22" bgcolor="#FFFFFF">
<%
String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
Leaf lf = new Leaf();
lf = lf.getLeaf(flowTypeCode);
FormDb fd = new FormDb();
fd = fd.getFormDb(lf.getFormCode());
Vector v = fd.getFields();
Iterator ir = v.iterator();
String options = "";
while (ir.hasNext()) {
	FormField ff = (FormField) ir.next();
	options += "<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>";
}
%>
<select name="fields">
<%=options%>
</select>
	<select name="compare" style="font-family:'宋体'">
	<option value=">=">>=</option>
	<option value="<="><=</option>
	<option value=">">></option>
	<option value="&lt;"><</option>
	<option value="=">=</option>
	<option value="<>"><></option>
	</select>
	&nbsp;<input name="condValue">	
	<br>
	当为等号时，如果有多个满足条件的值可以用逗号分隔，
	空表示为默认条件
	</td>
  </tr>
  <tr>
    <td height="22" align="center" bgcolor="#FFFFFF">
	<span id="condName">部门</span></td>
    <td height="22" bgcolor="#FFFFFF"><textarea name="deptNames" cols="45" rows="5" readOnly wrap="yes" id="deptNames"><%
			  String depts = "";
			  if (conditionType.equals(WorkflowLinkDb.COND_TYPE_DEPT)) {
				  depts = ParamUtil.get(request, "title");
				  String[] arydepts = StrUtil.split(depts, ",");  	  
				  int len = 0;
				  String deptNames = "";
				  if (arydepts!=null) {
					len = arydepts.length;
					DeptDb dd = new DeptDb();
					for (int i=0; i<len; i++) {
						if (deptNames.equals("")) {
							dd = dd.getDeptDb(arydepts[i]);
							deptNames = dd.getName();
						}
						else {
							dd = dd.getDeptDb(arydepts[i]);
							deptNames += "," + dd.getName();
						}
					}
				  }		  
				  out.print(deptNames);
			  }
			  else if (conditionType.equals(WorkflowLinkDb.COND_TYPE_ROLE)) {
				  depts = ParamUtil.get(request, "title");
				  String[] aryroles = StrUtil.split(depts, ",");  	  
				  int len = 0;
				  String roleNames = "";
				  if (aryroles!=null) {
					len = aryroles.length;
					RoleDb rd = new RoleDb();
					for (int i=0; i<len; i++) {
						rd = rd.getRoleDb(aryroles[i]);
						if (roleNames.equals("")) {
							roleNames = rd.getDesc();
						}
						else {
							roleNames += "," + rd.getDesc();
						}
					}
				  }		  
				  out.print(roleNames);			  	
			  }
		  %></textarea>
      <input type="hidden" name="depts" value="<%=depts%>">
      <br>	
      <span class="TableData">
      <input id="deptAddBtn" title="添加部门" onClick="openWinDepts()" type="button" value="添加部门" name="button">
	  <input name="roleAddBtn" type="button" onClick="openWin('../role_multi_sel.jsp?', 526, 435)" value="添加角色">
      <input id=deptClearBtn title="清空部门" onClick="deptNames.value='';depts.value=''" type="button" value="清 空" name="button">
      <br>
      空表示除其它分支条件外的部门或角色</span></td>
  </tr>  
  <tr align="center">
    <td height="28" colspan="2" bgcolor="#FFFFFF"><input name="okbtn" type="button" class="button1" onClick="ModifyLink()" value=" 确 定 ">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input name="okbtn2" type="button" class="button1" onClick="ClearCond()" value=" 清除条件 ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input name="cancelbtn" type="button" class="button1" onClick="window.close()" value=" 取 消 "></td>
  </tr>
</table>
</BODY></HTML>
