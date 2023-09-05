<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<%
String op = ParamUtil.get(request, "op");
if(op.equals("download")){
	String QRCodeImagePath = request.getRealPath("images/yimioa_mobile_qrcode.png");

	response.setContentType("application/octet-stream");
	response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode("yimioa_mobile_qrcode.png"));

	BufferedInputStream bis = null;
	BufferedOutputStream bos = null;

	try {
		bis = new BufferedInputStream(new FileInputStream(QRCodeImagePath));
		bos = new BufferedOutputStream(response.getOutputStream());
		
		byte[] buff = new byte[2048];
		int bytesRead;
		
		while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
			bos.write(buff,0,bytesRead);
		}
	} catch(final IOException e) {
		LogUtil.getLog(getClass()).error(e);
	} finally {
		if (bis != null)
			bis.close();
		if (bos != null)
			bos.close();
	}
		
	out.clear();
	out = pageContext.pushBody();
	return ;
}
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>二维码图片处理</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->

  </head>
  
<body onload="onload();">
<div id="content"></div>
</body>
<script>
function onload() {
	<%if (op.equals("printer")) {%>
		document.getElementById("content").innerHTML = window.opener.getPrintContent();
		window.print();
	<%}%>
}
</script>
</html>
