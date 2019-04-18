<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.sale.*"%>
<%
String op = ParamUtil.get(request, "op");

String formCode = ParamUtil.get(request, "formCode");
// formCode = "contract";
if (formCode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, "表单编码不存在！"));
	return;
}

String formCodeRelated = ParamUtil.get(request, "formCodeRelated");

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCodeRelated);


// 置嵌套表需要用到的pageType
request.setAttribute("pageType", "add");
// 置NestSheetCtl需要用到的formCode
request.setAttribute("formCode", formCodeRelated);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
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

<script src="../js/jquery.form.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />

<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.min.css" />
<script src="../js/jquery.bgiframe.js"></script>

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<!-- 
<script src="../js/jquery-ui/jquery-ui.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />

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

function visualForm_onsubmit() {
	document.getElementById("btn_submit").disabled=true;
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
<%@ include file="customer_inc_nav.jsp"%>
<script>
o("menu5").className="current"; 
</script>

<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
if (fd==null || !fd.isLoaded()) {
	out.println(StrUtil.jAlert("表单不存在！","提示"));
	return;
}

String relateFieldValue = "";
int parentId = ParamUtil.getInt(request, "parentId"); // 父模块的ID
int customerId = parentId;
if (parentId==-1) {
	out.print(SkinUtil.makeErrMsg(request, "缺少父模块记录的ID！"));
	return;
} else {
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);
	relateFieldValue = fdm.getRelateFieldValue(parentId, formCodeRelated);
	if (relateFieldValue==null) {
		out.print(StrUtil.jAlert_Back("请检查模块是否相关联！","提示"));
		return;
	}
}

if (!SalePrivilege.canUserSeeCustomer(request, customerId)) {
%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />	
<%
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
	return;
}

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);

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
		re = fdm.create(application, request);
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
		out.print(StrUtil.jAlert_Redirect("保存成功！","提示", "customer_sales_order_list.jsp?parentId=" + parentId + "&customerId=" + customerId + "&formCodeRelated=" + formCodeRelated + "&formCode=" + formCode + "&isShowNav=" + isShowNav));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
 %>
<div class="spacerH"></div>
<form action="?op=saveformvalue&customerId=<%=customerId%>&parentId=<%=parentId%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&isShowNav=<%=isShowNav%>" method="post" enctype="multipart/form-data" name="visualForm" onsubmit="return visualForm_onsubmit()" id="visualForm">
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td align="left"><%com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, fd);
			out.print(rd.rendForAdd());
		  %><br />
	<%if (fd.isHasAttachment()) {%>			  
		<table style="width:98%" align="center"><tr><td><script>initUpload()</script></td></tr></table>
	<%}%>
		</td>
    </tr>
    <tr>
      <td height="30" align="center"><input class="btn" type="submit" name="Submit" id="btn_submit" value=" 添 加 " />
      <input name="cws_id" value="<%=relateFieldValue%>" type="hidden" />
	  </td>
    </tr>
</table>
<span id="spanTempCwsIds"></span>
</form>
</body>
<script>
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
</html>
