<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/runqianReport4.tld" prefix="report" %>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.runqian.report4.usermodel.Context"%>
<html>
<head>
<title></title>
</head>
<body>

<%
    request.setCharacterEncoding( "UTF-8" );
    StringBuffer param=new StringBuffer();
	Enumeration paramNames = request.getParameterNames();
	if(paramNames!=null){
		while(paramNames.hasMoreElements()){
			String paramName = (String) paramNames.nextElement();
			String paramValue=request.getParameter(paramName);
			if(paramValue!=null){
				
				//把参数拼成name=value;name2=value2;.....的形式
				param.append(paramName).append("=").append(paramValue).append(";");
			}
		}
	}
%>


<table align="center" width="100%" height="100%" >
	<tr><td>
		<report:html name="report1" reportFileName="ZXT.raq"
		funcBarLocation=""
		width="-1"
			params="<%=param.toString()%>"
			exceptionPage="/reportJsp/myError2.jsp"
		/>
	</td></tr>
</table>


</body>
</html>
