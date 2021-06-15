<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.forum.*" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="com.redmoon.forum.plugin2.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.base.*"%>
<%@ page import="com.redmoon.blog.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/><jsp:useBean id="userservice" scope="page" class="com.redmoon.forum.person.userservice"/><%
if (!privilege.isUserLogin(request)) {
	response.sendRedirect("../door.jsp");
	return;
}

String boardcode = request.getParameter("boardcode");
String userName = privilege.getUser(request);
if (!privilege.canUserDo(request, boardcode, "add_topic")) {
	response.sendRedirect("../info.jsp?info= " + StrUtil.UrlEncode(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String privurl = StrUtil.toHtml(ParamUtil.get(request, "privurl"));
Leaf curleaf = new Leaf();
curleaf = curleaf.getLeaf(boardcode);
if (curleaf==null || !curleaf.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "res.label.forum.addtopic", "board_none"))); // "版块不存在"));
	return;
}
String boardname = curleaf.getName();
String blogUserDir = ParamUtil.get(request, "blogUserDir");
if (blogUserDir.equals(""))
	blogUserDir = UserDirDb.DEFAULT;
	
//if (!privilege.canWebEdit(request)) {
//	response.sendRedirect("addtopic_new.jsp?privurl="+StrUtil.UrlEncode(privurl, "utf-8")+"&boardcode="+boardcode+"&boardname="+StrUtil.UrlEncode(boardname, "utf-8"));
//	return;
//}

String name="";
if (privilege.isUserLogin(request)) {
	name = privilege.getUser(request);
}

// 取得皮肤路径
String skinPath = SkinMgr.getSkinPath(request);

com.redmoon.forum.Config cfg1 = com.redmoon.forum.Config.getInstance();
int msgTitleLengthMin = cfg1.getIntProperty("forum.msgTitleLengthMin");
int msgTitleLengthMax = cfg1.getIntProperty("forum.msgTitleLengthMax");

int msgLengthMin = cfg1.getIntProperty("forum.msgLengthMin");
int msgLengthMax = cfg1.getIntProperty("forum.msgLengthMax");

int maxAttachmentCount = cfg1.getIntProperty("forum.maxAttachmentCount");
int maxAttachmentSize = cfg1.getIntProperty("forum.maxAttachmentSize");

String plugin2Code = ParamUtil.get(request, "plugin2Code");
String pluginCode = ParamUtil.get(request, "pluginCode");

String addFlag = ParamUtil.get(request, "addFlag");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="x-ua-compatible" content="ie=7" />
<title>
<lt:Label res="res.label.forum.addtopic" key="addtopic"/> - <%=Global.AppName%></title>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css">
@import url("../util/jscalendar/calendar-win2k-2.css");
</style>
<script src="inc/ubbcode.jsp"></script>
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

var recordFilePath = "";

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

function showvote(isshow)
{
	if (frmAnnounce.isvote.checked)
	{
		frmAnnounce.vote.style.display = "";
	}
	else
	{
		frmAnnounce.vote.style.display = "none";		
	}
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
	
	if (document.getElementById("blogDirCode")!=null && document.frmAnnounce.blogDirCode.value=="not") {
		alert("<lt:Label res="res.label.blog.user.dir" key="alert"/>");
		return false;
	}	
	
	loadDataToWebeditCtrl(frmAnnounce, frmAnnounce.webedit);
	frmAnnounce.webedit.AddField("ip", "<%=request.getRemoteAddr()%>");
	frmAnnounce.webedit.AddField("plugin2Code", "<%=plugin2Code%>");
	frmAnnounce.webedit.AddField("pluginCode", "<%=pluginCode%>");

	document.frmAnnounce.submit1.disabled = true;

	frmAnnounce.webedit.UploadArticle();
	if (frmAnnounce.webedit.ReturnMessage.indexOf("+")!=-1) {
		alert(frmAnnounce.webedit.ReturnMessage);
		window.location.href = "<%=privurl%>";
	}
	else
		alert(frmAnnounce.webedit.ReturnMessage);
		
	document.frmAnnounce.submit1.disabled = false;
		
	return false;
}

function SubmitWithFileDdxc() {
	if (!cws_validateMode())
		return false;

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
	frmAnnounce.webedit.AddField("plugin2Code", "<%=plugin2Code%>");	
	frmAnnounce.webedit.MTUpload();
}
function onAddFile(index, fileName, filePath, fileSize, modifyDate) {
	
}
function Operate() {
	recordFilePath = frmAnnounce.Recorder.FilePath;
	frmAnnounce.webedit.InsertFileToList(recordFilePath);
}

function clearAll() {
	document.frmAnnounce.title.value=""
	cws_putText('');			
}

function checkWebEditInstalled() {
	var bCtlLoaded = false;
	try	{
		if (typeof(frmAnnounce.webedit.AddField)=="undefined")
			bCtlLoaded = false;
		if (typeof(frmAnnounce.webedit.AddField)=="unknown") {
			bCtlLoaded = true;
		}
	}
	catch (ex) {
	}
	if (!bCtlLoaded) {
		if (confirm("<lt:Label res="res.label.forum.addtopic" key="bctl_need_setup"/>")) {
			window.open("../activex/oa_client.exe");
		}
	}	
}

function window_onload() {
	checkWebEditInstalled();
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
//-->
</script>
<script language=JavaScript src='inc/formpost.js'></script>
</head>
<body onLoad="window_onload()">
<%if (!boardcode.equals(Leaf.CODE_BLOG)) {%>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<%@ include file="inc/position.jsp"%>
<%}%>
<%
int i=0;
%>
<form name=frmAnnounce method="post" action="">
<table class="tableCommon" width="98%" border="1" align="center">
    <thead>
      <tr>
        <td colspan="3" class="td_title"><lt:Label res="res.label.forum.addtopic" key="topic_add_to"/>
          <a href="listtopic.jsp?boardcode=<%=boardcode%>"><%=boardname%></a></td>
      </tr>
	</thead>
    <TBODY>
      <%
if (cfg1.getBooleanProperty("forum.addUseValidateCode")) {
%>
      <tr>
        <td width="20%"><lt:Label res="res.label.forum.addtopic" key="input_validatecode"/></td>
        <td colspan="2"><input name="validateCode" type="text" size="1">
          <img src='../validatecode.jsp' border=0 align="absmiddle" style="cursor:hand" onClick="this.src='../validatecode.jsp'" alt="<lt:Label res="res.label.forum.index" key="refresh_validatecode"/>"></td>
      </tr>
      <%}%>
      <tr>
        <td><lt:Label res="res.label.forum.addtopic" key="topic_add_to_board"/></td>
        <td colspan="2"><%
		UserConfigDb ucd = new UserConfigDb();
		long blogId = ParamUtil.getLong(request, "blogId", UserConfigDb.NO_BLOG);
		if (blogId==UserConfigDb.NO_BLOG) {
			ucd = ucd.getUserConfigDbByUserName(privilege.getUser(request));
		}
		else {
			ucd = ucd.getUserConfigDb(blogId);
		}
		
        java.util.Enumeration e = request.getParameterNames();
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
          <select name="boardcode" onChange="onChangeBoard(this)">
            <option value="not" selected>
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
          <script>
			frmAnnounce.boardcode.value = "<%=boardcode%>";
		  </script>
          <%if (boardcode.equals(Leaf.CODE_BLOG)) {%>
          <lt:Label res="res.label.forum.addtopic" key="info_board_blog"/>
          <%}%></td>
      </tr>
      <%if (com.redmoon.blog.Config.getInstance().isBlogOpen && ucd!=null && ucd.isLoaded()) {%>
      <tr>
        <td><lt:Label res="res.label.forum.addtopic" key="add_to_blog"/></td>
        <td colspan="2"><%
		UserDirDb udd = new UserDirDb();
		%>
          <select name=blogUserDir>
            <option value="<%=UserDirDb.DEFAULT%>">
            <lt:Label res="res.label.forum.addtopic" key="default_dir"/>
            </option>
            <%=udd.toOptions(blogId)%>
          </select>
          <script>
		frmAnnounce.blogUserDir.value = "<%=blogUserDir%>";
		  </script>
          <input name=isBlog value=1 type=checkbox <%=boardcode.equals(Leaf.CODE_BLOG)?"checked":""%>>
          <lt:Label res="res.label.forum.addtopic" key="add_to_blog"/>
(<%=ucd.getTitle()%>&nbsp;-&nbsp;
<%if (ucd.getType()==UserConfigDb.TYPE_PERSON) {%>
<lt:Label res="res.label.blog.user.userconfig" key="type_person"/>
<%}else{%>
<lt:Label res="res.label.blog.user.userconfig" key="type_group"/>
<%}%>
)
<input name="blogId" value="<%=ucd.getId()%>" type="hidden">
<script>
		  var blogCatalogCode = "";
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
           <input name="isLocked" value="1" type="checkbox">
           <lt:Label res="res.label.forum.addtopic" key="canReply"/>	    </td>
      </tr>
      <%}%>
      <tr>
        <td colspan="3"><%
Vector vplugin = pm.getAllPluginUnitOfBoard(boardcode);
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
          <%			}
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
		
if (!plugin2Code.equals("")) {
	Plugin2Mgr p2m = new Plugin2Mgr();
	Plugin2Unit p2u = p2m.getPlugin2Unit(plugin2Code);
%>
          <jsp:include page="<%=p2u.getAddTopicPage()%>" flush="true">
				<jsp:param name="boardcode" value="<%=StrUtil.UrlEncode(boardcode)%>" /> 
				</jsp:include>
          <%}%>        </td>
      </tr>
    </TBODY>
    <TBODY>
      <tr>
        <td width="20%"><lt:Label res="res.label.forum.addtopic" key="topic_title"/></td>
        <td width="80%" colspan="2"><SELECT name=font onchange=DoTitle(this.options[this.selectedIndex].value)>
            <OPTION selected value="">
            <lt:Label res="res.label.forum.addtopic" key="sel_topic"/>
            </OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_origi"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_origi"/>
            </OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_from"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_from"/>
            </OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_water"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_water"/>
            </OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_discuss"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_discuss"/>
            </OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_help"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_help"/>
            </OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_recommend"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_recommend"/>
            </OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_notice"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_notice"/>
            </OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_note"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_note"/>
            </OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_image"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_image"/>
            </OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_advise"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_advise"/>
            </OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_download"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_download"/>
            </OPTION>
            <OPTION value=<lt:Label res="res.label.forum.addtopic" key="pre_share"/>>
            <lt:Label res="res.label.forum.addtopic" key="pre_share"/>
            </OPTION>
          </SELECT>
          <input name="topic" type="text" id="topic" size="60" maxlength="80">
          <input type="hidden" name="privurl" value="<%=privurl%>">
          <%
		  String threadType = ParamUtil.get(request, "threadType");
		  if (threadType.equals("")) {
		  	threadType = "" + ThreadTypeDb.THREAD_TYPE_NONE;
		  }
		  %>
          <%
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
		  %>        </td>
      </tr>
      <tr>
        <td valign="top"><lt:Label res="res.label.forum.addtopic" key="emote_icon"/>
          <br>        </td>
        <td colspan="2"><iframe src="iframe_emote.jsp" height="25"  width="500px" marginwidth="0" marginheight="0" frameborder="0" scrolling="yes"></iframe>
          <input type="hidden" name="expression" value="<%=MsgDb.EXPRESSION_NONE%>"></td>
      </tr>
      <tr>
        <td valign="top" width="20%"><%
		String display="none",ischecked="false";
		if (isvote.equals("1")) {
		display = "";
		ischecked = "checked";
		}
		%>
          <input type="checkbox" name="isvote" value="1" onClick="showvote()" <%=ischecked%>>
          <lt:Label res="res.label.forum.addtopic" key="vote_option"/>        </td>
        <td width="80%" colspan="2"><lt:Label res="res.label.forum.addtopic" key="vote_expire"/>
          <input id="expire_date" name="expire_date">
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
          <input name="max_choice" size=1 value="1">
          <lt:Label res="res.label.forum.addtopic" key="vote_item"/>
		  <!--
          [<a href="#" onClick="spwhitepad();return false;">
          <lt:Label res="res.label.forum.addtopic" key="whitepad"/>
          </a>]
		  -->
          <script language="JavaScript">
	function spwhitepad(){
		var win = window.open("../spwhitepad/editor.htm","spwhitepadeditor","width=420,height=340,left=200,top=50,toolbar=no,menubar=no,scrollbars=no,resizable=no,location=no,status=no");
		win.focus();
	}
	
	function insertStroke(code) {
		setHTML(getHTML() + code);
	}
	</script>
          <br>
          <textarea style="display:<%=display%>" cols="55" name="vote" rows="8" wrap="VIRTUAL"></textarea>
          <lt:Label res="res.label.forum.addtopic" key="vote_option_one_line"/></td>
      </tr>
      <tr>
        <td valign="bottom"><table width="100%" border="0" cellspacing="0" cellpadding="5">
            <tr>
              <td><a href="javascript:payme()">
                <lt:Label res="res.label.forum.addtopic" key="fee_to_me"/>
                </a></td>
            </tr>
          </table>
          <%if (cfg1.getBooleanProperty("forum.canUserSetReplyExperiencePointSee") || privilege.isManager(request, boardcode)) {%>
          <table width="100%" border="0" cellspacing="0" cellpadding="5">
            <tr>
              <td><a href="javascript:replyCanSee()">
                <lt:Label res="res.label.forum.edittopic" key="see_by_reply"/>
                </a></td>
            </tr>
            <tr>
              <td><a href="javascript:canSee('credit')">
                <lt:Label res="res.label.forum.edittopic" key="see_by_credit"/>
                </a></td>
            </tr>
            <tr>
              <td><a href="javascript:canSee('experience')">
                <lt:Label res="res.label.forum.edittopic" key="see_by_experience"/>
                </a></td>
            </tr>
            <tr>
              <td><a href="javascript:usePoint()">
                <lt:Label res="res.label.forum.edittopic" key="see_by_fee"/>
                </a></td>
            </tr>
          </table>
          <%}%>
		  <%if (cfg1.getBooleanProperty("forum.email_notify")) {%>		  
          <input type="checkbox" name="email_notify" value="1">
          <lt:Label res="res.label.forum.addtopic" key="email_notify"/>
		  <%}%>
		  <%if (cfg1.getBooleanProperty("forum.msg_notify")) {%>
          <BR><input type="checkbox" name="msg_notify" value="1">
          <lt:Label res="res.label.forum.addtopic" key="msg_notify"/>
		  <%}%>
		  <%if (cfg1.getBooleanProperty("forum.sms_notify")) {%>
          <BR><input type="checkbox" name="sms_notify" value="1">
          <lt:Label res="res.label.forum.addtopic" key="sms_notify"/>
		  <%}%>
		  <%if (cfg1.getBooleanProperty("forum.waterMarkImg") && cfg1.getBooleanProperty("forum.waterMarkOptional")) {%>
          <BR><input type="checkbox" name="isNeedWaterMark" value="1" checked="checked">
          <lt:Label res="res.label.forum.addtopic" key="is_need_watermark"/>
		  <%}%>        </td>
        <td>
		<%
		request.setAttribute("isWebedit", "true");
		%>
		<%@ include file="../editor_full/editor.jsp"%></td>
        
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
                  <param name="PostScript" value="<%=Global.virtualPath%>/forum/addtopictodbwe.jsp">
                  <param name="PostScriptDdxc" value="<%=Global.virtualPath%>/forum/forumddxc.jsp">
                  <param name="SegmentLen" value="204800">
                  <param name="BasePath" value="<%=BasePath%>">
                </object></td>
            </tr>
            <tr>
              <td align="center" valign="middle" bgcolor="#FFFFFF"><table width="100%" id="table_recorder" style="display:none">
                  <tr>
                    <td align="center"><a href="../activex/oa_client.exe">
                      <lt:Label res="res.label.forum.addtopic" key="download_recorder"/>
                      </a></td>
                  </tr>
                  <tr>
                    <td align="center"><OBJECT ID="Recorder" CLASSID="CLSID:E4A3D135-E189-48AF-B348-EF5DFFD99A67" codebase="../activex/cloudym.CAB#version=1,2,0,1">
                      </OBJECT></td>
                  </tr>
                </table></td>
            </tr>
          </table>
          <%if (cfg1.getBooleanProperty("forum.isTag")) {
				String limit = StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.showtopic", "tag_limit"), new Object[] { new Integer(cfg1.getIntProperty("forum.tagLenMax")) });
				limit += "&nbsp;&nbsp;" + StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.showtopic", "tag_single_limit"), new Object[] { new Integer(cfg1.getIntProperty("forum.tagSingleLenMax")) });
				boolean tagOnlySystemAllowed = cfg1.getBooleanProperty("forum.tagOnlySystemAllowed");
		  %>
          <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <tr>
              <td height="24"><lt:Label res="res.label.forum.showtopic" key="tag"/>
                ：
                <input name="tag" size="50" <%=tagOnlySystemAllowed?"readonly":""%>>
                <br>
                (
                <lt:Label res="res.label.forum.showtopic" key="tag_format"/>
                ,<%=limit%> )</td>
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
        <td colspan="3" valign="bottom"><!--<input name="cmdok2" type="button" value="断点续传" onClick="return SubmitWithFileDdxc()">
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-->
          <input name="submit1" type=button onClick="frmAnnounce_onsubmit()" value="<lt:Label res="res.label.forum.addtopic" key="commit"/>">
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
          <input type=button onClick="checkCount()" value="<lt:Label res="res.label.forum.addtopic" key="checkcount"/>">
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
          <input name="cmdcancel" type="button" onClick="clearAll()" value="<lt:Label res="res.label.forum.addtopic" key="clear"/>">
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		  <!--
          <input name="cmdcancel2" type="button" onClick="table_recorder.style.display=''" value="<lt:Label res="res.label.forum.addtopic" key="record"/>">
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		  -->
          <input type="button" onClick="window.location.href='addtopic_new.jsp?pluginCode=<%=StrUtil.UrlEncode(pluginCode)%>&threadType=<%=threadType%>&plugin2Code=<%=StrUtil.UrlEncode(plugin2Code)%>&boardcode=<%=StrUtil.UrlEncode(boardcode)%>&isvote=<%=isvote%>&blogUserDir=<%=blogUserDir%>&blogId=<%=blogId%>&addFlag=<%=addFlag%>&privurl=<%=privurl%>'" value="<lt:Label res="res.label.forum.addtopic" key="add_normal"/>">
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
          <input type="button" onClick="window.location.href='addtopic.jsp?threadType=<%=threadType%>&pluginCode=<%=StrUtil.UrlEncode(pluginCode)%>&plugin2Code=<%=StrUtil.UrlEncode(plugin2Code)%>&boardcode=<%=StrUtil.UrlEncode(boardcode)%>&isvote=<%=isvote%>&blogUserDir=<%=blogUserDir%>&blogId=<%=blogId%>&addFlag=<%=addFlag%>&privurl=<%=privurl%>'" value="<lt:Label res="res.label.forum.addtopic" key="add_ubb"/>"></td>
      </tr>
    </TBODY>
</table>
</form>
<%if (!boardcode.equals(Leaf.CODE_BLOG)) {%>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
<%}%>
</body>
</html>
