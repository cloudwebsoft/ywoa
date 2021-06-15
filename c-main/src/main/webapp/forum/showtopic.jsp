<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ad.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin2.*"%>
<%@ page import="com.redmoon.forum.plugin.base.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%@ taglib uri="/WEB-INF/tlds/AdTag.tld" prefix="ad"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
long pageBeginTime =  System.currentTimeMillis();

String querystring = StrUtil.getNullString(request.getQueryString());
String privurl = request.getRequestURL()+"?"+StrUtil.UrlEncode(querystring);

if (!Privilege.isUserLogin(request)) {
	if (!ForumDb.getInstance().canGuestSeeTopic()) {
		response.sendRedirect(request.getContextPath() + "/info.jsp?op=login&info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "info_please_login")) + "&privurl=" + privurl);
		return;
	}
}

long rootid;
try {
	rootid = ParamUtil.getLong(request, "rootid");
}
catch (Exception e) {
	response.sendRedirect(request.getContextPath() + "/info.jsp?info= " + StrUtil.UrlEncode(SkinUtil.LoadString(request, SkinUtil.ERR_ID))); // 标识非法！
	return;
}

MsgDb msgdb = new MsgDb();
msgdb = msgdb.getMsgDb(rootid);

if (msgdb.getCheckStatus()==MsgDb.CHECK_STATUS_NOT) {
	response.sendRedirect(request.getContextPath() + "/info.jsp?info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "res.label.forum.showtopic", "check_not")) + "&privurl=" + privurl);	
	return;
}

// 保存下来，以用于快速回复区的插件提示
MsgDb rootMsgDb = msgdb;

if (!msgdb.isLoaded()) {
	response.sendRedirect(request.getContextPath() + "/info.jsp?info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "res.label.forum.showtopic", "topic_lost"))); // "该贴已不存在！"));
	return;
}

// 如果是朋友圈版块中的贴子
if (!com.redmoon.forum.plugin.group.GroupPrivilege.canUserSee(request, msgdb)) {
	out.print(SkinUtil.makeErrMsg(request, "只有圈内成员才能访问!"));
	return;
}

String boardcode = msgdb.getboardcode();

try {
	privilege.checkCanEnterBoard(request, boardcode);
}
catch (ErrMsgException e) {
	response.sendRedirect(request.getContextPath() + "/info.jsp?info=" + StrUtil.UrlEncode(e.getMessage()) + "&privurl=" + privurl);
	return;
}

if (!privilege.canUserDo(request, boardcode, "view_topic")) {
	response.sendRedirect(request.getContextPath() + "/info.jsp?info= " + StrUtil.UrlEncode(SkinUtil.LoadString(request, "pvg_invalid")) + "&privurl=" + privurl);
	return;
}

if (msgdb.getCheckStatus()==MsgDb.CHECK_STATUS_DUSTBIN) {
	if (!Privilege.isMasterLogin(request)) {
		response.sendRedirect(request.getContextPath() + "/info.jsp?info= " + StrUtil.UrlEncode(SkinUtil.LoadString(request, "res.label.forum.showtopic", "check_dustbin")));
		return;
	}
	else {
		response.sendRedirect("showtopic_tree.jsp?rootid=" + rootid);	
		return;
	}	
}

Leaf msgLeaf = new Leaf();
msgLeaf = msgLeaf.getLeaf(boardcode);

String boardname = msgLeaf.getName();

UserSession.setBoardCode(request, boardcode);

// 取得皮肤路径
String skinPath = SkinMgr.getSkinPath(request, msgLeaf);

com.redmoon.forum.Config cfg1 = com.redmoon.forum.Config.getInstance();
int msgTitleLengthMin = cfg1.getIntProperty("forum.msgTitleLengthMin");
int msgTitleLengthMax = cfg1.getIntProperty("forum.msgTitleLengthMax");
int msgLengthMin = cfg1.getIntProperty("forum.msgLengthMin");
int msgLengthMax = cfg1.getIntProperty("forum.msgLengthMax");
int maxAttachmentCount = cfg1.getIntProperty("forum.maxAttachmentCount");
int maxAttachmentSize = cfg1.getIntProperty("forum.maxAttachmentSize");

com.redmoon.forum.util.SeoConfig scfg = new com.redmoon.forum.util.SeoConfig();
String seoTitle = scfg.getProperty("seotitle");
String seoKeywords = scfg.getProperty("seokeywords");
String seoHead = scfg.getProperty("seohead");
String seoDescription = StrUtil.left(msgdb.getContent(),100);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE><%=StrUtil.toHtml(msgdb.getTitle())%> - <%=Global.AppName%> <%=seoTitle%></TITLE>
<%=seoHead%>
<META http-equiv=Content-Type content=text/html; charset=utf-8>
<META name="keywords" content="<%=seoKeywords%>">
<META name="description" content="<%=StrUtil.toHtml(seoDescription)%>">
<LINK href="<%=skinPath%>/css.css" type=text/css rel=stylesheet>
<LINK href="images/bbs.ico" rel="SHORTCUT ICON">
<script tyle="text/javascript" language="javascript" src="../inc/common.js"></script>
<SCRIPT language=JavaScript>
<!--
var warterMarkLen = <%=cfg1.getIntProperty("forum.waterMarkCodeLen")%>;
var copyright = "(<%=Global.AppName%>) [<a target=\"_blank\" href=\"<%=Global.getRootPath()%>\"><%=Global.getRootPath()%></a>]";
//-->
</SCRIPT>
<SCRIPT language=JavaScript src="inc/showtopic.js"></SCRIPT>
<SCRIPT language=JavaScript src="images/nereidFade.js"></SCRIPT>
<SCRIPT>
var i=0;
function formCheck(){
	i++;
	document.frmAnnounce.Content.value = getHTML();
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
	
	if (i>=1) 
	{
		document.frmAnnounce.submit1.disabled = true;
	}
	return true;
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

function restoreContent() {
	setHtml(o("Content"));
}
</SCRIPT>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"></HEAD>
<BODY onLoad="restoreContent()">
<div id="wrapper">
	<%@ include file="inc/header.jsp"%>
  	<div id="main">
  	<jsp:include page="inc/position.jsp" flush="true">
    <jsp:param name="boardcode" value="<%=StrUtil.UrlEncode(boardcode)%>" />      
	</jsp:include>
  	<ad:AdTag type="<%=AdDb.TYPE_FLOAT%>" boardCode="<%=boardcode%>"></ad:AdTag>
	<ad:AdTag type="<%=AdDb.TYPE_COUPLE%>" boardCode="<%=boardcode%>"></ad:AdTag>
	<jsp:useBean id="userservice" scope="page" class="com.redmoon.forum.person.userservice"/>
	<%
	// 刷新在线时间
	userservice.refreshStayTime(request, response);
	
	String sqlt = "select id from sq_thread where boardcode=" + StrUtil.sqlstr(boardcode)+"  ORDER BY msg_level desc,redate desc";
	ThreadBlockIterator irthread = msgdb.getThreads(sqlt, boardcode, 0, 200);
	irthread.setIndex(msgdb);
	
	UserMgr um = new UserMgr();
	
	PluginMgr pmnote = new PluginMgr();
	Vector vplugin = pmnote.getAllPluginUnitOfBoard(boardcode);
	if (vplugin.size()>0) {
		Iterator irpluginnote = vplugin.iterator();
		while (irpluginnote.hasNext()) {
			PluginUnit pu = (PluginUnit)irpluginnote.next();
			IPluginUI ipu = pu.getUI(request, response, out);
			IPluginViewShowMsg pv = ipu.getViewShowMsg(boardcode, msgdb);
			String note = pv.render(UIShowMsg.POS_NOTE);
			if (!note.equals("")) {
		%>
		<div class="position">
			<%out.print(pu.getName(request) + "&nbsp;" + note + "<BR>");%>
		</div>
		<%
			}
			boolean isShow = false;
			if (pu.getType().equals(PluginUnit.TYPE_BOARD))
				isShow = true;
			else if (pu.getType().equals(PluginUnit.TYPE_TOPIC)) {
				if (pu.getUnit().isPluginMsg(msgdb.getId()))
					isShow = true;
			}
			if (isShow)		
				pv.render(UIShowMsg.POS_AFTER_NOTE);	
		}
	}

	String showUserName = ParamUtil.get(request, "showUserName");
	String sql = SQLBuilder.getShowtopicSql(request, response, out, rootMsgDb, showUserName);	// "select id from sq_message where rootid=" + rootid + " ORDER BY lydate asc"; //orders"; 这样会使得顺序上不按时间，平板式时会让人觉得奇怪

	int pagesize = cfg1.getIntProperty("forum.showTopicPageSize");
	long totalmsg = msgdb.getMsgCount(sql, boardcode, rootid);
	
	ForumPaginator paginator = new ForumPaginator(request, totalmsg, pagesize);
	int curpage = paginator.getCPage(request);
	//设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0) {
		curpage = 1;
		totalpages = 1;
	}
	
	int start = (curpage-1)*pagesize;
	int end = curpage*pagesize;
	
	MsgBlockIterator irmsg = msgdb.getMsgs(sql, boardcode, rootid, start, end);
%>
  	<div class="btnArea">
  	<span class="addTopic">
		<%
		String addpage = "addtopic_new.jsp";
		String replypage = "addreply_new.jsp";
		if (cfg1.getBooleanProperty("forum.isWebeditTopicEnabled") && msgLeaf.getWebeditAllowType()==Leaf.WEBEDIT_ALLOW_TYPE_REDMOON_FIRST) {
			addpage = "addtopic_we.jsp";
			replypage = "addreply_we.jsp";
		}
		%>
	  <span id="buttonSpan" style="display:none">
		<A href="<%=addpage%>?isvote=1&boardcode=<%=boardcode%>&privurl=<%=privurl%>"> 
        <IMG src="<%=skinPath%>/images/votenew_<%=SkinUtil.getLocale(request)%>.gif" border=0 alt="<lt:Label res="res.label.forum.listtopic" key="vote_btn"/>">&nbsp;&nbsp;<lt:Label res="res.label.forum.listtopic" key="vote_btn"/></A>	  
	  <%
		if (vplugin.size()>0) {
			Iterator irplugin = vplugin.iterator();
			while (irplugin.hasNext()) {
				PluginUnit pu = (PluginUnit)irplugin.next();
				IPluginUI ipu = pu.getUI(request, response, out);
				IPluginViewListThread pv = ipu.getViewListThread(boardcode);
				if (pu.getType().equals(PluginUnit.TYPE_TOPIC) && !pu.getButton().equals("")) {%><a title="<%=pu.getName(request)%>" href="<%=addpage%>?pluginCode=<%=pu.getCode()%>&boardcode=<%=StrUtil.UrlEncode(boardcode)%>&privurl=<%=privurl%>"><img src="<%=skinPath + "/" + pu.getButton()%>_<%=SkinUtil.getLocale(request)%>.gif" border="0"><%=pu.getName(request)%></a><%}
			}
		}
		Vector vplugin2 = msgLeaf.getAllPlugin2();
		Iterator irplugin2 = vplugin2.iterator();
		while (irplugin2.hasNext()) {
			com.redmoon.forum.plugin2.Plugin2Unit p2u = (com.redmoon.forum.plugin2.Plugin2Unit)irplugin2.next();
		%><a href="<%=addpage%>?plugin2Code=<%=p2u.getCode()%>&boardcode=<%=StrUtil.UrlEncode(boardcode)%>&privurl=<%=privurl%>"><img src="<%=skinPath + "/images/" + p2u.getButton()%>_<%=SkinUtil.getLocale(request)%>.gif" border="0"><%=p2u.getName(request)%></a><%}%></span>
	  	<a href="<%=addpage%>?boardcode=<%=boardcode%>&privurl=<%=privurl%>"><img onmouseover='menuOffY=29;if($("buttonSpan").innerHTML!="") showmenu(event, $("buttonSpan").innerHTML, 0);menuOffY=18' src="<%=skinPath%>/images/post_<%=SkinUtil.getLocale(request)%>.gif" border=0 alt="<lt:Label res="res.label.forum.showtopic" key="addtopic"/>"></a>
		<a href="<%=replypage%>?boardcode=<%=boardcode%>&replyid=<%=rootid%>&privurl=<%=privurl%>"> <img src="<%=skinPath%>/images/newreply_<%=SkinUtil.getLocale(request)%>.gif" border=0 alt="<lt:Label res="res.label.forum.showtopic" key="addreply"/>"></a>
		&nbsp;&nbsp;<a href="<%=ForumPage.getShowTopicPage(request, 1, rootid, rootid, 1, "")%>" title="<lt:Label res="res.label.forum.showtopic" key="tree_view"/>"><img border=0 src="images/treeview.gif"></a>&nbsp;&nbsp;
        <%
		if (irthread.hasPrevious()) {
			MsgDb prevMsg = (MsgDb)irthread.previous();
			irthread.next();
		%>
			<A href="<%=ForumPage.getShowTopicPage(request, prevMsg.getId())%>"><IMG alt=<lt:Label res="res.label.forum.showtopic" key="show_pre"/> src="images/prethread.gif" border=0></A>
	  	<%}%>&nbsp;&nbsp;
        <A href="javascript:location.reload()"><IMG alt=<lt:Label res="res.label.forum.showtopic" key="refresh"/> src="images/refresh.gif" border=0></A>
        &nbsp;
        <%if (irthread.hasNext()) {
        	MsgDb nextMsg = (MsgDb)irthread.next();
		%>
        	<A href="<%=ForumPage.getShowTopicPage(request, nextMsg.getId())%>"><IMG alt=<lt:Label res="res.label.forum.showtopic" key="show_after"/> src="images/nextthread.gif" border=0></A>
        <%}else{%>
        	&nbsp;
        <%}%>
		&nbsp;<lt:Label res="res.label.forum.showtopic" key="hit_begin"/> <b><span id="spanhit" name="spanhit"><%=rootMsgDb.getHit() + 1%></span></b><lt:Label res="res.label.forum.showtopic" key="hit_end"/>			
	  </span>
	  <span class="op">
	  <%
      out.print(paginator.getShowTopicCurPageBlock(request, rootid, "up"));
  	  %>
	  </span>
	</div>
	<div class="showTopicWrapper" id="showTopic">
	<table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0" class="showTopicTab">
      <thead>
        <tr style="display:none">
          <td class="separaterL" style="border:0px;height:0px"></td>
          <td class="separaterR" style="border:0px;height:0px"></td>
        </tr>
        <tr>
          <td colspan="2">
		  <span class="topicTitle">
		  <lt:Label res="res.label.forum.showtopic" key="topic"/><%=DefaultRender.RenderFullTitle(request, rootMsgDb)%>
		  </span>
		  <span class="topicOp">
			  <%
			  String allclr = "";
			  String rootclr = "";
			  if (showUserName.equals(rootMsgDb.getName())) {
			  	rootclr = "red";
			  }
			  else {
			  	allclr = "red";
			  }
			  %>
			  <a href="showtopic.jsp?rootid=<%=rootid%>"><font color="<%=allclr%>">[<lt:Label res="res.label.forum.showtopic" key="all_user"/>]</font></a>
			  <a href="showtopic.jsp?rootid=<%=rootid%>&showUserName=<%=StrUtil.UrlEncode(rootMsgDb.getName())%>"><font color="<%=rootclr%>">[<lt:Label res="res.label.forum.showtopic" key="root_user"/>]</font></a>
			  <A href="javascript:window.print()">[<lt:Label res="res.label.forum.showtopic" key="print"/>]</A>
			  <A href="javascript:window.external.AddFavorite(location.href,document.title)">[<lt:Label res="res.label.forum.showtopic" key="favoriate"/>]</A>
		  </span>
		  </td>
        </tr>
      </thead>
	  <%if (rootMsgDb.isBlog() || rootMsgDb.getLastOperate()!=MsgDb.LAST_OPERATE_NONE) {%>
      <tbody>
        <tr>
          <td colspan="2" class="showTopicTd"><div class="opHis">
			<%if (rootMsgDb.isBlog()) {%>
			<a href="../blog/showblog.jsp?rootid=<%=rootMsgDb.getId()%>" target="_blank">
			<lt:Label res="res.label.forum.showtopic" key="topic_blog"/>
			</a>&nbsp;&nbsp;&nbsp;&nbsp;
			<%}%>
			<%
			if (rootMsgDb.getLastOperate()!=MsgDb.LAST_OPERATE_NONE) {
				MsgOperateDb mod = new MsgOperateDb();
				mod = mod.getMsgOperateDb(rootMsgDb.getLastOperate());
				if (mod!=null)
					out.print("<a target=_blank href='topic_op.jsp?msgId=" + rootMsgDb.getId() + "'>" + mod.getOperateDesc(request) + "</a>");
			}
			%>
		  </div></td>
        </tr>
      </tbody>
	  <%}%>
<%
// 取得显示设置
BoardRenderDb boardRender = new BoardRenderDb();
boardRender = boardRender.getBoardRenderDb(boardcode);
IPluginRender render = boardRender.getRender();
String name="",lydate="",content="",topic="";
String RegDate="",Gender="",RealPic="",email="",sign="",myface="",nick="";
int experience=0;
int addcount=0;
long id;
int credit=0;
int islocked=0,iselite=0,lylevel=0,isguide=0;
int type=0;
int show_ubbcode=1,show_smile=1;
int iswebedit = 0;
int i = 0;
Vector v_ad = AdDb.getADOnBoard(boardcode, AdDb.TYPE_TOPIC_BOTTOM);
int ad_count = 0;
while (irmsg.hasNext()) {
	msgdb = (MsgDb)irmsg.next();
	i++;
	id = msgdb.getId();
	name = msgdb.getName();
	topic = msgdb.getTitle();
	content = msgdb.getContent();
	//lydate = com.redmoon.forum.ForumSkin.formatDateTime(request, msgdb.getAddDate());
	lydate = DateUtil.format(msgdb.getAddDate(), "yyyy-MM-dd HH:mm:ss");
	type = msgdb.getType();
	islocked = msgdb.getIsLocked();
	iselite = msgdb.getIsElite();
	lylevel = msgdb.getLevel();
	iswebedit = msgdb.getIsWebedit();
	show_ubbcode = msgdb.getShowUbbcode();
	show_smile = msgdb.getShowSmile();

	UserDb user = null;
	if (!name.equals("")) {
		user = um.getUser(name);
		nick = user.getNick();
		RealPic = user.getRealPic();
		Gender = user.getGender();
		if (Gender.equals("M"))
			Gender = SkinUtil.LoadString(request, "res.label.forum.showtopic", "sex_man"); // "男";
		else if (Gender.equals("F"))
			Gender = SkinUtil.LoadString(request, "res.label.forum.showtopic", "sex_woman"); // "女";
		else
			Gender = SkinUtil.LoadString(request, "res.label.forum.showtopic", "sex_none"); // "不详";
		RegDate = com.redmoon.forum.ForumSkin.formatDate(request, user.getRegDate());
		experience = user.getExperience();
		credit = user.getCredit();
		addcount = user.getAddCount();
		email = user.getEmail();
		sign = user.getSign();
		myface = user.getMyface();
	}
%>	  
      <tbody>
        <tr>
          <td class="userInfo showTopicTd" style="background:#e5ecf2"><ul>
            <%if (!name.equals("")) {%>
				<%
				  BoardManagerDb bm = new BoardManagerDb();
				  bm = bm.getBoardManagerDb(Leaf.CODE_ROOT, name);
				 
				%>
                <li>
                  <%if (!name.equals("")) {%>
			<a style="line-height:22px;font-weight:700; font-size:14px" target="_blank" href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(user.getName())%>" title="<%=StrUtil.toHtml(nick)%><lt:Label res="res.label.forum.showtopic" key="user_info"/>"><%=user.getNick()%></a>
			<%}%>
                </li>
				<li>
                <%if (myface.equals("")) {%>
                	<img src="images/face/<%=RealPic.equals("") ? "face.gif" : RealPic%>" />
                <%}else{%>
                	<img src="<%=user.getMyfaceUrl(request)%>" />
                <%}%>
				</li>
                 <li style='color:#ff3368;font-weight:700'>
                <%
				
				// 验证是否为总版
				  if (bm.isLoaded() && !bm.isHide()) {
					out.print(SkinUtil.LoadString(request, "res.label.forum.listtopic", "superManager"));
				  }
				  else {
					bm = bm.getBoardManagerDb(boardcode, name);
					if (bm.isLoaded() && !bm.isHide()) {
						out.print( SkinUtil.LoadString(request, "res.label.forum.listtopic", "manager"));
					}
				  }
				   UserGroupDb ugd = user.getUserGroupDb();
				  if (!ugd.getCode().equals(UserGroupDb.EVERYONE)) {
					out.print("&nbsp;&nbsp;" +ugd.getDesc());
				  }
				%>
                </li>
                <li><img src="images/<%=user.getLevelPic()%>" /></li>
                 <li><font color="#2174c4"><lt:Label res="res.label.forum.showtopic" key="rank"/></font><%=user.getLevelDesc()%></li>
			  	<li><font color="#2174c4"><lt:Label res="res.label.forum.showtopic" key="gender"/></font><%=Gender%></li>				
				<%
				ScoreMgr sm = new ScoreMgr();
				Vector vscore = sm.getAllScore();
				Iterator irscore = vscore.iterator();
				String str = "";
				while (irscore.hasNext()) {
					ScoreUnit scoreUnit = (ScoreUnit) irscore.next();
					if (scoreUnit.isDisplay()) {
						out.print("<li><font color='#2174c4'>" + scoreUnit.getName(request) + "：</font>" + (int)scoreUnit.getScore().getUserSum(user.getName()) + "</li>");
					}
				}
				%>
				<li><font color="#2174c4"><lt:Label res="res.label.forum.showtopic" key="topic_count"/></font>
				<%=addcount%></li>
				<li><font color="#2174c4"><lt:Label res="res.label.forum.showtopic" key="topic_elite"/></font>
				<%=user.getEliteCount()%></li>
				<li><font color="#2174c4"><lt:Label res="res.label.forum.showtopic" key="reg_date"/></font>
				<%=RegDate%></li>
				<li><font color="#2174c4"><lt:Label res="res.label.forum.showtopic" key="online_status"/></font>
				<%
				OnlineUserDb ou = new OnlineUserDb();
				ou = ou.getOnlineUserDb(user.getName());
				if (ou.isLoaded())
					out.print(SkinUtil.LoadString(request, "res.label.forum.showtopic", "online_status_yes")); // "在线");
				else
					out.print(SkinUtil.LoadString(request, "res.label.forum.showtopic", "online_status_no")); // "离线");
				%>
				</li>
				<%if (cfg1.getBooleanProperty("forum.isOnlineTimeRecord")) {%>
				<li>
				<font color="#2174c4"><lt:Label res="res.label.forum.showtopic" key="online_time"/></font>
				<%=(int)user.getOnlineTime()%>
				<lt:Label res="res.label.forum.showtopic" key="hour"/></li>
				<%}%>
				<%if (cfg1.getBooleanProperty("forum.isFactionUsed")) {
					UserPropDb up = new UserPropDb();
					up = up.getUserPropDb(user.getName());
					String faction = StrUtil.getNullStr(up.getString("faction"));
					if (!faction.equals("")) {
						FactionDb fd = new FactionDb();
						fd = fd.getFactionDb(faction);
						if (fd!=null)
							faction = fd.getName();
					}
					if (!faction.equals("")) {						
				%>
					<li><font color="#2174c4"><lt:Label res="res.label.forum.showtopic" key="faction"/></font><%=faction%></li>
				<%	}
				}%>
				<%
				if (cfg1.getBooleanProperty("forum.showFlowerEgg")) {
					UserPropDb up = new UserPropDb();
					up = up.getUserPropDb(user.getName());
					%>
					<li><img src="../images/flower.gif" />&nbsp;(<%=up.getInt("flower_count")%>)&nbsp;<img src="../images/egg.gif" />&nbsp;(<%=up.getInt("egg_count")%>)</li>
				<%}%>
			<%}else{%>
				<li><lt:Label res="res.label.forum.showtopic" key="anonym"/></li>
			<%}%>
          </ul>			
		  </td>
          <td class="showTopicTd topicCnt">
		  <div class="topicCntHeader">
			<a name="#<%=id%>" id="#<%=id%>"></a>
			
			发表于&nbsp;<%=lydate%>
			<%if (com.redmoon.blog.Config.getInstance().isBlogOpen) {%>
				&nbsp;&nbsp;<a target="_blank" href="../blog/myblog.jsp?blogUserName=<%=StrUtil.UrlEncode(name)%>"><lt:Label res="res.label.forum.showtopic" key="blog"/></a>
			<%}%>
			&nbsp;&nbsp;<a href="#" onclick="hopenWin('../message/send.jsp?receiver=<%=StrUtil.UrlEncode(nick,"utf-8")%>',320,260)"><img src="images/pm.gif" border="0" align="absmiddle" />&nbsp;<lt:Label res="res.label.forum.showtopic" key="send_short_msg"/></a>
			<%
			if (vplugin.size()>0) {
				Iterator irplugin = vplugin.iterator();
				while (irplugin.hasNext()) {
					com.redmoon.forum.plugin.PluginUnit pu = (com.redmoon.forum.plugin.PluginUnit)irplugin.next();
					com.redmoon.forum.plugin.base.IPluginUI ipu = pu.getUI(request, response, out);
					com.redmoon.forum.plugin.base.IPluginViewCommon pvc = ipu.getViewCommon();
					if (pvc!=null) {
						String viewStr = pvc.render(com.redmoon.forum.plugin.base.IPluginViewCommon.POS_TOPIC_TOOLBAR);
						if (!viewStr.equals(""))
							out.print("&nbsp;&nbsp;" + viewStr);
					}
				}
			}

			String editpage = "edittopic_new.jsp";
			if (iswebedit==MsgDb.WEBEDIT_UBB) {
			editpage = "edittopic.jsp";
			} else if (iswebedit==MsgDb.WEBEDIT_REDMOON) {
			editpage = "edittopic_we.jsp";
			}
			String mstr = "<a href='addfriend.jsp?friend=" + StrUtil.UrlEncode(name) + "'>" + SkinUtil.LoadString(request, "res.label.forum.showtopic", "add_friend") + "</a>";
			mstr += "<a href='" + editpage + "?boardcode=" + StrUtil.UrlEncode(boardcode) + "&editid=" + id + "&privurl=" + privurl + "'>" + SkinUtil.LoadString(request, "res.label.forum.showtopic", "topic_edit") + "</a>";
			mstr += "<a href='myfavoriate.jsp?op=add&privurl=" + privurl + "&id=" + rootid + "'>" + SkinUtil.LoadString(request, "res.label.forum.showtopic", "add_favoriate") + "</a>";
			mstr += "<a href='message_report.jsp?privurl=" + privurl + "&msg_id=" + id + "'>" + SkinUtil.LoadString(request, "res.label.forum.showtopic", "message_report") + "</a>";
			mstr += "<a href='message_recommend.jsp?privurl=" + privurl + "&msg_id=" + id + "'>" + SkinUtil.LoadString(request, "res.label.forum.showtopic", "message_recommend") + "</a>";		  
			mstr += "<a href='score_transfer.jsp?nick=" + StrUtil.UrlEncode(nick) + "'>" + SkinUtil.LoadString(request, "res.label.forum.showtopic", "bestow_scroe") + "</a>";
			if (rootid==id) { // 当为根贴时
				String toptitle="";
				toptitle = SkinUtil.LoadString(request, "res.label.forum.showtopic", "top_board"); // "版块置顶";
                mstr += "<a href='manager/changeontop.jsp?privurl=" + privurl + "&boardcode=" + StrUtil.UrlEncode(boardcode) + "&id=" + id + "'>" + toptitle + "</a>";
				if (privilege.canManage(request, id)) {
					String locktitle="",elitetitle="",guidetitle="";
					int dolock = (islocked==1)?0:1;
					if (dolock==1)
						locktitle = SkinUtil.LoadString(request, "res.label.forum.showtopic", "lock"); // "锁定";
					else
						locktitle = SkinUtil.LoadString(request, "res.label.forum.showtopic", "unlock"); // "解锁";
					int doelite = (iselite==1)?0:1;
					if (doelite==1)
						elitetitle = SkinUtil.LoadString(request, "res.label.forum.showtopic", "elite"); // "置为精华";
					else
						elitetitle = SkinUtil.LoadString(request, "res.label.forum.showtopic", "elite_not"); // "取消精华";
					mstr += "<a href='manager/manage.jsp?boardcode=" + StrUtil.UrlEncode(boardcode) + "&action=setLocked&id=" + id + "&value=" + dolock + "&privurl=" + privurl + "'>" + locktitle + "</a>";
					mstr += "<a title='" + elitetitle + "' href='manager/manage.jsp?boardcode=" + boardcode + "&action=setElite&id=" + id + "&value=" + doelite + "&privurl=" + privurl + "'>" + elitetitle + "</a>";
					mstr += "<a href='manager/changeboard.jsp?ids=" + id + "&boardcode=" + StrUtil.UrlEncode(boardcode) + "&privurl=" + privurl + "'>" + SkinUtil.LoadString(request, "res.label.forum.showtopic", "change_board") + "</a>";
					mstr += "<a href='manager/riseorfall.jsp?ids=" + id + "&boardcode=" + StrUtil.UrlEncode(boardcode) + "&privurl=" + privurl + "'>" + SkinUtil.LoadString(request, "res.label.forum.showtopic", "riseorfall") + "</a>";
					mstr += "<a href='manager/topic_merge.jsp?ids=" + id + "&boardcode=" + StrUtil.UrlEncode(boardcode) + "&privurl=" + privurl + "'>" + SkinUtil.LoadString(request, "res.label.forum.listtopic", "topic_merge") + "</a>";
            	}
                mstr += "<a href='manager/changecolor.jsp?id=" + id + "&boardcode=" + StrUtil.UrlEncode(boardcode) + "'>" + SkinUtil.LoadString(request, "res.label.forum.showtopic", "change_color") + "</a>";
            }
			if (msgdb.isRootMsg())
				mstr += "<a onclick=checkclick('" + SkinUtil.LoadString(request, "res.label.forum.showtopic", "topic_del_confirm") + "') href='manager/topic_del.jsp?boardcode=" + StrUtil.UrlEncode(boardcode) + "&delid=" + id + "&privurl=listtopic.jsp?boardcode=" + StrUtil.UrlEncode(boardcode) + "'>" + SkinUtil.LoadString(request, "res.label.forum.showtopic", "topic_del") + "</a>";
			else
				mstr += "<a onclick=checkclick('" + SkinUtil.LoadString(request, "res.label.forum.showtopic", "topic_del_confirm") + "') href='manager/topic_del.jsp?" + "boardcode=" + StrUtil.UrlEncode(boardcode) + "&delid=" + id + "&privurl=" + privurl + "'>" + SkinUtil.LoadString(request, "res.label.forum.showtopic", "topic_del") + "</a>";
			mstr += "<a href='manager/topic_merge.jsp?ids=" + msgdb.getId() + "&boardcode=" + StrUtil.UrlEncode(boardcode) + "&privurl=" + StrUtil.getUrl(request) + "'>" + SkinUtil.LoadString(request, "res.label.forum.listtopic", "topic_merge") + "</a>";		  
			
			if (vplugin.size()>0) {
				Iterator irplugin = vplugin.iterator();
				while (irplugin.hasNext()) {
					PluginUnit pu = (PluginUnit)irplugin.next();
					IPluginUI ipu = pu.getUI(request, response, out);
					IPluginViewShowMsg pv = ipu.getViewShowMsg(boardcode, msgdb);
					boolean isShow = false;
					if (pu.getType().equals(PluginUnit.TYPE_BOARD) || pu.getType().equals(PluginUnit.TYPE_FORUM))
						isShow = true;
					else if (pu.getType().equals(PluginUnit.TYPE_TOPIC)) {
						if (pu.getUnit().isPluginMsg(msgdb.getId()))
							isShow = true;
					}
					if (isShow) {
						mstr += pv.render(UIShowMsg.POS_TOPIC_OPERATE_MENU);
					}
				}
			}		  
			%>
			&nbsp;&nbsp;<a href='#' onmouseover="showmenu(event, &quot;<%=mstr%>&quot;, 0)"><img src="images/edit.gif" border="0" align="absmiddle" />&nbsp;<lt:Label res="res.label.forum.showtopic" key="fun_menu"/></a>
			&nbsp;&nbsp;<% if (islocked==0) {%><a href="addreply_new.jsp?boardcode=<%=boardcode%>&amp;replyid=<%=id%>&amp;privurl=<%=privurl%>"><img src="images/replynow.gif" border="0" align="absmiddle" />&nbsp;<lt:Label res="res.label.forum.showtopic" key="topic_reply"/></a><%}%>			
			&nbsp;&nbsp;<a href="addreply_new.jsp?boardcode=<%=StrUtil.UrlEncode(boardcode)%>&amp;replyid=<%=id%>&amp;quote=1&amp;privurl=<%=privurl%>"><img src="images/reply.gif" border="0" align="absmiddle" />&nbsp;<lt:Label res="res.label.forum.showtopic" key="topic_quote"/></a>			
			&nbsp;&nbsp;<a href="javascript:copyText(document.all.content<%=i%>);"><img src="images/copy.gif" border="0" align="absmiddle" />&nbsp;<lt:Label res="res.label.forum.showtopic" key="topic_copy"/></a>			
			<%if (user!=null && user.isLoaded() && !user.getHome().equals("")) {%>
		  	&nbsp;&nbsp;<a href="<%=user.getHome()%>" target="_blank"><img src="images/home.gif" width="16" height="16" border="0" align="absmiddle" />&nbsp;<lt:Label res="res.label.forum.showtopic" key="home"/></a>
			<%}%>
			<%if (user!=null && user.isLoaded() && cfg1.getBooleanProperty("forum.isShowQQ") && !user.getOicq().equals("")) {%>
			&nbsp;&nbsp;<a href="http://wpa.qq.com/msgrd?V=1&amp;Uin=<%=user.getOicq()%>&amp;Site=By CWBBS&amp;Menu=yes" target="_blank"><img src="http://wpa.qq.com/pa?p=1:<%=user.getOicq()%>:4" align="middle" border="0" />&nbsp;<lt:Label res="res.label.forum.showtopic" key="send_qq_msg"/></a>
			<%}%>			
			<%if (user!=null && cfg1.getBooleanProperty("forum.isOrderMusic")) {%>
			&nbsp;&nbsp;<a href="music_order.jsp?boardcode=<%=boardcode%>&amp;userName=<%=StrUtil.UrlEncode(user.getName())%>"><img src="images/music.gif" border="0" width="16" height="16" align="absmiddle" />&nbsp;<lt:Label res="res.label.forum.showtopic" key="music_order"/></a>
			<%}%>		  
		  </div>
		  <div class="topicCntTitle"><%=render.RenderTitle(request, msgdb)%>
			<%
			if (cfg1.getBooleanProperty("forum.isTag")) {
				Vector vtag = msgdb.getTags();
				if (vtag.size()>0) {
					out.print(SkinUtil.LoadString(request, "res.label.forum.showtopic", "tag") + "：");
					Iterator irtag = vtag.iterator();
					TagDb td2 = new TagDb();
					while (irtag.hasNext()) {
						TagMsgDb tmd = (TagMsgDb)irtag.next();
						TagDb td = td2.getTagDb(tmd.getLong("tag_id"));
						if (td!=null) {
				%>
				<a class="linkTag" target="_blank" href="listtag.jsp?tagId=<%=tmd.getLong("tag_id")%>"><%=td.getString("name")%></a>&nbsp;&nbsp;
				<%
						}
					}
				}
			}
			%>
        </div>
		<%
		if (vplugin.size()>0) {
			Iterator irplugin = vplugin.iterator();
			while (irplugin.hasNext()) {
				PluginUnit pu = (PluginUnit)irplugin.next();
				IPluginUI ipu = pu.getUI(request, response, out);
				IPluginViewShowMsg pv = ipu.getViewShowMsg(boardcode, msgdb);
				boolean isShow = false;
				if (pu.getType().equals(PluginUnit.TYPE_BOARD) || pu.getType().equals(PluginUnit.TYPE_FORUM))
					isShow = true;
				else if (pu.getType().equals(PluginUnit.TYPE_TOPIC)) {
					if (pu.getUnit().isPluginMsg(msgdb.getId()))
						isShow = true;
				}
				if (isShow) {
					if (pu.isShowName())
						out.print(pu.getName(request) + "&nbsp;");
					String strBeforeMsg = pv.render(UIShowMsg.POS_BEFORE_MSG);
					if (!strBeforeMsg.equals(""))
						out.print("<div>" + strBeforeMsg + "</div>");
				}
			}
		}
		
		MsgPollDb mpd = null;
		mpd = render.RenderVote(request, msgdb);
		if (type==1 && mpd!=null) {%>
			<form action="vote.jsp?privurl=<%=privurl%>" method="post" name="formvote" id="formvote">
			<table class="voteTab" width="100%" cellpadding="4" cellspacing="0" style="table-layout:auto">
			<%
			String ctlType = "radio";
			if (mpd.getInt("max_choice")>1)
				ctlType = "checkbox";
			Vector options = mpd.getOptions(msgdb.getId());
			int len = options.size();
			
			int[] re = new int[len];
			int[] bfb = new int[len];
			int total = 0;
			int k = 0;
			for (k=0; k<len; k++) {
				MsgPollOptionDb opt = (MsgPollOptionDb)options.elementAt(k);					
				re[k] = opt.getInt("vote_count");
				total += re[k];
			}
			if (total!=0) {
				for (k=0; k<len; k++) {
					bfb[k] = (int)Math.round((double)re[k]/total*100);
				}
			}
			%>
			<thead>
			  <tr>
				<td colspan="3"><b>
				  <lt:Label res="res.label.forum.showtopic" key="vote"/>
				  <%
				  java.util.Date epDate = mpd.getDate("expire_date");
				  if (epDate!=null) {%>
				  &nbsp;
				  <lt:Label res="res.label.forum.showtopic" key="vote_expire_date"/>
				  &nbsp;<%=ForumSkin.formatDate(request, epDate)%>
				  <%}%>
				  <%if (mpd.getInt("max_choice")==1) {%>
				  <lt:Label res="res.label.forum.showtopic" key="vote_type_single"/>
				  <%}else{%>
				  <lt:Label res="res.label.forum.showtopic" key="vote_type_multiple"/>
				  <%=mpd.getInt("max_choice")%>
				  <%}%>
				</b></td>
			  </tr>
			  </thead>
			  <tr>
			  <%
				int barId = 0;
				String showVoteUser = ParamUtil.get(request, "showVoteUser");
				for (k=0; k<len; k++) {
					MsgPollOptionDb opt = (MsgPollOptionDb)options.elementAt(k);
				%>
				<td width="47%"><%=k+1%>.
				  <input type="<%=ctlType%>" name="votesel" value="<%=k%>" />
				  &nbsp;<%=StrUtil.toHtml(opt.getString("content"))%></td>
				<td width="37%"><img src="images/vote/bar<%=barId%>.gif" width="<%=bfb[k]-8%>%" height="10" /></td>
			    <td width="16%"><strong><%=re[k]%>
                    <lt:Label res="res.label.forum.showtopic" key="vote_unit"/>
                </strong>&nbsp;<%=bfb[k]%>%
                <%
					if (showVoteUser.equals("1")) {
						String[] userAry = StrUtil.split(opt.getString("vote_user"), ",");
						if (userAry!=null) {
							int userLen = userAry.length;
							String userNames = "";
							for (int n=0; n<userLen; n++) {
								UserDb ud = um.getUser(userAry[n]);
								if (userNames.equals(""))
									userNames = ud.getNick();
								else
									userNames += ",&nbsp;" + ud.getNick();
							}
							out.print(userNames);
						}
					}
					%></td>
			  </tr>
			 <%
				barId ++;
				if (barId==10)
					barId = 0;				
			}%>
			  <tr>
				<td colspan="3" align="center"><input name="button" type="button" onclick="window.location.href='?rootid=<%=rootid%>&amp;showVoteUser=1'" value="<lt:Label res="res.label.forum.showtopic" key="vote_show_user"/>" />
				  &nbsp;
				<%
				if (epDate!=null) {
					if (DateUtil.compare(epDate, new java.util.Date()) == 1) {
				%>
				  <input name="submit" type="submit" value="<lt:Label res="res.label.forum.showtopic" key="vote"/>" />
				  <%}else{%>
				  <b>
					<lt:Label res="res.label.forum.showtopic" key="vote_end"/>
					</b>
				  <%}
				  }else{%>
				  <input name="submit" type="submit" value="<lt:Label res="res.label.forum.showtopic" key="vote"/>" />
				  <%}%>
				  <input type="hidden" name="boardcode2" value="<%=boardcode%>" />
				  <input type="hidden" name="boardname" value="<%=boardname%>" />
				  <input type="hidden" name="voteid" value="<%=id%>" />				</td>
			  </tr>
		  </table>
		</form>
		<%}%>
		<%if (msgdb.isRootMsg()) {%>
		<ad:AdTag type="<%=AdDb.TYPE_TOPIC_RIGHT%>" boardCode="<%=boardcode%>"></ad:AdTag>
		<%}%>
		<div class="topicCntContent" id="content<%=i%>">
		<%
			if (!msgdb.getPlugin2Code().equals("")) {
				Plugin2Mgr p2m = new Plugin2Mgr();
				Plugin2Unit p2u = p2m.getPlugin2Unit(msgdb.getPlugin2Code());
				out.print(p2u.getUnit().getRender().rend(request, msgdb));
			}
			out.print(render.RenderContent(request, msgdb));
			// if (msgdb.getIsWebedit()==msgdb.WEBEDIT_REDMOON) {
				String att = render.RenderAttachment(request, msgdb);
				out.print(att);
			// }
			%>
		<%
		if (vplugin.size()>0) {
			Iterator irplugin = vplugin.iterator();
			while (irplugin.hasNext()) {
				PluginUnit pu = (PluginUnit)irplugin.next();
				IPluginUI ipu = pu.getUI(request, response, out);
				IPluginViewShowMsg pv = ipu.getViewShowMsg(boardcode, msgdb);
					boolean isShow = false;
					if (pu.getType().equals(PluginUnit.TYPE_BOARD) || pu.getType().equals(PluginUnit.TYPE_FORUM))
						isShow = true;
					else if (pu.getType().equals(PluginUnit.TYPE_TOPIC)) {
						if (pu.getUnit().isPluginMsg(msgdb.getId()))
							isShow = true;
					}
					if (isShow) {
						out.print(pv.render(UIShowMsg.POS_AFTER_MSG) + "<BR>");
					}
			}
		}%>
		</div>
		<%			
		int waterMarkCode = cfg1.getIntProperty("forum.waterMarkCode");
		boolean isWaterMarkCode = false;
		if (waterMarkCode!=0) {
			if (waterMarkCode==1) {
				if (!Privilege.isUserLogin(request))
					isWaterMarkCode = true;
			} else if (waterMarkCode==2) {
				isWaterMarkCode = true;
			}
			if (isWaterMarkCode) {
			%>
			  <script language="JavaScript" type="text/javascript">
			waterMarkCode("content<%=i%>");
			</script>
			<%}
		}
		out.print("<div class='topicCntSign'>");
		if (!name.equals("") && !sign.equals("")) {
			out.print("----------------------------------------------<BR />");
			sign = StrUtil.toHtml(sign);
			if (cfg1.getBooleanProperty("forum.sign_ubb"))
				out.print(StrUtil.ubb(request, sign, true));
			else
				out.print(sign);
		}
		out.print("</div>");
		%>
	<div class="topicCntFooter">
	<span class="topicCntFooterR">
	<%if (Privilege.isMasterLogin(request)) {%>
	  IP: <%=msgdb.getIp()%>&nbsp;&nbsp;
	  <%}%>
	  <%if (msgdb.getReplyid()==-1) {%>
	  <lt:Label res="res.label.forum.showtopic" key="topic_owner"/>
	  <%}else{%>
	  <%=(curpage-1)*pagesize+i%>&nbsp;<lt:Label res="res.label.forum.showtopic" key="topic_floor"/>
	  <%}%>
	  &nbsp;&nbsp;<a href="#top"><img src="<%=skinPath%>/images/go_top.gif" alt="<lt:Label res="res.label.forum.showtopic" key="go_top"/>" align="absmiddle" border="0" /></a>
	</span>
	<span><%
	if (v_ad.size()>0) {
		if (ad_count < v_ad.size()) {
			AdDb ad = (AdDb)v_ad.get(ad_count);
			ad_count ++;
			if (ad_count == v_ad.size())
				ad_count = 0;
	%>
		<%=ad.render(request)%>
		<%}
	}
	%>
	</span>
	</div>
		  </td>
        </tr>
      </tbody>
      <tbody>
        <tr>
          <td class="separaterL"></td>
          <td class="separaterR"></td>
        </tr>
      </tbody>
	<%if (msgdb.isRootMsg()) {%>
		<tbody><tr><td colspan="2" class="showTopicTd"><ad:AdTag type="<%=AdDb.TYPE_TOPIC_AFTER%>" boardCode="<%=boardcode%>"></ad:AdTag></td></tr></tbody>
	<%}%>
<%
}
%>
</table>
	
	</div>
	<div class="topicPage">
		<span class="topicPageR"><%
		out.print(paginator.getShowTopicCurPageBlock(request, rootid, "down"));
		%></span>
		<a href="<%=addpage%>?boardcode=<%=boardcode%>&privurl=<%=privurl%>">
		<img onmouseover='menuOffY=29;if(buttonSpan.innerHTML!="") showmenu(event, buttonSpan.innerHTML, 0);menuOffY=18' src="<%=skinPath%>/images/post_<%=SkinUtil.getLocale(request)%>.gif" alt="<lt:Label res="res.label.forum.showtopic" key="addtopic"/>" border=0 align="absmiddle"></a> <a href="<%=replypage%>?boardcode=<%=boardcode%>&replyid=<%=rootid%>&privurl=<%=privurl%>"> <img src="<%=skinPath%>/images/newreply_<%=SkinUtil.getLocale(request)%>.gif" alt="<lt:Label res="res.label.forum.showtopic" key="addreply"/>" border=0 align="absmiddle"></a>
		<select name="selboard" onChange="if(this.options[this.selectedIndex].value!='not'){location='listtopic.jsp?boardcode=' + this.options[this.selectedIndex].value;}">
		<option value="not" selected>
		<lt:Label res="res.label.forum.showtopic" key="sel_board"/>
		</option>
		<%
		Directory dir = new Directory();
		Leaf leaf = dir.getLeaf(Leaf.CODE_ROOT);
		com.redmoon.forum.DirectoryView dv = new com.redmoon.forum.DirectoryView(leaf);
		dv.ShowDirectoryAsOptions(request, privilege, out, leaf, leaf.getLayer());
		%>
		</select>
	</div>
	<br />
	<%
	if (privilege.canUserDo(request, boardcode, "reply_topic")) {
	%>
<FORM name="frmAnnounce" onSubmit="return formCheck()" action="addquickreplytodb.jsp?privurl=<%=privurl%>" method="post">
<table class="tableCommon" width="100%">
	  <thead>
	  <tr>
	    <td colspan="3" style="font-family:'微软雅黑'"><lt:Label res="res.label.forum.showtopic" key="quick_reply"/></td>
      </tr>
	  </thead>
	  <tr><td colspan="3"><%	
	if (vplugin.size()>0) {
		Iterator irplugin = vplugin.iterator();
		while (irplugin.hasNext()) {
			PluginUnit pu = (PluginUnit)irplugin.next();
			IPluginUI ipu = pu.getUI(request, response, out);
			IPluginViewShowMsg pv = ipu.getViewShowMsg(boardcode, rootMsgDb);
			
				boolean isShow = false;
				if (pu.getType().equals(PluginUnit.TYPE_BOARD))
					isShow = true;
				else if (pu.getType().equals(PluginUnit.TYPE_TOPIC)) {
					if (pu.getUnit().isPluginMsg(rootMsgDb.getId()))
						isShow = true;
				}
				if (isShow) {
					if (!pu.getAddReplyPage().equals("")) {
		%>
          <jsp:include page="<%=pu.getAddReplyPage()%>" flush="true">
          <jsp:param name="msgRootId" value="<%=rootid%>" />          
          <jsp:param name="isQuickReply" value="true" />          </jsp:include>
          <%			}
					else {
						out.print(pu.getName(request) + "&nbsp;" + pv.render(UIShowMsg.POS_QUICK_REPLY_NOTE) + "<BR>");
						out.print(pv.render(UIShowMsg.POS_QUICK_REPLY_ELEMENT) + "<BR>");
					}
				}
		}
	}
	%>
</td>
	</tr>
	  <tr>
	    <td width="22%"><lt:Label res="res.label.forum.showtopic" key="quick_reply_title"/></td>
	    <td colspan="2"><input name="topic" value="<%=SkinUtil.LoadString(request, "res.label.forum.showtopic", "reply") + StrUtil.toHtml(rootMsgDb.getTitle())%>" size="40" />
          <input type="hidden" name="replyid" value="<%=rootid%>" />
          <input type="hidden" name="boardcode" value="<%=boardcode%>" />
          <%
	if (cfg1.getBooleanProperty("forum.addUseValidateCode")) {
	%>
          <lt:Label res="res.label.forum.showtopic" key="input_validatecode"/>
          <input name="validateCode2" type="text" size="1" />
          <img src='../validatecode.jsp' border="0" align="absmiddle" style="cursor:hand" onclick="this.src='../validatecode.jsp'" alt="<lt:Label res="res.label.forum.index" key="refresh_validatecode"/>" />
          <%}%>
          </td>
      </tr>
	  <tr>
	    <td><lt:Label res="res.label.forum.showtopic" key="sel_emote"/>
          <iframe src="iframe_browlist.jsp" height="120"  width="98%" marginwidth="0" marginheight="0" frameborder="0" scrolling="yes"></iframe>
          <input type="hidden" name="expression" value="<%=MsgDb.EXPRESSION_NONE%>" />
          <br />
          <input type="checkbox" value="0" name="show_ubbcode" />
          <lt:Label res="res.label.forum.showtopic" key="forbid_ubb"/>
          <br />
          <input type="checkbox" value="0" name="show_smile" />
          <lt:Label res="res.label.forum.showtopic" key="forbid_emote"/></td>
	    <td width="81%">
			<%
			String rpath = request.getContextPath();
			%>  
			<textarea id="Content" name="Content" style="display:none"></textarea>
			<link rel="stylesheet" href="<%=rpath%>/editor/edit.css">
			<script src="<%=rpath%>/editor/DhtmlEdit.js"></script>
			<script src="<%=rpath%>/editor/editjs.jsp"></script>
			<script src="<%=rpath%>/editor/editor_s.jsp"></script>  
			<script>cws_setMode(0)</script>		</td>
	    
      </tr>
	  <tr>
	    <td colspan="3" align="center"><input tabindex="4" type="submit" value="<lt:Label res="res.label.forum.showtopic" key="reply_topic"/>" name="submit1" />
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		<input name="button2" type="button" onclick="checkCount()" value="<lt:Label res="res.label.forum.addtopic" key="checkcount"/>" /></td>
      </tr>
	</table>
	</FORM>
	<%}%>
	<div style="padding-top:10px;"><%
	long pageEndTime =  System.currentTimeMillis();
	long t = pageEndTime - pageBeginTime;
	%>
	<lt:Label res="res.label.forum.listtopic" key="page_run"/><%=t%>
	<lt:Label res="res.label.forum.listtopic" key="mili_second"/></div>
	
</div>
<%
// 贴子访问统计
request.setAttribute("rootMsgDb", rootMsgDb);
request.setAttribute("isIncreaseHit", "0");
if (paginator.getCurPage()==1) {
	request.removeAttribute("isIncreaseHit");
}
%>
<%@ include file="../inc/topic_hit_count.jsp"%>
<%@ include file="inc/footer.jsp"%>
</div>
</BODY>
<script>
function setBrow(brow) {
	frmAnnounce.expression.value = brow
}
</script>
</HTML>