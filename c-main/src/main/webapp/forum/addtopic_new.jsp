<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin2.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.base.*"%>
<%@ page import="com.redmoon.blog.*"%>
<%@ page import="java.util.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/><%
String boardcode = ParamUtil.get(request, "boardcode");
if (boardcode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, "boardcode is null."));
	return;
}

Leaf curleaf = new Leaf();
curleaf = curleaf.getLeaf(boardcode);
if (curleaf==null || !curleaf.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "res.label.forum.addtopic", "board_none"))); // "版块不存在"));
	return;
}

String userName = privilege.getUser(request);
if (!privilege.canUserDo(request, boardcode, "add_topic")) {
	response.sendRedirect("../info.jsp?info= " + StrUtil.UrlEncode(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

com.redmoon.forum.UserSession.setBoardCode(request, boardcode);

String boardname = curleaf.getName();
String blogUserDir = ParamUtil.get(request, "blogUserDir");
if (blogUserDir.equals(""))
	blogUserDir = UserDirDb.DEFAULT;
	
// 取得皮肤路径
String skincode = curleaf.getSkin();
if (skincode.equals("") || skincode.equals(UserSet.defaultSkin)) {
	skincode = UserSet.getSkin(request);
	if (skincode==null || skincode.equals(""))
		skincode = UserSet.defaultSkin;
}

SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

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
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.addtopic" key="addtopic"/> - <%=Global.AppName%></title>
<LINK href="<%=skinPath%>/css.css" type=text/css rel=stylesheet>
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css">
@import url("../util/jscalendar/calendar-win2k-2.css");
</style>
<script src="../inc/common.js"></script>
<script language="javascript">
<!--
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

var attachCount = 1;

function addImg(attchId, imgPath){
	var img = "<img alt='<%=cn.js.fan.web.SkinUtil.LoadString(request, "res.cn.js.fan.util.StrUtil", "click_open_win")%>' style='cursor:hand' onclick=\"window.open('" + imgPath + "')\" src='" + imgPath + "'>";
	if ((IframeID.document.selection)&&(IframeID.document.selection.type == "Text")) {
		var range = IframeID.document.selection.createRange();
		range.pasteHTML(range.htmlText + "<BR>" + img + "<BR>");
	}
	else {
		//IframeID.document.body.innerHTML = IframeID.document.body.innerHTML + "<BR>" + img + "<BR>";
		cws_InsertSymbol(img);
	}
	divTmpAttachId.innerHTML += "<input type=hidden name=tmpAttachId value='" + attchId + "'>";
	attachCount++;
	if (attachCount><%=maxAttachmentCount%>) {
		// uploadTable.style.display = "none"; // 隐藏了，但是如果已选择了文件，还是会被上传
		uploadTable.outerHTML = "";
	}
}
function getAttachCount(){
	return attachCount - 1;
}
function showvote(isshow){
	if (document.frmAnnounce.elements["isvote"].checked){
		o("voteDiv").style.display="";
	}
	else{
		o("voteDiv").style.display="none";		
	}
}
function frmAnnounce_onsubmit(){
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
		
	if (getText().length<<%=msgLengthMin%>)
	{
		alert("<lt:Label res="res.forum.MsgDb" key="err_too_short_content"/><%=msgLengthMin%>");
		return false;
	}
	if (getText().length><%=msgLengthMax%>)
	{
		alert("<lt:Label res="res.forum.MsgDb" key="err_too_large_content"/><%=msgLengthMax%>");
		return false;
	}
	if (document.getElementById("blogDirCode")!=null && document.frmAnnounce.blogDirCode.value=="not") {
		alert("<lt:Label res="res.label.blog.user.dir" key="alert"/>");
		return false;
	}
	document.frmAnnounce.submit1.disabled = true;
}
function AddAttach(){
	if (attachCount>=<%=maxAttachmentCount%>) {
		window.alert("<lt:Label res="res.label.forum.addtopic" key="topic_max_attach"/><%=maxAttachmentCount%>");
		return;
	}
	updiv.insertAdjacentHTML("BeforeEnd", "<table width=100%><tr><lt:Label res="res.label.forum.addtopic" key="file"/>&nbsp;<input type='file' name='filename" + attachCount + "' size=10>&nbsp;<lt:Label res="res.label.forum.addtopic" key="att_desc"/>&nbsp;<input name='filename" + attachCount + "Desc'><td></td></tr></table>");
	attachCount += 1;
}
function checkCount(){
	var len = getText().length;
	var str = "<lt:Label res="res.label.forum.addtopic" key="content_cur_count"/>" + len + "\n<lt:Label res="res.label.forum.addtopic" key="content_count_limit"/><%=msgLengthMin%> - <%=msgLengthMax%>";
	if (len<<%=msgLengthMin%>)
		str += "\n<lt:Label res="res.forum.MsgDb" key="err_too_short_content"/><%=msgLengthMin%>";
	if (len><%=msgLengthMax%>)
		str += "\n<lt:Label res="res.forum.MsgDb" key="err_too_large_content"/><%=msgLengthMax%>";
	window.alert(str);
}
function restoreContent(){
	setHTML(document.frmAnnounce.Content.value);
}
function setBrow(brow){
document.frmAnnounce.elements["expression"].value=brow;
}
//-->
</script>
<script src="inc/ubbcode.jsp"></script>
</head>
<body onLoad="restoreContent()">
<div id="wrapper">
<%
String addFlag = ParamUtil.get(request, "addFlag");
if (addFlag.equals("") && !boardcode.equals(Leaf.CODE_BLOG)) {%>
<%@ include file="inc/header.jsp"%>
<div id="main">
<%@ include file="inc/position.jsp"%>
<%}%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="userservice" scope="page" class="com.redmoon.forum.person.userservice"/>
<%
long blogId = ParamUtil.getLong(request, "blogId", UserConfigDb.NO_BLOG);
String privurl = ParamUtil.get(request, "privurl");
if (privurl.equals("") && addFlag.equals("blog")) {
	privurl = "../blog/user/listtopic.jsp?blogId=" + blogId;
}
int i=0;
%>
<form name=frmAnnounce method="post" action="addtopictodb.jsp?addFlag=<%=StrUtil.UrlEncode(addFlag)%>" enctype="MULTIPART/FORM-DATA" onSubmit="return frmAnnounce_onsubmit()">
  <table width="100%" class="tableCommon">
    <thead>
	<tr>
      <td colspan="3" style=" font-size:14px"><lt:Label res="res.label.forum.addtopic" key="topic_add_to"/>
        <%if (boardcode.equals(Leaf.CODE_BLOG)) {%>
        <%=boardname%>
        <%}else{%>
        <a href="listtopic.jsp?boardcode=<%=boardcode%>" style="font-size:14px; color:#008dd9"><%=boardname%></a>
        <%}%></td>
    </tr></thead>
    <tr>
      <td width="21%"><lt:Label res="res.label.forum.addtopic" key="topic_add_to_board"/></td>
      <td colspan="2"><%
		UserConfigDb ucd = new UserConfigDb();
		if (blogId==UserConfigDb.NO_BLOG) {
			ucd = ucd.getUserConfigDbByUserName(privilege.getUser(request));
		}
		else {
			ucd = ucd.getUserConfigDb(blogId);
		}
		
        Enumeration e = request.getParameterNames();
        String queryStrWithoutBoardcode = ""; // 去除boardcode之后的query string
        while (e.hasMoreElements()) {
            String param = (String) e.nextElement();
            if (!param.equals("boardcode")) {
                String v = ParamUtil.get(request, param);
                queryStrWithoutBoardcode += "&" + param + "=" + StrUtil.UrlEncode(v);
            }
        }
		%>
        <script>
		  var bcode<%=i%> = "<%=boardcode%>";
		  function onChangeBoard(boardCodeObj) {
		  	if(boardCodeObj.options[boardCodeObj.selectedIndex].value=='not'){
				alert(boardCodeObj.options[boardCodeObj.selectedIndex].text+' <lt:Label res="res.label.forum.addtopic" key="can_not_sel"/>');
				boardCodeObj.value=bcode<%=i%>;
				return false;
			}
			window.location.href = "addtopic_new.jsp?boardcode=" + boardCodeObj.value + "<%=queryStrWithoutBoardcode%>";
		  }
		  </script>
        <select name="boardcode" onchange="onChangeBoard(this)">
          <option value="not" selected="selected">
          <lt:Label res="res.label.forum.addtopic" key="sel_board"/>
          </option>
          <%
				com.redmoon.forum.Directory directory = new com.redmoon.forum.Directory();
				com.redmoon.forum.Leaf lf = directory.getLeaf("root");
				com.redmoon.forum.DirectoryView dv = new com.redmoon.forum.DirectoryView(lf);
				dv.ShowDirectoryAsOptions(request, privilege, out, lf, lf.getLayer());
				%>
          <%// 如果用户未激活博客或者未加入某团队博客，则不显示博客版块
			if (com.redmoon.blog.Config.getInstance().isBlogOpen && ucd!=null && ucd.isLoaded()) {
			%>
          <option value="<%=Leaf.CODE_BLOG%>">&nbsp;&nbsp;&nbsp;&nbsp;
          <lt:Label res="res.label.forum.addtopic" key="blog"/>
          </option>
          <%}%>
          <%
			com.redmoon.forum.plugin.PluginMgr pm = new com.redmoon.forum.plugin.PluginMgr();
			Iterator irPlugin = pm.getAllPlugin().iterator();
			while (irPlugin.hasNext()) {
				com.redmoon.forum.plugin.PluginUnit pu = (com.redmoon.forum.plugin.PluginUnit)irPlugin.next();
				com.redmoon.forum.plugin.base.IPluginUI ipu = pu.getUI(request, response, out);
				com.redmoon.forum.plugin.base.IPluginViewCommon pvc = ipu.getViewCommon();
				if (pvc!=null) {
					out.print(pvc.render(com.redmoon.forum.plugin.base.IPluginViewCommon.POS_BOARD_SELECT_OPTION));
				}
			}
			%>
        </select>
        <%if (boardcode.equals(Leaf.CODE_BLOG)) {%>
        <lt:Label res="res.label.forum.addtopic" key="info_board_blog"/>
        <%}%>
        <script>
		document.frmAnnounce.elements["boardcode"].value = "<%=boardcode%>";
		</script>	  </td>
    </tr>
	<%
	if (com.redmoon.blog.Config.getInstance().isBlogOpen && ucd!=null && ucd.isLoaded()) {
	%>	
    <tr>
      <td><lt:Label res="res.label.forum.addtopic" key="add_to_blog"/></td>
      <td colspan="2">
		<%
		UserDirDb udd = new UserDirDb();
		%>
            <input name=isBlog value=1 type=checkbox <%=boardcode.equals(Leaf.CODE_BLOG)?"checked":""%> />
            <lt:Label res="res.label.forum.addtopic" key="add_to_blog"/>
          (<%=ucd.getTitle()%>&nbsp;-&nbsp;
          <%if (ucd.getType()==UserConfigDb.TYPE_PERSON) {%>
          <lt:Label res="res.label.blog.user.userconfig" key="type_person"/>
          <%}else{%>
          <lt:Label res="res.label.blog.user.userconfig" key="type_group"/>
          <%}%>
          )
          <select name=blogUserDir>
            <option value="<%=UserDirDb.DEFAULT%>">
              <lt:Label res="res.label.forum.addtopic" key="default_dir"/>
            </option>
            <%=udd.toOptions(ucd.getId())%>
          </select>
          <script>
		  frmAnnounce.blogUserDir.value = "<%=blogUserDir%>";
		  </script>
          <input name="blogId" value="<%=ucd.getId()%>" type="hidden" />
          <script>
		  var blogCatalogCode = "";
		  </script>
          <select name="blogDirCode" onchange="if(this.options[this.selectedIndex].value=='not'){alert(this.options[this.selectedIndex].text+' 不能被选择！'); this.value=blogCatalogCode; return false;}else blogCatalogCode=this.value">
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
          <input name="isLocked" value="1" type="checkbox" />
          <lt:Label res="res.label.forum.addtopic" key="canReply"/>	  </td>
    </tr>
    <%}%>	
    <tr>
      <td colspan="3"><%
Vector vplugin = pm.getAllPluginUnitOfBoard(boardcode);
String pluginCode = ParamUtil.get(request, "pluginCode");
if (vplugin.size()>0) {
	Iterator irplugin = vplugin.iterator();
	while (irplugin.hasNext()) {
		PluginUnit pu = (PluginUnit)irplugin.next();
		IPluginUI ipu = pu.getUI(request, response, out);
		IPluginViewAddMsg pv = ipu.getViewAddMsg(boardcode);
		if (pv.IsPluginBoard()) {
			boolean showPlugin = false;
			if (pu.getType().equals(PluginUnit.TYPE_BOARD) || pu.getType().equals(PluginUnit.TYPE_FORUM))
				showPlugin = true;
			else if (pu.getType().equals(pu.TYPE_TOPIC)) {
				if (pluginCode.equals(pu.getCode()))
					showPlugin = true;
			}
			if (showPlugin) {
				if (!pu.getAddTopicPage().equals("")) {
%>
        <jsp:include page="<%=pu.getAddTopicPage()%>" flush="true">
        <jsp:param name="boardcode" value="<%=StrUtil.UrlEncode(boardcode)%>" />        
</jsp:include>
        <%				}
				else {
					if (pu.isShowName())
						out.print(pu.getName(request) + ":&nbsp;");
					out.print("<div>" + pv.render(UIAddMsg.POS_TITLE) + "</div>");
					out.print("<div>" + pv.render(UIAddMsg.POS_FORM_ELEMENT) + "</div>");
				}
			}
		}
	}
}

String hit = request.getParameter("hit");
String isvote = StrUtil.getNullString(request.getParameter("isvote"));
String plugin2Code = ParamUtil.get(request, "plugin2Code");
if (!plugin2Code.equals("")) {
	Plugin2Mgr p2m = new Plugin2Mgr();
	Plugin2Unit p2u = p2m.getPlugin2Unit(plugin2Code);
%>
        <jsp:include page="<%=p2u.getAddTopicPage()%>" flush="true">
        <jsp:param name="boardcode" value="<%=StrUtil.UrlEncode(boardcode)%>" />        
</jsp:include>
        <%}%></td>
    </tr>
    <%
	if (cfg1.getBooleanProperty("forum.addUseValidateCode")) {
	%>	
    <tr>
      <td><lt:Label res="res.label.forum.addtopic" key="input_validatecode"/></td>
      <td colspan="2"><input name="validateCode" type="text" size="1" />
        <img src='../validatecode.jsp' border="0" align="absmiddle" style="cursor:hand" onclick="this.src='../validatecode.jsp'" alt="<lt:Label res="res.label.forum.index" key="refresh_validatecode"/>" /></td>
    </tr>
    <%}%>	
    <tr>
      <td><lt:Label res="res.label.forum.addtopic" key="topic_title"/></td>
      <td colspan="2">
		<select name=font onchange=DoTitle(this.options[this.selectedIndex].value)>
            <option selected value="">
            <lt:Label res="res.label.forum.addtopic" key="sel_topic"/>
            </option>
            <option value=<lt:Label res="res.label.forum.addtopic" key="pre_origi"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_origi"/>
            </option>
            <option value=<lt:Label res="res.label.forum.addtopic" key="pre_from"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_from"/>
            </option>
            <option value=<lt:Label res="res.label.forum.addtopic" key="pre_water"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_water"/>
            </option>
            <option value=<lt:Label res="res.label.forum.addtopic" key="pre_discuss"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_discuss"/>
            </option>
            <option value=<lt:Label res="res.label.forum.addtopic" key="pre_help"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_help"/>
            </option>
            <option value=<lt:Label res="res.label.forum.addtopic" key="pre_recommend"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_recommend"/>
            </option>
            <option value=<lt:Label res="res.label.forum.addtopic" key="pre_notice"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_notice"/>
            </option>
            <option value=<lt:Label res="res.label.forum.addtopic" key="pre_note"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_note"/>
            </option>
            <option value=<lt:Label res="res.label.forum.addtopic" key="pre_image"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_image"/>
            </option>
            <option value=<lt:Label res="res.label.forum.addtopic" key="pre_advise"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_advise"/>
            </option>
            <option value=<lt:Label res="res.label.forum.addtopic" key="pre_download"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_download"/>
            </option>
            <option value=<lt:Label res="res.label.forum.addtopic" key="pre_share"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_share"/>
            </option>
        </select>
            <input name="topic" type="text" id="topic" size="60" maxlength="80" />
            <input type="hidden" name="boardname" value="<%=boardname%>" />
            <input type="hidden" name="hit" value="<%=hit%>" />
            <input type="hidden" name="privurl" value="<%=privurl%>" />
            <%
		  String threadType = ParamUtil.get(request, "threadType");
		  if (threadType.equals("")) {
		  	threadType = "" + ThreadTypeDb.THREAD_TYPE_NONE;
		  }

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
		  frmAnnounce.threadType.value = "<%=threadType%>";
		  </script>
            <%}
		  %>
		  <input type="hidden" name="expression" value="<%=MsgDb.EXPRESSION_NONE%>" />
		
<%
		String display="none",ischecked="false";
		if (isvote.equals("1")) {
		display = "";
		ischecked = "checked";
		}
		%></td>
    </tr>
    <tr>
      <td><input type="checkbox" name="isvote" value="1" onclick="showvote()" <%=ischecked%> />
        <lt:Label res="res.label.forum.addtopic" key="vote_option"/></td>
      <td colspan="2"><div id="voteDiv" style="display:<%=display%>">
        <lt:Label res="res.label.forum.addtopic" key="vote_expire"/>
        <input id="expire_date" name="expire_date" />
        <script type="text/javascript">
    Calendar.setup({
        inputField     :    "expire_date",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
      </script>
        <lt:Label res="res.label.forum.addtopic" key="vote_max_choice"/>
        <input name="max_choice" size="1" value="1" />
        <lt:Label res="res.label.forum.addtopic" key="vote_item"/>
        <br />
        <textarea cols="55" name="vote" rows="8" wrap="virtual"></textarea>
        <lt:Label res="res.label.forum.addtopic" key="vote_option_one_line"/>
      </div></td>
    </tr>
    <tr>
      <td valign="top"><lt:Label res="res.label.forum.showtopic" key="sel_emote"/>
        <iframe src="iframe_browlist.jsp" height="120"  width="100%" marginwidth="0" marginheight="0" frameborder="0" scrolling="yes"></iframe>
        <div> <a href="javascript:payme()">
        <lt:Label res="res.label.forum.addtopic" key="fee_to_me"/>
      </a> </div>
        <%if (cfg1.getBooleanProperty("forum.canUserSetReplyExperiencePointSee") || privilege.isManager(request, boardcode)) {%>
        <div><a href="javascript:replyCanSee()">
          <lt:Label res="res.label.forum.edittopic" key="see_by_reply"/>
        </a></div>
        <div><a href="javascript:canSee('credit')">
          <lt:Label res="res.label.forum.edittopic" key="see_by_credit"/>
        </a></div>
        <div><a href="javascript:canSee('experience')">
          <lt:Label res="res.label.forum.edittopic" key="see_by_experience"/>
        </a></div>
        <div><a href="javascript:usePoint()">
          <lt:Label res="res.label.forum.edittopic" key="see_by_fee"/>
        </a></div>
        <%}%>
        <%if (cfg1.getBooleanProperty("forum.email_notify")) {%>
        <input type="checkbox" name="email_notify" value="1" />
        <lt:Label res="res.label.forum.addtopic" key="email_notify"/>
        <%}%>
        <%if (cfg1.getBooleanProperty("forum.msg_notify")) {%>
        <br />
        <input type="checkbox" name="msg_notify" value="1" />
        <lt:Label res="res.label.forum.addtopic" key="msg_notify"/>
        <%}%>
        <%if (cfg1.getBooleanProperty("forum.sms_notify")) {%>
        <br />
        <input type="checkbox" name="sms_notify" value="1" />
        <lt:Label res="res.label.forum.addtopic" key="sms_notify"/>
        <%}%>
        <%if (cfg1.getBooleanProperty("forum.waterMarkImg") && cfg1.getBooleanProperty("forum.waterMarkOptional")) {%>
        <br />
        <input type="checkbox" name="isNeedWaterMark" value="1" checked="checked" />
        <lt:Label res="res.label.forum.addtopic" key="is_need_watermark"/>
        <%}%></td>
      <td width="81%" valign="top">
          <%@ include file="../editor_full/editor.jsp"%>
        <%
		if (privilege.canUserUpload(request, boardcode)) {
		%>
        <div id="uploadTable" style="width:98%; margin:0 auto;">
          <lt:Label res="res.label.forum.addtopic" key="file"/>
          <input type="file" name="filename" size="10" />
          <lt:Label res="res.label.forum.addtopic" key="att_desc"/>&nbsp;<input name="filenameDesc" />
          <input name="button2" type="button" onclick="AddAttach()" value="<lt:Label res="res.label.forum.addtopic" key="add_attach"/>" />
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
          </select>
          <br />
          <lt:Label res="res.label.forum.addtopic" key="file_limit_count"/>
          <%=maxAttachmentCount%>,
          <lt:Label res="res.label.forum.addtopic" key="file_limit_all_size"/>
          <%=maxAttachmentSize%>K
          <%
				if (privilege.isUserLogin(request)) {
					UserPrivDb upd = new UserPrivDb();
					upd = upd.getUserPrivDb(privilege.getUser(request));				
					%>
          ,&nbsp;<%=SkinUtil.LoadString(request, "res.label.forum.addtopic", "upload_remain_count")%><%=upd.getInt("attach_day_count") - upd.getAttachTodayUploadCount()%>
	  <%}%>
        </div>
        <div id="updiv" name="updiv"></div>
      <%}%>

<input type=hidden name="Content" />
            <input type=hidden name="isWebedit" value="<%=MsgDb.WEBEDIT_NORMAL%>" />
            <%if (cfg1.getBooleanProperty("forum.isTag")) {
				String limit = StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.showtopic", "tag_limit"), new Object[] { new Integer(cfg1.getIntProperty("forum.tagLenMax")) });
				limit += "&nbsp;&nbsp;" + StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.showtopic", "tag_single_limit"), new Object[] { new Integer(cfg1.getIntProperty("forum.tagSingleLenMax")) });
				boolean tagOnlySystemAllowed = cfg1.getBooleanProperty("forum.tagOnlySystemAllowed");
		  %>
            <div>
              <lt:Label res="res.label.forum.showtopic" key="tag"/>
              ：
              <input name="tag" size="50" <%=tagOnlySystemAllowed?"readonly":""%> />
              <br />
              (
              <lt:Label res="res.label.forum.showtopic" key="tag_format"/>
              ,<%=limit%>)</div>
          <div>
              <script>
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
		  %>
          </div>
          <div>
              <%if (privilege.isUserLogin(request) && !tagOnlySystemAllowed) {%>
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
		  %>
          </div>
      <%}%>	  </td>
     <!-- <td style="display:none" width="29%" align="center" valign="top"><%if (addFlag.equals("")) {%>
        <%@ include file="inc_emot.jsp"%>
      <%}%></td> -->
    </tr>
    <tr>
      <td colspan="3" align="center">
		<input  id="submit1" name="submit1" type=submit value="<lt:Label res="res.label.forum.addtopic" key="commit"/>" />
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		<input  name="button" type=button onclick="checkCount()" value="<lt:Label res="res.label.forum.addtopic" key="checkcount"/>" />
		<!--
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		<input name="button" type="button" onclick="window.location.href='addtopic.jsp?addFlag=<%=addFlag%>&threadType=<%=threadType%>&pluginCode=<%=StrUtil.UrlEncode(pluginCode)%>&plugin2Code=<%=StrUtil.UrlEncode(plugin2Code)%>&boardcode=<%=StrUtil.UrlEncode(boardcode)%>&blogUserDir=<%=blogUserDir%>&blogId=<%=blogId%>&privurl=<%=privurl%>'" value="<lt:Label res="res.label.forum.addtopic" key="add_ubb"/>" />
		-->
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		<%if (privilege.canWebEditRedMoon(request, boardcode)) {%>
		<input name="button" type="button" onclick="window.location.href='addtopic_we.jsp?addFlag=<%=addFlag%>&threadType=<%=threadType%>&pluginCode=<%=StrUtil.UrlEncode(pluginCode)%>&plugin2Code=<%=StrUtil.UrlEncode(plugin2Code)%>&boardcode=<%=StrUtil.UrlEncode(boardcode)%>&blogUserDir=<%=blogUserDir%>&blogId=<%=blogId%>&privurl=<%=privurl%>'" value="<lt:Label res="res.label.forum.addtopic" key="add_we"/>" />
		<%}%>
		<div id="divTmpAttachId"></div>
		</td>
    </tr>
  </table>
</form>
<%if (addFlag.equals("") && !boardcode.equals(Leaf.CODE_BLOG)) {%>
</div>
<%@ include file="inc/footer.jsp"%>
<%}%>
</div>
</body>
</html>
