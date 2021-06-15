<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.module.nav.*"%>
<%@ page import="com.redmoon.forum.util.*"%>
<%@ page import="com.redmoon.forum.err.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.base.*"%>
<%@ page import="com.redmoon.forum.miniplugin.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt"%>
<%@ taglib uri="/WEB-INF/tlds/AdTag.tld" prefix="ad"%>
<jsp:useBean id="userservice" scope="page" class="com.redmoon.forum.person.userservice" />
<%
Privilege privilege1 = new Privilege();

// 登记访客
try {
	privilege1.enrolGuest(request,response);
}
catch (UserArrestedException e) {
	response.sendRedirect("info.jsp?info=" + StrUtil.UrlEncode(e.getMessage()));
	return;
}
// 刷新在位时间
userservice.refreshStayTime(request,response);

com.redmoon.forum.util.SeoConfig scfg = new com.redmoon.forum.util.SeoConfig();
String seoTitle = scfg.getProperty("seotitle");
String seoKeywords = scfg.getProperty("seokeywords");
String seoDescription = scfg.getProperty("seodescription");
String seoHead = scfg.getProperty("seohead");

com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
// 判断是否该显示框架
String strIsFrame = CookieBean.getCookieValue(request, com.redmoon.forum.ui.ForumPage.COOKIE_IS_FRAME);
boolean isFrame = false;
if (strIsFrame.equals("y"))
	isFrame = true;
else {
	if (strIsFrame.equals(""))
		isFrame = cfg.getBooleanProperty("forum.isFrame");
}

String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD><TITLE><%=Global.AppName%> <%=seoTitle%></TITLE>
<%=seoHead%>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<META name="keywords" content="<%=seoKeywords%>">
<META name="description" content="<%=seoDescription%>">
<link href="<%=skinPath%>/css.css" type="text/css" rel="stylesheet" />
<%if (isFrame) {%>
	<script language="javascript">
	/*
	if(top==self){
		window.location.href = "frame.jsp";
	}*/
	</script>
<%}%>
<script src="<%=skinPath%>/js.js"></script>
<LINK href="images/bbs.ico" rel="SHORTCUT ICON">
<script src="../inc/common.js"></script>
<SCRIPT>
var curpage = 1;
function loadonline(boardcode, CPages){
	var targetImg=$("followImg000");
	var targetDiv=$("followDIV000");
	if ("object"==typeof(targetImg)){
		if (curpage!=CPages || targetDiv.style.display!='block'){
			curpage = CPages;
			targetDiv.style.display="block";
			targetImg.src="images/minus.gif";
			$("advance").innerText="<lt:Label res="res.label.forum.index" key="close_online"/>";
			if (isIE())
				window.frames["hiddenframe"].location.replace("online.jsp?boardcode="+boardcode+"&CPages=" + CPages);
			else{
				var frm = document.getElementById("hiddenframe");
				frm.contentWindow.location.replace("online.jsp?boardcode="+boardcode+"&CPages=" + CPages);
			}
		}
		else{
			targetDiv.style.display="none";
			targetImg.src="images/plus.gif";
			$("advance").innerText="<lt:Label res="res.label.forum.index" key="show_online"/>"
		}
	}
}
function collapse(boardCode, isCollapse) {
	var today=new Date();
	var expireDate=new Date();
	expireDate.setTime(expireDate.getTime()+1000*60*60*24*30);//30天
	set_cookie(boardCode+"_collapse", isCollapse, expireDate);
	window.location.reload();
}
function onLoad() {
<%if (cfg.getBooleanProperty("forum.popWinNotLogin") && !Privilege.isUserLogin(request)){%>
if (get_cookie("popLayer")==""){ 
set_cookie("popLayer", "yes");
openWinLayer('', '', false, 500, 300, 5000);
ajaxpage('ajax_welcome.jsp','popLayer');
}
<%}%>
}
</SCRIPT>
<BODY onLoad="onLoad()">
<%
UserSession.setBoardCode(request, "");
UserMgr um = new UserMgr();
int pagesize = cfg.getIntProperty("forum.showTopicPageSize");
%>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main"><%
ForumDb forum = new ForumDb();
forum = forum.getForumDb();
%><script src="../inc/ajax_getpage.jsp"></script><script src="../inc/poplayer.js"></script>	
  <div id="bbsInfo">
    	<div>
		<span id="topicBlock">
		<lt:Label res="res.label.forum.index" key="today_post"/><%=forum.getTodayCount()%><lt:Label res="res.label.forum.index" key="pian"/>,		
      	<lt:Label res="res.label.forum.index" key="yestoday_post"/><%=forum.getYestodayCount()%><lt:Label res="res.label.forum.index" key="pian"/>,
		<lt:Label res="res.label.forum.index" key="topic_count"/><%=forum.getTopicCount()%><lt:Label res="res.label.forum.index" key="pian"/>,
      	<lt:Label res="res.label.forum.index" key="post_count"/><%=forum.getPostCount()%><lt:Label res="res.label.forum.index" key="pian"/>
		</span>
	  	<span id="userBlock"><img src="<%=request.getContextPath()%>/forum/<%=skinPathHeader%>/images/arrow.jpg" style="margin-left:10px" />&nbsp;&nbsp;
		<%
		int msgCountHeader = 0;
		if (!com.redmoon.forum.Privilege.isUserLogin(request)) {%>
        	欢迎您访问社区！
        	<!--
			<lt:Label res="res.label.forum.index" key="welcome"/>,&nbsp;
			<lt:Label res="res.label.forum.index" key="need_regist"/>
            -->
		<%}else{
			UserDb me = new UserDb();
			me = me.getUser(com.redmoon.forum.Privilege.getUser(request));
			com.redmoon.forum.message.MessageMgr Msg = new com.redmoon.forum.message.MessageMgr();
			msgCountHeader = Msg.getNewMsgCount(request);
		%>
			<a href="<%=rootpath%>/usercenter.jsp"><%=me.getNick()%></a>&nbsp;<a  href="javascript:hopenWin('<%=rootpath%>/message/message.jsp',320,260)">
			<lt:Label res="res.label.forum.index" key="msgbox"/>(<font class="redfont"><%=msgCountHeader%></font>)</a>
			<lt:Label res="res.label.forum.index" key="last_login"/><%=DateUtil.format(me.getLastTime(), "yyyy-MM-dd")%>
		<%}
		request.setAttribute("msgCount", new Integer(msgCountHeader));
		%></span>
		</div>
      	<div>
	  	<span id="memberBlock">
      	<lt:Label res="res.label.forum.index" key="most_post"/><%=forum.getMaxCount()%><lt:Label res="res.label.forum.index" key="pian"/>,
		<lt:Label res="res.label.forum.index" key="most_post_date"/><%=DateUtil.format(forum.getMaxDate(), "yyyy-MM-dd")%>,
		<lt:Label res="res.label.forum.index" key="all_user_count"/><%=forum.getUserCount()%>,
		<a href="<%=rootpath%>/listmember.jsp"><lt:Label res="res.label.forum.index" key="user_new"/></a>
		<a href="<%=rootpath%>/userinfo.jsp?username=<%=StrUtil.UrlEncode(forum.getUserNew())%>" target="_blank"><b><%=StrUtil.getNullStr(um.getUser(forum.getUserNew()).getNick())%></b></a>
		<%--<a href="rss.jsp"><img src="<%=request.getContextPath()%>/images/rss.gif" alt="rss订阅" border="0" align="absmiddle"></a>	--%>
		</span>
		<span id="listBlock">
		<a href="<%=request.getContextPath()%>/forum/search_do.jsp"><lt:Label res="res.label.forum.index" key="topic_new"/></a>|<a href="listhot.jsp"><lt:Label res="res.label.forum.index" key="topic_hot"/></a>|<a href="<%=request.getContextPath()%>/forum/stats.jsp?type=postsrank"><lt:Label res="res.label.forum.stats" key="credits_rank"/></a>|<a href="<%=request.getContextPath()%>/listmember.jsp"><lt:Label res="res.label.forum.index" key="view_member"/></a>
	  	</span>
    	</div>
  </div>
	<ad:AdTag type="<%=AdDb.TYPE_TEXT%>" boardCode="<%=Leaf.CODE_ROOT%>"></ad:AdTag>
<%
if (cfg.getBooleanProperty("forum.isTag")) {%>
<div id="tagBox">
  <a href="tag.jsp"><lt:Label res="res.label.forum.index" key="tag_hot"/></a>：
  <%
  TagDb td = new TagDb();
  boolean tagOnlySystemAllowed = cfg.getBooleanProperty("forum.tagOnlySystemAllowed");
  String sqlListTag;
  if (tagOnlySystemAllowed) {
  	sqlListTag = td.getTable().getSql("listSystemTag");
  }
  else {
  	sqlListTag = td.getTable().getSql("listTag");
  }
  QObjectBlockIterator qbi = td.getQObjects(sqlListTag, 0, 10);
  while (qbi.hasNext()) {
  	td = (TagDb)qbi.next();
	String tagName = td.getString("name");
	String color = StrUtil.getNullStr(td.getString("color"));
	if (!color.equals("")) {
		tagName = "<font color='" + color + "'>" + tagName +  "</font>";
	}
  %>
  	<a class="linkTag" href="listtag.jsp?tagId=<%=td.getLong("id")%>"><%=TagMgr.render(request, td)%></a><font color="disabled">(<%=td.getInt("count")%>)</font>&nbsp;&nbsp;
  <%}
  %>
  </div>
<%}%>
	<ad:AdTag type="<%=AdDb.TYPE_FLOAT%>" boardCode="<%=Leaf.CODE_ROOT%>"></ad:AdTag>
	<ad:AdTag type="<%=AdDb.TYPE_COUPLE%>" boardCode="<%=Leaf.CODE_ROOT%>"></ad:AdTag>
<%
MiniPluginMgr mpm = new MiniPluginMgr();
MiniPluginUnit indexUnit = mpm.getMiniPluginUnit("index_new_elite_top");
if (indexUnit!=null && indexUnit.isPlugin()) {
%>
<%@ include file="miniplugin/index/newelitetop.jsp"%>
<%}%>
	<div id="homeBg">
		<div id="outWrapper" class="outWithSide">
			<div id="mainLeft" class="mLWithSide">
			  <div id="inWrapper" class="inWithSide">
					<div id="notice">
						<table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td width="30" align="left"><img src="<%=skinPath%>/images/icon/notice.gif" /></td>
								<td align="left" width="80" style=" text-indent:1em;font-size:16px;"><lt:Label res="res.label.forum.index" key="notice"/></td>
								<td align="left">
								  <div id="noticeContent">
										<ul id="nCItem">
											<%
											Vector vnotice = forum.getAllNotice();
											if (vnotice.size()!=0) {
												Iterator irnotice = vnotice.iterator();
												int noticeIndex = 1;
												while (irnotice.hasNext()) {
													MsgDb md = (MsgDb)irnotice.next();%>
													<%
													String color = StrUtil.getNullString(md.getColor());
													String tp = DefaultRender.RenderFullTitle(request, md);
													if (!color.equals(""))
														tp = "<font color='" + color + "'>" + tp + "</font>";
													if (md.isBold())
														tp = "<B>" + tp + "</B>";
													%>
													<li><%=noticeIndex%>.&nbsp;<a href="<%=ForumPage.getShowTopicPage(request, md.getId())%>"><%=tp%>&nbsp;[<%=com.redmoon.forum.ForumSkin.formatDate(request, md.getAddDate())%>]</a></li>
												<%
													noticeIndex++;
												}
											}else{%>
											<li>&nbsp;</li>
											<%}%>
										</ul>
								</div>
<script>
	scrollNotice("nCItem", 4000, 2);
</script>
								</td>
								<td width="1" bgcolor="#FFFFFF"></td>
								<td width="32"><%=vnotice.size()%></td>
								<td width="1" bgcolor="#FFFFFF"></td>
								<td width="40"><img src="<%=skinPath%>/images/icon/notice_up.gif" style="cursor:pointer" onClick="scrollUpOrDown('nCItem', 'up')" /><img src="<%=skinPath%>/images/icon/notice_down.gif" style="cursor:pointer" onClick="scrollUpOrDown('nCItem', 'down')" /></td>
							</tr>
						</table>
					</div>
	<%
	// PluginMgr pmnote = new PluginMgr();
	EntranceMgr em = new EntranceMgr();
	MsgMgr mm = new MsgMgr();
	
	String boardField = ParamUtil.get(request, "boardField");
	boolean isShowBoardField = !boardField.equals("");

	if (isShowBoardField) {
		Leaf leaf = new Leaf();
		leaf = leaf.getLeaf(boardField);
		if (leaf==null) {
			out.print(StrUtil.Alert_Back(StrUtil.HtmlEncode(boardField) + " is not found."));
			return;
		}
	}
	
	LeafChildrenCacheMgr dlcm = new LeafChildrenCacheMgr(Leaf.CODE_ROOT);
	java.util.Vector vt = dlcm.getChildren();
	Iterator ir = vt.iterator();
	boolean isDisplay = false;
	boolean isFounded = false;
	while (ir.hasNext()) {
		Leaf leaf = (Leaf) ir.next();
		String parentCode = leaf.getCode();
		if(isShowBoardField) {
			if (boardField.equals(leaf.getCode())) {
				isFounded = true;
			}
			else
				continue;
		}
		if (leaf.isDisplay(request, privilege1)) {
			CookieBean cookiebean = new CookieBean();
			boolean isCollapse = false;
			String sCollapse = CookieBean.getCookieValue(request, leaf.getCode() + "_collapse");
			if (sCollapse==null || sCollapse.equals(""))
				isCollapse = leaf.getDisplayStyle()!=Leaf.DISPLAY_STYLE_VERTICAL;
			else
				isCollapse = sCollapse.equals("true");
			if (!isCollapse) {
	%>
                    <div class="boardArea" id="boardArea_0">
                      <div class="boardAreaTitle">
                        <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
                          <tr>
                            <td align="left" style="font-size:22px; font-family:'微软雅黑';"><span style="border-left:3px solid #4796de; padding-left:10px"><%=leaf.getName()%></span></td>
                            <td align="right"><img alt="<lt:Label res="res.label.forum.index" key="horizontal"/>" style="cursor:pointer" src="<%=skinPath%>/images/icon/boardliststyle_0.gif" onClick="collapse('<%=leaf.getCode()%>', 'true')" /><img alt="<lt:Label res="res.label.forum.index" key="board_field"/>" onClick="window.location.href='index.jsp?boardField=<%=leaf.getCode()%>'" style="cursor:pointer" src="<%=skinPath%>/images/icon/quick.jpg" /></td>
                          </tr>
                        </table>
                      </div>
                      <div class="content">
                        <table width="100%" border="0" cellpadding="0" cellspacing="0" class="boardlist">
                        <%
						MsgDb md = null;
						LeafChildrenCacheMgr dl = new LeafChildrenCacheMgr(parentCode);
						java.util.Vector v = dl.getChildren();
						Iterator ir1 = v.iterator();
						while (ir1.hasNext()) {
							Leaf lf = (Leaf) ir1.next();
							md = mm.getMsgDb(lf.getAddId());
							if (!lf.isDisplay(request, privilege1))
								continue;
						%>
                          <tbody>
                            <tr>
                              <td width="10%" align="center">
							 	<%
							  String logo = StrUtil.getNullString(lf.getLogo());
							  if (!logo.equals("")) {
							  %>
                                  <img src="images/board_logo/<%=logo%>" align="absmiddle" />
                              <%}%>
							  </td>
                              <td align="left"><a class="boardName" href="<%=ForumPage.getListTopicPage(request, lf.getCode())%>"><%=lf.getNameWithStyle()%></a>
                                <%
								int chcount = lf.getChildCount();
								/*
								Vector vplugin = pmnote.getAllPluginUnitOfBoard(lf.getCode());
								if (vplugin.size()>0) {
									out.print("<font color=#aaaaaa>");
									Iterator irpluginnote = vplugin.iterator();
									while (irpluginnote.hasNext()) {
										PluginUnit pu = (PluginUnit)irpluginnote.next();
										out.print(pu.getName(request) + "&nbsp;");
									}
									out.print("</font>");
								}
								*/
								Vector ventrance = em.getAllEntranceUnitOfBoard(lf.getCode());
								if (ventrance.size()>0) {
									Iterator irpluginent = ventrance.iterator();
									while (irpluginent.hasNext()) {
										EntranceUnit eu = (EntranceUnit)irpluginent.next();
										out.print("<img src='" + skinPath + "/images/passport.gif' alt='" + eu.getDesc(request) + "'>&nbsp;");
									}	
								}						
								%>
                                <font color="#888888">(<lt:Label res="res.label.forum.index" key="today"/>&nbsp;<%=lf.getTodayCount()%>)</font>
                                <br />
                                 <%if (!lf.getDescription().equals("")) {%>
                                <%=lf.getDescription()%><br />
								<%} %>
								<div class="msgTitle">
                                 <lt:Label res="res.label.forum.index" key="board_manager"/>
                                <%
								Vector managers = mm.getBoardManagers(lf.getCode());
								Iterator irmgr = managers.iterator();
								BoardManagerDb bmd = new BoardManagerDb();
								while (irmgr.hasNext()) {
									UserDb user = (UserDb) irmgr.next();
									bmd = bmd.getBoardManagerDb(lf.getCode(), user.getName());
									if (!bmd.isHide()) {
								  %>
									<a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(user.getName())%>" style="color:#f96e00"><%=user.getNick()%></a>&nbsp;
									<%}
								}%></div>
								<font color="#888888"><div class="msgReplier"><lt:Label res="res.label.forum.index" key="topic_count"/>&nbsp;<%=lf.getTopicCount()%></div><lt:Label res="res.label.forum.index" key="post_count"/>&nbsp;<%=lf.getPostCount()%></font><br /><%
								MsgDb mdb = mm.getMsgDb(md.getRootid());
								%><div class="msgTitle"><lt:Label res="res.label.forum.index" key="topic"/>
                                <%if (md.isLoaded()) {
									if (md.getReplyid()==-1){%>
									<a title="<%=StrUtil.toHtml(mdb.getTitle())%>" href="<%=ForumPage.getShowTopicPage(request, mdb.getId())%>" style="color:#018ed9"><%=DefaultRender.RenderFullTitle(request, mdb)%></a>
									<%}else{
									int CPages = (int)Math.ceil((double)md.getOrders()/pagesize);  
									%>
									<a title="<%=StrUtil.toHtml(mdb.getTitle())%>" href="<%=ForumPage.getShowTopicPage(request, md.getRootid(), CPages, ""+md.getId())%>" style="color:#018ed9"><%=DefaultRender.RenderFullTitle(request, mdb)%></a>
									<%}
						  		}%></div>
                                <font color="#888888"><div class="msgReplier"><%if (md.isLoaded()) {%>
									<%if (md.getReplyid()==-1) {%>
									<lt:Label res="res.label.forum.index" key="topic_post"/>
									<%}else{%>
									<lt:Label res="res.label.forum.index" key="topic_reply"/>
									<%}%>
									<%if (!md.getName().equals("")) {
										UserDb mdUser = um.getUser(md.getName());
									%>
									<a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(md.getName())%>" title="<%=StrUtil.toHtml(mdUser.getNick())%>" style="color:#f96e00"><%=mdUser.getNick()%></a>
									<%}else{%>
									<lt:Label res="res.label.forum.showtopic" key="anonym"/>
									<%}
								}%></div>
                                <lt:Label res="res.label.forum.index" key="topic_date"/>
                                <%=com.redmoon.forum.ForumSkin.formatDateTimeShort(request, md.getAddDate())%></font>
                                <%if (chcount>0) {%>
                                <div>
									<%
									LeafChildrenCacheMgr lfc = new LeafChildrenCacheMgr(lf.getCode()); 
									Vector chv = lfc.getLeafChildren();
									Iterator chir = chv.iterator();
									while (chir.hasNext()) {
										Leaf chlf = (Leaf) chir.next();
										if (chlf.isDisplay(request, privilege1)) {
									%>
										<a href="<%=ForumPage.getListTopicPage(request, chlf.getCode())%>"><font color="<%=chlf.getColor()%>"><%=chlf.getName()%></font></a>&nbsp;
									<%}
									}%>
                                </div>
                                <%}%>
                              </td>
                              <td width="10%" align="center">
							 	<%if (lf.isLocked()) {%>
                                  <img alt="<lt:Label res="res.label.forum.index" key="board_lock"/>" src="<%=skinPath%>/images/board_lock.png" />
                                <%}else{%>
                                  <%if (lf.getTodayCount()>0) {%>
                                  <img alt="<lt:Label res="res.label.forum.index" key="board_new"/>" src="<%=skinPath%>/images/board_new.png" />
                                  <%}
								  //else{%>
                                 <!-- <img alt="<lt:Label res="res.label.forum.index" key="board_nonew"/>" src="<%=skinPath%>/images/board_nonew.gif" /> -->
                                  <%//}%>
                                <%}%>
							  </td>
                            </tr>
                          </tbody>
                          <tbody>
                            <tr>
                              <td class="line" colspan="3" style="padding:0 !important"></td>
                            </tr>
                          </tbody>
                          <%}%>
                        </table>
                      </div>
                    </div>
                    <%}else{%>
                    <div class="boardArea" id="boardArea_1">
                      <div class="boardAreaTitle">
                        <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
                          <tr>
                            <td align="left" style="font-size:18px; font-family:'微软雅黑';color:#008dd9;"><%=leaf.getName()%></td>
                            <td align="right"><img alt="<lt:Label res="res.label.forum.index" key="vertical"/>" style="cursor:pointer" src="<%=skinPath%>/images/icon/boardliststyle_1.jpg" onClick="collapse('<%=leaf.getCode()%>', 'false')" /><img alt="<lt:Label res="res.label.forum.index" key="board_field"/>" style="cursor:pointer" onClick="window.location.href='index.jsp?boardField=<%=leaf.getCode()%>'" src="<%=skinPath%>/images/icon/quick.jpg" /></td>
                          </tr>
                        </table>
                      </div>
                      <div class="content">
                        <table width="100%" border="0" cellpadding="0" cellspacing="0" class="boardlist">
                          	<%
							MsgDb md = new MsgDb();
							LeafChildrenCacheMgr lccm = new LeafChildrenCacheMgr(leaf.getCode());
							java.util.Vector v3 = lccm.getChildren();
							Iterator ir3 = v3.iterator();
							int row = 0;
							while (ir3.hasNext()) {
								row ++;
								Leaf lf = (Leaf) ir3.next();
								if (!lf.isDisplay(request, privilege1)) {
									continue;
								}
								md = md.getMsgDb(lf.getAddId());
							%>
                          <tr>
                            <td width="10%" align="center">
							 <%
							  String logo = StrUtil.getNullString(lf.getLogo());
							  if (!logo.equals("")) {
							  %>
                                  <img src="images/board_logo/<%=logo%>" align="absmiddle" />
                              <%}%>
                            </td>
                            <td width="40%" align="left"><a class="boardName" href="<%=ForumPage.getListTopicPage(request, lf.getCode())%>"><%=lf.getNameWithStyle()%></a>&nbsp;<font color="#888888">(<lt:Label res="res.label.forum.index" key="today"/>&nbsp;<%=lf.getTodayCount()%>)</font><br />
                                <img src="<%=skinPath%>/images/icon/arrowhead.jpg" />&nbsp;<%=lf.getDescription()%><br />
                            
							<lt:Label res="res.label.forum.index" key="board_manager"/>
                                  <%
						  Vector managers = mm.getBoardManagers(lf.getCode());
						  Iterator irmgr = managers.iterator();
						  BoardManagerDb bmd = new BoardManagerDb();
						  while (irmgr.hasNext()) {
							UserDb user = (UserDb) irmgr.next();
				            bmd = bmd.getBoardManagerDb(lf.getCode(), user.getName());
							if (!bmd.isHide()) {
						  %>
                                  <a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(user.getName())%>" style="color:#f96e00"><%=user.getNick()%></a>&nbsp;
                            <%}
						  }%>						    </td>
                            <%if (ir3.hasNext()) {
								lf = (Leaf)ir3.next();
								if (!lf.isDisplay(request, privilege1)) {
									continue;
								}				
			%>
                            <td width="10%" align="center"> <%
							  logo = StrUtil.getNullString(lf.getLogo());
							  if (!logo.equals("")) {
							  %>
                                  <img src="images/board_logo/<%=logo%>" align="absmiddle" />
                              <%}%>
                              </td>
                            <td width="40%" align="left"><a class="boardName" href="<%=ForumPage.getListTopicPage(request, lf.getCode())%>"><%=lf.getNameWithStyle()%></a>&nbsp;<font color="#888888">(<lt:Label res="res.label.forum.index" key="today"/>&nbsp;<%=lf.getTodayCount()%>)</font><br />
                                <img src="<%=skinPath%>/images/icon/arrowhead.jpg" />&nbsp;<%=lf.getDescription()%><br />
                              <lt:Label res="res.label.forum.index" key="board_manager"/>
                                  <%
						  managers = mm.getBoardManagers(lf.getCode());
						  irmgr = managers.iterator();
						  while (irmgr.hasNext()) {
							UserDb user = (UserDb) irmgr.next();
				            bmd = bmd.getBoardManagerDb(lf.getCode(), user.getName());
							if (!bmd.isHide()) {
						  %>
                                  <a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(user.getName())%>" style="color:#f96e00"><%=user.getNick()%></a>&nbsp;
                            <%}
						  }%></td>
                            <%}%>
                          </tr>
                          <%}%>
                        </table>
                      </div>
                    </div>
                    <%		}
		}
		if (isFounded)
			break;
	}
%>
		  		<%if (forum.isShowLink()) {%>
			        <div class="boardArea link">
                      <div class="boardAreaTitle">
                        <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
                          <tr>
                            <td align="left">
                             <span style="font-size:16px; font-family:'微软雅黑';border-left:3px solid #4796de; padding-left:10px">
                              <lt:Label res="res.label.forum.index" key="link"/>
                              </span>
                            </td>
                            <td align="right">&nbsp;</td>
                          </tr>
                        </table>
                      </div>
			          <div class="content">
                        <div id="linkInfo">
							<%
							LinkDb ld = new LinkDb();
							String listsql = ld.getListSql(LinkDb.KIND_DEFAULT, LinkDb.USER_SYSTEM);
							com.cloudwebsoft.framework.base.ObjectBlockIterator irlink;
							int totalLink = ld.getObjectCount(listsql, LinkDb.getVisualGroupName(LinkDb.KIND_DEFAULT, LinkDb.USER_SYSTEM));
							int m = 0;
							if (totalLink>0) {
								out.print("<ul>");
								irlink = ld.getObjects(listsql, LinkDb.getVisualGroupName(LinkDb.KIND_DEFAULT, LinkDb.USER_SYSTEM), 0, totalLink);
								while (irlink.hasNext()) {
									ld = (LinkDb) irlink.next();
									%>
									<li><a target="_blank" href="<%=ld.getUrl()%>" title="<%=ld.getTitle()%>">
									<%if (!ld.getImage().equals("")) {
										if (StrUtil.getFileExt(ld.getImage()).equals("swf")) {%>
											<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,29,0" width="88" height="31">
											<param name="movie" value="<%=ld.getImageUrl(request)%>">
											<param name="quality" value="high">
											<embed src="<%=ld.getImageUrl(request)%>" quality="high" pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash" width="88" height="31"></embed>
											</object>
										<%}else{%>
										  <img src="<%=ld.getImageUrl(request)%>" width="88" height="31" border="0">
									  <%}						
									}else{%>
										<%=ld.getTitle()%>
									<%}%>
									</a></li>
								<%}
								out.print("</ul>");
							}%>
							<%
							listsql = ld.getListSql("second", LinkDb.USER_SYSTEM);
							totalLink = ld.getObjectCount(listsql, LinkDb.getVisualGroupName("second", LinkDb.USER_SYSTEM));
							if (totalLink>0) {
								out.print("<ul>");
								irlink = ld.getObjects(listsql, LinkDb.getVisualGroupName(LinkDb.KIND_DEFAULT, LinkDb.USER_SYSTEM), 0, totalLink);
								m = 0;
								while (irlink.hasNext()) {
									ld = (LinkDb) irlink.next();
									%>
									<li><a target="_blank" href="<%=ld.getUrl()%>" title="<%=ld.getTitle()%>">
									<%if (!ld.getImage().equals("")) {
										if (StrUtil.getFileExt(ld.getImage()).equals("swf")) {%>
											<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,29,0" width="88" height="31">
											<param name="movie" value="<%=ld.getImageUrl(request)%>">
											<param name="quality" value="high">
											<embed src="<%=ld.getImageUrl(request)%>" quality="high" pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash" width="88" height="31"></embed>
											</object>
										<%}else{%>
										  <img src="<%=ld.getImageUrl(request)%>" width="88" height="31" border="0">
									  <%}						
									}else{%>
										<%=ld.getTitle()%>
									<%}%>
									</a></li>
							<%	}
								out.print("</ul>");
							}%>	
							<%
							listsql = ld.getListSql("three", LinkDb.USER_SYSTEM);
							totalLink = ld.getObjectCount(listsql, LinkDb.getVisualGroupName("three", LinkDb.USER_SYSTEM));
							if (totalLink>0) {
								out.print("<ul>");
								irlink = ld.getObjects(listsql, LinkDb.getVisualGroupName(LinkDb.KIND_DEFAULT, LinkDb.USER_SYSTEM), 0, totalLink);
								m = 0;
								while (irlink.hasNext()) {
									ld = (LinkDb) irlink.next();
									%>
									<li><a target="_blank" href="<%=ld.getUrl()%>" title="<%=ld.getTitle()%>">
									<%if (!ld.getImage().equals("")) {
										if (StrUtil.getFileExt(ld.getImage()).equals("swf")) {%>
											<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,29,0" width="88" height="31">
											<param name="movie" value="<%=ld.getImageUrl(request)%>">
											<param name="quality" value="high">
											<embed src="<%=ld.getImageUrl(request)%>" quality="high" pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash" width="88" height="31"></embed>
											</object>
										<%}else{%>
										  <img src="<%=ld.getImageUrl(request)%>" width="88" height="31" border="0">
									  <%}
									}else{%>
										<%=ld.getTitle()%>
									<%}%>
									</a></li>
							<%	}
								out.print("</ul>");
							}%>
                        </div>
		              </div>
	            </div>
				<%}%>
			  </div>
			</div>
		</div>
		<%@ include file="sidebar.jsp"%>

	</div>
<%if (privilege1.canUserDo(request, "", "view_online")) {%>
  <div id="onlineInfo">
    <table width="100%" border="0" cellpadding="0" cellspacing="0">
      <tr>
        <td id="OLInfoTitle"><%
		OnlineInfo oli = new OnlineInfo();
		int allcount = oli.getAllCount();
		int allusercount = oli.getAllUserCount();
		int allguestcount = allcount - allusercount;
		%>
		<lt:Label res="res.label.forum.index" key="online"/>
		<%=allcount%>
		<lt:Label res="res.label.forum.index" key="ren"/>
		<lt:Label res="res.label.forum.index" key="online_reg_count"/>
		<%=allusercount%>
		<lt:Label res="res.label.forum.index" key="ren"/>
		<lt:Label res="res.label.forum.index" key="online_guest_count"/>
		<%=allguestcount%>
		<lt:Label res="res.label.forum.index" key="ren"/>
		&nbsp;
		<lt:Label res="res.label.forum.index" key="today_post"/>
		<b><%=forum.getTodayCount()%></b>&nbsp;
		<img id="followImg000" style="CURSOR:pointer" src="images/plus.gif" border="0" loaded="no" onClick="loadonline('', 1)" alt="<lt:Label res="res.label.forum.index" key="show_online"/>" />
		<span id="advance"><lt:Label res="res.label.forum.index" key="online_list"/></span>
		<lt:Label res="res.label.forum.index" key="create_date"/>
		<%=com.redmoon.forum.ForumSkin.formatDate(request, forum.getCreateDate())%> |&nbsp;
		<lt:Label res="res.label.forum.index" key="online_max_count"/>
		<%=forum.getMaxOnlineCount()%>
		<lt:Label res="res.label.forum.index" key="ren"/>
		&nbsp;<%=com.redmoon.forum.ForumSkin.formatDate(request, forum.getMaxOnlineDate())%></td>
      </tr>
      <tr>
        <td><div class="line"></div></td>
      </tr>
      <tr>
        <td><img src="<%=skinPath%>/images/group.jpg" />&nbsp;&nbsp;<img src="<%=skinPath%>/images/group_admin.jpg" />&nbsp;&nbsp;管理员&nbsp;&nbsp;<img src="<%=skinPath%>/images/group_boardmgr.jpg" />&nbsp;&nbsp;版主&nbsp;&nbsp;<img src="<%=skinPath%>/images/group_member.jpg" />&nbsp;&nbsp;会员&nbsp;&nbsp;<img src="<%=skinPath%>/images/group_everyone.jpg" />&nbsp;&nbsp;游客</td>
        </tr>
      <tr>
        <td>
		<div id="followDIV000" name="followDIV000">
		<div style="height:100%;display:none; BORDER-RIGHT: black 1px solid; PADDING-RIGHT: 2px; BORDER-TOP: black 1px solid; PADDING-LEFT: 2px; PADDING-BOTTOM: 2px; MARGIN-LEFT: 18px; BORDER-LEFT: black 1px solid; WIDTH: 240px; COLOR: black; PADDING-TOP: 2px; BORDER-BOTTOM: black 1px solid; BACKGROUND-COLOR: lightyellow" 
		onclick="loadonline('', 1)"><lt:Label res="res.label.forum.index" key="wait"/></DIV>
        </div>
		</td>
        </tr>
    </table>
  </div>
 &nbsp;
<%}%>
</div>
<%
int msgCount = 0;
Integer msgCountObj = (Integer)request.getAttribute("msgCount");
if (msgCountObj!=null) {
	msgCount = msgCountObj.intValue();
}
if (msgCount>0) {
	%>
	<%if (msgCount>0) {%>
	   <%@ include file="inc/msg_popup.jsp"%>
	<%
	}
}
%>
<%@ include file="inc/footer.jsp"%>
</div>
</BODY>
<%
// 用于统计论坛首页访问计数
request.setAttribute("boardcode", Leaf.CODE_ROOT);
%>
<%@ include file="../inc/topic_hit_count.jsp"%>
</HTML>
