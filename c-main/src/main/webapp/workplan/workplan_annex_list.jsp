<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.db.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>工作计划进度信息</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
	<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
	<link href="../lte/css/font-awesome.css?v=4.4.0" rel="stylesheet">
	<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
	<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
	<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
	<script src="../js/datepicker/jquery.datetimepicker.js"></script>
	<style>
		.workplan_appraise {
			color: blue;
		}
	</style>
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
String priv="read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
int workplan_annex_day_edit_limit = cfg.getInt("workplan_annex_day_edit_limit");
%>
<%@ include file="workplan_show_inc_menu_top.jsp"%>
<script>
	o("menu3").className = "current";
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
<div class="spacerH"></div>
<%
	int id = ParamUtil.getInt(request, "id");

	long taskId = ParamUtil.getLong(request, "taskId", -1);
	String me = privilege.getUser(request);
	String op = ParamUtil.get(request, "op");
	if (op.equals("delAnnex")) {
		boolean re = false;
		try {%>
<script>
	$(".treeBackground").addClass("SD_overlayBG2");
	$(".treeBackground").css({"display": "block"});
	$(".loading").css({"display": "block"});
</script>
<%
	WorkPlanAnnexMgr wam = new WorkPlanAnnexMgr();
	re = wam.del(request);
%>
<script>
	$(".loading").css({"display": "none"});
	$(".treeBackground").css({"display": "none"});
	$(".treeBackground").removeClass("SD_overlayBG2");
</script>
<%
		} catch (Exception e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		}
		if (re) {
			out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "提示", "workplan_show.jsp?id=" + id));
		} else {
			out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_success"), "提示"));
		}
		return;
	}

	WorkPlanMgr wpm = new WorkPlanMgr();
	WorkPlanDb wpd = null;
// 检查权限
	try {
		wpd = wpm.getWorkPlanDb(request, id, "see");
	} catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}

	boolean isWorkPlanManager = false;

	com.redmoon.oa.workplan.Privilege pvg = new com.redmoon.oa.workplan.Privilege();
	if (pvg.canUserManageWorkPlan(request, id))
		isWorkPlanManager = true;

	int year = ParamUtil.getInt(request, "year", -1);
	int month = ParamUtil.getInt(request, "month", -1);
	int day = ParamUtil.getInt(request, "day", -1);
	long total = 0;
	int pagesize = 20;
	int curpage = ParamUtil.getInt(request, "CPages", 1);

	String action = ParamUtil.get(request, "action");
	String strBeginDate = ParamUtil.get(request, "beginDate");
	String strEndDate = ParamUtil.get(request, "endDate");
	Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
	Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
	String what = ParamUtil.get(request, "what");
	WorkPlanAnnexDb wad = new WorkPlanAnnexDb();
	ListResult lr = null;
	if (taskId != -1) {
		String sql = wad.getTable().getSql("listWorkPlanAnnexOfTask");
		lr = wad.listResult(sql, new Object[]{new Long(id), new Long(taskId), DateUtil.getDate(year, month - 1, day)}, curpage, pagesize);
	} else {
		// 如果年份不为-1，则为取某一天的汇报
		if (year != -1) {
			String sql = wad.getTable().getSql("listWorkPlanAnnexForDay");
			lr = wad.listResult(sql, new Object[]{new Long(id), DateUtil.getDate(year, month - 1, day)}, curpage, pagesize);
		} else {
			if ("search".equals(action)) {
				String sql = wad.getSqlForSearch(id, beginDate, endDate, what);
				lr = wad.listResult(sql, curpage, pagesize);
			}
			else {
				String sql = wad.getTable().getSql("listWorkPlanAnnex");
				lr = wad.listResult(sql, new Object[]{new Long(id)}, curpage, pagesize);
			}
		}
	}

	total = lr.getTotal();

	Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages == 0) {
		curpage = 1;
		totalpages = 1;
	}

	UserMgr um = new UserMgr();
	WorkPlanTaskDb wptd = new WorkPlanTaskDb();

	// 如果不是显示指定的某天
	if (year==-1) {
%>
<table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0" align="center">
	<tr>
		<td style="text-align: center">
			<form id="formSearch" name="formSearch" action="workplan_annex_list.jsp" method="get">
				<input name="action" value="search" type="hidden"/>
				<input name="id" value="<%=id%>" type="hidden"/>
				从
				<input id="beginDate" name="beginDate" value="<%=strBeginDate%>" size=10>&nbsp;
				至
				<input id="endDate" name="endDate" value="<%=strEndDate%>" size=10>&nbsp;
				内容
				<input name=what size=20 value="<%=what%>">
				&nbsp;
				<input class="tSearch" name="submit" type=submit value="搜索">
			</form>
		</td>
	</tr>
</table>
<%
	}
%>
<table width="98%" align="center" border="0" cellpadding="0" cellspacing="0" class="percent80" height="25">
	<tr>
		<td width="50%" align="left">
			<%
				// 如果显示指定的某天
				if (year!=-1) {
			%>
			<%=year%>-<%=month%>-<%=day%>
			<%
				}
			%>
			<%
				if (taskId == -1) {
					if (year!=-1) {
			%>
			&nbsp;&nbsp;<a href="workplan_annex_list.jsp?id=<%=id%>&taskId=<%=taskId%>">全部汇报</a>
			<%
					}
			%>
			&nbsp;&nbsp;<i class="fa fa-th" style="color:#666" aria-hidden="true"></i>&nbsp;<a title="日历视图" href="workplan_annex_day.jsp?id=<%=id%>">日历</a>
			<%
				}
			%>
		</td>
		<td width="50%" align="right">&nbsp;找到符合条件的记录 <b><%=paginator.getTotal() %>
		</b> 条　每页显示 <b><%=paginator.getPageSize() %>
		</b> 条　页次 <b><%=curpage %>/<%=totalpages %>
		</b></td>
	</tr>
</table>
<%
        Iterator ir = lr.getResult().iterator();
		WorkPlanAnnexAttachment wfaa = new WorkPlanAnnexAttachment();	
		while (ir.hasNext()) {
			wad = (WorkPlanAnnexDb)ir.next();
			String zrr = wad.getString("user_name"); // 责任人
		%>
        <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent80">
        <thead>
          <tr>
            <td height="22" class="tabStyle_1_title" style="text-align:left">
            <span style="float:left">
            <%
			WorkPlanTaskDb task = (WorkPlanTaskDb)wptd.getQObjectDb(new Long(wad.getLong("task_id")));
			if (task!=null) {
				if (wad.getInt("check_status")==WorkPlanAnnexDb.CHECK_STATUS_WAIT) {
				  %>
				  <img id="chk_img_<%=wad.getLong("id")%>" src="../images/check_wait.png" align="absmiddle" />
				  <%
				}
				else if (wad.getInt("check_status")==WorkPlanAnnexDb.CHECK_STATUS_PASSED) {
				  %>
			  <img src="../images/check_pass.png" align="absmiddle" />
				  <%
				}
				else if (wad.getInt("check_status")==WorkPlanAnnexDb.CHECK_STATUS_UNPASS) {
				  %>
				  <img src="../images/check_unpass.png" align="absmiddle" />
				  <%
				}
				else {
				  %>
				  <img src="../images/fileicon/txt.gif" align="absmiddle" />
			  <%
				}
				%>
				<a title="只看该任务的进展" href="workplan_annex_list.jsp?id=<%=id%>&taskId=<%=task.getLong("id")%>"><span style="color:#000"><%=task.getString("name")%></span></a>&nbsp;&nbsp;
            <%}%>            
          	用户：<%=um.getUserDb(wad.getString("user_name")).getRealName()%>
            &nbsp;&nbsp;&nbsp;日期： <%=DateUtil.format(wad.getDate("add_date"), "yyyy-MM-dd")%>
            <%
			if (wad.getInt("annex_type")==WorkPlanAnnexDb.TYPE_NORMAL) {
				%>
				[日报]
				<%
			}
			else if (wad.getInt("annex_type")==WorkPlanAnnexDb.TYPE_FLOW) {
				%>
				&nbsp;&nbsp;[&nbsp;汇报流程&nbsp;]
				<%
			}
			%>
            </span>
				<span style="float:right">
			<%
			// 流程型的汇报不能被编辑和删除
			if (wad.getInt("annex_type")==WorkPlanAnnexDb.TYPE_NORMAL) {
				// 计划负责人或者任务的责任人可以修改
				if (isWorkPlanManager || zrr.equals(me)) {
					Date addDate = wad.getDate("add_date");
					if (DateUtil.datediff(new java.util.Date(), addDate)<=workplan_annex_day_edit_limit) {

			%>
            		&nbsp;&nbsp;&nbsp;<a href="workplan_annex_edit.jsp?annexId=<%=wad.getLong("id")%>&id=<%=id%>"><span style="color:#222">编辑</span></a>
			<%
					}
				}
				if (isWorkPlanManager) {
				%>
					&nbsp;&nbsp;<a href="javascript:;" onclick="jConfirm('您确定要删除吗？','提示',function(r){ if(!r){return;}else{window.location.href='workplan_show.jsp?op=delAnnex&amp;annexId=<%=wad.getLong("id")%>&amp;id=<%=id%>'}}) "
																		style="cursor:pointer"><span style="color:#222">删除</span></a>
            <%
				}
			}
			%>
            	</span>
			</td>
          </tr>
        </thead>
          <tr>
            <td height="22">
            <%if (wad.getInt("old_progress")!=wad.getInt("progress")) {%>
                <div style="clear:both;">
                    <div style="float:left; margin-top:5px">原进度&nbsp;</div>
                    <div style="float:left">
                        <div class="progressBar" style="padding:0px; margin:0px; height:20px; width:100px">
                          <div class="progressBarFore" style="width:<%=wad.getInt("old_progress")%>%;">
                          </div>
                            <div class="progressText">
                              <%=wad.getInt("old_progress")%>%
                            </div>
                        </div>
                    </div>
                </div>
                <div style="clear:both; padding-top:3px">
                  <div style="float:left; margin-top:5px;">现进度&nbsp;</div>
                  <div style="float:left">
                      <div class="progressBar" style="padding:0px; margin:0px; height:20px; width:100px">
                        <div class="progressBarFore" style="width:<%=wad.getInt("progress")%>%;">
                        </div>
                          <div class="progressText">
                            <%=wad.getInt("progress")%>%
                          </div>
                      </div>
                  </div>
                </div>
            <%}else{%>
                <div style="float:left; margin-top:5px;">&nbsp;进度未变&nbsp;</div>
                <div style="float:left">
                  <div class="progressBar" style="padding:0px; margin:0px; height:20px; width:100px">
                      <div class="progressBarFore" style="width:<%=wad.getInt("progress")%>%;">
                      </div>
                      <div class="progressText">
                        <%=wad.getInt("progress")%>%
                      </div>
                  </div>
                </div>
			<%}%>
            </td>
          </tr>
          <tr>
            <td>
			<%
			if (wad.getInt("annex_type")==WorkPlanAnnexDb.TYPE_FLOW) {
                WorkflowDb wf = wad.getWorkflowDb();
            %>
                流程：&nbsp;&nbsp;<a href="javascript:;" onclick="addTab('<%=task.getString("name")%>汇报流程', '<%=request.getContextPath()%>/flow_modify.jsp?flowId=<%=wf.getId()%>')"><%=wf.getTitle()%></a>&nbsp;&nbsp;(<%=wf.getStatusDesc()%>)
            <%}%>            
			<%=wad.getString("content")%>
        	<%
			Vector wfaav = wfaa.getAttachments(wad.getLong("id"));
			Iterator wfaair = wfaav.iterator();
			while (wfaair.hasNext()) {
				WorkPlanAnnexAttachment aa = (WorkPlanAnnexAttachment)wfaair.next();
			%>
                <br />
                <%if (StrUtil.isImage(StrUtil.getFileExt(aa.getDiskName()))) {%>
                <div align="center"><a href="<%=aa.getAttachmentUrl(request)%>" target="_blank"><img src="<%=aa.getAttachmentUrl(request)%>" border="0" onload="javascript:if(this.width>screen.width-333)this.width=screen.width-333" /></a></div>
          <%}else{%>
                <a href="<%=aa.getAttachmentUrl(request)%>" target="_blank"><img style="width:16px;margin-right:5px" src="../netdisk/images/sort/<%=com.redmoon.oa.netdisk.Attachment.getIcon(StrUtil.getFileExt(aa.getDiskName()))%>" border="0" /><%=aa.getName()%></a>
			  <%}
			}
			
			String appraise = "";
			if (wad.getString("appraise")!=null)
				appraise = wad.getString("appraise");
			if (!appraise.equals("")) {
				%>
              <div class="workplan_appraise" style="border-top:1px dashed #cccccc">
                    <div style="padding:5px 0px">
                    <%
					UserDb checker = new UserDb();
					checker = checker.getUserDb(wad.getString("checker"));
					%>
                    <%=checker.getRealName()%>&nbsp;&nbsp;<%=DateUtil.format(wad.getDate("check_date"), "yyyy-MM-dd HH:mm:ss")%>
                    </div>
                    <%=StrUtil.toHtml(appraise)%>
              </div>
				<%
			}
			%>
			</td>
          </tr>
        </table>
<%}%>
        <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="percent80">
          <tr>
            <td height="23" align="right"><%
				String querystr = "id=" + id + "&taskId=" + taskId + "&action=" + action + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate + "&what=" + StrUtil.UrlEncode(what);
				out.print(paginator.getPageBlock(request,"workplan_annex_list.jsp?"+querystr));
				%>
              &nbsp;&nbsp;</td>
          </tr>
      </table>

<div id="dlg" style="display:none">
	<form id="form1">
		进度&nbsp;<input id="progress" name="progress" size="3"/>&nbsp;%
		<br/>
		结果&nbsp;
		<input id="check_status" name="check_status" type="radio" value="<%=WorkPlanAnnexDb.CHECK_STATUS_PASSED%>"
			   checked/>
		<img src="../images/check_pass.png" align="absmiddle"/>通过&nbsp;
		<input name="check_status" type="radio" value="<%=WorkPlanAnnexDb.CHECK_STATUS_UNPASS%>"/>
		<img src="../images/check_unpass.png" align="absmiddle"/>不通过
		<br/>
		意见&nbsp;
		<textarea id="appraise" name="appraise" style="width:290px; height:100px"></textarea>
	</form>
</div>
</body>
<script>
	$(function () {
		$('#beginDate').datetimepicker({
			lang:'ch',
			timepicker:false,
			format:'Y-m-d',
			formatDate:'Y/m/d',
		});
		$('#endDate').datetimepicker({
			lang:'ch',
			timepicker:false,
			format:'Y-m-d',
			formatDate:'Y/m/d',
		});
	});

	var progressCtl = new LiveValidation('progress');
	progressCtl.add(Validate.Presence);
	progressCtl.add(Validate.Numericality, {minimum: 0, maximum: 100});

	function check(annexId, progress) {
		o("progress").value = progress;
		$("#dlg").dialog({
			title: "审核",
			modal: true,
			bgiframe: true,
			width: 370,
			height: 230,
			// bgiframe:true,
			buttons: {
				"取消": function () {
					$(this).dialog("close");
				},
				"确定": function () {
					if (!LiveValidation.massValidate(progressCtl.formObj.fields)) {
						return false;
					}
					$.ajax({
						type: "post",
						url: "workplan_do.jsp",
						data: {
							op: "checkAnnex",
							check_status: getRadioValue("check_status"),
							progress: o("progress").value,
							appraise: o("appraise").value,
							id: annexId
						},
						dataType: "html",
						beforeSend: function (XMLHttpRequest) {
							$('#dlg').showLoading();
						},
						success: function (data, status) {
							data = $.parseJSON(data);
							if (data.ret == "0") {
								jAlert(data.msg, "提示");
							} else {
								jAlert(data.msg, "提示");
								if (getRadioValue("check_status") == "<%=WorkPlanAnnexDb.CHECK_STATUS_PASSED%>")
									o("chk_img_" + annexId).src = "../images/check_pass.png";
								else
									o("chk_img_" + annexId).src = "../images/check_unpass.png";
							}
						},
						complete: function (XMLHttpRequest, status) {
							$('#dlg').hideLoading();
						},
						error: function (XMLHttpRequest, textStatus) {
							// 请求出错处理
							alert(XMLHttpRequest.responseText);
						}
					});

					$(this).dialog("close");
				}
			},
			closeOnEscape: true,
			draggable: true,
			resizable: true
		});
}
</script>
</html>
