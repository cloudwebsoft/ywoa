<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.redmoon.oa.pvg.Privilege"%>
<%
	/*
    - 功能描述：
    - 访问规则：从flow_dispose.jsp中通过include script访问
    - 过程描述：
    - 注意事项：
    - 创建者：fgf
    - 创建时间：2018-10-13
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
%>
function synPayrollField() {
	jConfirm('您确定要同步么？', '提示', function(r) {
		if (r) {
			$.ajax({
				type: "post",
				url: "<%=rootpath %>/salary/synPayrollField.do",
				contentType:"application/x-www-form-urlencoded; charset=iso8859-1",		
				data: {
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
		}
	});
}