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

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCodeRelated);
if (fd==null || !fd.isLoaded()) {
	out.println(StrUtil.Alert("表单不存在！"));
	return;
}

// 用于表单域选择窗体宏控件
request.setAttribute("formCodeRelated", formCodeRelated);

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
		out.print(StrUtil.Alert_Back("请检查模块是否相关联！"));
		return;
	}
}

ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
if (!mpd.canUserAppend(privilege.getUser(request))) {
	%>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<%
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
	return;
}

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);
int projectId = ParamUtil.getInt(request, "projectId", -1);

if (op.equals("saveformvalue")) {
	boolean re = false;
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	try {
		if (formCode.equals("project") && formCodeRelated.equals("project_members")) {
			re = fdm.createPrjMember(application, request);
		} else {
			re = fdm.create(application, request);
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	if (re) {
		projectId = StrUtil.toInt(fdm.getFieldValue("projectId"), -1);
		%>
		<script>
		// 如果有父窗口，则自动刷新父窗口
		if (window.opener!=null) {
		  window.opener.location.reload();
		}
		</script>		
		<%
		out.print(StrUtil.Alert_Redirect("保存成功！", "module_list_relate.jsp?projectId=" + projectId + "&parentId=" + parentId + "&menuItem=" + menuItem + "&formCodeRelated=" + formCodeRelated + "&formCode=" + formCode + "&isShowNav=" + isShowNav));
	}
	else {
		out.print(StrUtil.Alert_Back("操作失败！"));
	}
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta name="renderer" content="ie-stand">
<title>智能模块设计-添加内容</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery.raty.min.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>
<script src="../inc/upload.js"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>
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
</head>
<body>
<%
if (isShowNav==1) {
%>
<%@ include file="prj_inc_menu_top.jsp"%>
<script>
o("menu<%=menuItem%>").className="current"; 
</script>
<%}%>
<div class="spacerH"></div>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <form action="?op=saveformvalue&menuItem=<%=menuItem%>&parentId=<%=parentId%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&isShowNav=<%=isShowNav%>" method="post" enctype="multipart/form-data" name="visualForm" id="visualForm">
    <tr>
      <td align="left"><%com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, fd);
			out.print(rd.rendForAdd());
		  %><br />
	<%if (fd.isHasAttachment()) {%>			  
		<script>initUpload()</script>
	<%}%>
    
      		<input id="parentId" name="parentId" value="<%=parentId%>" type="hidden" />              
		</td>
    </tr>
    <tr>
      <td height="30" align="center"><input class="btn" type="submit" name="Submit" value=" 添 加 " />
      <input name="cws_id" value="<%=relateFieldValue%>" type="hidden" />
      <input name="projectId" value="<%=projectId%>" type="hidden" />
	  </td>
    </tr>
  </form>
</table>
</body>
</html>
