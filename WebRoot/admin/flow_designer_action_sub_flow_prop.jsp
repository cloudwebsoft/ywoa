<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<TITLE>流程动作设定 - 子流程</TITLE>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%
String op = ParamUtil.get(request, "op");
String fieldWrite = ParamUtil.get(request, "hidFieldWrite");
String fieldHide = ParamUtil.get(request, "fieldHide");
String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
String dept = ParamUtil.get(request, "hidDept");
String nodeMode = ParamUtil.get(request, "hidNodeMode");
if (nodeMode.equals(""))
	nodeMode = "" + WorkflowActionDb.NODE_MODE_ROLE; 
if (op.equals("load"))
	nodeMode = "" + WorkflowActionDb.NODE_MODE_ROLE;
%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="cfg" scope="page" class="com.redmoon.oa.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = "";
String userRealName = "";
%>
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script language="JavaScript">
function openWin(url,width,height)
{
	var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}

var curFields, curFieldsShow
function OpenFormFieldSelWin(fields, fieldsShow) {
	curFields = fields;
	curFieldsShow = fieldsShow;
	// alert(curFields + " value=" + o(curFields).value);
	openWin("flow_predefine_form_field_sel.jsp?flowTypeCode=<%=flowTypeCode%>&fields=" + o(curFields).value, 500, 340);
}

function setFieldValue(v) {
	o(curFields).value = v;
	ModifyAction(false);
}

function setFieldText(v) {
	o(curFieldsShow).value = v;
}

function setDeptName(v) {
	o("deptName").value = v;
}

var flag = "";

function ModifyAction(isSubmit) {
	if (o("flagModify").checked)
		flag = "1";
	else
		flag = "0";
	if (o("flagDel").checked)
		flag += "1";
	else
		flag += "0";
	if (o("flagDiscardFlow").checked)
		flag += "1";
	else
		flag += "0";
	if (o("flagDelFlow").checked)
		flag += "1";
	else
		flag += "0";
	flag += o("flagSaveArchive").value;
	if (o("flagDelAttach").checked)
		flag += "1";
	else
		flag += "0";
	if (o("flagXorRadiate").checked)
		flag += "1";
	else
		flag += "0";
	if (o("flagXorAggregate").checked)
		flag += "1";
	else
		flag += "0";
	if (o("flagFinishFlow").checked)
		flag += "1";
	else
		flag += "0";
	var rankName = o("rank").options[o("rank").selectedIndex].text;
	var rel = "0";
	if (o("relateRoleToOrganization").checked)
		rel = "1";
	
	// 检查fieldWrite与fieldHide不能有重叠
	if (o("fieldHide").value.trim()!="" && o("fieldWrite").value.trim()!="") {
		var hides = o("fieldHide").value.trim().split(",");
		var writes = o("fieldWrite").value.trim().split(",");
		var writesText = o("fieldWriteText").value.trim().split(",");
		for (var i=0; i<hides.length; i++) {
			for (var j=0; j<writes.length; j++) {
				if (hides[i]==writes[j]) {
					alert("出现相同字段：" + writesText[j] + "，注意可填写字段与隐藏字段不能有重叠！");
					return;
				}
			}
		}
	}
	
	// 组合item2
	var item2 = "{" + o("relateToAction").value + ",<%=WorkflowActionDb.IGNORE_TYPE_NOT%>,<%=WorkflowActionDb.KIND_SUB_FLOW%>," + o("fieldHide").value.replaceAll(",", "|") + "}";
		
	if (isSubmit)
		window.parent.ModifyAction("", o("title").value, o("OfficeColorIndex").value, "", o("userName").value, o("userRealName").value, o("direction").value, o("rank").value, rankName, rel, o("fieldWrite").value, o("checkState").value, o("dept").value, flag, o("nodeMode").value, o("strategy").value, o("item1").value, item2);
	else
		window.parent.ModifyActionNotSubmit("", title.value, OfficeColorIndex.value, "", userName.value, userRealName.value, o("direction").value, rank.value, rankName, rel, fieldWrite.value, checkState.value, dept.value, flag, nodeMode.value, strategy.value, item1.value, item2);
	
	window.close();
}

function onload_win() {
	if (!window.parent.Designer.isActionSelected) {
		mainTable.style.display = "none";
		return;
	}
	else {
		mainTable.style.display = "";
	}

    var STATE_NOTDO = <%=WorkflowActionDb.STATE_NOTDO%>;
    var STATE_IGNORED = <%=WorkflowActionDb.STATE_IGNORED%>;
	var STATE_DOING = <%=WorkflowActionDb.STATE_DOING%>;
    var STATE_RETURN = <%=WorkflowActionDb.STATE_RETURN%>;
    var STATE_FINISHED = <%=WorkflowActionDb.STATE_FINISHED%>;

	var chkState = window.parent.getActionCheckState();
	if (chkState==STATE_FINISHED || chkState==STATE_DOING) {
		alert("动作已完成或者正在处理中时，不能被编辑！");
		window.close();
		return;
	}

	o("userName").value = window.parent.getActionJobCode();
	o("title").value = window.parent.getActionTitle();
	o("OfficeColorIndex").value = window.parent.getActionColorIndex();
	o("userRealName").value = window.parent.getActionJobName();
	o("direction").value = window.parent.getActionProxyJobCode();
	o("rank").value = window.parent.getActionProxyJobName();
	var rel = window.parent.getActionProxyUserRealName();
	if (rel=="1")
		o("relateRoleToOrganization").checked = true;
	else
		o("relateRoleToOrganization").checked = false;
	o("fieldWrite").value = window.parent.getActionFieldWrite();
	o("checkState").value = window.parent.getActionCheckState();
	
	o("nodeMode").value = window.parent.getActionNodeMode();
	
	if (o("nodeMode").value=="<%=WorkflowActionDb.NODE_MODE_ROLE%>")
		o("spanMode").innerHTML = "角色";
	else
		o("spanMode").innerHTML = "用户";
		
	<%if (dept.equals("")) {%>
		o("dept").value = window.parent.getActionDept();
	<%}%>
	flag = window.parent.getActionFlag();
	if (flag.length>=1) {
		if (flag.substr(0, 1)!="1")
			o("flagModify").checked = false;
	}
	if (flag.length>=2) {
		if (flag.substr(1, 1)!="1")
			o("flagDel").checked = false;
	}
	
	// flag.length长度为2时，是给新创建的节点设置属性
	
	if (flag.length>=3) {
		if (flag.substr(2, 1)!="1")
			o("flagDiscardFlow").checked = false;
	}
	else
		o("flagDiscardFlow").checked = false;
	
	if (flag.length>=4) {
		if (flag.substr(3, 1)!="1")
			o("flagDelFlow").checked = false;
	}
	else
		o("flagDelFlow").checked = false;
		
	if (flag.length>=5) {
		o("flagSaveArchive").value = flag.substr(4, 1);
	}
	else
		o("flagSaveArchive").value = "0";
		
	if (flag.length>=6) {
		if (flag.substr(5, 1)!="1")
			o("flagDelAttach").checked = false;
	}
	else
		o("flagDelAttach").checked = false;
		
	if (flag.length>=7) {
		if (flag.substr(6, 1)!="1")
			o("flagXorRadiate").checked = false;
	}
	else
		o("flagXorRadiate").checked = false;
	
	if (flag.length>=8) {
		if (flag.substr(7, 1)!="1")
			o("flagXorAggregate").checked = false;
	}
	else
		o("flagXorAggregate").checked = false;
		
	if (flag.length>=9) {
		if (flag.substr(8, 1)!="1")
			o("flagFinishFlow").checked = false;
	}
	else
		o("flagFinishFlow").checked = false;
		
	o("strategy").value = window.parent.getActionStrategy();
	
	o("item1").value = window.parent.getActionItem1();
	if (o("item1").value=="")
		o("item1").value = "0";
		
	var item2 = window.parent.getActionItem2();
	// 解析item2，格式{relateToAction,ignoreType,kind}
	if (item2.length>=2) {
		var items = item2.substring(1, item2.length-1);
		var itemAry = items.split(",");
		if (itemAry.length>=1) {
			if (itemAry[0].length>0) {
				o("relateToAction").value = itemAry[0];
			}
			if (itemAry.length>=2) {
				o("ignoreType").value = itemAry[1];
			}
			if (itemAry.length>=3) {
				o("kind").value = itemAry[2];
			}
			if (itemAry.length>=4) {
				o("fieldHide").value = itemAry[3].replaceAll("\\|", ",");
			}
		}
	}

	if (window.parent.getActionType()=="workflow_start") {
		span_direction.style.display = "none";
		span_starter.style.display = "none";
		o("kind").disabled = true;
	}
	else if (window.parent.getActionType()=="workflow_action") {
		span_self.style.display = "none";
		o("kind").disabled = false;
	}
	// 屏蔽删除标志，不再启用
	span_flag_del.style.display = "none";
	
	// document.frames["hiddenframe"].location.replace("flow_predefine_action_modify_getfieldtitle.jsp?flowTypeCode=<%=flowTypeCode%>&fieldWrite=" + o("fieldWrite").value + "&dept=" + o("dept").value + "&nodeMode=" + o("nodeMode").value); // 获取可写表单域的名称
	
	if (o("fieldHide").value.trim()!="") {
		$.get(
				"flow_predefine_action_modify_getfieldtitle_ajax.jsp",
				{
					fields:o("fieldHide").value,
					flowTypeCode:"<%=flowTypeCode%>"
				},
				function(data){
					o("fieldHideText").value = data.trim();
				}
			 );
	}
	
	if (o("fieldWrite").value.trim()!="") {
		$.get(
				"flow_predefine_action_modify_getfieldtitle_ajax.jsp",
				{
					fields:o("fieldWrite").value,
					flowTypeCode:"<%=flowTypeCode%>"
				},
				function(data){
					o("fieldWriteText").value = data.trim();
				}
			 );
	}	

	if (o("dept").value.trim()!="") {
		$.get(
				"flow_predefine_action_modify_get_dept_ajax.jsp",
				{
					dept:o("dept").value
				},
				function(data){
					o("deptName").value = data.trim();
				}
			 );
	}	
		
}

function window_onload() {
	onload_win();
}

function openWinDepts() {
	var ret = showModalDialog('../dept_multi_sel.jsp',window.self,'dialogWidth:480px;dialogHeight:320px;status:no;help:no;')
	if (ret==null)
		return;
	o("dept").value = "";
	o("deptName").value = "";
	for (var i=0; i<ret.length; i++) {
		if (o("dept").value=="") {
			o("dept").value += ret[i][0];
			o("deptName").value += ret[i][1];
		}
		else {
			o("dept").value += "," + ret[i][0];
			o("deptName").value += "," + ret[i][1];
		}
	}
	
	ModifyAction(false);	
}

function getSelUserNames() {
	if (o("nodeMode").value=="<%=WorkflowActionDb.NODE_MODE_USER%>") {
		if (o("userName").value=="$self" || o("userName").value=="$starter" || o("userName").value=="$userSelect")
			return "";
		else
			return o("userName").value;
	}
	else
		return "";
}

function getSelUserRealNames() {
	if (o("nodeMode").value=="<%=WorkflowActionDb.NODE_MODE_USER%>") {
		if (o("userName").value=="$self" || o("userName").value=="$starter" || o("userName").value=="$userSelect")
			return "";
		else
			return o("userRealName").value;
	}
	else
		return "";
}

function getUserRoles() {
	if (o("nodeMode").value=="<%=WorkflowActionDb.NODE_MODE_ROLE%>") {
		return o("userName").value;
	}
	else
		return "";
}

function getDepts() {
	return o("dept").value;
}

function setUsers(users, userRealNames) {
	o("userName").value = users;
	o("userRealName").value = userRealNames;
	o("nodeMode").value = "<%=WorkflowActionDb.NODE_MODE_USER%>";	
	
	spanMode.innerHTML = "用户";	
	
	ModifyAction(false);	
}

function setRoles(roleCodes, roleDescs) {
	o("userName").value = roleCodes;
	o("userRealName").value = roleDescs;
	o("nodeMode").value = "<%=WorkflowActionDb.NODE_MODE_ROLE%>";
	spanMode.innerHTML = "角色";
	if (o("relateRoleToOrganization").checked) {
		if (roleCodes.indexOf(",")!=-1) {
			o("relateRoleToOrganization").checked = false;
			alert("多个角色被选择，角色关联已经被取消！");
		}
	}
	
	ModifyAction(false);	
}

function openWinUserRoles() {
	var roleCodes = "";
	if (nodeMode.value=="<%=WorkflowActionDb.NODE_MODE_ROLE%>")
		roleCodes = o("userName").value
	showModalDialog('../role_multi_sel.jsp?roleCodes=' + roleCodes,window.self,'dialogWidth:526px;dialogHeight:435px;status:no;help:no;');
	return;
	
	var ret = showModalDialog('../userrole_multi_sel.jsp',window.self,'dialogWidth:500px;dialogHeight:480px;status:no;help:no;')
	if (ret==null)
		return;
	
	o("userName").value = "";
	o("userRealName").value = "";
	// deptName.value = "";
	// dept.value = "";
	for (var i=0; i<ret.length; i++) {
		if (o("userRealName").value=="") {
			o("userName").value += ret[i][0];
			o("userRealName").value += ret[i][1];
		}
		else {
			o("userName").value += "," + ret[i][0];
			o("userRealName").value += "," + ret[i][1];
		}
	}
	o("nodeMode").value = "<%=WorkflowActionDb.NODE_MODE_ROLE%>";
	spanMode.innerHTML = "角色";
	if (o("relateRoleToOrganization").checked) {
		if (ret.length>1) {
			o("relateRoleToOrganization").checked = false;
			alert("多个角色被选择，角色关联已经被取消！");
		}
	}
}

function checkRelation() {
	if (o("relateRoleToOrganization").checked) {
		// if (userName.value.indexOf(",")!=-1) {
		//	alert("当只有一个角色时，才能被关联！");
		//	relateRoleToOrganization.checked = false;
		// }
	}
}
</script>
</HEAD>
<BODY onLoad="window_onload()">
<table align="center" cellpadding="2" cellspacing="0" class="tabStyle_1" id="mainTable" style="padding:0px; margin:0px; margin-top:3px; width:100%">
  <tr>
    <td height="22" colspan="2" align="center"><input name="okbtn" type="button" class="btn" onclick="ModifyAction(true)" value=" 保存 " /></td>
  </tr>
  <tr>
    <td width="72" height="22">处理人员</td>
    <td height="22" align="left"><span id="span_self"> <a title="自动转换为发起人" href="#" onclick="userName.value='$self';userRealName.value='本人';nodeMode.value='1';spanMode.innerHTML='用户';ModifyAction(false)">本人</a>&nbsp;&nbsp; </span> <a href="#" onclick="openWinUserRoles()">选择角色</a>&nbsp;&nbsp;&nbsp;<a href="#" onclick="javascript:showModalDialog('../user_multi_sel.jsp',window.self,'dialogWidth:600px;dialogHeight:480px;status:no;help:no;')">选择用户</a><!--<a href="#" onClick="userName.value='$deptLeader';userRealName.value='部门领导';jobCode.value='';jobName.value='';proxyJobCode.value='';proxyJobName.value=''">部门领导</a>-->
      <!--&nbsp;&nbsp;<a href="#" onClick="userName.value='<%=WorkflowActionDb.PRE_TYPE_USER_SELECT%>';userRealName.value='用户自选';jobCode.value='';jobName.value='';proxyJobCode.value='';proxyJobName.value=''">用户自选</a>-->
    <br />
	<span id="span_starter"><a title="自动转换为发起人" href="#" onclick="userName.value='$starter';userRealName.value='自动转换为发起人';nodeMode.value='1';spanMode.innerHTML='发起人';ModifyAction(false)">发起人</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a title="由前一用户自行指定" href="#" onclick="userName.value='<%=WorkflowActionDb.PRE_TYPE_USER_SELECT%>';userRealName.value='自选用户';nodeMode.value='1';spanMode.innerHTML='发起人';ModifyAction(false)">自选用户</a>&nbsp;<br />
	<a title="自动转换为本节点往前两个节点处理人员" href="#" onclick="userName.value='<%=WorkflowActionDb.PRE_TYPE_FORE_ACTION%>';userRealName.value='自动转换为本节点往前两个节点处理人员';nodeMode.value='1';spanMode.innerHTML='往前两个节点人员';ModifyAction(false)">往前两个节点人员</a></span></td>
  </tr>
  <tr>
    <td height="22" align="left">标题</td>
    <td height="22"><input id="title" type="text" name="title" size="20" onchange="ModifyAction(false)"></td>
  </tr>
  <tr>
    <td height="22" align="left" bgcolor="#F9FAD3" title="角色与组织机构(行文方向)、职级、部门相关联">角色关联</td>
    <td height="22" bgcolor="#F9FAD3"><input onchange="ModifyAction(false)" type=checkbox id="relateRoleToOrganization" name="relateRoleToOrganization" value="1" checked title="设为关联后，如果角色中存在有多个用户，系统将自动根据组织机构图就近匹配，采用这种方式时，流程中的处理人员不能处于多个部门中，只有当设为关联时，行文方向才有效" onclick="checkRelation()" /></td>
  </tr>
  <tr>
    <td height="22" align="left" bgcolor="#F9FAD3" title="上一节点至本节点的行文方向">行文方向</td>
    <td height="22" bgcolor="#F9FAD3">
	  <textarea id="userName" name="userName" rows="3" readonly style="display:none;background-color:#eeeeee"><%=userName%></textarea>
      <input id="nodeMode" name="nodeMode" type="hidden" size="5" readonly value="<%=nodeMode%>">
      <font color="red" style="display:none">当前为：<span id="spanMode" name="spanMode"></span></font>
      <span id="span_direction">
      <select onchange="ModifyAction(false)" id="direction" name="direction">
      <option value="2" selected>上行</option>
      <option value="0">下行</option>
      <option value="1">平行(含本部门)</option>
      <option value="<%=WorkflowActionDb.DIRECTION_PARALLEL_MYDEPT_UP%>">平行(含本部门及上级部门)</option>
      <option value="3">本部门</option>
      </select>
      </span>	
	</td>
  </tr>
  <tr>
    <td height="22" align="left" bgcolor="#F9FAD3" title="上一节点至本节点的行文方向">关联节点</td>
    <td height="22" bgcolor="#F9FAD3">
    <select id="relateToAction" name="relateToAction">
    <option value="<%=WorkflowActionDb.RELATE_TO_ACTION_DEFAULT%>">关联上一节点</option>
    <option value="<%=WorkflowActionDb.RELATE_TO_ACTION_STARTER%>">关联开始节点</option>
    </select>
    </td>
  </tr>
  <tr>
    <td height="22" align="left">用户</td>
    <td height="22"><textarea id="userRealName" name="userRealName" readonly rows="3" style="width:140px;background-color:#eeeeee"><%=userRealName%></textarea>	</td>
  </tr>
  <tr>
    <td height="22" align="left">限定部门</td>
    <td height="22"><textarea name="deptName" rows="3" readonly id="deptName" style="width: 140px;background-color:#eeeeee"></textarea>
      <a href="javascript:openWinDepts();">选择</a>
      <input name="dept" type="hidden" id="dept" value="<%=dept%>"></td>
  </tr>
  <tr>
    <td height="22" align="left">用户职级</td>
    <td height="22">
	<select id="rank" name="rank" onchange="ModifyAction(false)">
	<option value="">不限定</option>
	</select><!--<input type="hidden" name="OfficeColorIndex" value="6" />-->
	<input id="checkState" name="checkState" value="<%=WorkflowActionDb.STATE_NOTDO%>" type="hidden"></td>
  </tr>
  <tr>
    <td height="22" align="left">结束节点</td>
    <td height="22"><select onchange="ModifyAction(false)" id="item1" name="item1">
      <option value="1">是</option>
      <option value="0">否</option>
    </select></td>
  </tr>
</table>
</BODY>
</HTML>