<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.forum.person.*"%>
<%@ page import = "com.redmoon.forum.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
// 安全验证
if (!privilege.isUserLogin(request))
{
	out.print(StrUtil.p_center(SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN)));
	return;
}

com.redmoon.forum.Config cfg1 = com.redmoon.forum.Config.getInstance();
int maxAttachmentCount = cfg1.getIntProperty("forum.maxAttachmentCount");
UserPrivDb upd = new UserPrivDb();
upd = upd.getUserPrivDb(privilege.getUser(request));

String action = ParamUtil.get(request, "action");
%>
<%
String op = ParamUtil.get(request, "op");

if (op.equals("not")) {
	String info = SkinUtil.LoadString(request, "res.forum.MsgMgr", "info_max_count");
	info = StrUtil.format(info, new Object[] {"" + maxAttachmentCount});
	out.print(info);
	return;
}
%>
<html>
<head>
<title>Upload Image</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="../common.css" type="text/css">
<script language="JavaScript" type="text/JavaScript">
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

function getAttachCount() {
<%if (action.equals("ubb")) {%>
	return window.parent.getAttachCount();
<%}else{%>
	return window.parent.dialogArguments.getAttachCount();
<%}%>	
}

function window_onload() {
	var attCount = 0;
	try {
		attCount = getAttachCount();
		if (attCount>=<%=maxAttachmentCount%>) {
			window.location.href = "uploadimg.jsp?op=not";
		}	
	}
	catch (e) {
		document.getElementById("upfileTable").style.display = "none";//Hide when use webedit control.
	}
}

function form1_onsubmit() {
	var fileName = form1.filename.value;
	var p = fileName.lastIndexOf(".");
	if (p==-1) {
		alert("<%=SkinUtil.LoadString(request, "res.label.forum.addtopic", "invalid_file")%>");
		return false;
	}
	else {
		var len = fileName.length;
		var ext = fileName.substring(p + 1, len).toLowerCase();
		if (ext=="gif" || ext=="jpg" || ext=="png" || ext=="bmp")
			;
		else {
			alert("<%=SkinUtil.LoadString(request, "res.label.forum.addtopic", "invalid_file")%>");
			return false;
		}
	}
}
</script><style type="text/css">
<!--
body {
	background-color: #dddddd;
}
-->
</style></head>
<body text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onLoad="return window_onload()">
<%
String[] re = null;
if (op.equals("upload")) {
	try {
		MsgMgr mm = new MsgMgr();
		re = mm.uploadImg(application, request);
	}
	catch (ErrMsgException e) {
		out.print(e.getMessage() + "&nbsp;&nbsp;<a href='uploadimg.jsp'>" + SkinUtil.LoadString(request, "back") + "</a>");
		return;
	}
	if (re!=null) {%>
		<script>
<%if (action.equals("ubb")) {%>
		window.parent.addImg("<%=re[0]%>", "<%=re[1]%>");
<%}else{%>		
		window.parent.dialogArguments.addImg("<%=re[0]%>", "<%=re[1]%>");
<%}%>
		</script>
	<%	out.print("<a href='uploadimg.jsp?action=" + action + "'>" + SkinUtil.LoadString(request, "res.label.forum.addtopic", "upload_success") + "</a>");
	%>
		<BR><%=SkinUtil.LoadString(request, "res.label.forum.addtopic", "upload_remain_count")%><%=upd.getInt("attach_day_count") - upd.getAttachTodayUploadCount()%>	
	<%
		return;
	}
	else {
		out.print("上传失败！");
		return;
	}	
}
%><form name="form1" style="margin:0px" enctype="MULTIPART/FORM-DATA" action="uploadimg.jsp?op=upload&action=<%=action%>" method="post" onSubmit="return form1_onsubmit()">
<table id="upfileTable" width="100%" align="center" class="p9">
    <tr> 
      <td height="22">
	  <div id="uploadDiv">
	  <%=cn.js.fan.security.Form.getTokenHideInput(request)%>
	  <input name=filename type=file id="filename"> <input type=submit value="<%=SkinUtil.LoadString(request, "res.label.forum.addtopic", "upload_img")%>">
<br>
	<%=SkinUtil.LoadString(request, "res.label.forum.addtopic", "upload_remain_count")%><%=upd.getInt("attach_day_count") - upd.getAttachTodayUploadCount()%>
	<%if (cfg1.getBooleanProperty("forum.waterMarkImg") && cfg1.getBooleanProperty("forum.waterMarkOptional")) {%>
    <input type="checkbox" name="isNeedWaterMark" value="1" checked="checked">
    <lt:Label res="res.label.forum.addtopic" key="is_need_watermark"/>
	<%}%>	
	</div>
	  </td>
    </tr>
</table>
</form>
</body>
</html>