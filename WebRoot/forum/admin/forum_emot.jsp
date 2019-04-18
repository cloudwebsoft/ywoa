<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import ="com.redmoon.forum.ui.*"%>
<%@ page import ="com.redmoon.forum.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.io.*"%>
<%@ include file="../inc/nocache.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="default.css" rel="stylesheet" type="text/css">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<title><lt:Label res="res.label.forum.admin.forum_face" key="face_manage"/></title>
<style type="text/css">
<!--
body {
	margin-left:0px;
	margin-top: 0px;
}
-->
</style></head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int group = ParamUtil.getInt(request, "group", 1);

FileViewer fileViewer = new FileViewer(cn.js.fan.web.Global.realPath + EmotMgr.EMOTPATH + "emot" + group + "/");

fileViewer.init(); 
String op = ParamUtil.get(request, "op");
String directory = ParamUtil.get(request, "directory");
String sourceName = ParamUtil.get(request, "sourceName");
String filenamePath = directory + "/" + sourceName; 
File file = new File(filenamePath);//源文件
if (op.equals("modify")) {
	  String destName = ParamUtil.get(request, "destName");
	  File dest = new File(directory + "/" + destName); //目标文件
	  int endIndex = destName.indexOf(".");
	  if (destName.equals("")) {
	    out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.forum.admin.forum_face", "err_msg")));
	    return;
      }		  
	  else {
		  if (file.renameTo(dest)) {
			out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "forum_emot.jsp?group=" + group));
			EmotMgr em = new EmotMgr();
			em.refresh();
			return;
		  }
	  }
}
  
if (op.equals("del")) {
	 if (file.delete()) {
		out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "forum_emot.jsp?group=" + group));
		EmotMgr em = new EmotMgr();
		em.refresh();
		return;
	 }
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><a href="forum_emot.jsp?group=1">表情一</a>&nbsp;&nbsp;<a href="forum_emot.jsp?group=2">表情二</a>&nbsp;&nbsp;<a href="forum_emot.jsp?group=0">默认</a></td>
  </tr>
</table>
<br>
 <table width="92%"  border="0" align="center" cellpadding="0" cellspacing="1" bgcolor="#999999">
  <tr class="thead">
    <td width="29%"><lt:Label res="res.label.forum.admin.forum_face" key="pic"/></td>
    <td width="38%"><lt:Label res="res.label.forum.admin.forum_face" key="filename"/>      </td>
    <td width="33%"><lt:Label key="op"/> </td>
   </tr>
<%
int k=0;
while(fileViewer.nextFile()){
  if (fileViewer.getFileName().lastIndexOf("gif") != -1 || fileViewer.getFileName().lastIndexOf("jpg") != -1 || fileViewer.getFileName().lastIndexOf("png") != -1 || fileViewer.getFileName().lastIndexOf("bmp") != -1 && fileViewer.getFileName().indexOf("face") != -1) {
%>  
<FORM METHOD=POST id="form<%=k%>" name="form<%=k%>" ACTION='?'>
  <tr>
    <td bgcolor="#FFF7FF">&nbsp;<a href="../../<%=EmotMgr.EMOTPATH%>emot<%=group + "/" + fileViewer.getFileName()%>" target="_blank">&nbsp;<img src="../../<%=EmotMgr.EMOTPATH%>emot<%=group + "/" + fileViewer.getFileName()%>" border="0" /></a></td>
    <td bgcolor="#FFF7FF">&nbsp;
      <input type="input" value="<%=fileViewer.getFileName()%>" size="30" name="destName"/></td>
    <td bgcolor="#FFF7FF">&nbsp;
	  <input type="submit" value="<lt:Label key="op_modify"/>" /><input type="hidden" name="directory" value="<%=fileViewer.getDirectory()%>"/>
      <input type="hidden" name="sourceName" value="<%=fileViewer.getFileName()%>"/>
      <input type="hidden" name="op" value="modify"/>
      <input type="hidden" name="group" value="<%=group%>"/>	  &nbsp;
      &nbsp;&nbsp;
      <input type="button" value="<lt:Label key="op_del"/>" onClick="del(form<%=k%>)"/></td>
    </tr>
</form>
<%
  k++;
 } //end if 
}%>  
</table>

<table width="100%" height="52" align="center" class="p9">
  <form name="form_upload" enctype="MULTIPART/FORM-DATA" action="forum_emot_upload.jsp" method="post">
    <tr> 
      <td height="46"><div align="center">上传表情 
          <input name=filename type=file id="filename"> 
          <input type=submit value=<lt:Label key="ok"/>>&nbsp;&nbsp;&nbsp; 
          <input type="hidden" name="group" value="<%=group%>"/>
          <br>
      </div></td>
    </tr>
  </form>
</table>
</body>
<script>
  function del(formObj) {
  	formObj.op.value = "del";
    formObj.submit();
  }
</script>
</html>
