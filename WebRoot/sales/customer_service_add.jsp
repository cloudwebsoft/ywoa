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
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="../js/jquery.raty.min.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>
<script src="../inc/upload.js"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />

<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.min.css" />

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

	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
	return;
}

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);

if (op.equals("saveformvalue")) {
	boolean re = false;
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	try {
		re = fdm.create(application, request);
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
		out.print(StrUtil.jAlert_Redirect("保存成功！","提示", "customer_service_list.jsp?parentId=" + parentId + "&customerId=" + customerId + "&formCodeRelated=" + formCodeRelated + "&formCode=" + formCode + "&isShowNav=" + isShowNav));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}

// 置嵌套表需要用到的pageType
request.setAttribute("pageType", "add");

 %>
<%@ include file="customer_inc_nav.jsp"%>
<script>
o("menu6").className="current"; 
</script>
<div class="spacerH"></div>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <form action="?op=saveformvalue&customerId=<%=customerId%>&parentId=<%=parentId%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&isShowNav=<%=isShowNav%>" method="post" enctype="multipart/form-data" name="visualForm" id="visualForm">
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
      <td height="30" align="center"><input class="btn" type="submit" name="Submit" value=" 添 加 " />
      <input name="cws_id" value="<%=relateFieldValue%>" type="hidden" />
	  </td>
    </tr>
  </form>
</table>
</body>
</html>
