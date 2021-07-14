<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="org.json.*"%>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	if (!privilege.isUserPrivValid(request, "read")) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	String dt = DateUtil.format(new Date(), "yyyy-MM-dd");

	String formCode = ParamUtil.get(request, "formCode");
	String formCodeRelated = ParamUtil.get(request, "formCodeRelated");
	String menuItem = ParamUtil.get(request, "menuItem");
	String moduleCode = ParamUtil.get(request, "code");

	String mode = ParamUtil.get(request, "mode");
	String tagName = ParamUtil.get(request, "tagName");
	int parentId = ParamUtil.getInt(request, "parentId", -1);

	// 通过选项卡标签关联
	boolean isSubTagRelated = "subTagRelated".equals(mode);

	if (isSubTagRelated) {
		String tagUrl = ModuleUtil.getModuleSubTagUrl(moduleCode, tagName);
		try {
			JSONObject json = new JSONObject(tagUrl);
			if (!json.isNull("formRelated")) {
				formCodeRelated = json.getString("formRelated");
			}
			else {
				out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "选项卡关联配置不正确！"));
				return;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8" />
	<meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no" />
	<title>日历看板</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<link rel="stylesheet" type="text/css" href="../js/fullcalendar/main.min.css"/>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script type="text/javascript" src="../js/fullcalendar/main.min.js" ></script>
	<script src='../js/fullcalendar/locales-all.js'></script>
</head>
<body>
<%@ include file="module_inc_menu_top.jsp"%>
<script>
	o("menu<%=menuItem%>").className="current";
</script>
<div class="spacerH"></div>
<div class="canlendar" id="calendar" style="width: 90%; margin: 0px auto"></div>
<script>
	function format(date, fmt) {
		if (typeof date == 'string') {
			return date;
		}

		if (!fmt) fmt = "yyyy-MM-dd hh:mm:ss";

		if (!date || date == null) return null;
		var o = {
			'M+': date.getMonth() + 1, // 月份
			'd+': date.getDate(), // 日
			'h+': date.getHours(), // 小时
			'm+': date.getMinutes(), // 分
			's+': date.getSeconds(), // 秒
			'q+': Math.floor((date.getMonth() + 3) / 3), // 季度
			'S': date.getMilliseconds() // 毫秒
		}
		if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length))
		for (var k in o) {
			if (new RegExp('(' + k + ')').test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length === 1) ? (o[k]) : (('00' + o[k]).substr(('' + o[k]).length)))
		}
		return fmt
	}

	document.addEventListener('DOMContentLoaded', function() {
		var initialLocaleCode = 'zh-cn';

		var calendarEl = document.getElementById('calendar');

		var calendar = new FullCalendar.Calendar(calendarEl, {
			initialDate: '<%=dt%>',
			initialView: 'dayGridMonth',
			nowIndicator: true,
			locale: initialLocaleCode,
			headerToolbar: {
				left: 'prev,next today',
				center: 'title',
				right: 'dayGridMonth,timeGridWeek,timeGridDay' // ,listWeek'
			},
			weekNumbers: true,
			weekNumberCalculation: 'ISO',
			navLinks: true, // can click day/week names to navigate views
			editable: false,
			selectable: true,
			selectMirror: true,
			dayMaxEvents: true, // allow "more" link when too many events
			displayEventEnd: true,
			events: function (info, successCallback, failureCallback) {//加载事件
				var start = format(info.start, 'yyyy-MM-dd hh:mm:ss');
				var end = format(info.end, 'yyyy-MM-dd hh:mm:ss');
				var json = new Array();
				json = getEvents(start, end);//获取事件
				successCallback(json);//回调函数
				//返回数据字段
				//id:标识
				//title:标题
				//color:事件颜色
				//textColor:标题颜色
				//start:开始时间
				//end:结束时间
				//editable:是否可拖动

			},
			loading: function(bool) {
				// document.getElementById('loading').style.display = bool ? 'block' : 'none';
			},
			eventClick: function(info) {
				// console.log(info);
				addTab(info.event.title, '<%=request.getContextPath()%>/visual/module_show.jsp?id=' + info.event.id + '&code=<%=formCodeRelated%>');
				/*alert('Event: ' + info.event.title);
				alert('Coordinates: ' + info.jsEvent.pageX + ',' + info.jsEvent.pageY);
				alert('View: ' + info.view.type);*/

				// change the border color just for fun
				// info.el.style.borderColor = 'red';

			}
		});

		calendar.render();
	});

	function getEvents(start, end) {
		var r = [];
		$.ajax({
			type: "get",
			async: false,
			url: "moduleListCalendar.do",
			data: {
				code: "<%=formCodeRelated%>",
				start: start,
				end: end
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				// $('body').showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				r = data;
			},
			complete: function(XMLHttpRequest, status){
				// $('body').hideLoading();
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
		return r;
	}

</script>
</body>
</html>