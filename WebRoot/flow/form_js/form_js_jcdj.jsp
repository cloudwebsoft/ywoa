<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>

function onBasciCtlChange(obj) {
	$.ajax({
		url: "<%=request.getContextPath()%>/officeequip/officeequip_do.jsp?op=equip_total&officeName=" + obj.value,
		type: "post",
		dataType: "json",
		success: function(data, status){
			if(data.ret == 1){
				$('#tip_info').html("");
				o('count').value = data.msg;
				$('#span_count').html(data.msg);
			} else {
				$('#tip_info').html(data.msg);
				$('#span_count').html("");
			}
		},
		error: function(XMLHttpRequest, textStatus){
			alert(XMLHttpRequest.responseText);
		}
	});	
}
