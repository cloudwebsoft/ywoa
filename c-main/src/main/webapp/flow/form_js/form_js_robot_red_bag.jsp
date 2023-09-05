<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.kaoqin.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.robot.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
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
    String op = ParamUtil.get(request, "op");
    if (op.equals("start")) {
        response.setContentType("text/html;charset=utf-8");
        JSONObject json = new JSONObject();
        long id = ParamUtil.getLong(request, "id", -1);
        boolean re = false;
        try {
            re = RobotUtil.startRedbag(id);
        } catch (ErrMsgException e) {
            json.put("ret", 0);
            json.put("msg", e.getMessage());
        }
        if (re) {
            json.put("ret", 1);
            json.put("msg", "操作成功！");
        }
        out.print(json);
        return;
    }

    response.setContentType("text/javascript;charset=utf-8");
%>

function startRedBag(id) {
	jConfirm('您确定要开始么？', '提示', function(r) {
		if (r) {
			$.ajax({
				type: "post",
				url: "<%=request.getContextPath()%>/flow/form_js/form_js_robot_red_bag.jsp",
				data: {
					op: "start",
					id: id
				},
				dataType: "html",
				beforeSend: function(XMLHttpRequest) {
				},
				success: function(data, status) {
					data = $.parseJSON(data);
					jAlert(data.msg, "提示");
				},
				complete: function(XMLHttpRequest, status){
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
			});
		}
	});
}
