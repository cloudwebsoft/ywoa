<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.io.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
	String skincode = UserSet.getSkin(request);
	if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
	SkinMgr skm = new SkinMgr();
	Skin skin = skm.getSkin(skincode);
	String skinPath = skin.getPath();
%>
<html>
<head>
<title>选择图标</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="../<%=skinPath%>/css.css" rel="stylesheet" type="text/css" />
<style>

</style>
</head>
<body class="menu_sel_body">
<div align="center">
<script language=javascript>

function changeIcon(icon){
	window.opener.selBigIcon(icon);
	window.close();
}

</script>
<%
com.redmoon.forum.ui.FileViewer fileViewer = new com.redmoon.forum.ui.FileViewer(Global.getAppPath(request) + "images/bigicons/");
fileViewer.init();
%>
<table border="0" width=80%  class="tTable"><center>
<thead>
<tr>
  <td align=center class="tTd">请点击选择图标</td>
<tr>
</thead>
<td valign="center">
<table border="0"  class="cBigTable">
<tbody>
<%
int k=0;
while(fileViewer.nextFile()){
  if (fileViewer.getFileName().lastIndexOf("gif") != -1 || fileViewer.getFileName().lastIndexOf("jpg") != -1 || fileViewer.getFileName().lastIndexOf("png") != -1 || fileViewer.getFileName().lastIndexOf("bmp") != -1 && fileViewer.getFileName().indexOf("face") != -1) {
	if (k==0)
		out.print("<tr align=center class=''>");
	String fileName = fileViewer.getFileName();
%>
	<td class="imgBg">&nbsp;<img style="cursor:pointer;" onClick="changeIcon('<%=fileName%>')" alt='<lt:Label res="res.label.forum.user" key="check_selected"/>' src="../images/bigicons/<%=fileViewer.getFileName()%>" border="0"/></td>		
<%
	k++;	
	if (k==10)
		out.write("</tr>");
	if (k==10) k=0;
 }
}%>
</tbody>
</table></center>
</table>
</div>
</body>