<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
    response.setContentType("text/javascript;charset=utf-8");

    String fieldName = ParamUtil.get(request, "fieldName");
    String formCode = ParamUtil.get(request, "formCode");
    FormDb fd = new FormDb();
    fd = fd.getFormDb(formCode);
    FormField ff = fd.getFormField(fieldName);
    String desc = StrUtil.getNullStr(ff.getDescription());
    String birthField = "";
    if (!"".equals(desc)) {
        birthField = desc;
    }
%>
$(findObj("<%=fieldName%>")).change(function() {
	var fieldName = "<%=fieldName%>";
	var obj = this;
    console.log('<%=fieldName%>', $(this).val());
    console.log('birthField=<%=birthField%>');
	if ($(this).val()=="") {
		return;
	}

    var isMap = "<%=birthField%>" != "";
    console.log('isMap', isMap);
    /*
    if (!isMap) {
        return;
    }
    */

    var ajaxData = {
        val: $(this).val()
    };
	ajaxPost('/module_check/checkIDCard', ajaxData).then((data) => {
		console.log('data', data);
		if (data.ret==0) {
            // console.log('msg=' + data.msg);
            $('#errMsgBox_' + fieldName).remove();
            $(obj).parent().append("<span id='errMsgBox_" + fieldName + "' class='LV_validation_message LV_invalid'>" + data.msg + "</span>");
            if (isMap) {
                if (typeof(findObj('<%=birthField%>').value)=="string") {
                    findObj('<%=birthField%>').value = "";
                }
                else {
                    findObj('<%=birthField%>').innerHTML = "";
                }
            }
        }
        else {
            $('#errMsgBox_' + fieldName).remove();
            if (isMap && findObj('<%=birthField%>')) {
                if (typeof(findObj('<%=birthField%>').value)=="string") {
                    findObj('<%=birthField%>').value = data.birthday;
                }
                else {
                    findObj('<%=birthField%>').innerHTML = data.birthday;
                }
            }
        }
	});
}) 
