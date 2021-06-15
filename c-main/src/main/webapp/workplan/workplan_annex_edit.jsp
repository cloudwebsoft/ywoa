<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作计划进度信息编辑</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />

<script src="../inc/upload.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
<script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
<script>
function form1_onsubmit() {
	if (o("progress").value>100) {
		jAlert("进度请填写小于或等于100的数值！","提示");
		return false;
	}
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
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0">
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String priv = "read";
	if (!privilege.isUserPrivValid(request, priv)) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	long annexId = ParamUtil.getInt(request, "annexId");

	WorkPlanAnnexDb wpad = new WorkPlanAnnexDb();
	wpad = (WorkPlanAnnexDb) wpad.getQObjectDb(new Long(annexId));

	int workplanId = wpad.getInt("workplan_id");
	WorkPlanDb wpd = new WorkPlanDb();
	wpd = wpd.getWorkPlanDb(workplanId);

	com.redmoon.oa.workplan.Privilege pvg = new com.redmoon.oa.workplan.Privilege();
	// 计划负责人或者本人可以修改
	if (pvg.canUserManageWorkPlan(request, workplanId)|| privilege.getUser(request).equals(wpad.getString("user_name"))) {
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		int workplan_annex_day_edit_limit = cfg.getInt("workplan_annex_day_edit_limit");
		int workplan_annex_week_edit_limit = cfg.getInt("workplan_annex_week_edit_limit");
		int workplan_annex_month_edit_limit = cfg.getInt("workplan_annex_month_edit_limit");
		Date addDate = wpad.getDate("add_date");
		int annexType = wpad.getInt("annex_type");
		if (annexType == WorkPlanAnnexDb.TYPE_NORMAL) {
			if (DateUtil.diff(new java.util.Date(), addDate) > workplan_annex_day_edit_limit) {
				out.print(SkinUtil.makeErrMsg(request, "已超期，不能修改！"));
				return;
			}
		}
		else if (annexType == WorkPlanAnnexDb.TYPE_WEEK) {
			if (DateUtil.diff(new java.util.Date(), addDate) > workplan_annex_week_edit_limit) {
				out.print(SkinUtil.makeErrMsg(request, "已超期，不能修改！"));
				return;
			}
		}
		else if (annexType == WorkPlanAnnexDb.TYPE_MONTH) {
			if (DateUtil.diff(new java.util.Date(), addDate) > workplan_annex_month_edit_limit) {
				out.print(SkinUtil.makeErrMsg(request, "已超期，不能修改！"));
				return;
			}
		}
	}
	else {
		out.print(SkinUtil.makeErrMsg(request, "权限非法！"));
		return;
	}

	String op = ParamUtil.get(request, "op");
	if (op.equals("edit")) {
		boolean re = false;
		try {
			WorkPlanAnnexMgr wam = new WorkPlanAnnexMgr();
%>
<script>
	$(".treeBackground").addClass("SD_overlayBG2");
	$(".treeBackground").css({"display": "block"});
	$(".loading").css({"display": "block"});
</script>
<%
	re = wam.save(application, request);
%>
<script>
	$(".loading").css({"display": "none"});
	$(".treeBackground").css({"display": "none"});
	$(".treeBackground").removeClass("SD_overlayBG2");
</script>
<%
	} catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "提示", "workplan_annex_edit.jsp?annexId=" + annexId + "&id=" + workplanId));
	} else {
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_success"), "提示"));
	}
	return;
} else if (op.equals("delAtt")) {
	long attId = ParamUtil.getLong(request, "attId");
	WorkPlanAnnexAttachment wpaa = new WorkPlanAnnexAttachment(attId);
	boolean re = false;
%>
<script>
	$(".treeBackground").addClass("SD_overlayBG2");
	$(".treeBackground").css({"display": "block"});
	$(".loading").css({"display": "block"});
</script>
<%
	re = wpaa.del();
%>
<script>
	$(".loading").css({"display": "none"});
	$(".treeBackground").css({"display": "none"});
	$(".treeBackground").removeClass("SD_overlayBG2");
</script>
<%
		if (re) {
			out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "workplan_annex_edit.jsp?annexId=" + annexId));
		} else {
			out.print(StrUtil.jAlert_Back("操作失败！", "提示"));
		}
		return;
	}
%>
<br>
<form action="workplan_annex_edit.jsp?op=edit&annexId=<%=annexId%>" name="form1" onsubmit="return form1_onsubmit()" method="post" enctype="multipart/form-data">
<table width="600" border="0" align="center" cellPadding="2" cellSpacing="0" class="tabStyle_1 percent80">
  <tbody>
    <tr>
      <td colspan="3" align="center" noWrap class="tabStyle_1_title"><%=wpd.getTitle()%>&nbsp;-&nbsp;汇报</td>
    </tr>
    <tr>
      <td width="12%" align="center" noWrap>任务</td>
      <td width="47%">
		<%
		WorkPlanTaskDb wptd = new WorkPlanTaskDb();
		wptd = (WorkPlanTaskDb)wptd.getQObjectDb(new Long(wpad.getLong("task_id")));
        %>
		<%=wptd.getString("name")%>，原进度为&nbsp;<%=wpad.getInt("old_progress")%>%
        ，现进度为&nbsp;
        <input id="progress" name="progress" title="如果进度不变，则表示仅汇报信息" size="3" value="<%=wpad.getInt("progress")%>" />&nbsp;%
        <input name="old_progress" value="<%=wpad.getInt("old_progress")%>" type="hidden" />
        <script>
        var progress = new LiveValidation('progress');
        progress.add(Validate.Presence);
        progress.add(Validate.Numericality, { minimum: 0, maximum: 100 });
        </script>      
      </td>
      <td width="41%" style="padding-left:10px">
      <div id="slider" style="width:50%"></div>
      <script>
        $( "#progress" ).change(function() {
          $( "#slider" ).slider( "value", $("#progress").val() );
        });
      </script>       
      </td>
    </tr>
    <tr>
      <td align="center" noWrap>内容</td>
      <td colspan="2">
        <textarea id="content" name="content" style="display:none"><%=wpad.getString("content")%></textarea>
        <script>
			CKEDITOR.replace('content',
				{
					// skin : 'kama',
					toolbar : 'Middle'
				});
		  </script>
        <input type=hidden name="id" value="<%=annexId%>" />
        </td>
    </tr>
    <%
	WorkPlanAnnexAttachment wpa = new WorkPlanAnnexAttachment();
	Vector attV = wpa.getAttachments(annexId);
	if (attV.size()>0) {
	%>
    <tr>
      <td align="center" noWrap>附件</td>
      <td colspan="2">
		<%
		java.util.Iterator attir = attV.iterator();
		while (attir.hasNext()) {
			WorkPlanAnnexAttachment att = (WorkPlanAnnexAttachment)attir.next();
		%>
          <div><img src="../netdisk/images/sort/<%=com.redmoon.oa.netdisk.Attachment.getIcon(StrUtil.getFileExt(att.getDiskName()))%>" width="17" height="17">&nbsp;<a target="_blank" href="<%=att.getAttachmentUrl(request)%>"><%=att.getName()%></a>&nbsp;&nbsp;&nbsp;<a href="#" onClick="jConfirm('您确定要删除吗？','提示',function(r){ if(!r){return;}else{window.location.href='workplan_annex_edit.jsp?op=delAtt&annexId=<%=wpad.getLong("id")%>&attId=<%=att.getId()%>'}}) " style="cursor:pointer">删除</a></div>
        <%}%>
      </td>
    </tr>
    <%}%>
    <tr>
      <td colspan="3" noWrap>
		<script>initUpload()</script>
      </td>
    </tr>
    <tr align="middle">
      <td colSpan="3" align="center" noWrap><input class="btn" type="submit" value="确认">
      	<input name="workplanId" value="<%=workplanId%>" type="hidden" />
        <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden" />
        <%
		com.redmoon.oa.workplan.Privilege pvgWorkPlan = new com.redmoon.oa.workplan.Privilege();
		int checkStatus = 0;
		if (pvgWorkPlan.canUserManageWorkPlan(request, workplanId)) {
			checkStatus = 1;
		}
		%>
        <input name="check_status" value="<%=checkStatus%>" type="hidden" />      
      </td>
    </tr>
  </tbody>
</table>
</form>
</body>
<script>
$(function() {
	$( "#slider" ).slider({
	  value:<%=wptd.getInt("progress")%>,
	  min: 0,
	  max: 100,
	  step: 5,
	  slide: function( event, ui ) {
		$( "#progress" ).val( ui.value );
	  }
	});
	// $( "#progress" ).val( $( "#slider" ).slider( "value" ) );
  });
</script>
</html>