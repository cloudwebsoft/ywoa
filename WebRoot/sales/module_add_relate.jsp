<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");

String formCode = ParamUtil.get(request, "formCode");
// formCode = "contract";
if (formCode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String formCodeRelated = ParamUtil.get(request, "formCodeRelated");
String menuItem = ParamUtil.get(request, "menuItem");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "menuItem", menuItem, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}

String moduleCode = ParamUtil.get(request, "code");

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCodeRelated);


String relateFieldValue = "";
int parentId = ParamUtil.getInt(request, "parentId"); // 父模块的ID
if (parentId==-1) {
	out.print(SkinUtil.makeErrMsg(request, "缺少父模块记录的ID！"));
	return;
}
else {
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);
	relateFieldValue = fdm.getRelateFieldValue(parentId, formCodeRelated);
	if (relateFieldValue==null) {
		out.print(StrUtil.jAlert_Back("请检查模块是否相关联！","提示"));
		return;
	}
}

// 用于表单域选择窗体宏控件及查询选择宏控件
request.setAttribute("formCodeRelated", formCodeRelated);

ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
if (!mpd.canUserAppend(privilege.getUser(request))) {
	%>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<%
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
	return;
}

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);


%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta name="renderer" content="ie-stand">
<title>智能模块设计-添加内容</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery.raty.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="../inc/livevalidation_standalone.js"></script>
<script src="../inc/upload.js"></script>
<script src="<%=request.getContextPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/flow_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>

<script src="<%=request.getContextPath()%>/flow/form_js/form_js_<%=formCode%>.jsp"></script>

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

<script>
$(function() {
	SetNewDate();
});

function setradio(myitem,v)
{
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

// 控件完成上传后，调用Operate()
function Operate() {
	// alert(redmoonoffice.ReturnMessage);
}
</script>
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
</head>
<body>
<%
if (isShowNav==1) {
%>
<%@ include file="customer_inc_nav.jsp"%>
<script>
o("menu<%=menuItem%>").className="current"; 
</script>
<%}%>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
if (fd==null || !fd.isLoaded()) {
	out.println(StrUtil.jAlert("表单不存在！","提示"));
	return;
}
if (op.equals("saveformvalue")) {
	boolean re = false;
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		if (formCode.equals("project") && formCodeRelated.equals("project_members")) {
			re = fdm.createPrjMember(application, request);
		} else {
			re = fdm.create(application, request);
		}
		%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
	if (re) {
		%>
		<script>
		// 如果有父窗口，则自动刷新父窗口
		if (window.opener!=null) {
		  window.opener.location.reload();
		}
		</script>
		<%
		out.print(StrUtil.jAlert_Redirect("保存成功！","提示", "module_list_relate.jsp?code=" + StrUtil.UrlEncode(moduleCode) + "&customerId=" + parentId + "&parentId=" + parentId + "&menuItem=" + menuItem + "&formCodeRelated=" + formCodeRelated + "&formCode=" + formCode + "&isShowNav=" + isShowNav));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
 %>
<div class="spacerH"></div>
<form action="?op=saveformvalue&code=<%=StrUtil.UrlEncode(moduleCode)%>&customerId=<%=parentId %>&parentId=<%=parentId%>&menuItem=<%=menuItem%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&isShowNav=<%=isShowNav%>" method="post" enctype="multipart/form-data" name="visualForm" id="visualForm">
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td align="left">
      <div>
	  <%
	  com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, fd);
	  out.print(rd.rendForAdd());
	  %>
      </div>
	  <%if (fd.isHasAttachment()) {%>
      <div style="clear:both">
		  <script>initUpload()</script>
      </div>
	  <%}%>
	  </td>
    </tr>
    <tr>
      <td height="30" align="center"><input id="btnAdd" class="btn" type="submit" name="btnAdd" value=" 添 加 " />
      <input name="cws_id" value="<%=relateFieldValue%>" type="hidden" />
      <input id="helper" value="1" type="hidden" />      
	  </td>
    </tr>
</table>
</form>
</body>
<script>
$(function() {
	var f_helper = new LiveValidation('helper');
	
	$('#btnAdd').click(function() {
		if (!LiveValidation.massValidate(f_helper.formObj.fields)) {
			jAlert("请检查表单中的内容填写是否正常！","提示");
			return;
		}
		$('#btnAdd').attr("disabled", true);
		$('#visualForm').submit();
	});
});
</script>
</html>
