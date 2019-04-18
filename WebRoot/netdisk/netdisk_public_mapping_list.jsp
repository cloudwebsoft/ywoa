<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.file.FileUtil"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="java.io.File"%>
<%@ page import="java.sql.Date"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String dirCode = ParamUtil.get(request, "dir_code");
PublicLeafPriv lp = new PublicLeafPriv(dirCode);
if (lp.canUserSeeByAncestor(privilege.getUser(request)))
	;
else {
	out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>网络硬盘-公共文件列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">映射文件列表</td>
    </tr>
  </tbody>
</table>
<br>
<table class="tabStyle_1 percent98">
  <tbody>
    <tr>
      <td class="tabStyle_1_title">文件名</td>
      <td class="tabStyle_1_title">大小</td>
      <td class="tabStyle_1_title">类型</td>
      <td class="tabStyle_1_title">修改时间</td>
      <td class="tabStyle_1_title">操作</td>
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
      <td style="PADDING-LEFT: 10px"><img src="images/folder.gif" width="20" height="20" align="absmiddle"><a href="netdisk_public_mapping_list.jsp?mappingAddress=<%=StrUtil.UrlEncode(path)%>"><%=directoryName%></a></td>
      <td style="PADDING-LEFT: 10px">&nbsp;</td>
      <td>文件夹</td>
      <td align="center"><%=lastModifiedTime%></td>
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
      <td style="PADDING-LEFT: 10px"><img src="images/<%=Attachment.getIcon(ext)%>" border="0"><a href="netdisk_mapping_getfile.jsp?mappingAddress=<%=StrUtil.UrlEncode(path)%>&fileName=<%=StrUtil.UrlEncode(directoryName)%>" target="_blank"><%=directoryName%></a></td>
      <td style="PADDING-LEFT: 10px"><%=fileLength%>KB</td>
      <td>&nbsp;</td>
      <td><%=lastModifiedTime%></td>
      <td><a href="netdisk_mapping_downloadfile.jsp?mappingAddress=<%=StrUtil.UrlEncode(path)%>&fileName=<%=StrUtil.UrlEncode(directoryName)%>">下载</a></td>
    </tr>
<%
}
%>
  </tbody>
</table>
</body>
</html>