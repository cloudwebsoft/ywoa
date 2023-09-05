<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.kaoqin.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.robot.*"%>
<%@ page import = "java.net.*"%>
<%@ page import = "java.util.*"%>
<%@ page import="com.redmoon.oa.flow.WorkflowDb" %>
<%
	/*
    - 功能描述：紅包設置
    - 访问规则：从flow_dispose.jsp中通过include script访问
    - 过程描述：
    - 注意事项：
    - 创建者：fgf
    - 创建时间：
    ==================
    - 修改者：
    - 修改时间：
    - 修改原因:
    - 修改点:
    */

	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");

	Privilege pvg = new Privilege();
	int flowId = ParamUtil.getInt(request, "flowId", -1);

	String op = ParamUtil.get(request, "op");
	if ("getJh".equals(op)) {
		response.setContentType("text/html;charset=utf-8");
		JSONObject json = new JSONObject();
		int nf = ParamUtil.getInt(request, "nf", -1);
		int yf = ParamUtil.getInt(request, "yf", -1);
		if (yf == 1) {
			nf -= 1;
			yf = 12;
		} else {
			yf -= 1;
		}
		String sql = "select * from ft_gzjh where nf=? and yf=? and cws_status=1 order by id desc";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = jt.executeQuery(sql, new Object[]{nf, yf});
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord) ri.next();
			String xyjdmb = rr.getString("xyjdmb");
			String xyzdgz = rr.getString("xyzdgz");
			json.put("ret", 1);
			json.put("xyjdmb", xyjdmb);
			json.put("xyzdgz", xyzdgz);
		} else {
			json.put("ret", 0);
		}
		out.print(json.toString());
		return;
	}

	response.setContentType("text/javascript;charset=utf-8");

	WorkflowDb wf = new WorkflowDb();
	wf = wf.getWorkflowDb(flowId);
	if (wf.getStatus() == WorkflowDb.STATUS_NONE) {
%>
function ajaxGetJh() {
	// 获取上月的计划
	$.ajax({
		type: "post",
		contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
		url: "<%=request.getContextPath()%>/flow/form_js/form_js_gzjh.jsp",
		async: false,
		data: {
			op: "getJh",
			nf: $('#nf').val(),
			yf: $('#yf').val(),
			flowId: <%=flowId%>
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			$('body').showLoading();
		},
		success: function(data, status) {
			data = $.parseJSON(data);
			// jAlert(data.msg, "提示");
			if (data.ret==1) {
				o('byjdmb').value = data.xyjdmb;
				o('byzdgz').value = data.xyzdgz;
			}
			else {
				consoleLog($('#yf').val() + "月的上月计划不存在！");
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

$(function() {
	$('#nf').change(function() {
		ajaxGetJh();
	});

	$('#yf').change(function() {
		ajaxGetJh();
	});

});
<%
	}
%>