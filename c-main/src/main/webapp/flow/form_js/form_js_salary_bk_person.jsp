<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.kaoqin.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.archive.*"%>
<%@ page import = "com.redmoon.oa.dept.DeptUserDb"%>
<%@ page import = "com.redmoon.oa.dept.DeptMgr"%>
<%@ page import = "java.util.List"%>
<%
	/*
    - 功能描述：
    - 访问规则：从flow_dispose.jsp中通过include script访问
    - 过程描述：
    - 注意事项：
    - 创建者：fgf
    - 创建时间：2018-10-11
    ==================
    - 修改者：
    - 修改时间：
    - 修改原因:
    - 修改点:
    */

	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
	response.setContentType("text/javascript;charset=utf-8");

	String rootpath = request.getContextPath();

	Privilege privilege = new Privilege();
	String unitCode = "";
	if (!privilege.isValid(request, "read")) {
		unitCode = privilege.getUserUnitCode(request);
	}

// 取帐套的ID
	long bookId = ParamUtil.getLong(request, "parentId", -1);
%>
function setUsers(userNames, userRealNames) {
	var formulaStr = "#selBookPerson(<%=bookId %>, \"" + userNames + "\")";
	$.ajax({
		type: "post",
		url: "<%=rootpath %>/visual/formula/doFormula.do",
		contentType:"application/x-www-form-urlencoded; charset=iso8859-1",		
		data: {
			formula: formulaStr,
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			$('body').showLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data);
			if (data.ret=="0") {
				jAlert(data.msg, "提示");
			}
			else {
				jAlert(data.msg, "提示", function() {
                	window.location.reload();
                });
			}
		},
		complete: function(XMLHttpRequest, status){
			$('body').hideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});		
}

function selPerson() {
	openWin("<%=rootpath%>/user_multi_sel.jsp?unitCode=<%=unitCode %>&isForm=false", 800, 600);
}

function generateSalary() {
	var ids = getIdsSelected();
	if (ids == "") {
    	jAlert("请选择人员！", "提示");
    	return;
    }
    
	var yearOpts = "", monthOpts = "";
	var date = new Date();
	var year = date.getFullYear(); 
	var month = date.getMonth()+1;
	for (var i=year-10; i<=year; i++) {
		var sel = "";
		if (i==year) {
			sel = "selected";
		}	
		yearOpts += "<option value='" + i + "' " + sel + ">" + i + "</option>";
	}
	for (var i=1; i<=12; i++) {
		var sel = "";
		if (i==month) {
			sel = "selected";
		}
		monthOpts += "<option value='" + i + "' " + sel + ">" + i + "</option>";
	}
	
	yearOpts = "年份<select id='year' name='year'>" + yearOpts + "</select>&nbsp;&nbsp;";
	monthOpts = "月份<select id='month' name='month'>" + monthOpts + "</select>";
        
	$("<div id='dlg' style='display:none'>" + yearOpts + monthOpts + "</div>").dialog({
		title: "请选择要生成的年月份",
		modal: true,
		// bgiframe:true,
		width:200,
		height:50,
		buttons: {
			"取消": function() {
				$(this).dialog("close");
			},
			"确定": function() {
				$.ajax({
					type: "post",
					url: "<%=rootpath %>/salary/generateSalaryForUsers.do",
					contentType:"application/x-www-form-urlencoded; charset=iso8859-1",		
					data: {
						ids: ids,
	                	bookId : <%=bookId %>,
	                	year: $('#year').val(),
	                	month: $('#month').val()
					},
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
						$('body').showLoading();
					},
					success: function(data, status){
						data = $.parseJSON(data);
						jAlert(data.msg, "提示");
					},
					complete: function(XMLHttpRequest, status){
						$('body').hideLoading();
					},
					error: function(XMLHttpRequest, textStatus){
						// 请求出错处理
						alert(XMLHttpRequest.responseText);
					}
				});						
				$(this).dialog("close");
			}
		}
	});
}