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
<%@ page import = "com.redmoon.oa.post.PostUserDb"%>
<%@ page import = "com.redmoon.oa.post.PostUserMgr"%>
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