<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "java.text.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>工作计划 - 周报</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
	<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
	<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
	<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
	<style type="text/css">
		.loading {
			display: none;
			position: fixed;
			z-index: 1801;
			top: 45%;
			left: 45%;
			width: 100%;
			margin: auto;
			height: 100%;
		}

		.SD_overlayBG2 {
			background: #FFFFFF;
			filter: alpha(opacity=20);
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
			filter: alpha(opacity=80);
		}
	</style>
	<script>
		function yearChange(obj, isChange) {
			y = document.form1.year.value;
			if (isChange == true) {
				y--;
				document.form1.year.value = y;
				o("form1").submit();
			} else {
				y++;
				document.form1.year.value = y;
				o("form1").submit();
			}
		}

		function monthChange(obj, isChange) {
			m = document.form1.month.value;
			if (isChange == true) {
				m--;
				document.form1.month.value = m;
				o("form1").submit();
			} else {
				m++;
				document.form1.month.value = m;
				o("form1").submit();
			}
		}

		function onYearChange(obj) {
			var y = obj.options[obj.options.selectedIndex].value;
			form1.year.value = y;
			o("form1").submit();
		}

		function onMonthChange(obj) {
			var m = obj.options[obj.options.selectedIndex].value;
			document.form1.month.value = m;
			o("form1").submit();
		}
	</script>
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
	String priv = "read";
	if (!privilege.isUserPrivValid(request, priv)) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
%>
<%@ include file="workplan_show_inc_menu_top.jsp" %>
<script>
	o("menu8").className = "current";
</script>
<div class="spacerH"></div>
<%
	int id = ParamUtil.getInt(request, "id");
	int y = ParamUtil.getInt(request, "year", -1);
	int m = ParamUtil.getInt(request, "month", -1);
	Calendar c1 = Calendar.getInstance();
	int year = c1.get(Calendar.YEAR);
	if (y == -1) {
		y = year;
	}
	if (m == -1) {
		m = c1.get(Calendar.MONTH) + 1;
	}

	String op = ParamUtil.get(request, "op");
	if (op.equals("delAnnex")) {
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

	com.redmoon.oa.workplan.Privilege pvg = new com.redmoon.oa.workplan.Privilege();
	if (!pvg.canUserSeeWorkPlan(request, id)) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	WorkPlanDb wpd = new WorkPlanDb();
	wpd = wpd.getWorkPlanDb(id);

	WorkPlanAnnexDb wad = new WorkPlanAnnexDb();

	UserMgr um = new UserMgr();
	WorkPlanTaskDb wptd = new WorkPlanTaskDb();
	// 取出根任务
	try {
		wptd = wptd.getRootTask(id);
	} catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}

	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	int workplan_annex_week_add_limit = cfg.getInt("workplan_annex_week_add_limit");
	int workplan_annex_week_edit_limit = cfg.getInt("workplan_annex_week_edit_limit");
%>
<table width="98%" align="center" border="0" cellpadding="0" cellspacing="0" class="percent80" height="25">
	<tr>
		<td align="center">
			<form id="form1" name="form1" action="workplan_annex_list_week.jsp" method="get">
				开始时间：<%=DateUtil.format(wpd.getBeginDate(), "yyyy-MM-dd")%>
				&nbsp;&nbsp;
				结束时间：<%=DateUtil.format(wpd.getEndDate(), "yyyy-MM-dd")%>
				<input id="id" name="id" type="hidden" value="<%=id%>"/>
				&nbsp;&nbsp;<a href="#" onclick="yearChange(<%=y%>,true)"><img title="上一年" src="../plan/images/1.gif"/></a>
				&nbsp; <a href="#" onclick="monthChange(<%=m%>,true)">
				<img title="上一月" src="../plan/images/4.gif"/></a> &nbsp;
				<select id="year" name="year" onChange="onYearChange(this)">
					<%
						year += 1;
						for (int i = 0; i < 30; i++) {
							if (y == year) {
					%>
					<option value="<%=year%>" selected="selected"><%=year%>年</option>
					<%} else {%>
					<option value="<%=year%>"><%=year%>年</option>
					<%
							}
							year--;
						}
					%>
				</select>
				<select id="month" name="month" onchange="onMonthChange(this)">
					<%
						for (int i = 1; i <= 12; i++) {
							if (m == i) {
					%>
					<option value="<%=i%>" selected="selected"><%=i%>月</option>
					<%} else {%>
					<option value="<%=i%>"><%=i%>月</option>
					<%
							}
						}
					%>
				</select>
				&nbsp; <a href="#" onclick="monthChange(<%=m%>,false)"><img title="下一月" src="../plan/images/3.gif"/></a>
				&nbsp; <a href="#" onclick="yearChange(<%=y%>,false)">
				<img title="下一年" src="../plan/images/2.gif"/></a>
				（超期<%=workplan_annex_week_add_limit%>天内可汇报，汇报后<%=workplan_annex_week_edit_limit%>天内可修改）
			</form>
		</td>
	</tr>
</table>
<table id="mainTable" width="98%" align="center" class="tabStyle_1 percent98">
  <tr>
    <td class="tabStyle_1_title" width="7%">周数</td>
    <td class="tabStyle_1_title" width="9%">时间</td>
    <td class="tabStyle_1_title" width="8%">汇报人</td>
    <td class="tabStyle_1_title" width="14%">进度</td>
    <td class="tabStyle_1_title" width="51%">内容</td>
    <td class="tabStyle_1_title" width="11%">操作</td>
  </tr>
  <%
	int dd = DateUtil.getDayCount(y, m-1);
	//得到每月的第一天和最后一天是一年的第几周
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
    Calendar c = Calendar.getInstance();
	c.setFirstDayOfWeek(Calendar.MONDAY);	

    c.setTime(df.parse(y+"-"+m+"-"+"1"));
    int e = c.get(Calendar.DAY_OF_WEEK)-1;//每月的第一天是星期几
	if(e == 0){
		e=7;
	}
	int ww[] = new int[2];
	ww[0] = c.get(Calendar.WEEK_OF_YEAR);
	c.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(y+"-"+m+"-"+dd));

	int week1 = c.get(Calendar.WEEK_OF_YEAR);
	ww[1] = week1+1;

	Calendar current = Calendar.getInstance();
	WorkPlanAnnexAttachment wfaa = new WorkPlanAnnexAttachment();
	if (ww[0]>ww[1]) {
		c.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(y+"-"+m+"-"+ (dd-7)));	
		ww[1] = c.get(Calendar.WEEK_OF_YEAR);
	}
	
	for(int i=ww[0];i<=ww[1];i++){%>
  <tr>
    <td style="height:80px" align="center">第<%=i%>周
    </td>
    <td style="height:80px" align="center"><%
	Calendar calendar = Calendar.getInstance();
	calendar.clear();
	calendar.setFirstDayOfWeek(Calendar.MONDAY);
	calendar.set(Calendar.WEEK_OF_YEAR, i);
	calendar.set(Calendar.YEAR, y);
	
	// Now get the first day of week.
	Date date = calendar.getTime();
	Date date1 = DateUtil.addDate(date, 6);
	%>
    <%=DateUtil.format(date, "yyyy-MM-dd")%>
    至
    <%=DateUtil.format(date1, "yyyy-MM-dd")%>
    </td>
    <td align="center">
    <%
	WorkPlanAnnexDb wpa = wad.getWorkPlanAnnexDb(id, y, WorkPlanAnnexDb.TYPE_WEEK, i);
	if (wpa!=null) {
	%>
    <%=um.getUserDb(wpa.getString("user_name")).getRealName()%>
    <%}%>
    </td>
    <td align="center">
    <%
	if (wpa!=null) {
	%>	
        <div class="progressBar" style="padding:0px; margin:0px; height:20px; width:120px">
          <div class="progressBarFore" style="width:<%=wpa.getInt("progress")%>%;">
          </div>
            <div class="progressText">
              <%=wpa.getInt("progress")%>%
            </div>
        </div>
    <%}%>
    </td>
    <td align="left">
    <%
	if (wpa!=null) {
			out.print(wpa.getString("content"));

			Vector wfaav = wfaa.getAttachments(wpa.getLong("id"));
			Iterator wfaair = wfaav.iterator();
			while (wfaair.hasNext()) {
				WorkPlanAnnexAttachment aa = (WorkPlanAnnexAttachment)wfaair.next();
			%>
                <br />
                <%if (StrUtil.isImage(StrUtil.getFileExt(aa.getDiskName()))) {%>
                <div align="center" style="padding-bottom:5px"><a href="<%=aa.getAttachmentUrl(request)%>" target="_blank"><img src="<%=aa.getAttachmentUrl(request)%>" border="0" onload="javascript:if(this.width>screen.width-333)this.width=screen.width-333" /></a></div>
          <%}else{%>
                <div style="padding-bottom:5px"><a href="<%=aa.getAttachmentUrl(request)%>" target="_blank"><img src="../netdisk/images/<%=com.redmoon.oa.netdisk.Attachment.getIcon(StrUtil.getFileExt(aa.getDiskName()))%>" border="0" /><%=aa.getName()%></a></div>
			  <%}
			}
			
			String appraise = "";
			if (wpa.getString("appraise")!=null)
				appraise = wpa.getString("appraise");
			if (!appraise.equals("")) {
				%>
              <div class="workplan_appraise" style="border-top:1px dashed #cccccc; padding-top:5px">
                    <div style="padding:5px 0px">
                    <%
					UserDb checker = new UserDb();
					checker = checker.getUserDb(wpa.getString("checker"));
					%>
                    <%=checker.getRealName()%>&nbsp;&nbsp;<%=DateUtil.format(wpa.getDate("check_date"), "yyyy-MM-dd HH:mm:ss")%>
                    </div>
                    <div id="appraise<%=wpa.getLong("id")%>"><%=StrUtil.toHtml(appraise)%></div>
              </div>
				<%
			}
			%>		
		<%
	}
	%>
    </td>
    <td align="center">
    <%
	if (pvg.canUserManageWorkPlan(request, id)) {
		if (wpa==null) {
            Date now = new java.util.Date();
            if (now.before(wpd.getEndDate()) && now.after(wpd.getBeginDate())) {
                int expireDays = DateUtil.datediff(now, date1);
                // 处于一周的最后一天或在规定的超期范围内
                if ((expireDays>=0 && expireDays <= workplan_annex_week_add_limit) || (now.before(DateUtil.addDate(date1, 1)) && now.after(date))) {
                %>
                <a href="<%=request.getContextPath()%>/workplan/workplan_annex_add.jsp?id=<%=id%>&annexYear=<%=y%>&annexType=<%=WorkPlanAnnexDb.TYPE_WEEK%>&annexItem=<%=i%>&taskId=<%=wptd.getLong("id")%>">汇报</a>
                <%
                }
            }
		}
		else {
			Date addDate = wpa.getDate("add_date");
			if (DateUtil.datediff(new java.util.Date(), addDate)<=workplan_annex_week_edit_limit) {
			%>
			<a href="javascript:;" onclick="addTab('第<%=i%>周 周报', '<%=request.getContextPath()%>/workplan/workplan_annex_edit.jsp?annexId=<%=wpa.getLong("id")%>&id=<%=id%>')">编辑</a>
			&nbsp;&nbsp;
            <%
			}
			%>
			<a href="javascript:;" onClick="jConfirm('您确定要删除吗？','提示',function(r){if(!r){return;}else{ window.location.href='workplan_show.jsp?op=delAnnex&annexId=<%=wpa.getLong("id")%>&id=<%=id%>&privurl=<%=StrUtil.getUrl(request)%>'}}) " style="cursor:pointer">删除</a>        
		<%
		}
	}
	%>
    </td>
  </tr>
  <%}
%>
</table>
</body>
<script>
	$(document).ready(function () {
		$("#mainTable td").mouseout(function () {
			if ($(this).parent().parent().get(0).tagName != "THEAD")
				$(this).parent().find("td").each(function (i) {
					$(this).removeClass("tdOver");
				});
		});

		$("#mainTable td").mouseover(function () {
			if ($(this).parent().parent().get(0).tagName != "THEAD")
				$(this).parent().find("td").each(function (i) {
					$(this).addClass("tdOver");
				});
		});
	});
</script>
</html>