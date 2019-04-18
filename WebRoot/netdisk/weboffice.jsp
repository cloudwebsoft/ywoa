<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%
int id = ParamUtil.getInt(request, "id");
int attachId = ParamUtil.getInt(request, "attachId");

Document doc = new Document();
doc = doc.getDocument(id);
Attachment att = doc.getAttachment(1, attachId);
String ext = att.getExt();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE>在线编辑</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<SCRIPT language=javascript>
<!--
function window_onload() {
}

window.onbeforeunload = function(){   
	var n = window.event.screenX - window.screenLeft;   
	var b = n > document.documentElement.scrollWidth-20;   
	if(b && window.event.clientY < 0 || window.event.altKey)   
	{   
	//alert("是关闭而非刷新");   
	window_onbeforeunload();
	}else{
	//alert("是刷新而非关闭");
	}
}

function window_onbeforeunload() {
var doc;
try {
	doc = document.all.WebOffice1.GetDocumentObject();
}
catch (e) {
	return;
}
var activeDoc = new Object(doc);
var app = activeDoc.Application;
activeDoc.Saved = true;
/*
document.all.WebOffice1.CloseDoc(0); // 似乎没有什么用处
*/

//app.Quit(); // 发现在有的win2003 + office2003机器上会提示WORD当前窗口处于打开状态

//event.returnValue='您确定要关闭吗?';

// 阻塞3秒，当无印章时，此方法有效，但是当插入印章，并保存文件后，word会崩溃，而当采用event.returnValue时，始终有效，改进后用showModalDialog阻塞后退出
// 而如果调用这一行app.Quit();则会使得印章在插入并上传WORD文件后，出现崩溃，如果没有印章，则不会出现此情况
// 将showModalDialog移至本方法尾部时，问题看似都解决了，如果放在这一行document.all.WebOffice1.Close()之前，可能不行
// sleep(3000);

document.all.WebOffice1.Close();
delete activeDoc;

// 隐藏窗口，看起来象已关闭窗口
// window.moveTo(1600,1600);
showModalDialog('waitdlg.jsp',window.self,'dialogWidth:300px;dialogHeight:50px;status:no;help:no;');
}

function sleep(msecs){
var start = new Date().getTime();
var cur = start
while(cur - start < msecs){
cur = new Date().getTime();
}
}

function WebOffice1_NotifyCtrlReady() {
	WebOffice1.LoadOriginalFile("<%=Global.getFullRootPath(request)%>/netdisk/netdisk_office_get.jsp?id=<%=id%>&attachId=<%=attachId%>","<%=ext%>");
}

function upload() {
// document.all.WebOffice1.OptionFlag|=128;
document.all.WebOffice1.HttpInit();
document.all.WebOffice1.HttpAddPostString("id", "<%=id%>");
document.all.WebOffice1.HttpAddPostString("attachId", "<%=attachId%>");
document.all.WebOffice1.HttpAddPostCurrFile("attachment", "<%=att.getName()%>");
var ret = document.all.WebOffice1.HttpPost("<%=Global.getFullRootPath(request)%>/netdisk/netdisk_office_upload.jsp?id=<%=id%>&attachId=<%=attachId%>");
alert(ret);
}

//-->
</SCRIPT>
<SCRIPT LANGUAGE=javascript FOR=WebOffice1 EVENT=NotifyCtrlReady>
<!--
WebOffice1_NotifyCtrlReady();
//-->
</SCRIPT>
</HEAD>
<BODY onLoad="window_onload()">
<TABLE cellSpacing=0 cellPadding=0 width=100% border=0>
  <TBODY>
    <TR>
      <TD height="35" align="center">
        <input class="btn" type="button" onclick="upload()" value="保存文件" />	  </TD>
    </TR>
    <TR>
      <TD height="576" colSpan=2 vAlign=top class=leftBorder>
        <SCRIPT src="../inc/LoadWebOffice.js"></SCRIPT>
	  </TD>
    </TR>
  </TBODY>
</TABLE>
</BODY>
</HTML>
