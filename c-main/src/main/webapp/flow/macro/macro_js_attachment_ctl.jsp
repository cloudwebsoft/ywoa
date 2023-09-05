<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%
	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
	response.setContentType("text/javascript;charset=utf-8");

	Privilege pvg = new Privilege();
	if (!pvg.isUserLogin(request)) {
		return;
	}
%>
function changeToUpload(fieldName) {
	return;
}

function deAttachment(attId, fieldName, flowId, docId) {
	var ajaxData = {
		"flowId": flowId,
		"docId": docId,
		"attachId": attId
	}
	ajaxPost('/flow/delAttach', ajaxData).then((data) => {
		console.log('data', data);
		myMsg(data.msg);
		if (data.code == 200) {
			if (fieldName != null) {
				$('#helper_' + fieldName).remove();
			}
			// 刷新附件
			reloadAttachment();
		}
	});
}

function delAtt(attId, fieldName, flowId, docId) {
	myConfirm('提示', '您确定要删除么', function() { deAttachment(attId, fieldName, flowId, docId) });
}
