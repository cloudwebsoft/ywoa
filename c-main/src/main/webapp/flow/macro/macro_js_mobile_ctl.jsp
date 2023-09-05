<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "cn.js.fan.util.*"%>
<%
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
    response.setContentType("text/javascript;charset=utf-8");

    String fieldName = ParamUtil.get(request, "fieldName");
    String formCode = ParamUtil.get(request, "formCode");
%>
$(o("<%=fieldName%>")).change(function() {
	var fieldName = "<%=fieldName%>";
	var obj = this;
	if ($(this).val=="") {
		return;
	}

    var ajaxData = {
        fieldName: "<%=fieldName%>",
        formCode: "<%=formCode%>",
        val: $(this).val()
    };
    ajaxPost('/module_check/checkMobile', ajaxData).then((data) => {
		console.log('data', data);
		if (data.ret==0) {
            $('#errMsgBox_' + fieldName).remove();
            $(obj).parent().append("<span id='errMsgBox_" + fieldName + "' class='LV_validation_message LV_invalid'>" + data.msg + "</span>");
        }
        else {
            $('#errMsgBox_' + fieldName).remove();
        }
	});
}) 
