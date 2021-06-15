<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%
	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
	response.setContentType("text/javascript;charset=utf-8");
%>
function onBasciCtlChange(obj) {
	$.ajax({
		url: "<%=request.getContextPath()%>/officeequip/officeequip_do.jsp?op=equip_storecount&officeName=" + obj.value,
		type: "post",
		dataType: "json",
		success: function(data, status){
			if(data.ret == 1){
				$('#tip_info').html("");
				o('inCount').value = data.inCount;
				$('#in_count').html(data.inCount);
				o('receiveCount').value = data.receiveCount;
				$('#receive_count').html(data.receiveCount);
				o('borrowCount').value = data.borrowCount;
				$('#borrow_count').html(data.borrowCount);
				o('returnCount').value = data.returnCount;
				$('#return_count').html(data.returnCount);
				o('storeCount').value = data.storeCount;
				$('#store_count').html(data.storeCount);
			} else {
				$('#tip_info').html(data.msg);
				o('inCount').value = "";
				$('#in_count').html("");
				o('receiveCount').value = "";
				$('#receive_count').html("");
				o('borrowCount').value = "";
				$('#borrow_count').html("");
				o('returnCount').value = "";
				$('#return_count').html("");
				o('storeCount').value = "";
				$('#store_count').html("");
			}
		},
		error: function(XMLHttpRequest, textStatus){
			alert(XMLHttpRequest.responseText);
		}
	});	
}