<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ taglib uri="/WEB-INF/tlds/HelpDocTag.tld" prefix="help" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
int flowId = ParamUtil.getInt(request, "flowId");
int actionId = ParamUtil.getInt(request, "actionId");
long myActionId = ParamUtil.getLong(request, "myActionId");

WorkflowDb wf = new WorkflowDb();
wf = wf.getWorkflowDb(flowId);

boolean isFlowNotDisplay = ParamUtil.get(request, "isFlowDisplay").equals("false");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>
<%
String kind = License.getInstance().getKind();
if (kind.equalsIgnoreCase(License.KIND_COM)) {
%>
	<lt:Label res="res.flow.Flow" key="notify"/>
<%
}
else {
%>
	<lt:Label res="res.flow.Flow" key="distribute"/>
<%
}
%>
</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style>
.spanUnit {
	display:block;
	width:150px;
	float:left;
}
.spanDel {
	font-size:16px;
	color:red;
	cursor:pointer;
	padding-left:3px;
}
#divUnit {
	height:150px;
	overflow-y:auto;
}
</style>
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<script language="JavaScript" type="text/JavaScript">
<!--
var selUserNames = "";
var selUserRealNames = "";

function getSelUserNames() {
	return selUserNames;
}

function getSelUserRealNames() {
	return selUserRealNames;
}

function openWinUsers() {
	selUserNames = form1.users.value;
	selUserRealNames = form1.userRealNames.value;
	showModalDialog('../user_multi_sel.jsp',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')
}

function setUsers(users, userRealNames) {
	form1.users.value = users;
	form1.userRealNames.value = userRealNames;
	
	o("divUser").innerHTML = "";
	
	if (users=="") {
		return;
	}
	
	var aryUser = users.split(",");
	var aryRealName = userRealNames.split(",");
	
	for (var i=0; i<aryUser.length; i++) {
		o("divUser").innerHTML += "<span class='spanUnit' id='" + aryUser[i] + "'>" + aryRealName[i] + "<span class='spanDel' onclick=\"removeUser('" + aryUser[i] + "', '" + aryRealName[i] + "')\">×</span></span>";
	}
	
}

function openWinDepts() {
	var ret = showModalDialog('../unit_multi_sel.jsp?isOnlyUnitCheckable=true&isIncludeChild=false',window.self,'dialogWidth:480px;dialogHeight:320px;status:no;help:no;')
	if (ret==null)
		return;
	// o("divUnit").innerHTML = "";
	// form1.depts.value = "";
	for (var i=0; i<ret.length; i++) {
		// 检查是否有重复
		if (("," + form1.depts.value).indexOf("," + ret[i][0])!=-1) {
			continue;
		}
		
		if (form1.depts.value=="") {
			form1.depts.value += ret[i][0];
		}
		else {
			form1.depts.value += "," + ret[i][0];
		}
		o("divUnit").innerHTML += "<span class='spanUnit' id='" + ret[i][0] + "'>" + ret[i][1] + "<span class='spanDel' onclick=\"remove('" + ret[i][0] + "')\">×</span></span>";
	}
}

function getDepts() {
	return "";
	// return form1.depts.value;
}

function getDept() {
	return "";
	// return form1.depts.value;
}
//-->
</script>
</head>
<body>
<form action="" name="form1" method="post" enctype="multipart/form-data" onsubmit="return form1_onsubmit()">
<table id="tabDistribute" style="width:100%" class="tabStyle_1 percent98">
  <tbody>
    <tr>
      <td colspan="2" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="choiceUint"/></td>
    </tr>
    <tr>
      <td nowrap><lt:Label res="res.flow.Flow" key="tit"/></td>
      <td>
      <input id="title" name="title" value="<%=wf.getTitle()%>" size="50" />
      </td>
    </tr>
    <%
    if (!isFlowNotDisplay) {
    %>
    <tr>
      <td colspan="2" nowrap>
        <lt:Label res="res.flow.Flow" key="seeProcess"/>：
        <input id='isFlowDisplay' name='isFlowDisplay' type='radio' value='1' checked /><lt:Label res="res.flow.Flow" key="yes"/><input id='isFlowDisplay' name='isFlowDisplay' type='radio' value='0' /><lt:Label res="res.flow.Flow" key="no"/>     
      </td>
    </tr>
    <%}%>
    <%
	DeptDb dd = new DeptDb();
	dd = dd.getDeptDb(DeptDb.ROOTCODE);
	Iterator ir = dd.getChildren().iterator();
	boolean isHasUnit = false;
	while (ir.hasNext()) {
		dd = (DeptDb)ir.next();
		if (dd.getType()==DeptDb.TYPE_UNIT) {
			isHasUnit = true;
			break;
		}
	}
	if (isHasUnit) {
	%>
    <tr>
      <td colspan="2">
        <div style="text-align:center; clear:both">
          <input class="btn" title="添加单位" onClick="openWinDepts()" type="button" value='<lt:Label res="res.flow.Flow" key="add"/>' name="button">
          &nbsp;
          <input class="btn" title="清空单位" onClick="o('depts').value=''; o('divUnit').innerHTML='';" type="button" value='<lt:Label res="res.flow.Flow" key="empty"/>' name="button">
        </div></td>
      </tr>
    <tr>
      <td width="81" style="height:150px"><lt:Label res="res.flow.Flow" key="unit"/></td>
      <td width="722" valign="top">
        <div id="divUnit">
        </div>
        </td>
    </tr>
    <%}%>
    <tr>
      <td colspan="2" noWrap align="center">
        <input class="btn" onClick="openWinUsers()" type="button" value='<lt:Label res="res.flow.Flow" key="add"/>' name="button">
        &nbsp;&nbsp;
        <input class="btn" onClick="o('users').value=''; o('userRealNames').value=''; o('divUser').innerHTML='';" type="button" value='<lt:Label res="res.flow.Flow" key="empty"/>' name="button">
        </td>
      </tr>
    <tr>
      <td style="height:250px" noWrap><lt:Label res="res.flow.Flow" key="people"/></td>
      <td valign="top">
        <div id="divUser">
        </div>
      	<input type="hidden" id="depts" name="depts">
        <input name="users" id="users" type="hidden">
        <textarea name="userRealNames" style="width:98%; display:none" rows="6" readOnly wrap="yes" id="userRealNames"></textarea>
        </td>
    </tr>
    <tr class="TableControl" align="middle">
      <td colSpan="2" align="center" noWrap><input class="btn" value='<lt:Label res="res.flow.Flow" key="sure"/>' type="button" onclick="distribute()">
        &nbsp;&nbsp;
        <input name="button" type="button" onclick="window.close();" class="btn" value='<lt:Label res="res.flow.Flow" key="close"/>'>
        &nbsp;&nbsp;
      </td></tr>
  </tbody>
</table>
</form>
</body>
<script>
function remove(dept) {
	$('#' + dept).remove();
	
	var val = ("," + o("depts").value).replace("," + dept, "");
	o("depts").value = val.substring(1);
}

function removeUser(name, realName) {
	$('#' + name).remove();
	
	var val = ("," + o("users").value).replace("," + name, "");
	o("users").value = val.substring(1);
	
	val = ("," + o("userRealNames").value).replace("," + realName, "");
	o("userRealNames").value = val.substring(1);

}

function distribute() {
	if (o("depts").value=="" && o("users").value=="") {
		jAlert('<lt:Label res="res.flow.Flow" key="choiceUint"/>','提示');
		return;
	}
	<%	
	if (kind.equalsIgnoreCase(License.KIND_COM)) {
	%>
		var t = "<lt:Label res="res.flow.Flow" key="isNotify"/>";
	<%
	}
	else {
	%>
		var t = "<lt:Label res="res.flow.Flow" key="isDistribute"/>";
	<%
	}	
	%>
	jConfirm(t,'提示',function(r){
		if(!r){return;}
		else{
			$.ajax({
			type: "post",
			url: "../flow_dispose_do.jsp",
			data: {
				myop: "distribute",
				flowId: <%=flowId%>,
				actionId: <%=actionId%>,
				myActionId: <%=myActionId%>,
				title: o("title").value,
				depts: o("depts").value,
				users: o("users").value,
				isFlowDisplay: getRadioValue("isFlowDisplay")
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$('#tabDistribute').showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				if (data.ret=="1") {
					isDocDistributed = true;
					// jAlert(data.msg, "提示");
					jAlert(data.msg,"提示");
					window.close();
				}
				else {
					jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
					$('#tabDistribute').hideLoading();
				}
			},
			complete: function(XMLHttpRequest, status){
				$('#tabDistribute').hideLoading();				
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				jAlert(XMLHttpRequest.responseText,"提示");
			}
		});
		}
	})
}
</script>
</html>
