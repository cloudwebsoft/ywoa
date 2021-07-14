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
$(o("<%=fieldName%>")).change(function() {
	var fieldName = "<%=fieldName%>";
	var obj = this;
	if ($(this).val=="") {
		return;
	}
    $.ajax({
        type: "post",
        url: "<%=request.getContextPath()%>/module_check/checkIDCard.do",
        data : {
            val: $(this).val()
        },
        dataType: "html",
        beforeSend: function(XMLHttpRequest) {
            // $('body').ShowLoading();
        },
        success: function(data, status) {
			data = $.parseJSON(data);
			if (data.ret==0) {
                // console.log('msg=' + data.msg);
                $('#errMsgBox_' + fieldName).remove();
				$(obj).parent().append("<span id='errMsgBox_" + fieldName + "' class='LV_validation_message LV_invalid'>" + data.msg + "</span>");
                if (typeof(o('<%=birthField%>').value)=="string") {
                    o('<%=birthField%>').value = "";
                }
                else {
                    o('<%=birthField%>').innerHTML = "";
                }
			}
			else {
				$('#errMsgBox_' + fieldName).remove();
                if (o('<%=birthField%>')) {
                    if (typeof(o('<%=birthField%>').value)=="string") {
                        o('<%=birthField%>').value = data.birthday;
                    }
                    else {
                        o('<%=birthField%>').innerHTML = data.birthday;
                    }
                }
			}
        },
        complete: function(XMLHttpRequest, status) {
            // $('body').hideLoading();
        },
        error: function(XMLHttpRequest, textStatus) {
            // 请求出错处理
            alert(XMLHttpRequest.responseText);
        }
    });		    
}) 
