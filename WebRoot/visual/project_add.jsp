<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>智能模块设计-添加内容</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="../js/jquery-ui/jquery-ui.js"></script>
<script src="../js/jquery.bgiframe.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />

<script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
<script src="../inc/upload.js"></script>
<script src="<%=request.getContextPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/flow_js.jsp"></script>

<script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<!-- 
<style type="text/css"> 
@import url("<%=request.getContextPath()%>/util/jscalendar/calendar-win2k-2.css"); 
</style>

<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar-setup.js"></script>
 -->
<%
String op = ParamUtil.get(request, "op");

String formCode = ParamUtil.get(request, "formCode");
// formCode = "contract";
if (formCode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCode);
if (fd==null || !fd.isLoaded()) {
	out.println(StrUtil.jAlert("表单不存在！","提示"));
	return;
}

ModulePrivDb mpd = new ModulePrivDb(formCode);
if (!mpd.canUserAppend(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

// 置嵌套表需要用到的pageType
request.setAttribute("pageType", "add");
// 置NestSheetCtl需要用到的formCode
request.setAttribute("formCode", formCode);

if (op.equals("saveformvalue")) {
	boolean re = false;
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	try {
		re = fdm.create(application, request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("保存成功！", "提示","../project/project_list.jsp?op=mine&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
%>
<script>
$(function() {
	SetNewDate();
});

function setradio(myitem,v) {
   var radioboxs = document.all.item(myitem);
   if (radioboxs!=null)
   {
	 for (i=0; i<radioboxs.length; i++)
	 {
		if (radioboxs[i].type=="radio")
		{
			if (radioboxs[i].value==v)
				radioboxs[i].checked = true;
		}
	 }
   }
}

function SubmitResult() {
	// 检查是否已选择意见
	if (getradio("resultValue")==null || getradio("resultValue")=="") {
		jAlert("您必须选择一项意见!","提示");
		return false;
	}
	visualForm.op.value='finish';
	visualForm.submit();
}

// 控件完成上传后，调用Operate()
function Operate() {
	// alert(redmoonoffice.ReturnMessage);
}

// 记录添加的嵌套表格2记录的ID
function addTempCwsId(formCode, cwsId) {
	var name = "<%=com.redmoon.oa.visual.FormDAO.NAME_TEMP_CWS_IDS%>_" + formCode;
    var inp;
    try {
        inp = document.createElement('<input type="hidden" name="' + name + '" />');
    } catch(e) {
        inp = document.createElement("input");
        inp.type = "hidden";
        inp.name = name;
    }
    inp.value = cwsId;
	
	spanTempCwsIds.appendChild(inp);
}
</script>
</head>
<body>
<%@ include file="../project/project_inc_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<div class="spacerH"></div>
<form action="?op=saveformvalue&formCode=<%=StrUtil.UrlEncode(formCode)%>" method="post" enctype="multipart/form-data" name="visualForm" id="visualForm">
<table width="60%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td align="left">
	<%
	com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, fd);
	out.print(rd.rendForAdd());
	%>
    <br />
	<%if (fd.isHasAttachment()) {%>
		<script>initUpload()</script>
	<%}%>
      </td>
    </tr>
    <tr>
      <td height="30" align="center">
      <span id="spanTempCwsIds"></span>
      <input type="submit" class="btn" name="Submit" value=" 添 加 " />
      </td>
    </tr>
</table>
</form><br />
</body>
</html>
