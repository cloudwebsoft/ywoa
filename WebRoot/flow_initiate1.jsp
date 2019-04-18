<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ page import = "cn.js.fan.db.ResultIterator"%>
<%@ page import = "com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@ page import = "cn.js.fan.db.ResultRecord"%>
<%@ page import = "com.redmoon.oa.post.PostFlowMgr"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="flow.init";
if (!privilege.isUserPrivValid(request, priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = privilege.getUser(request);
UserDb user = new UserDb();
user = user.getUserDb(userName);
int emailId = ParamUtil.getInt(request,"emailId",-1);
String op = ParamUtil.get(request, "op"); // 会议申请，op为typeCode
String flowTitle = "";
if (!op.equals("")) {
	Leaf lf = new Leaf();
	lf = lf.getLeaf(op);
	if (lf!=null)
		flowTitle = lf.getName(request);
	else {
		out.print(SkinUtil.makeErrMsg(request, "流程类型不存在！"));
		return;
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>发起流程</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />
<link href="lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet">
<script src="inc/common.js"></script>
<script src="js/jquery.js"></script>
<script src="js/jquery-ui/jquery-ui.js"></script>
<script src="js/jquery.bgiframe.js"></script>
<script src="inc/flow_js.jsp"></script>
<script type="text/javascript" src="inc/livevalidation_standalone.js"></script>
<script language="JavaScript" type="text/JavaScript">
<!--
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++)
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

function sel(code, name, type) {
	if (type==0) {
		jAlert("请选择流程类型！","提示");
		return;
	}
	$("typeCode").value = code;
	$("divName").innerHTML = name;
	$("title").value = name + $("curTime").value;
}

function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

function form1_onsubmit() {
	if (form1.typeCode.value=="not") {
		jAlert("请选择正确的流程类型！","提示");
		return false;
	}
}

function trim(strValue) 
{
	var r = strValue.replace(/^\s*|\s*$/g,"");
	jAlert(strValue + "\r\n" + r,"提示");
	return r;
}

function trimOptionText(strValue) 
{
	// 注意option中有全角的空格，所以不直接用trim
	var r = strValue.replace(/^　*|\s*|\s*$/g,"");
	return r;
}

function onload() {
	<%
	if (!op.equals("")) {
	%>
	form1.submit();
	<%
	}
	%>
}
//-->
</script>
</head>
<body onload="onload()">
<%if (op.equals("")) {%>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1"><lt:Label res="res.flow.Flow" key="InitiateProcess"/></td>
    </tr>
  </tbody>
</table>
<%}%>
<%
	Leaf lf2 = new Leaf();
	lf2 = lf2.getLeaf("root");
%>
<table align="center" class="percent98">
	<%if (op.equals("")) {%>
  	<tr> 
    	<td>
<%
        Directory dir = new Directory();
		Leaf rootlf = dir.getLeaf(Leaf.CODE_ROOT);
		DirectoryView dv = new DirectoryView(rootlf);
        Vector children = dir.getChildren(Leaf.CODE_ROOT);
		Iterator ri = children.iterator();
		String unitCode = privilege.getUserUnitCode(request);
		PostFlowMgr pfMgr = new PostFlowMgr();
		ArrayList<String> list = pfMgr.listCanUserStartFlow(userName);
		while (ri.hasNext()) {
			Leaf childlf = (Leaf) ri.next();
			// 发起流程界面
			if (childlf.isOpen() && dv.canUserSeeWhenInitFlow(request, childlf)) {
				%>
				<div style="line-height:1.5; margin-top:5px; margin-bottom:10px; padding:5px;">
				<div style="width:98%; font-weight:bold; padding-bottom:10px; border-bottom:1px dashed #cccccc; margin-bottom:6px; clear:both">
				<i class="fa fa-tags"></i>
				<%=childlf.getName(request)%>
                </div>
				<%
				Iterator ir = childlf.getChildren().iterator();
				while (ir.hasNext()) {
					Leaf chlf = (Leaf)ir.next();
					if (chlf.isOpen() && dv.canUserSeeWhenInitFlow(request, chlf)) {
						if(chlf.getParentCode().equals("performance")){
							if (list.contains(chlf.getCode())) {
						%>
							<div style="margin-right:20px;float:left; width:200px; height:40px;overflow:hidden"><a href="javascript:initFlow('<%=chlf.getCode()%>', '<%=chlf.getName(request)%>', '<%=chlf.getType()%>')"><%=chlf.getName(request)%></a></div>
						<% }
						}else{
						%>
                        <div style="margin-right:20px;float:left; width:200px; height:40px;overflow:hidden"><a href="javascript:initFlow('<%=chlf.getCode()%>', '<%=chlf.getName(request)%>', '<%=chlf.getType()%>')"><%=chlf.getName(request)%></a></div>
						<%}
					}
				}
			}
			%>
			</div>
			<%
		}
%>
        </td>
	</tr>
	<%}%>
</table>

<div id="dlg" style="display:none">
<form action="flow_initiate1_do.jsp" method=post name="form1" id=form1 onSubmit="return form1_onsubmit()">
<table width="48%" class="percent98">
    <tr>
		<td>流程类型：
			<span id="divName">
  <%
	String flowTypeName = "";
	if(!op.equals("")) {
		lf2 = lf2.getLeaf(op);
		if(lf2==null) {
			out.print("流程类型" + op + "未找到！");
		}
		else {
			flowTypeName = lf2.getName();
			out.print(lf2.getName());
		}
	} else {
%>
			  <span id="spanFlowTypeName">请点击选择流程类型</span>
  <%
	}
%>
	    </span>
        </td>
      </tr>
    <%
	  	long projectId = ParamUtil.getLong(request, "projectId", -1);	
	%>
    <tr id="prjTr" style="display:<%=projectId==-1?"none":""%>">
      <td>关联项目：
        <%
		String prjName = "";
		if (projectId!=-1) {
			FormMgr fm = new FormMgr();
			FormDb fd = fm.getFormDb("project");

			com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
			com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(projectId);
			
			prjName = fdao.getFieldValue("name");
	  	}
	  %>
        <input id="projectId" name="projectId" type="hidden" value="<%=projectId%>" />
        <input id="emailId" name="emailId" type="hidden" value="<%=emailId%>" />
        <input id="projectId_realshow" name="projectId_realshow" readonly value="<%=prjName%>" />
        <input name="button" type="button" onclick='openWinProjectList(projectId)' value='选择' class="btn" />
        </td>
      </tr>
    <tr>
      <td>流程等级：
        <input name="level" type="radio" value="<%=WorkflowDb.LEVEL_NORMAL%>" checked /><img src="images/general.png" align="absmiddle" />普通
        <input name="level" type="radio" value="<%=WorkflowDb.LEVEL_IMPORTANT%>" /><img src="images/important.png" align="absmiddle" />&nbsp;重要
        <input name="level" type="radio" value="<%=WorkflowDb.LEVEL_URGENT%>" /><img src="images/urgent.png" align="absmiddle" />&nbsp;紧急      </td>
      </tr>
    <tr>
		<td>流程名称：
  	<%
	java.util.Date d = new java.util.Date();
	%>
		  <input name="curTime" value="[<%=DateUtil.format(d, "yyyy-MM-dd HH:mm:ss")%>]" type="hidden">
		  <!--<input name="title" type="text" size="50" value="<%=user.getRealName()%><%=flowTypeName%>[<%=DateUtil.format(d, "yyyy-MM-dd HH:mm:ss")%>]">-->
		  <input id="title" name="title" type="text" size="30" value="<%=flowTitle%>" />
		  <input id="typeCode" name="typeCode" type="hidden" value="<%=op%>">
          </td>
      </tr>
</table>
<%
	Enumeration paramNames = request.getParameterNames();
	while (paramNames.hasMoreElements()) {
		String paramName = (String) paramNames.nextElement();
		String[] paramValues = request.getParameterValues(paramName);
		if (paramValues.length == 1) {
			String paramValue = paramValues[0];
			// 过滤掉formCode
			if (paramName.equals("typeCode") || paramName.equals("title") || paramName.equals("op"))
				;
			else {%>
				 <input name="<%=paramName%>" value="<%=paramValue%>" type="hidden" />
            <%}
		}
	}
%>
</form>
</div>
<script>
var title = new LiveValidation('title');
title.add(Validate.Presence, { failureMessage:'请填写名称！'} );			
</script>
</body>
<script>
function relateProject() {
	o("prjTr").style.display = "";
}

function initFlow(code, name, type) {
<%
String isFromPaperSW=ParamUtil.get(request, "isFromPaperSW");
long paperFlowId = ParamUtil.getLong(request, "paperFlowId", -1);
%>

	addTab(name, "<%=request.getContextPath()%>/flow_initiate1_do.jsp?typeCode=" + code + "&projectId=<%=projectId%>&title=" + encodeURI("<%=flowTitle%>") + "&level=<%=WorkflowDb.LEVEL_NORMAL%>&curTime=[<%=DateUtil.format(d, "yyyy-MM-dd HH:mm:ss")%>]" + "&isFromPaperSW=<%=isFromPaperSW%>&paperFlowId=<%=paperFlowId%>");
	return;
	
	o("typeCode").value = code;
	o("spanFlowTypeName").innerHTML = name;
	o("title").value = name;
	
	// if (type!="<%=Leaf.TYPE_FREE%>") {
		o('form1').submit();
		return;
	// }
	
	if (true)
		return;
	
	$("#dlg").dialog({title:"请填写流程信息", modal: true, 
						buttons: {
							"取消":function() {
								$(this).dialog("close");
							},
							"确定": function() {
								var areAllValid = LiveValidation.massValidate( [ title ] );
								if (!areAllValid)
									return;

								o('form1').submit();
								$(this).dialog("close");
							}

							<%if (projectId==-1 && (License.getInstance().isEnterprise() || License.getInstance().isGroup() || com.redmoon.oa.kernel.License.getInstance().isPlatform())) {%>
							,
							"关联项目": function() {
								relateProject();
							}
							<%}%>							
						}, 
						closeOnEscape: true, 
						draggable: true,
						resizable:true,
						width:500
					});
	o("title").focus();
}
</script>
</html>