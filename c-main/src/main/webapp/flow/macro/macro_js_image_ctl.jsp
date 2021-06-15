<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "java.util.regex.*"%>
<%@ page import = "org.json.*"%>
<%
	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
	response.setContentType("text/javascript;charset=utf-8");

	String w = ParamUtil.get(request, "w");
	String h = ParamUtil.get(request, "h");
	if ("".equals(w)) {
		w = "width:130px";
	}
	else {
		w = "width:" + w + "px";
	}
	if (!"".equals(h)) {
		h = "height:" + h + "px";
	}
%>
$(function() {
	$('.image-ctl').change(function() {
		var fieldName = $(this).attr("name");
		var curFile = this.files[0];
		var reader = new FileReader();
		reader.readAsDataURL(curFile);
		reader.onload = function () {
			$('#' + fieldName + 'Img').html('<img style="margin:5px;<%=w%>;<%=h%>" src="'+ reader.result +'"/>');
		}
	});
});