<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.sale.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String myname = privilege.getUser( request );

String op = ParamUtil.get(request, "op");

String formCode = ParamUtil.get(request, "formCode"); // 主模块编码
if (formCode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, "编码不能为空！"));
	return;
}

String formCodeRelated = ParamUtil.get(request, "formCodeRelated"); // 从模块编码
String menuItem = ParamUtil.get(request, "menuItem");

int id = ParamUtil.getInt(request, "id");
// 置嵌套表需要用到的cwsId
request.setAttribute("cwsId", "" + id);
// 置页面类型
request.setAttribute("pageType", "edit");

request.setAttribute("formCode", formCodeRelated); // 这里是为了使嵌套表在getNestSheet方法中，传递当前编辑的嵌套表的父模块的表单编码

int parentId = ParamUtil.getInt(request, "parentId", -1); // 父模块的ID，仅用于导航，如果导航不显示，则不用传递该参数，用例：module_show_realte.jsp编辑按钮

int customerId = ParamUtil.getInt(request, "customerId");

if (!SalePrivilege.canUserManageCustomer(request, customerId)) {
%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />	
	<%
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCodeRelated);

com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);


%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>智能设计-编辑内容</title>
<meta name="renderer" content="ie-stand">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="../js/jquery.raty.min.js"></script>
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
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
if (op.equals("saveformvalue")) {
	boolean re = false;
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = fdm.update(application, request);
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
		  window.opener.focus();
		  window.close();		  
		}
		</script>		
		<%
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", request.getRequestURL() + "?customerId=" + customerId + "&parentId=" + parentId + "&id=" + id + "&menuItem=" + menuItem + "&formCodeRelated=" + formCodeRelated + "&formCode=" + StrUtil.UrlEncode(formCode) + "&isShowNav=" + isShowNav));
	}
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}
else if (op.equals("delAttach")) {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	int attachId = ParamUtil.getInt(request, "attachId");
	com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment(attachId);
	att.del();
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
}
if (isShowNav==1) {
%>
<%@ include file="customer_inc_nav.jsp"%>
<script>
o("menu6").className="current";
</script>
<div class="spacerH"></div>
<%}
else
	out.print("<BR>");
%>
<form action="customer_service_edit.jsp?op=saveformvalue&customerId=<%=customerId%>&parentId=<%=parentId%>&id=<%=id%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&isShowNav=<%=isShowNav%>" method="post" enctype="multipart/form-data" name="visualForm" id="visualForm">
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td align="left"><table width="100%">
        <tr>
          <td><%
			com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
			out.print(rd.rend());
		  %></td>
        </tr>
      </table></td>
    </tr>
	<%if (fd.isHasAttachment()) {%>	
    <tr>
      <td align="left"><table style="width:98%" align="center"><tr><td><script>initUpload()</script></td></tr></table></td>
    </tr>
    <tr>
      <td align="left"><%
com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);
Iterator ir = fdao.getAttachments().iterator();
				  while (ir.hasNext()) {
				  	com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) ir.next(); %>
          <table width="82%"  border="0" cellpadding="0" cellspacing="0">
            <tr>
              <td width="5%" height="31" align="right"><img src="<%=Global.getRootPath()%>/images/attach.gif" /></td>
              <td>&nbsp; <a target="_blank" href="<%=Global.getRootPath()%>/visual_getfile.jsp?attachId=<%=am.getId()%>"><%=am.getName()%></a>&nbsp;&nbsp;&nbsp;&nbsp;[<a href="#" onClick="jConfirm('您确定要删除吗？','提示',function(r){if(!r){return;}else{ window.location.href='?op=delAttach&parentId=<%=parentId%>&id=<%=id%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&formCodeRelated=<%=formCodeRelated%>&attachId=<%=am.getId()%>'}}) " style="cursor:pointer">删除</a>]<br />              </td>
            </tr>
          </table>
        <%}
			  %></td>
    </tr>
	<%}%>
    <tr>
      <td height="30" align="center">
      	<input name="id" value="<%=id%>" type="hidden" />
      	<input type="submit" class="btn" name="Submit" value=" 修 改 " />
        &nbsp;&nbsp;
		<%if (isShowNav==0) {%>
      	<input type="button" class="btn" value=" 关 闭 " onClick="window.close()" />
		<%}%>
		</td>
    </tr>
</table>
</form>
</body>
</html>
