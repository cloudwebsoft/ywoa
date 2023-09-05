<%@ page language="java" import="com.redmoon.oa.util.MobileScanDownload" pageEncoding="utf-8"%>
<%@page import="nl.bitwalker.useragentutils.OperatingSystem"%>
<%@page import="nl.bitwalker.useragentutils.UserAgent"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE HTML>
<html>
  <head>
    <base href="<%=basePath%>">
    <title>下载App</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
  </head>
  <body >
  <%
	UserAgent ua = new UserAgent(request.getHeader("User-Agent"));
	OperatingSystem os = ua.getOperatingSystem();
	OperatingSystem os_parent = os.getGroup();
	boolean isMobile = os.isMobileDevice();
	if(isMobile){
		boolean isAndroid = os_parent.equals(OperatingSystem.ANDROID);
		String downloadUrl = MobileScanDownload.downloadMobileClientByAngent(isAndroid);
		if(downloadUrl.endsWith(MobileScanDownload.IOS_NO_APPROVAL)){
			out.print("<h1 style='font-size: 56px;'>IOS提交审核中,请耐心等待!</h1>");
		}else{
			response.sendRedirect(downloadUrl);
		}
	}
	%>
  </body>
</html>
