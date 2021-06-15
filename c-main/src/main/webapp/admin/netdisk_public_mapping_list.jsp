<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.file.FileUtil"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="java.io.File"%>
<%@ page import="java.sql.Date"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>网络硬盘-公共文件列表</title>
<link href="default.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
.style4 {
	color: #FFFFFF;
	font-weight: bold;
}
-->
</style>
</head>
<body bgcolor="#FFFFFF" text="#000000">
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String mappingAddress = ParamUtil.get(request, "mappingAddress");
File file = new File(mappingAddress);
File[] FileDirectoryArr = null;
FileDirectoryArr = file.listFiles();
if(FileDirectoryArr == null){
   out.print(StrUtil.Alert_Back("该路径不存在！"));
   return;
}

int i = 0;
Vector dvt = new Vector();
Vector fvt = new Vector();
String directoryName = "",fileName = ""; 
while(i < FileDirectoryArr.length){
	file = FileDirectoryArr[i];
	if(file.isDirectory()){
		dvt.addElement(file);
	}else{
	 	fvt.addElement(file);
	}
    i++;
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td height="28" class="head">映射文件列表&nbsp;&nbsp;&nbsp;<a href="netdisk_public_dir_frame.jsp">共享目录</a></td>
    </tr>
  </tbody>
</table>
<br>
<table style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="thead" style="PADDING-LEFT: 10px" noWrap width="30%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">文件名</td>
      <td class="thead" style="PADDING-LEFT: 10px" noWrap width="15%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">大小</td>
      <td class="thead" noWrap width="15%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">类型</td>
      <td class="thead" noWrap width="20%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">修改时间</td>
      <td class="thead" noWrap width="20%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">操作</td>
    </tr>
<%
String path = "";
Iterator dir = null;
Date lastModifiedTime = null;
dir = dvt.iterator();
while(dir!=null && dir.hasNext()){
	 file = (File)dir.next();
     directoryName = file.getName(); 
	 lastModifiedTime = new Date(file.lastModified());
	 path = file.getAbsolutePath();
%>
    <tr class="row" style="BACKGROUND-COLOR: #ffffff">
      <td style="PADDING-LEFT: 10px"><img src="../netdisk/images/folder.gif" width="20" height="20" align="absmiddle"><a href="netdisk_public_mapping_list.jsp?mappingAddress=<%=StrUtil.UrlEncode(path)%>"><%=directoryName%></a></td>
      <td style="PADDING-LEFT: 10px">&nbsp;</td>
      <td>文件夹</td>
      <td><%=lastModifiedTime%></td>
      <td>&nbsp;</td>
    </tr>
<%
}
Iterator fir = null;
fir = fvt.iterator();
long fileLength = -1;
String ext = "";
while(fir!=null && fir.hasNext()){
	 file = (File)fir.next();
     directoryName = file.getName(); 
	 fileLength = file.length()/1024; 
	 if(fileLength == 0 && file.length() > 0)
		 fileLength = 1;
	 lastModifiedTime = new Date(file.lastModified());
	 ext = StrUtil.getFileExt(directoryName);
	 path = file.getAbsolutePath();
%>
    <tr class="row" style="BACKGROUND-COLOR: #ffffff">
      <td style="PADDING-LEFT: 10px"><img src="../netdisk/images/<%=Attachment.getIcon(ext)%>" border="0"><a href="../netdisk/netdisk_mapping_getfile.jsp?mappingAddress=<%=StrUtil.UrlEncode(path)%>&fileName=<%=StrUtil.UrlEncode(directoryName)%>" target="_blank"><%=directoryName%></a></td>
      <td style="PADDING-LEFT: 10px"><%=fileLength%>KB</td>
      <td>&nbsp;</td>
      <td><%=lastModifiedTime%></td>
      <td><a href="../netdisk/netdisk_mapping_downloadfile.jsp?mappingAddress=<%=StrUtil.UrlEncode(path)%>&fileName=<%=StrUtil.UrlEncode(directoryName)%>">下载</a></td>
    </tr>
<%
}
%>
  </tbody>
</table>
</body>
</html>