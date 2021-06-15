<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.forum.*" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="com.redmoon.forum.plugin2.*"%>
<%@ page import="com.redmoon.forum.person.UserSet"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.base.*"%>
<%@ page import="com.redmoon.blog.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	response.sendRedirect("../info.jsp?op=login&info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "info_please_login")));
	return;
}

int i=0;
String editid = request.getParameter("editid");
String privurl = request.getParameter("privurl");
if (editid==null && !StrUtil.isNumeric(editid)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.ERR_ID)));
	return;
}

String name = privilege.getUser(request);

MsgMgr mm = new MsgMgr();
MsgDb Topic = mm.getMsgDb(Long.parseLong(editid));

if (!privilege.canEdit(request, Topic)) {
	response.sendRedirect(request.getContextPath() + "/info.jsp?info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String boardcode = Topic.getboardcode();
String boardname;
Leaf curleaf = new Leaf();
curleaf = curleaf.getLeaf(boardcode);
boardname = curleaf.getName();
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
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.edittopic" key="edittopic"/> - <%=Global.AppName%></title>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<script>
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

function getradio(myitem)
{
     var radioboxs = document.all.item(myitem);
     if (radioboxs!=null)
     {
       for (i=0; i<radioboxs.length; i++)
          {
            if (radioboxs[i].type=="radio" && radioboxs[i].checked)
              {
                 return radioboxs[i].value;
              }
          }
     }
	 return "";
}

function frmAnnounce_onsubmit()
{
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
	if (document.getElementById("blogDirCode")!=null && document.frmAnnounce.blogDirCode.value=="not") {
		alert("<lt:Label res="res.label.blog.user.dir" key="alert"/>");
		return false;
	}
		
	var html;
	html = cws_getText();
	html = cws_rCode(html,"<a>　</a>","");	
		
	if (html.length<<%=msgLengthMin%>)
	{
		alert("<lt:Label res="res.forum.MsgDb" key="err_too_short_content"/><%=msgLengthMin%>");
		return false;
	}
	if (html.length><%=msgLengthMax%>)
	{
		alert("<lt:Label res="res.forum.MsgDb" key="err_too_large_content"/><%=msgLengthMax%>");
		return false;
	}	
	
	
	loadDataToWebeditCtrl(frmAnnounce, frmAnnounce.webedit);
	frmAnnounce.webedit.AddField("ip", "<%=request.getRemoteAddr()%>");
	frmAnnounce.webedit.UploadArticle();
	if (frmAnnounce.webedit.ReturnMessage.indexOf("+")!=-1) {
		alert(frmAnnounce.webedit.ReturnMessage);
		window.location.href = "<%=privurl%>";
	}
	else
		alert(frmAnnounce.webedit.ReturnMessage);
	return false;
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
	if (document.getElementById("blogDirCode")!=null && document.frmAnnounce.blogDirCode.value=="not") {
		alert("<lt:Label res="res.label.blog.user.dir" key="alert"/>");
		return false;
	}
	
	var html;
	html = cws_getText();
	html = cws_rCode(html,"<a>　</a>","");	

	if (html.length<<%=msgLengthMin%>)
	{
		alert("<lt:Label res="res.forum.MsgDb" key="err_too_short_content"/><%=msgLengthMin%>");
		return false;
	}
	if (html.length><%=msgLengthMax%>)
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

function clearAll(){
	document.frmAnnounce.title.value=""
	IframeID.document.body.innerHTML = "";
}

function changeAttachName(attach_id, msgId, nm) {
	var obj = findObj(nm);
	// document.frames.hideframe.location.href = "fwebedit_do.jsp?op=changeattachname&page_num=1&doc_id=" + doc_id + "&attach_id=" + attach_id + "&newname=" + obj.value
	form3.action = "edittopicdo_we.jsp?op=changeattachname&msgId=" + msgId + "&attach_id=" + attach_id;
	form3.newname.value = obj.value;
	form3.submit();
}

function delAttach(attach_id, msgId) {
	if (!window.confirm("<lt:Label res="res.label.forum.edittopic" key="confirm_del"/>")) {
		return;
	}
	document.frames.hideframe.location.href = "edittopicdo_we.jsp?op=delAttach&msgId=" + msgId + "&attach_id=" + attach_id
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
function onAddFile(index, fileName, filePath, fileSize, modifyDate) {
	
}
</script>
<script language=JavaScript src='inc/formpost.js'></script>
<script src="inc/ubbcode.jsp"></script>
</head>
<body>
<%
String editFlag = ParamUtil.get(request, "editFlag");
if (!editFlag.equals("blog") && !boardcode.equals(Leaf.CODE_BLOG)) {%>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<jsp:include page="inc/position.jsp" flush="true">
<jsp:param name="boardcode" value="<%=StrUtil.UrlEncode(boardcode)%>" />
</jsp:include>
<%}%>
<%
String topic="",content="";
int expression=1;

String show_ubbcode = "" + Topic.getShowUbbcode();
String show_smile = "" + Topic.getShowSmile();
String email_notify = "" + Topic.getEmailNotify();
long rootid = Topic.getRootid();
topic = Topic.getTitle();
content = Topic.getContent();
expression = Topic.getExpression();
%>
<form name=frmAnnounce method="post" action="edittopicdo_we.jsp">
  <table class="tableCommon" width="98%" border="1" align="center">
    <thead>
      <tr> 
      <td colspan="2"><lt:Label res="res.label.forum.edittopic" key="edittopic"/>&nbsp;&nbsp;<a href="showtopic_tree.jsp?rootid=<%=Topic.getRootid()%>&showid=<%=Topic.getId()%>"><%=StrUtil.toHtml(Topic.getTitle())%></a></td>
      </tr>
	</thead>
    <TBODY>
      <tr>
        <td colspan="2"><%
String pluginCode = Topic.getRootMsgPluginCode();
PluginMgr pm = new PluginMgr();
Vector vplugin = pm.getAllPluginUnitOfBoard(boardcode);
if (vplugin.size()>0) {
	Iterator irplugin = vplugin.iterator();
	while (irplugin.hasNext()) {
		PluginUnit pu = (PluginUnit)irplugin.next();
		IPluginUI ipu = pu.getUI(request, response, out);
		IPluginViewEditMsg pv = ipu.getViewEditMsg(boardcode, Topic.getId());
		if (pv.IsPluginBoard()) {
			boolean showPlugin = false;
			if (pu.getType().equals(PluginUnit.TYPE_BOARD) || pu.getType().equals(PluginUnit.TYPE_FORUM))
				showPlugin = true;
			else if (pu.getType().equals(pu.TYPE_TOPIC)) {
				if (pluginCode.equals(pu.getCode()))
					showPlugin = true;
			}
			if (showPlugin) {		
				if (!pu.getEditTopicPage().equals("")) {
	%>
					<jsp:include page="<%=pu.getEditTopicPage()%>" flush="true">
					<jsp:param name="msgId" value="<%=editid%>" /> 
					</jsp:include>
	<%			}
				else {
					if (pu.isShowName())
						out.print(pu.getName(request) + ":&nbsp;");
					out.print(pv.render(UIEditMsg.POS_FORM_NOTE) + "<BR>");
					out.print(pv.render(UIEditMsg.POS_FORM_ELEMENT));
				}
			}
		}
	}
}

String plugin2Code = Topic.getPlugin2Code();
if (!plugin2Code.equals("")) {
	Plugin2Mgr p2m = new Plugin2Mgr();
	Plugin2Unit p2u = p2m.getPlugin2Unit(plugin2Code);
%>
	<jsp:include page="<%=p2u.getEditTopicPage()%>" flush="true">
	<jsp:param name="msgId" value="<%=editid%>" /> 
	</jsp:include>	
<%}%></td>
      </tr>
	  <%
	  // 判别该用户的博客是否已被激活及该贴是否为根贴，只有两项全符合，才会出现编辑博客的选项
	  if (com.redmoon.blog.Config.getInstance().isBlogOpen && Topic.getReplyid()==-1) {
		  UserConfigDb ucd = new UserConfigDb();
		  // ucd = ucd.getUserConfigDb(Topic.getBlogId());
		  ucd = ucd.getUserConfigDbByUserName(Topic.getName());
		  if (ucd!=null && ucd.isLoaded()) {
		  %>
      <tr>
        <td><lt:Label res="res.label.forum.addtopic" key="add_to_blog"/></td>
        <td><%
				UserDirDb udd = new UserDirDb();
				String checked = "";
				if (Topic.isBlog())
					checked = "checked";
				%>
				<select name=blogUserDir>
				  <option value="<%=UserDirDb.DEFAULT%>"><lt:Label res="res.label.forum.addtopic" key="default_dir"/></option>
				  <%=udd.toOptions(ucd.getId())%>
				</select>
				<script>
					frmAnnounce.blogUserDir.value = "<%=Topic.getBlogUserDir()%>";
				</script>
				<input name=isBlog value=1 type=checkbox <%=checked%>>
				<lt:Label res="res.label.forum.addtopic" key="add_to_blog"/>
				<input name="blogId" value="<%=ucd.getId()%>" type="hidden">
				(<%=ucd.getTitle()%>&nbsp;-&nbsp;
				<%if (ucd.getType()==UserConfigDb.TYPE_PERSON) {%>
				<lt:Label res="res.label.blog.user.userconfig" key="type_person"/>
				<%}else{%>
				<lt:Label res="res.label.blog.user.userconfig" key="type_group"/>
				<%}%>) 
				<input name="isLocked" value="1" type="checkbox" <%=Topic.getIsLocked()==1?"checked":""%>>
				<lt:Label res="res.label.forum.addtopic" key="canReply"/>
				<script>
				var blogCatalogCode = "<%=Topic.getBlogDirCode()%>";
				</script>
				<select name="blogDirCode" onChange="if(this.options[this.selectedIndex].value=='not'){alert(this.options[this.selectedIndex].text+' 不能被选择！'); this.value=blogCatalogCode; return false;}else blogCatalogCode=this.value">
				<option value="" selected>
				<lt:Label res="res.label.forum.addtopic" key="select_title"/>
				</option>
				<%
				com.redmoon.blog.Directory blogDirectory = new com.redmoon.blog.Directory();
				com.redmoon.blog.Leaf blogLf = blogDirectory.getLeaf(com.redmoon.blog.Leaf.ROOTCODE);
				com.redmoon.blog.DirectoryView blogDv = new com.redmoon.blog.DirectoryView(blogLf);
				blogDv.ShowDirectoryAsOptions(out, blogLf, blogLf.getLayer());
				%>
				</select>
				<script>
				frmAnnounce.blogDirCode.value = "<%=Topic.getBlogDirCode()%>";
				</script>
		</td>
      </tr>
	  <%}
	  }%>
    </TBODY>
    <TBODY>
      <tr> 
      <td width="20%"><lt:Label res="res.label.forum.addtopic" key="topic_title"/></td>
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
		  <input name="topic" type="text" id="topic" size="60" maxlength="80" value="<%=StrUtil.HtmlEncode(topic)%>">
          <input type=hidden name="editid" value="<%=editid%>">
          <input type="hidden" name="boardcode" value="<%=boardcode%>">
          <input type="hidden" name="boardname" value="<%=boardname%>">
          <input type="hidden" name="privurl" value="<%=privurl%>">
<%
		  if (Topic.isRootMsg()) {
		  ThreadTypeDb ttd = new ThreadTypeDb();
		  Vector ttv = ttd.getThreadTypesOfBoard(boardcode);
		  if (ttv.size()>0) {
		  	Iterator ir = ttv.iterator();
		  %>
          <lt:Label res="res.label.forum.addtopic" key="thread_type"/>
          <select name="threadType">
            <option value="<%=ThreadTypeDb.THREAD_TYPE_NONE%>">
              <lt:Label key="wu"/>
            </option>
            <%
		  	while (ir.hasNext()) {
				ttd = (ThreadTypeDb)ir.next();
		  %>
            <option value="<%=ttd.getId()%>"><%=ttd.getName()%></option>
            <%}%>
          </select>
		  <script>
		  frmAnnounce.threadType.value = "<%=Topic.getThreadType()%>";
		  </script>
          <%}
		  }
		  %>
	[<a href="#" onClick="spwhitepad();return false;">
	<lt:Label res="res.label.forum.addtopic" key="whitepad"/>
	</a>]
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
        <td width="20%"><lt:Label res="res.label.forum.addtopic" key="emote_icon"/>        <br>          </td>
        <td width="80%"><iframe src="iframe_emote.jsp?expression=<%=Topic.getExpression()%>" height="25"  width="500px" marginwidth="0" marginheight="0" frameborder="0" scrolling="yes"></iframe>
        <input type="hidden" name="expression" value="<%=Topic.getExpression()%>"></td>
      </tr>
      <tr>
        <td align="left" valign="bottom"><table width="100%" border="0" cellspacing="0" cellpadding="5">
          <tr>
            <td><a href="javascript:payme()"><lt:Label res="res.label.forum.addtopic" key="fee_to_me"/></a></td>
          </tr>
        </table>
	      <%if (cfg1.getBooleanProperty("forum.canUserSetReplyExperiencePointSee") || privilege.isManager(request, boardcode)) {%>
            <table width="100%" border="0" cellspacing="0" cellpadding="5">
              <tr>
                <td><a href="javascript:replyCanSee()"><lt:Label res="res.label.forum.edittopic" key="see_by_reply"/></a></td>
              </tr>
              <tr>
                <td><a href="javascript:canSee('credit')">
                  <lt:Label res="res.label.forum.edittopic" key="see_by_credit"/>
                </a></td>
              </tr>
              <tr>
                <td><a href="javascript:canSee('experience')"><lt:Label res="res.label.forum.edittopic" key="see_by_experience"/></a></td>
              </tr>
              <tr>
                <td><a href="javascript:usePoint()"><lt:Label res="res.label.forum.edittopic" key="see_by_fee"/></a></td>
              </tr>
            </table>
          <%}%>
	    <%if (cfg1.getBooleanProperty("forum.email_notify") && Topic.isRootMsg()) {%>		  
		<input type="checkbox" id="email_notify" name="email_notify" value="1" <%=(Topic.getEmailNotify()==1)?"checked":""%>>
		<lt:Label res="res.label.forum.addtopic" key="email_notify"/>
		<%}%>
	    <%if (cfg1.getBooleanProperty("forum.msg_notify") && Topic.isRootMsg()) {%>		  
		<BR><input type="checkbox" id="msg_notify" name="msg_notify" value="1" <%=(Topic.getMsgNotify()==1)?"checked":""%>>
		<lt:Label res="res.label.forum.addtopic" key="msg_notify"/>
		<%}%>		
	    <%if (cfg1.getBooleanProperty("forum.sms_notify") && Topic.isRootMsg()) {%>		  
		<BR><input type="checkbox" id="sms_notify" name="sms_notify" value="1" <%=(Topic.getSmsNotify()==1)?"checked":""%>>
		<lt:Label res="res.label.forum.addtopic" key="sms_notify"/>
		<%}%>
        <script language="JavaScript">
		<!--
		<% if (show_ubbcode.equals("0")) { %>
			frmAnnounce.show_ubbcode.checked = true;
		<% } %>
		<% if (show_smile.equals("0")) { %>
			frmAnnounce.show_smile.checked = true;
		<% } %>
		//-->
		  </script>
		  <%if (cfg1.getBooleanProperty("forum.waterMarkImg") && cfg1.getBooleanProperty("forum.waterMarkOptional")) {%>
          <BR><input type="checkbox" name="isNeedWaterMark" value="1" checked="checked">
          <lt:Label res="res.label.forum.addtopic" key="is_need_watermark"/>
		  <%}%>		  
		  </td><td align="left">
		<%
		request.setAttribute("isWebedit", "true");
		%>		  
		  <%@ include file="../editor_full/editor.jsp"%>
		  <pre id="tmpContent" style="display:none"><%=content%></pre>
		  <script>
		  setHTML(document.getElementById("tmpContent").innerHTML);
		  </script>
		  <%
			  if (Topic!=null) {
				  Vector attachments = Topic.getAttachments();
				  Iterator ir = attachments.iterator();
				  while (ir.hasNext()) {
				  	Attachment am = (Attachment) ir.next(); %>
		  <br>
          <table width="98%"  border="0" cellspacing="0" cellpadding="0">
            <tr>
              <td width="4%" align="center"><img src=../images/attach.gif width="17" height="17"></td>
              <td width="96%">&nbsp;
                  <input name="attach_name<%=am.getId()%>" value="<%=am.getName()%>" size="30">
              &nbsp;<a href="javascript:changeAttachName('<%=am.getId()%>', '<%=Topic.getId()%>', '<%="attach_name"+am.getId()%>')"><lt:Label res="res.label.forum.edittopic" key="change_name"/></a> &nbsp;<a href="javascript:delAttach('<%=am.getId()%>', '<%=Topic.getId()%>')"><lt:Label res="res.label.forum.addtopic" key="del_attach"/></a>&nbsp;&nbsp;
				<%if (am.isRemote()) {%>
				<a target=_blank href="<%=com.redmoon.forum.Config.getInstance().getProperty("forum.ftpUrl") + "/" + am.getVisualPath()%>/<%=am.getDiskName()%>"><lt:Label res="res.label.forum.edittopic" key="view"/></a>
				<%}else{%>
				<a target=_blank href="<%=request.getContextPath() + "/" + com.redmoon.forum.Config.getInstance().getAttachmentPath() + "/" + am.getVisualPath()%>/<%=am.getDiskName()%>"><lt:Label res="res.label.forum.edittopic" key="view"/></a>
				<%}%>
			  <!--&nbsp;<a href="#" onClick="document.frames.hideframe.location.href='edittopicdo_we.jsp?op=changeAttachOrders&id=<%=Topic.getId()%>&direction=up&attachId=<%=am.getId()%>'"><img src="../images/arrow_up.gif" alt="往上" width="16" height="20" border="0" align="absmiddle"></a>&nbsp;<a href="#" onClick="document.frames.hideframe.location.href='edittopicdo_we.jsp?op=changeAttachOrders&id=<%=Topic.getId()%>&direction=down&attachId=<%=am.getId()%>'"><img src="../images/arrow_down.gif" alt="往下" width="16" height="20" border="0" align="absmiddle"></a>--></td>
            </tr>
          </table>
          <%}
			  }
			  %></td>
      </tr>
      <tr>
        <td align="left" valign="bottom">&nbsp;</td>
        <td align="left"><table id="table_ctrl" border="0" cellpadding="0" cellspacing="1" bgcolor="#CCCCCC">
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
                  <!--height=280断点续传-->
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
                  <param name="PostScript" value="<%=Global.virtualPath%>/forum/edittopicdo_we.jsp">
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
            </table></td>
          </tr>
        </table>
          <%if (cfg1.getBooleanProperty("forum.isTag")) {
		  		Vector vtag = Topic.getTags();
				Iterator irtag = vtag.iterator();
				String tagNames = "";
				TagDb td2 = new TagDb();
				while (irtag.hasNext()) {
					TagMsgDb tmd = (TagMsgDb)irtag.next();
					TagDb td = td2.getTagDb(tmd.getLong("tag_id"));
					if (td!=null) {
						if (tagNames.equals(""))
							tagNames = td.getString("name");
						else
							tagNames += " " + td.getString("name");
					}
				}
				String limit = StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.showtopic", "tag_limit"), new Object[] { new Integer(cfg1.getIntProperty("forum.tagLenMax")) });
				limit += "&nbsp;&nbsp;" + StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.showtopic", "tag_single_limit"), new Object[] { new Integer(cfg1.getIntProperty("forum.tagSingleLenMax")) });
				boolean tagOnlySystemAllowed = cfg1.getBooleanProperty("forum.tagOnlySystemAllowed");
		  %>
          <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <tr>
              <td height="24"><lt:Label res="res.label.forum.showtopic" key="tag"/>
                ：
                <input name="tag" size="50" value="<%=tagNames%>" <%=tagOnlySystemAllowed?"readonly":""%>>
                <BR>(
                <lt:Label res="res.label.forum.showtopic" key="tag_format"/>
,<%=limit%>)</td>
            </tr>
            <tr>
              <td height="24"><script>
			  function addTag(name) {
			  	if (frmAnnounce.tag.value=="")
					frmAnnounce.tag.value = name;
				else
					frmAnnounce.tag.value += " " + name;
			  }
			  </script>
                  <lt:Label res="res.label.forum.showtopic" key="tag_sys"/>
                ：
                <%
		  TagDb td = new TagDb();
		  Vector tags = td.getTagsOfSystem();
		  Iterator ir = tags.iterator();
		  while (ir.hasNext()) {
		  	td = (TagDb)ir.next();
			out.print("<a href=\"javascript:addTag('" + td.getString("name") + "')\">" + td.getString("name") + "</a>&nbsp;&nbsp;");
		  }
		  %></td>
            </tr>
            <tr>
              <td height="24"><%if (privilege.isUserLogin(request) && !tagOnlySystemAllowed) {%>
                  <lt:Label res="res.label.forum.showtopic" key="tag_mine"/>
                ：
                <%
			  tags = td.getTagsOfUser(privilege.getUser(request));
			  ir = tags.iterator();
			  while (ir.hasNext()) {
				td = (TagDb)ir.next();
				out.print("<a href=\"javascript:addTag('" + td.getString("name") + "')\">" + td.getString("name") + "</a>&nbsp;&nbsp;");
			  }
		  }
		  %></td>
            </tr>
          </table>
        <%}%></td>
      </tr>
      <tr align="center">
        <td colspan="2">
		<!--<input name="cmdok2" type="button" value="断点续传" onClick="return SubmitWithFileDdxc()">
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-->
<input type=button onClick="frmAnnounce_onsubmit()" value="<lt:Label res="res.label.forum.addtopic" key="commit"/>">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input type=button onClick="checkCount()" value="<lt:Label res="res.label.forum.addtopic" key="checkcount"/>">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input name="cmdcancel" type="button" onClick="clearAll()" value="<lt:Label res="res.label.forum.addtopic" key="clear"/>">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<!--
<input name="cmdcancel2" type="button" onClick="table_recorder.style.display=''" value="<lt:Label res="res.label.forum.addtopic" key="record"/>">--></td>
      </tr>
    </TBODY>
</table>
</form>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <form name="form3" action="?" method="post"><td align="center">
			<input name="newname" type="hidden">
			</td></form>
  </tr>
</table>
<iframe id="hideframe" name="hideframe" src="edittopicdo_we.jsp" width=0 height=0></iframe>
<%if (!editFlag.equals("blog") && !boardcode.equals(Leaf.CODE_BLOG)) {%>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
<%}%>
</body>
</html>
