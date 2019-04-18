<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
int id = ParamUtil.getInt(request, "id");
DocTemplateMgr dtm = new DocTemplateMgr();
DocTemplateDb dtd = dtm.getDocTemplateDb(id);
String op = ParamUtil.get(request, "op");
if (op.equals("edit")) {
	try {
		if (dtm.modifyByWeboffice(application, request)) {
			out.print("操作成功！");
		}
		else
			out.print("操作失败！");
	}
	catch (ErrMsgException e) {
		out.print(e.getMessage());
	}
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE>公文模板在线编辑</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style>
body {
background-color:#eeeeee;
}
</style>
<script src="../inc/common.js"></script>
<SCRIPT language=javascript>
<!--
function window_onload() {
}

window.onbeforeunload = function(){   
	var n = window.event.screenX - window.screenLeft;   
	var b = n > document.documentElement.scrollWidth-20;   
	if(b && window.event.clientY < 0 || window.event.altKey)   
	{   
	//是关闭而非刷新
	window_onbeforeunload();
	}else{
	//是刷新而非关闭
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
//document.all.WebOffice1.CloseDoc(0); // 似乎没有什么用处
// app.Quit(); // 有时需用这行，有时不用IE又会崩溃

//event.returnValue='您确定要关闭吗?';

// 阻塞3秒，当无印章时，此方法有效，但是当插入印章，并保存文件后，word会崩溃，而当采用event.returnValue时，始终有效，改进后用showModalDialog阻塞后退出
// 而如果调用这一行app.Quit();则会使得印章在插入并上传WORD文件后，出现崩溃，如果没有印章，则不会出现此情况
// 将showModalDialog移至本方法尾部时，问题看似都解决了，如果放在这一行document.all.WebOffice1.Close()之前，可能不行
// sleep(3000);

document.all.WebOffice1.Close();
delete activeDoc;

// 隐藏窗口，看起来象已关闭窗口
// window.moveTo(1600,1600);
showModalDialog('../netdisk/waitdlg.jsp',window.self,'dialogWidth:300px;dialogHeight:50px;status:no;help:no;');
}

function getWaitTime() {
	return 1000;
}

function sleep(msecs){
var start = new Date().getTime();
var cur = start
while(cur - start < msecs){
cur = new Date().getTime();
}
}

function WebOffice1_NotifyCtrlReady() {
	WebOffice1.LoadOriginalFile("<%=dtd.getFileUrl(request)%>","doc");
}

function upload() {
	// document.all.WebOffice1.OptionFlag|=128;
	document.all.WebOffice1.HttpInit();
	document.all.WebOffice1.HttpAddPostString("id", "<%=id%>");
	// document.all.WebOffice1.HttpAddPostString("title", "<%=dtd.getTitle()%>");// 不支持中文
	document.all.WebOffice1.HttpAddPostString("sort", "<%=dtd.getSort()%>");
	document.all.WebOffice1.HttpAddPostCurrFile("attachment", "flowFileName.doc");
	var ret = document.all.WebOffice1.HttpPost("<%=Global.getFullRootPath(request)%>/admin/flow_doc_template_weboffice.jsp?op=edit&id=<%=id%>").trim();
	alert(ret.trim());
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
        <input class="btn" type="button" onclick="window.location.reload(true)" value="刷新" />&nbsp;&nbsp;
      <input class="btn" type="button" onclick="upload()" value="保存" /></TD>
    </TR>
	<TR>
      <TD height="576" colSpan=2 vAlign=top class=leftBorder>
        <SCRIPT src="../inc/LoadWebOffice.js"></SCRIPT>
		<script>
		setWebofficeHeight(585);
		</script>
	  </TD>
    </TR>
  </TBODY>
</TABLE>
</BODY>
</HTML>
