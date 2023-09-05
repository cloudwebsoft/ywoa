<%@page contentType="text/html;charset=utf-8" %>
<%@page import="com.redmoon.oa.ui.SkinMgr"%>
<%@page import="com.redmoon.oa.pvg.RoleDb"%>
<%@page import="com.redmoon.oa.dept.DeptMgr"%>
<%@page import="com.redmoon.oa.dept.DeptDb"%>
<%@page import="com.redmoon.oa.dept.DeptView"%>
<%@page import="cn.js.fan.util.*"%>
<%@page import="java.util.*"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="org.xml.sax.InputSource"%>
<%@page import="java.io.StringReader"%>
<%@page import="org.jdom.Element"%>
<%@page import="com.redmoon.oa.base.*"%>
<%@page import="com.redmoon.oa.flow.Leaf"%>
<%@page import="com.redmoon.oa.flow.*" %>
<%@page import="com.redmoon.oa.sms.*" %>
<%@page import="com.redmoon.oa.flow.macroctl.*" %>
<%@page import="com.redmoon.oa.visual.*" %>
<%@page import="com.redmoon.oa.flow.FormDb"%>
<%@page import="com.redmoon.oa.flow.FormField"%>
<%@page import="org.json.JSONObject"%>
<%@page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr"%>
<%@page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
String op = ParamUtil.get(request, "op");
String internalName = ParamUtil.get(request, "internalName");
if ("getActionNode".equals(op)) {
	response.setContentType("text/html;charset=utf-8");
	String flowString = request.getParameter("flowString");
	WorkflowDb wf = new WorkflowDb();
	Vector actionVector = wf.getActionsFromString(flowString);
	Iterator actionIterator = actionVector.iterator();
	while (actionIterator.hasNext()) {
		WorkflowActionDb wa = (WorkflowActionDb)actionIterator.next();
		%>
		<div><input name="actionNames" type="checkbox" value="<%=wa.getInternalName()%>"  />&nbsp;<%=wa.getJobName()%>：<%=wa.getTitle()%></div>
        <%
	}
	return;
}

Leaf lf = new Leaf();
lf = lf.getLeaf(flowTypeCode);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
	  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	  <title>消息配置</title>
	  <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	  <script type="text/javascript" src="../inc/common.js"></script>
	  <script src="<%=request.getContextPath()%>/js/jquery-1.9.1.min.js"></script>
	  <script src="<%=request.getContextPath()%>/js/jquery-migrate-1.2.1.min.js"></script>
	  <script src="../js/jquery.xmlext.js"></script>
	  <script src="../js/jquery.form.js"></script>
	  <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
	  <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
	  <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	  <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	  <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

	  <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	  <script src="<%=request.getContextPath()%>/js/bootstrap/js/bootstrap.min.js"></script>

	  <script src="../inc/livevalidation_standalone.js"></script>

  </head>
  <body>
  <%
	if ("setMsgProp".equals(op)) {
		String[] actions = ParamUtil.getParameters(request, "actionNames");
		String[] deptFields = ParamUtil.getParameters(request, "deptFields");
		String[] userFields = ParamUtil.getParameters(request, "userFields");
		String users = ParamUtil.get(request, "users");
		String roles = ParamUtil.get(request, "roles");
		
		String isMsg = ParamUtil.get(request, "isMsg");
		String isMail = ParamUtil.get(request, "isMail");
		String isSms = ParamUtil.get(request, "isSms");
		String isFlowShow = ParamUtil.get(request, "isFlowShow");
		
		String title = ParamUtil.get(request, "title");
		String content = ParamUtil.get(request, "content");
		
		String strActions = "";
		if (actions!=null) {
			for (int i=0; i<actions.length; i++) {
				strActions += strActions.equals("")?actions[i]:"," + actions[i];
			}
		}
		
		String strDepts = "";
		if (deptFields!=null) {
			for (int i=0; i<deptFields.length; i++) {
				strDepts += strDepts.equals("")?deptFields[i]:"," + deptFields[i];
			}
		}
	
		String strUsers = "";
		if (userFields!=null) {
			for (int i=0; i<userFields.length; i++) {
				strUsers += strUsers.equals("")?userFields[i]:"," + userFields[i];
			}
		}
		
		StringBuilder sb = new StringBuilder();	
		sb.append("<action internalName=\"" + internalName + "\">\r\n");	
		sb.append("<actionNames>" + strActions + "</actionNames>\r\n");
		sb.append("<deptFields>" + strDepts + "</deptFields>\r\n");
		sb.append("<userFields>" + strUsers + "</userFields>\r\n");
		sb.append("<users>" + users + "</users>\r\n");
		sb.append("<roles>" + roles + "</roles>\r\n");		
		sb.append("<title>" + title + "</title>\r\n");
		sb.append("<content>" + content + "</content>\r\n");
		sb.append("<isMsg>" + isMsg + "</isMsg>\r\n");
		sb.append("<isSms>" + isSms + "</isSms>\r\n");
		sb.append("<isMail>" + isMail + "</isMail>\r\n");
		
		sb.append("<isFlowShow>" + isFlowShow + "</isFlowShow>\r\n");
		
		sb.append("</action>\r\n");
		%>
		<textarea id="xmlContent" style="display:none"><%=sb.toString() %></textarea>
		<script>
		var xml = window.opener.getMsgProp();
		if (xml=="") {
			xml = "<actions></actions>";
		}
		try {
			xml = $.parseXML(xml);
		}
		catch (e) {
			alert(e);
		}
		
		$xml = $(xml);
		$xml.find("actions").children().each(function(i) {
			if ($(this).attr("internalName")=="<%=internalName%>") {
				$(this).remove();
				return false;
			}
		});		
		
		var $elem = $($.parseXML(o("xmlContent").value));				
		var newNode = null;
		if (typeof document.importNode == 'function') { 
			newNode = document.importNode($elem.find('action').get(0),true); 
		} else { 
			newNode = $elem.find('action').get(0);
		}
		$xml.find("actions").get(0).appendChild(newNode); 
				
		window.opener.setMsgProp($xml.xml());
		
		window.close();
		</script>
		<%
		return;
	}
  %>
  	<div class="spacerH"></div>
     <form id="frmMsgProp" action="flow_designer_msg_prop.jsp?op=setMsgProp" method="post">
     	<input name="flowTypeCode" type="hidden" value="<%=flowTypeCode %>" />
     	<input name="internalName" type="hidden" value="<%=internalName %>" />
	     <table width="610"  border="0" align="center" cellpadding="2" cellspacing="0" class="tabStyle_1 percent80"  id="tab" style="text-align:center;">
	    	<tr style="border: 1px solid #a8a8a8;border-top:1px;border-bottom:1px">
	    		<td colspan="2" class="tabStyle_1_title" >请选择节点、部门及用户</td>
    		</tr>
	    	<tr>
	    	  <td width="132">节点</td>
	    	  <td width="470" align="left">
              <div id="actionDiv"></div>
              </td>
    	   </tr>
	    	<tr>
	    	  <td>部门</td>
	    	  <td align="left">
              <%
			  FormDb fd = new FormDb();
			  fd = fd.getFormDb(lf.getFormCode());
			  Vector v = fd.getFields();
			  Iterator ir = v.iterator();
		      MacroCtlMgr mm = new MacroCtlMgr();
			  
			  while (ir.hasNext()) {
			  	FormField ff = (FormField)ir.next();
			  	if (ff.getType().equals(FormField.TYPE_MACRO)) {
                	String macroType = ff.getMacroType();
                	if (macroType.equals("macro_dept_sel_win") 
                		|| macroType.equals("macro_dept_select") 
                		|| macroType.equals("macro_my_dept_select")) {
                		%>
                		<input type="checkbox" name="deptFields" value="<%=ff.getName() %>"/>&nbsp;<%=ff.getTitle() %> <br/>
                		<%
                	}
			  	}
			  }
			  %>
              </td>
    	   </tr>
	    	<tr>
	    	  <td>用户</td>
	    	  <td align="left">
			  <%
			  ir = v.iterator();			  
			  while (ir.hasNext()) {
			  	FormField ff = (FormField)ir.next();
			  	if (ff.getType().equals(FormField.TYPE_MACRO)) {
                	String macroType = ff.getMacroType();
                	if (macroType.equals("macro_current_user") 
                		|| macroType.equals("macro_user_select") 
                		|| macroType.equals("role_user_select")
                		|| macroType.equals("macro_user_select_win")
                		|| macroType.equals("macro_user_multi_select_win")
                		|| macroType.equals("macro_depts_user_multi_sel_win")) {
                		%>
                		<input type="checkbox" name="userFields" value="<%=ff.getName() %>"/>&nbsp;<%=ff.getTitle() %> <br/>
                		<%
                	}
			  	}
			  }						
			  %>
			  </td>
    	   </tr>
	    	<tr>
	    	  <td align="center" >角色</td>
	    	  <td align="left" ><textarea name=roleDescs cols="40" rows="3"></textarea>
	    	    <input name="roles" type=hidden />
	    	    <br />
	    	    <input name="button2" class="btn btn-default" type="button" onClick="openWin('../role_multi_sel.jsp?roleCodes=' + o('roles').value + '&unitCode=<%=privilege.getUserUnitCode(request)%>', 526, 435)" value="选择"></td>
    	   </tr>
	    	<tr>
	    	  <td >用户</td>
	    	  <td align="left" ><textarea name="userRealNames" cols="40" rows="3" readonly="readOnly" wrap="yes" id="userRealNames"></textarea>
	    	    <input name="users" id="users" type="hidden">
                <br />
	    	    <input class="btn btn-default" title="添加收件人" onClick="openWinUsers()" type="button" value="选择" name="button" />
	    	    &nbsp;
	    	    <input class="btn btn-default" title="清空收件人" onClick="o('users').value=''; o('userRealNames').value='';" type="button" value="清空" name="button" /></td>
    	   </tr>           
	   		<tr>
	    	  <td>标题</td>
	    	  <td align="left">
	    	  <input id="title" name="title" style="width:300px;" />
					<div class="dropdown" style="display:inline">
					    <button type="button" class="btn dropdown-toggle" id="dropdownMenu1" data-toggle="dropdown">
					        <span class="caret"></span>
					    </button>
					    <ul num="0" class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">
					        <li role="presentation">
            					<a href="javascript:;" onClick="o('title').value += '$fromUser';" title="当前用户">当前用户</a>
            				</li>
					        <li role="presentation">            				
								<a href="javascript:;" onClick="o('title').value += '$flowTitle';" title="流程标题">流程标题</a>
							</li>
							<li role="presentation">
					           	<select onChange="o('title').value += '{$' + this.value + '}';">
			                    <option value="">请选择字段</option>
			                    <%
			                    ir = fd.getFields().iterator();
			                    while (ir.hasNext()) {
			                        FormField ff = (FormField) ir.next();
			                    %>
			                        <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
			                    <%}%>
			                  </select>
					        </li>
					    </ul>
					</div>
	    	  </td>
	    	</tr>
	    	<tr>
	    	  <td>内容</td>
	    	  <td align="left">
	    	  <textarea id="content" name="content" style="width:300px;"></textarea>
					<div class="dropdown" style="display:inline">
					    <button type="button" class="btn dropdown-toggle" id="dropdownMenu1" data-toggle="dropdown">
					        <span class="caret"></span>
					    </button>
					    <ul num="0" class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">
					        <li role="presentation">
            					<a href="javascript:;" onClick="o('content').value += '$fromUser';" title="当前用户">当前用户</a>
            				</li>
					        <li role="presentation">            				
								<a href="javascript:;" onClick="o('content').value += '$flowTitle';" title="流程标题">流程标题</a>
							</li>
							<li role="presentation">
					           	<select onChange="o('content').value += '{$' + this.value + '}';">
			                    <option value="">请选择字段</option>
			                    <%
			                    ir = fd.getFields().iterator();
			                    while (ir.hasNext()) {
			                        FormField ff = (FormField) ir.next();
			                    %>
			                        <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
			                    <%}%>
			                  </select>
					        </li>
					    </ul>
					</div>
	    	  </td>
	    	</tr>
	    	<tr>
	    	  <td>方式</td>
	    	  <td align="left">
	    	  <%
	    	  boolean isSmsUsed = SMSFactory.isUseSMS();
	    	  %>
              <input name="isMsg" type="checkbox" value="true" checked /> 消息
              &nbsp;&nbsp;
              <input name="isMail" type="checkbox" value="true" checked /> 邮件
              &nbsp;&nbsp;
              <span style="<%=isSmsUsed?"":"display:none" %>">
              <input name="isSms" type="checkbox" value="true" checked /> 短信
              </span>
              </td>
    	   </tr>
	    	<tr>
	    	  <td>流程</td>
	    	  <td align="left"><input name="isFlowShow" type="checkbox" value="true" checked />
    	      允许查看</td>
    	   </tr>
	    	<tr>
	    	  <td colspan="2" align="center">注：流程转交至下一节点时提醒</td>
    	   </tr>
        </table>
	    <table width="80%" border="0" align="center" cellpadding="0" cellspacing="0">
            <tr align="center">
     			<td colspan="3">
     			<input type="submit" value="确定" class="btn btn-default" /> 
     			&nbsp;&nbsp; <input type="button" value="清除" class="btn btn-default" onClick="window.opener.setMsgProp('');window.close()"/></td>
     		</tr>
     		<tr><td>&nbsp;</td></tr>
     	</table>
    </form>
  </body>
<script type="text/javascript">
var title = new LiveValidation('title');
title.add(Validate.Presence, { failureMessage:'请填写标题'} );
var content = new LiveValidation('content');
content.add(Validate.Presence, { failureMessage:'请填写内容'} );

function getActionNode() {
	var flowStr = window.opener.getFlowString();
	$.ajax({
		type: "post",
		url: "flow_designer_msg_prop.jsp",	
		data : {
			op: "getActionNode",
			flowTypeCode:"<%=flowTypeCode%>",
			flowString: flowStr
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			$("#actionDiv").html(data);
			
			var xml;
			try {
				xml = $.parseXML(window.opener.getMsgProp());
			}
			catch (e) {}
			
			if (xml==null) {
				return;
			}
			
			$xml = $(xml);
			$xml.find("actions").children().each(function(i){
				if ($(this).attr("internalName")=="<%=internalName%>") {
					var str = "";
					var actionNames = $(this).find("actionNames").text();
					// alert(actionNames);
					var deptFields = $(this).find("deptFields").text();
					var userFields = $(this).find("userFields").text();
					var title = $(this).find("title").text();
					var content = $(this).find("content").text();

					var users = $(this).find("users").text();
					var roles = $(this).find("roles").text();
					
					o("users").value = users;
					o("roles").value = roles;
					
					var isMsg = $(this).find("isMsg").text();
					var isMail = $(this).find("isMail").text();
					var isSms = $(this).find("isSms").text();
					var isFlowShow = $(this).find("isFlowShow").text();
					
					var aNames = actionNames.split(",");
					if (aNames!=null) {
						for (i=0; i<aNames.length; i++) {
							setCheckboxChecked("actionNames", aNames[i]);
						}
					}
					var dNames = deptFields.split(",");
					if (dNames!=null) {
						for (i=0; i<aNames.length; i++) {
							setCheckboxChecked("deptFields", dNames[i]);
						}
					}
					var uNames = userFields.split(",");
					if (uNames!=null) {
						for (i=0; i<uNames.length; i++) {
							setCheckboxChecked("userFields", uNames[i]);
						}
					}
					o("title").value = title;
					o("content").value = content;
					
					if (isMsg=="true") {
						o("isMsg").checked = true;
					}
					else {
						o("isMsg").checked = false;						
					}
					if (isMail=="true") {
						o("isMail").checked = true;
					}
					else {
						o("isMail").checked = false;						
					}
					if (isSms=="true") {
						o("isSms").checked = true;
					}
					else {
						o("isSms").checked = false;						
					}
					if (isFlowShow=="true") {
						o("isFlowShow").checked = true;
					}
					else {
						o("isFlowShow").checked = false;
					}
					
					if (o("users").value.trim()!="") {
						$.get(
								"<%=request.getContextPath()%>/visual/module_msg_prop.jsp",
								{
									op : "getUserNames",
									users:o("users").value
								},
								function(data){
									o("userRealNames").value = data.trim();
								}
							 );
					}	
					
					if (o("roles").value.trim()!="") {
						$.get("getRoleDescs.do", {
									roleCodes: o("roles").value
								},
								function (data) {
									o("roleDescs").value = data.trim();
								}
						);
					}
		
					return false;
				}
			});			
		},
		complete: function(XMLHttpRequest, status){
			// HideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			jAlert(XMLHttpRequest.responseText,"提示");
		}
	});	
}

$(function() {
	getActionNode();		
});

function openWinUsers() {
	selUserNames = frmMsgProp.users.value;
	selUserRealNames = frmMsgProp.userRealNames.value;
	openWin('../user_multi_sel.jsp', 800, 600);
}

function setUsers(users, userRealNames) {
	o("users").value = users;
	o("userRealNames").value = userRealNames;
}

function setRoles(roles, descs) {
	o("roles").value = roles;
	o("roleDescs").value = descs
}

function getSelUserNames() {
	return o("users").value;
}

function getSelUserRealNames() {
	return o("userRealNames").value;
}

function openWin(url,width,height) {
	var newwin=window.open(url,"msgPropWin","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width="+width+",height="+height);
	return newwin;
}
</script>
</html>

