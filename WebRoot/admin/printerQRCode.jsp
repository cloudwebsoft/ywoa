<%@ page language="java" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>二维码</title>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">    
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">
<link href="../skin/common/common.css" rel="stylesheet" type="text/css" />
<link href="../skin/common/printerQRcode.css" rel="stylesheet" type="text/css" />
</head>
<body style="width:100%;text-align:center;">
<div class="QRcodeContent">
  <div id="QRCodeimgRange" class="QRcodeimgRange">
  	<img title="初次扫描此二维码可以下载手机客户端，再次扫描可以设置服务器地址" src="<%=basePath %>images/yimioa_mobile_qrcode.png" />
  </div>
  <p class="QRcodeTitle">初次扫描此二维码可以下载手机端</p>
  <p class="QRcodeTitle">再次扫描可以设置服务器地址</p>
  <div class="QRcodeBtn">
  	<div class="QRcodeBtn_a"><a target="_blank" href="printerQRCode_do.jsp?op=download">下载</a></div>
  	<div class="QRcodeBtn_a"><a href="javascript:;" onclick="showImageReport();">打印</a></div>
  </div>
</div>
</body>
<script>
function getPrintContent(){
	return document.getElementById("QRCodeimgRange").innerHTML;
}
function showImageReport() {
	var preWin=window.open('printerQRCode_do.jsp?op=printer','','left=0,top=0,width=550,height=400,resizable=1,scrollbars=1, status=1, toolbar=1, menubar=1');	
}
</script>
</html>
