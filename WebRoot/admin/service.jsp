<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*,
				 java.text.*,
				 cn.js.fan.util.*,
				 com.redmoon.oa.fileark.*,
				 cn.js.fan.cache.jcs.*,
				 cn.js.fan.web.*,
				 com.redmoon.oa.kernel.*,
				 com.redmoon.oa.pvg.*,
				 com.redmoon.oa.Config,
				 com.redmoon.oa.SpConfig"
				 
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>service</title>
</head>
<body>
<%
	  License license = License.getInstance();
	  String enterpriseNum = license.getEnterpriseNum();
	  String type = license.getType();
	  String id = license.getId();
	  String name = license.getName();
	  String op = ParamUtil.get(request, "op");
	  
	  if ("".equals(op)) {
		response.sendRedirect("http://service.yimihome.com/public/customer/customer_login.jsp?enterpriseNum=" + enterpriseNum + "&type=" + type + "&id=" + id + "&name=" + StrUtil.UrlEncode(name) );
	  }
	  else if ("question".equals(op)) {
		response.sendRedirect("http://www.yimihome.com/customer/oa_question.jsp?enterpriseNum=" + enterpriseNum + "&type=" + type + "&id=" + id + "&name=" + StrUtil.UrlEncode(name) );	
	  }
%>
</body>
</html>