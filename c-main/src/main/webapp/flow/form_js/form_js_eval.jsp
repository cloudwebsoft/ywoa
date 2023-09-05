<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%
	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
	response.setContentType("text/javascript;charset=utf-8");

	int flowId = ParamUtil.getInt(request, "flowId", -1);
	String pageType = ParamUtil.get(request, "pageType");
	if (!ConstUtil.PAGE_TYPE_FLOW.equals(pageType)) {
		return;
	}
%>
function onNestAutoSelDone(event) {
	if (event.moduleCode == 'eval_dtl') {
		if (event.newIds.length > 0) {
			// 自动拉单
			refreshScore();
		}
		else {
			// 未自动拉单
		}
	}
}

function refreshScore() {
	// 统计分数
	$.ajax({
		type: "post",
		url: "score/calc.do",
		data: {
			moduleCode: 'eval_dtl',
			flowId: <%=flowId%>,
			beginYear: o('begin_year').value,
			endYear: o('end_year').value,
			beginMonth: o('begin_month').value,
			endMonth: o('end_month').value,
		},
		dataType: "json",
		beforeSend: function (XMLHttpRequest) {
			$('body').showLoading();
		},
		success: function (data, status) {
			if (data.ret == 1) {
				$.toaster({ priority : 'info', message : '自动计分已完成！' });
				refreshNestSheetCtleval_dtl();
			}
			else {
				$.toaster({ priority : 'info', message : data.msg });
			}
		},
		complete: function (XMLHttpRequest, status) {
			$('body').hideLoading();
		},
		error: function () {
			alert(XMLHttpRequest.responseText);
		}
	});
}

$(function() {
	if (eventTarget) {
		<%--eventTarget.addEvent(EVENT_TYPE.NEST_ADD, onNestChange);
		eventTarget.addEvent(EVENT_TYPE.NEST_EDIT, onNestChange);
		eventTarget.addEvent(EVENT_TYPE.NEST_DEL, onNestChange);--%>
		eventTarget.addEvent(EVENT_TYPE.NEST_AUTO_SEL_DONE, onNestAutoSelDone);
	}

	var oldBeginYearVal = o("begin_year").value;
	var oldBeginMonthVal = o("begin_month").value;
	var oldEndYearVal = o("end_year").value;
	var oldEndMonthVal = o("end_month").value;
	setInterval(function(){
		if (oldBeginYearVal != o("begin_year").value) {
			refreshScore();
			oldBeginYearVal = o("begin_year").value;
		}
		if (oldBeginMonthVal != o("begin_month").value) {
			refreshScore();
			oldBeginMonthVal = o("begin_month").value;
		}
		if (oldEndYearVal != o("end_year").value) {
			refreshScore();
			oldEndYearVal = o("end_year").value;
		}
		if (oldEndMonthVal != o("end_month").value) {
			refreshScore();
			oldEndMonthVal = o("end_month").value;
		}
	},500);
});