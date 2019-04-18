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
int i=0;
String replyid = request.getParameter("replyid");
if (replyid==null || !StrUtil.isNumeric(replyid)) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.ERR_ID)));
	return;
}

String privurl = StrUtil.toHtml(request.getParameter("privurl"));
String quote = StrUtil.getNullString(request.getParameter("quote"));
String quotecontent = "";
String retopic;

MsgMgr msgMgr = new MsgMgr();
MsgDb msgDb = msgMgr.getMsgDb(Integer.parseInt(replyid));
String boardcode = msgDb.getboardcode();

String userName = privilege.getUser(request);
if (!privilege.canUserDo(request, boardcode, "reply_topic")) {
	response.sendRedirect("../info.jsp?info= " + StrUtil.UrlEncode(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

retopic = msgDb.getTitle();
String qc = SkinUtil.LoadString(request, "res.label.forum.addreply", "quote_content");
qc = qc.replaceFirst("\\$u", msgDb.getName());
qc = qc.replaceFirst("\\$d", com.redmoon.forum.ForumSkin.formatDateTime(request, msgDb.getAddDate()));
quotecontent = qc + "\r\n\r\n" + msgDb.getContent();

retopic = SkinUtil.LoadString(request, "res.label.forum.addreply", "reply") + retopic;

Leaf lf = new Leaf();
lf = lf.getLeaf(boardcode);
String boardname = lf.getName();
String hit = request.getParameter("hit");

// 取得皮肤路径
String skinPath = SkinMgr.getSkinPath(request, lf);

com.redmoon.forum.Config cfg1 = com.redmoon.forum.Config.getInstance();
int msgTitleLengthMin = cfg1.getIntProperty("forum.msgTitleLengthMin");
int msgTitleLengthMax = cfg1.getIntProperty("forum.msgTitleLengthMax");

int msgLengthMin = cfg1.getIntProperty("forum.msgLengthMin");
int msgLengthMax = cfg1.getIntProperty("forum.msgLengthMax");

int maxAttachmentCount = cfg1.getIntProperty("forum.maxAttachmentCount");
int maxAttachmentSize = cfg1.getIntProperty("forum.maxAttachmentSize");

// 如果用户不是游客
if (privilege.isUserLogin(request)) {
    UserPrivDb upd = new UserPrivDb();
    upd = upd.getUserPrivDb(privilege.getUser(request));
    if (upd.getInt("is_default")==0) {
		maxAttachmentSize = upd.getInt("attach_size");
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.forum.addreply" key="addreply"/> - <%=Global.AppName%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<LINK href="<%=skinPath%>/css.css" type=text/css rel=stylesheet>
<script>
function frmAnnounce_onsubmit()
{
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
	document.frmAnnounce.submit1.disabled = true;		
}

var attachCount = 1;

function addImg(attchId, imgPath) {
	imgPath = imgPath.replace("http", "hhttttpp");
	var img = "[img]" + imgPath + "[/img]";
	if ((document.selection)&&(document.selection.type == "Text")) {
		var range = document.selection.createRange();
		range.text += img;
	}
	else {
		document.frmAnnounce.Content.value = document.frmAnnounce.Content.value + img;
		document.frmAnnounce.Content.focus();
	}
	
	divTmpAttachId.innerHTML += "<input type=hidden name=tmpAttachId value='" + attchId + "'>";
	attachCount++;
	if (attachCount><%=maxAttachmentCount%>) {
		// uploadTable.style.display = "none"; // 隐藏了，但是如果已选择了文件，还是会被上传
		uploadTable.outerHTML = "";
	}
}

function getAttachCount() {
	return attachCount - 1;
}

function AddAttach() {
	if (attachCount>=<%=maxAttachmentCount%>) {
		alert("<lt:Label res="res.label.forum.addtopic" key="topic_max_attach"/><%=maxAttachmentCount%>");
		return;
	}
	updiv.insertAdjacentHTML("BeforeEnd", "<table width=100%><tr><lt:Label res="res.label.forum.addtopic" key="file"/>&nbsp;<input type='file' name='filename" + attachCount + "' size=10><td></td></tr></table>");
	
	// updiv.innerHTML += "<table width=100%><tr><lt:Label res="res.label.forum.addtopic" key="file"/>&nbsp;<input type='file' name='filename" + attachCount + "' size=10><td></td></tr></table>";
	attachCount += 1;
}

function checkCount() {
	var len = document.frmAnnounce.Content.value.length;
	var str = "<lt:Label res="res.label.forum.addtopic" key="content_cur_count"/>" + len + "\n<lt:Label res="res.label.forum.addtopic" key="content_count_limit"/><%=msgLengthMin%> - <%=msgLengthMax%>";
	if (len<<%=msgLengthMin%>)
		str += "\n<lt:Label res="res.forum.MsgDb" key="err_too_short_content"/><%=msgLengthMin%>";
	if (len><%=msgLengthMax%>)
		str += "\n<lt:Label res="res.forum.MsgDb" key="err_too_large_content"/><%=msgLengthMax%>";
	window.alert(str);
}
</script>
</head>
<body>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<%@ include file="inc/position.jsp"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="userservice" scope="page" class="com.redmoon.forum.person.userservice" />
<br>
<script src="inc/ubbcode.jsp"></script>
<table width="100%" class="tableCommon">
<form name=frmAnnounce method="post" action="addreplytodb.jsp" enctype="MULTIPART/FORM-DATA" onSubmit="return frmAnnounce_onsubmit()">
    <THEAD>
      <tr> 
      <td colspan="2" class="td_title"> <lt:Label res="res.label.forum.addreply" key="reply_topic"/>&nbsp;<a href="showtopic_tree.jsp?rootid=<%=msgDb.getRootid()%>&showid=<%=msgDb.getId()%>"><%=msgDb.getTitle()%></a></td>
      </tr>
    </THEAD>
    <TBODY>
      <tr>
        <td colspan="2">
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
<lt:Label res="res.label.forum.addtopic" key="input_validatecode" />
</td><td><input name="validateCode" type="text" size="1">
  <img src='../validatecode.jsp' border=0 align="absmiddle" style="cursor:hand" onClick="this.src='../validatecode.jsp'" alt="<lt:Label res="res.label.forum.index" key="refresh_validatecode"/>"></td>
</tr>	  <%}%>	  
      <tr> 
      <td width="20%"><lt:Label res="res.label.forum.addreply" key="topic_reply"/></td>
        <td width="80%"> <SELECT name=font onchange=DoTitle(this.options[this.selectedIndex].value)>
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
          <input type="hidden" name="boardname" value="<%=boardname%>">
          <input type="hidden" name="hit" value="<%=hit%>">
          <input type="hidden" name="privurl" value="<%=privurl%>">
	[<a href="#" onClick="spwhitepad();return false;">手写板</a>]
	<script language="JavaScript">
	function spwhitepad(){
		var win = window.open("../spwhitepad/editor.htm","spwhitepadeditor","width=420,height=340,left=200,top=50,toolbar=no,menubar=no,scrollbars=no,resizable=no,location=no,status=no");
		win.focus();
	}
	
	function insertStroke(code) {
		frmAnnounce.Content.value += code;
	}
	</script>		  </td>
      </tr>
      <tr> 
        <td width="20%"><lt:Label res="res.label.forum.addtopic" key="emote_icon"/><br></td>
        <td><iframe src="iframe_emote.jsp" height="25"  width="58%" marginwidth="0" marginheight="0" frameborder="0" scrolling="yes"></iframe>
        <input type="hidden" name="expression" value="<%=MsgDb.EXPRESSION_NONE%>"></td>
      </tr>
      <tr> 
        <td rowspan="5" valign="bottom"><table width="100%" border="0" cellspacing="0" cellpadding="5">
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
          <%}%></td>
        <td>
		<%
		if (privilege.canUserUpload(request, boardcode)) {
		%>
		<iframe src="uploadimg.jsp?action=ubb" width=100% height="43" frameborder="0" scrolling="no"></iframe>
		<%}%>		</td>
      </tr>
      <tr> 
        <td><%@ include file="inc/getubb.jsp"%></td>
      </tr>
      <tr> 
        <td>
		<textarea cols="75" name="Content" rows="12" wrap="VIRTUAL" title="<lt:Label res="res.label.forum.addtopic" key="ctrl_enter"/>" onKeyDown="ctlent()" style="width:74%">
<%if (quote!=null && quote.equals("1"))
	out.print("\n[QUOTE]"+quotecontent+"[/QUOTE]" + "\r\n");%></textarea>
		<%
		if (privilege.canUserUpload(request, boardcode)) {
		%>	
<table width="100%" border=0 cellspacing=0 cellpadding=0 id="uploadTable">
<tr>
  <td class=tablebody1 valign=top height=10></td>
</tr>
<tr>
  <td class=tablebody1 valign=top><lt:Label res="res.label.forum.addtopic" key="file"/>&nbsp;<input type="file" name="filename" size=10>
<lt:Label res="res.label.forum.addtopic" key="file_limit_count"/><%=maxAttachmentCount%>&nbsp;<lt:Label res="res.label.forum.addtopic" key="file_limit_all_size"/><%=maxAttachmentSize%>K
<input name="button" type=button onClick="AddAttach()" value="<lt:Label res="res.label.forum.addtopic" key="add_attach"/>">
<select name="select">
  <option>
  <lt:Label res="res.label.forum.addtopic" key="upload_file_ext"/>
  </option>
  <%
				String[] ext = StrUtil.split(cfg1.getProperty("forum.ext"), ",");
				if (ext!=null) {
					int extlen = ext.length;
					for (int p=0; p<extlen; p++) {
						out.print("<option>" + ext[p] + "</option>");
					}
				}
				%>
</select></td></tr>
</table>
<div id=updiv name=updiv></div>	
<%}%>	</td>
      </tr>
      <tr>
        <td><iframe src="iframe_emotequick.jsp" height="35" name="emot" width="74%" marginwidth="0" marginheight="0" frameborder="0" scrolling="yes"></iframe></td>
      </tr>
      <tr>
        <td>
&nbsp;
<input name="submit1" type=submit value="<lt:Label res="res.label.forum.addtopic" key="commit"/>">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input type=button onClick="checkCount()" value="<lt:Label res="res.label.forum.addtopic" key="checkcount"/>">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input name="button2" type="button" onClick="window.location.href='addreply_new.jsp?replyid=<%=replyid%>&boardcode=<%=StrUtil.UrlEncode(boardcode)%>&rootid=<%=msgDb.getRootid()%>&privurl=<%=privurl%>'" value="<lt:Label res="res.label.forum.addtopic" key="add_normal"/>">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<%if (privilege.canWebEditRedMoon(request, boardcode)) {%>
<input name="button22" type="button" onClick="window.location.href='addreply_we.jsp?replyid=<%=replyid%>&boardcode=<%=StrUtil.UrlEncode(boardcode)%>&rootid=<%=msgDb.getRootid()%>&privurl=<%=privurl%>'" value="<lt:Label res="res.label.forum.addtopic" key="add_we"/>">
<%}%>
<div id="divTmpAttachId"></div></td>
      </tr>
      <tr> 
        <td colspan="2">&nbsp;</td>
      </tr>
    </TBODY></form>
</table>
<%@ include file="inc/footer.jsp"%>
</div>
</body>
</html>
