<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.base.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
String targeturl = StrUtil.getUrl(request);
if (!privilege.isUserLogin(request)) {
	response.sendRedirect("../door.jsp?targeturl="+targeturl);
	return;
}

int i=0;
String replyid = request.getParameter("replyid");
if (replyid==null || !StrUtil.isNumeric(replyid))
{
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.ERR_ID)));
	return;
}
MsgMgr msgMgr = new MsgMgr();
MsgDb msgDb = msgMgr.getMsgDb(Integer.parseInt(replyid));
String boardcode = msgDb.getboardcode();

String userName = privilege.getUser(request);
if (!privilege.canUserDo(request, boardcode, "reply_topic")) {
	response.sendRedirect("../info.jsp?info= " + StrUtil.UrlEncode(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String privurl = StrUtil.toHtml(request.getParameter("privurl"));
String quote = StrUtil.getNullString(request.getParameter("quote"));
String quotecontent = "";
String retopic;

retopic = msgDb.getTitle();
String qc = SkinUtil.LoadString(request, "res.label.forum.addreply", "quote_content");
qc = qc.replaceFirst("\\$u", msgDb.getName());
qc = qc.replaceFirst("\\$d", com.redmoon.forum.ForumSkin.formatDateTime(request, msgDb.getAddDate()));
quotecontent = qc + "\r\n\r\n" + msgDb.getContent();
quotecontent = "<table align=center style=\"width:80%\" cellpadding=5 cellspacing=1 class=quote><TR><TD>" + quotecontent + "</td></tr></table><BR>";

retopic = SkinUtil.LoadString(request, "res.label.forum.addreply", "reply") + retopic;

Leaf lf = new Leaf();
lf = lf.getLeaf(boardcode);
String boardname = lf.getName();
String hit = request.getParameter("hit");

// 取得皮肤路径
String skinPath = SkinMgr.getSkinPath(request);

com.redmoon.forum.Config cfg1 = com.redmoon.forum.Config.getInstance();
int msgTitleLengthMin = cfg1.getIntProperty("forum.msgTitleLengthMin");
int msgTitleLengthMax = cfg1.getIntProperty("forum.msgTitleLengthMax");
int msgLengthMin = cfg1.getIntProperty("forum.msgLengthMin");
int msgLengthMax = cfg1.getIntProperty("forum.msgLengthMax");
int maxAttachmentCount = cfg1.getIntProperty("forum.maxAttachmentCount");
int maxAttachmentSize = cfg1.getIntProperty("forum.maxAttachmentSize");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.addreply" key="addreply"/> - <%=Global.AppName%></title>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<script>
function SubmitWithFile(){
	if (!cws_validateMode())
		return false;

	if (document.frmAnnounce.topic.value.length<<%=msgTitleLengthMin%>)
	{
		alert("<lt:Label res="res.forum.MsgDb" key="err_too_short_title"/><%=msgTitleLengthMin%>");
		return false;
	}	

	if (document.frmAnnounce.topic.value.length><%=msgTitleLengthMax%>)
	{
		alert("<lt:Label res="res.forum.MsgDb" key="err_too_large_title"/><%=msgTitleLengthMax%>");
		return false;
	}	
	
	var html;
	html=cws_getText();
	html=cws_rCode(html,"<a>　</a>","");
 	document.frmAnnounce.Content.value=html;

	if (document.frmAnnounce.Content.value.length<<%=msgLengthMin%>)
	{
		alert("<lt:Label res="res.forum.MsgDb" key="err_too_short_content"/><%=msgLengthMin%>");
		return false;
	}
	if (document.frmAnnounce.Content.value.length><%=msgLengthMax%>)
	{
		alert("<lt:Label res="res.forum.MsgDb" key="err_too_large_content"/><%=msgLengthMax%>");
		return false;
	}
		
	loadDataToWebeditCtrl(frmAnnounce, frmAnnounce.webedit);
	frmAnnounce.webedit.AddField("ip", "<%=request.getRemoteAddr()%>");
	
	document.frmAnnounce.submit1.disabled = true;	
	
	frmAnnounce.webedit.UploadArticle();
	if (frmAnnounce.webedit.ReturnMessage.indexOf("+")!=-1) {
		alert(frmAnnounce.webedit.ReturnMessage);
		window.location.href = "<%=privurl%>";
	}
	else
		alert(frmAnnounce.webedit.ReturnMessage);
	document.frmAnnounce.submit1.disabled = false;		
}

function SubmitWithFileDdxc() {
	frmAnnounce.webedit.isDdxc = 1;
	if (document.frmAnnounce.topic.value.length<<%=msgTitleLengthMin%>)
	{
		alert("<lt:Label res="res.forum.MsgDb" key="err_too_short_title"/><%=msgTitleLengthMin%>");
		return false;
	}	

	if (document.frmAnnounce.topic.value.length><%=msgTitleLengthMax%>)
	{
		alert("<lt:Label res="res.forum.MsgDb" key="err_too_large_title"/><%=msgTitleLengthMax%>");
		return false;
	}	

	var html;
	html=cws_getText();
	html=cws_rCode(html,"<a>　</a>","");
 	document.frmAnnounce.Content.value=html;
	if (document.frmAnnounce.Content.value.length<<%=msgLengthMin%>)
	{
		alert("<lt:Label res="res.forum.MsgDb" key="err_too_short_content"/><%=msgLengthMin%>");
		return false;
	}
	if (document.frmAnnounce.Content.value.length><%=msgLengthMax%>)
	{
		alert("<lt:Label res="res.forum.MsgDb" key="err_too_large_content"/><%=msgLengthMax%>");
		return false;
	}
		
	loadDataToWebeditCtrl(frmAnnounce, frmAnnounce.webedit);
	frmAnnounce.webedit.AddField("ip", "<%=request.getRemoteAddr()%>");
	frmAnnounce.webedit.MTUpload();
}

function Operate() {
	recordFilePath = frmAnnounce.Recorder.FilePath;
	frmAnnounce.webedit.InsertFileToList(recordFilePath);
}
	
function frmAnnounce_onsubmit(){
	SubmitWithFile();
}

function checkCount() {
	var len = getHTML().length;
	var str = "<lt:Label res="res.label.forum.addtopic" key="content_cur_count"/>" + len + "\n<lt:Label res="res.label.forum.addtopic" key="content_count_limit"/><%=msgLengthMin%> - <%=msgLengthMax%>";
	if (len<<%=msgLengthMin%>)
		str += "\n<lt:Label res="res.forum.MsgDb" key="err_too_short_content"/><%=msgLengthMin%>";
	if (len><%=msgLengthMax%>)
		str += "\n<lt:Label res="res.forum.MsgDb" key="err_too_large_content"/><%=msgLengthMax%>";
	window.alert(str);
}
</script>
<script language=JavaScript src='inc/formpost.js'></script>
</head>
<body>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="userservice" scope="page" class="com.redmoon.forum.person.userservice" />
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<%@ include file="inc/position.jsp"%>
<form name=frmAnnounce method="post" action="addreplytodb.jsp" enctype="MULTIPART/FORM-DATA" onSubmit="return frmAnnounce_onsubmit()">
<table class="tableCommon" width="98%" border="1" align="center" cellpadding="4">
    <thead>
      <tr> 
      <td colspan="3"><lt:Label res="res.label.forum.addreply" key="reply_topic"/>&nbsp;<a href="showtopic_tree.jsp?rootid=<%=msgDb.getRootid()%>&showid=<%=msgDb.getId()%>"><%=msgDb.getTitle()%></a></td>
      </tr>
    </thead>
    <TBODY>
      <tr>
        <td colspan="3">
          <%
String pluginCode = msgDb.getRootMsgPluginCode();
		  
PluginMgr pm = new PluginMgr();
Vector vplugin = pm.getAllPluginUnitOfBoard(boardcode);
if (vplugin.size()>0) {
	Iterator irplugin = vplugin.iterator();
	long msgRootId = msgDb.getRootid();
	while (irplugin.hasNext()) {
		PluginUnit pu = (PluginUnit)irplugin.next();
		IPluginUI ipu = pu.getUI(request, response, out);
		IPluginViewAddReply pv = ipu.getViewAddReply(boardcode, msgRootId);
		if (pv.IsPluginBoard()) {
			boolean showPlugin = false;
			if (pu.getType().equals(PluginUnit.TYPE_BOARD) || pu.getType().equals(PluginUnit.TYPE_FORUM))
				showPlugin = true;
			else if (pu.getType().equals(pu.TYPE_TOPIC)) {
				if (pluginCode.equals(pu.getCode()))
					showPlugin = true;
			}
			if (showPlugin) {				
				if (!pu.getAddReplyPage().equals("")) {
	%>
					<jsp:include page="<%=pu.getAddReplyPage()%>" flush="true">
					<jsp:param name="boardcode" value="<%=StrUtil.UrlEncode(boardcode)%>" /> 
					</jsp:include>
	<%			}
				else {
					out.print(pu.getName(request) + ":&nbsp;" + pv.render(UIAddReply.POS_FORM_NOTE) + "<BR>");
					out.print(pv.render(UIAddReply.POS_FORM_ELEMENT));
				}		
			}	
		}
	}
}
%>        </td>
      </tr>
<%
if (cfg1.getBooleanProperty("forum.addUseValidateCode")) {
%>
<tr><td width="20%">
<lt:Label res="res.label.forum.addtopic" key="input_validatecode"/>
</td><td colspan="2"><input name="validateCode" type="text" size="1">
  <img src='../validatecode.jsp' border=0 align="absmiddle" style="cursor:hand" onClick="this.src='../validatecode.jsp'" alt="<lt:Label res="res.label.forum.index" key="refresh_validatecode"/>"></td>
</tr>	  <%}%>	  
      <tr> 
      <td width="20%"><lt:Label res="res.label.forum.addreply" key="topic_reply"/></td>
        <td width="80%" colspan="2"> <SELECT name=font onchange=DoTitle(this.options[this.selectedIndex].value)>
            <OPTION selected value=""><lt:Label res="res.label.forum.addtopic" key="sel_topic"/></OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_origi"/>><lt:Label res="res.label.forum.addtopic" key="pre_origi"/></OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_from"/>><lt:Label res="res.label.forum.addtopic" key="pre_from"/></OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_water"/>><lt:Label res="res.label.forum.addtopic" key="pre_water"/></OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_discuss"/>><lt:Label res="res.label.forum.addtopic" key="pre_discuss"/></OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_help"/>><lt:Label res="res.label.forum.addtopic" key="pre_help"/></OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_recommend"/>><lt:Label res="res.label.forum.addtopic" key="pre_recommend"/></OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_notice"/>><lt:Label res="res.label.forum.addtopic" key="pre_notice"/></OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_note"/>><lt:Label res="res.label.forum.addtopic" key="pre_note"/></OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_image"/>><lt:Label res="res.label.forum.addtopic" key="pre_image"/></OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_advise"/>><lt:Label res="res.label.forum.addtopic" key="pre_advise"/></OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_download"/>><lt:Label res="res.label.forum.addtopic" key="pre_download"/></OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_share"/>><lt:Label res="res.label.forum.addtopic" key="pre_share"/></OPTION>
          </SELECT> 
		  <input name="topic" type="text" id="topic" size="60" maxlength="80" value="<%=StrUtil.toHtml(retopic)%>">
		  <input type=hidden name=replyid value="<%=replyid%>">
          <input type="hidden" name="boardcode" value="<%=boardcode%>">
          <input type="hidden" name="privurl" value="<%=privurl%>">
	[<a href="#" onClick="spwhitepad();return false;"><lt:Label res="res.label.forum.addtopic" key="whitepad"/></a>]
	<script language="JavaScript">
	function spwhitepad(){
		var win = window.open("../spwhitepad/editor.htm","spwhitepadeditor","width=420,height=340,left=200,top=50,toolbar=no,menubar=no,scrollbars=no,resizable=no,location=no,status=no");
		win.focus();
	}
	
	function insertStroke(code) {
		setHTML(getHTML() + code);
	}
	</script>	    </td>
      </tr>
      <tr> 
        <td width="20%"><lt:Label res="res.label.forum.addtopic" key="emote_icon"/></td>
        <td width="80%" colspan="2"><iframe src="iframe_emote.jsp" height="25"  width="58%" marginwidth="0" marginheight="0" frameborder="0" scrolling="yes"></iframe>
        <input type="hidden" name="expression" value="<%=MsgDb.EXPRESSION_NONE%>"></td>
      </tr>
      <tr>
        <td valign="bottom"><table width="100%" border="0" cellspacing="0" cellpadding="5">
          <tr>
            <td><a href="javascript:ownerCanSee()">
              <lt:Label res="res.label.forum.addtopic" key="owner_to_see"/>
            </a></td>
          </tr>
        </table>
        <input type="checkbox" name="show_ubbcode" value="0">
          <lt:Label res="res.label.forum.showtopic" key="forbid_ubb"/><br> <input type="checkbox" name="show_smile" value="0">
          <lt:Label res="res.label.forum.showtopic" key="forbid_emote"/><br>
		  <%if (cfg1.getBooleanProperty("forum.waterMarkImg") && cfg1.getBooleanProperty("forum.waterMarkOptional")) {%>
          <input type="checkbox" name="isNeedWaterMark" value="1" checked="checked">
          <lt:Label res="res.label.forum.addtopic" key="is_need_watermark"/>
		  <%}%>		  
	    </td> 
        <td>
		<%
		request.setAttribute("isWebedit", "true");
		%>		
		<%@ include file="../editor_full/editor.jsp"%>
          <input type=hidden name="Content">
          <input type=hidden name="isWebedit" value="<%=MsgDb.WEBEDIT_NORMAL%>">
<%if (quote!=null && quote.equals("1")) {%>
		  <textarea name="tmpContent" style="display:none"><%=quotecontent%></textarea>
		  <script>
		  IframeID.document.body.innerHTML=frmAnnounce.tmpContent.value;
		  </script>
<%}%></td>
        <td valign="top"><%@ include file="inc_emot.jsp"%></td>
      </tr>
      <tr>
        <td valign="bottom">&nbsp;</td>
        <td colspan="2"><table width=""  border="0" cellpadding="0" cellspacing="1" bgcolor="#CCCCCC">
          <tr>
            <td bgcolor="#FFFFFF"><%
String filepath = com.redmoon.forum.Config.getInstance().getAttachmentPath() + "/" + MsgDb.getCurAttVisualPath();
String BasePath = "";
boolean isFtpUsed = com.redmoon.forum.Config.getInstance().getBooleanProperty("forum.ftpUsed");
if (isFtpUsed) {
	BasePath = StrUtil.getNullString(com.redmoon.forum.Config.getInstance().getProperty("forum.ftpUrl")) + "/" + MsgDb.getCurAttVisualPath();
}
%>
                <object classid="CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C" codebase="../activex/cloudym.CAB#version=1,2,0,1" width=400 height=173 align="middle" id="webedit">
                  <param name="Encode" value="utf-8">
                  <param name="MaxSize" value="<%=Global.MaxSize%>">
                  <!--上传字节-->
                  <param name="ForeColor" value="(255,255,255)">
                  <param name="BgColor" value="(107,154,206)">
                  <param name="ForeColorBar" value="(255,255,255)">
                  <param name="BgColorBar" value="(0,0,255)">
                  <param name="ForeColorBarPre" value="(0,0,0)">
                  <param name="BgColorBarPre" value="(200,200,200)">
                  <param name="FilePath" value="<%=filepath%>">
                  <param name="Relative" value="2">
                  <!--上传后的文件需放在服务器上的路径-->
                  <param name="Server" value="<%=request.getServerName()%>">
                  <param name="Port" value="<%=request.getServerPort()%>">
                  <param name="VirtualPath" value="<%=Global.virtualPath%>">
                  <param name="PostScript" value="<%=Global.virtualPath%>/forum/addreplytodbwe.jsp">
                  <param name="PostScriptDdxc" value="<%=Global.virtualPath%>/forum/forumddxc.jsp">
                  <param name="SegmentLen" value="204800">
				  <param name="BasePath" value="<%=BasePath%>">				  
              </object></td>
          </tr>
          <tr>
            <td align="center" bgcolor="#FFFFFF"><table width="100%" id="table_recorder" style="display:none">
                <tr>
                  <td align="center"><a href="../activex/RecordCtl.EXE"><lt:Label res="res.label.forum.addtopic" key="download_recorder"/></a></td>
                </tr>
                <tr>
                  <td align="center"><OBJECT ID="Recorder" CLASSID="CLSID:E4A3D135-E189-48AF-B348-EF5DFFD99A67" codebase="../activex/cloudym.CAB#version=1,2,0,1">
              </OBJECT></td>
                </tr>
              </table>              </td>
          </tr>
        </table></td>
      </tr>
      
      <tr>
        <td colspan="3" align="center">
		<!--<input name="cmdok2" type="button" class="singleboarder" value="断点续传" onClick="return SubmitWithFileDdxc()">
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-->
<input name="submit1" type="button" onClick="frmAnnounce_onsubmit()" value="<lt:Label res="res.label.forum.addtopic" key="commit"/>">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input type=button onClick="checkCount()" value="<lt:Label res="res.label.forum.addtopic" key="checkcount"/>">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;		
<input name="reset" type=reset value="<lt:Label res="res.label.forum.addtopic" key="rewrite"/>">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input name="cmdcancel2" type="button" onClick="table_recorder.style.display=''" value="<lt:Label res="res.label.forum.addtopic" key="record"/>">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input type="button" onClick="window.location.href='addreply_new.jsp?replyid=<%=replyid%>&boardcode=<%=StrUtil.UrlEncode(boardcode)%>&rootid=<%=msgDb.getRootid()%>&privurl=<%=privurl%>'" value="<lt:Label res="res.label.forum.addtopic" key="add_normal"/>">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input type="button" onClick="window.location.href='addreply.jsp?replyid=<%=replyid%>&boardcode=<%=StrUtil.UrlEncode(boardcode)%>&rootid=<%=msgDb.getRootid()%>&privurl=<%=privurl%>'" value="<lt:Label res="res.label.forum.addtopic" key="add_ubb"/>"></td>
      </tr>
    </TBODY>
</table></form>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</body>
</html>
