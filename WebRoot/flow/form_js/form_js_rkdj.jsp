<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>

function onBasciCtlChange(obj) {
	$.ajax({
		url: "<%=request.getContextPath()%>/officeequip/officeequip_do.jsp?op=equip_check&officeName=" + obj.value,
		type: "post",
		dataType: "json",
		success: function(data, status){
			if(data.ret == 1){
				$('#tip_info').html("");
			} else {
				$('#tip_info').html(data.msg);
			}
		},
		error: function(XMLHttpRequest, textStatus){
			alert(XMLHttpRequest.responseText);
		}
	});	
}