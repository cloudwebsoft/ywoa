<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@page import="java.io.*"%>
<%@page import="cn.js.fan.web.Global"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<%
	try {
		String scName = Global.AppName + ".url";
		scName = java.net.URLEncoder.encode(scName, "UTF-8");
		response.reset();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + scName);
		OutputStream os = response.getOutputStream();
		StringBuilder sb = new StringBuilder();  
		sb.append("[{000214A0-0000-0000-C000-000000000046}]\r\n")
			.append("Prop4=31,OA\r\n")
			.append("Prop3=19,2\r\n")
			.append("[{A7AF692E-098D-4C08-A225-D433CA835ED0}]\r\n")
			.append("Prop5=3,0\r\n")
			.append("Prop9=19,0\r\n")
			.append("Prop2=65,2C0000000000000001000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF19000000190000001904000061020000CF\r\n")
			.append("Prop6=3,1\r\n")
			.append("[InternetShortcut]\r\n")
			.append("URL=").append(Global.getFullRootPath(request)).append("\r\n")
			.append("IconFile=").append(Global.getFullRootPath(request)).append("/images/favicon.ico\r\n")
			.append("IconIndex=1\r\n")
			.append("[{9F4C2855-9F79-4B39-A8D0-E1D42DE1D5F3}]\r\n")
			.append("Prop5=8,Microsoft.Website.17CB1ED8.514E32B6\r\n");
		os.write(sb.toString().getBytes());  
		os.flush();  
		os.close();  
		out.clear();
		out = pageContext.pushBody();
	} catch (Exception e) {
		e.printStackTrace();
	}
%>
</body>
</html>
