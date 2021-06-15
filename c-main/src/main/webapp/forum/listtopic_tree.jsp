<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.forum.err.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.util.*"%>
<%@ page import="org.jdom.*"%>
<%@ page import="java.util.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.base.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="cn.js.fan.module.pvg.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%@ taglib uri="/WEB-INF/tlds/AdTag.tld" prefix="ad"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
long pageBeginTime =  System.currentTimeMillis();

String boardcode = ParamUtil.get(request, "boardcode");
if (boardcode.equals("")) {
	out.print(StrUtil.Alert_Back(cn.js.fan.web.SkinUtil.LoadString(request, "res.label.forum.listtopic", "need_board")));
	return;
}

Leaf curleaf = new Leaf();
curleaf = curleaf.getLeaf(boardcode);
if (curleaf==null || !curleaf.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "res.label.forum.listtopic", "no_board")));
	return;
}

if (curleaf.getType()==Leaf.TYPE_DOMAIN) {
	if (curleaf.getCode().equals(Leaf.CODE_ROOT))
		response.sendRedirect("index.jsp");
	else {
		// 如果当前版块的上级目录为根目录，则说明是一级区域，一级区域跳转至index.jsp，而二级以上区域则不跳转
		if (curleaf.getParentCode().equals(Leaf.CODE_ROOT)) {		
			response.sendRedirect("index.jsp?boardField=" + StrUtil.UrlEncode(boardcode));
			return;
		}
	}
}

try {
	privilege.checkCanEnterBoard(request, boardcode);
}
catch (ErrMsgException e) {
	response.sendRedirect("../info.jsp?info=" + StrUtil.UrlEncode(e.getMessage()));
	// e.printStackTrace();
	return;
}

String boardname = curleaf.getName();

int threadType = StrUtil.toInt(ParamUtil.get(request, "threadType"), ThreadTypeDb.THREAD_TYPE_NONE);

UserSession.setBoardCode(request, boardcode);

String username = privilege.getUser(request);

// 置用户所在版块
if (username!=null && !username.equals("")) {
 	OnlineUserDb ou = new OnlineUserDb();
	ou = ou.getOnlineUserDb(username);
	ou.setUserInBoard(boardcode);
}

// 取得皮肤路径
String skinPath = SkinMgr.getSkinPath(request, curleaf);

String op = StrUtil.getNullString(request.getParameter("op"));

//seo
com.redmoon.forum.util.SeoConfig scfg = new com.redmoon.forum.util.SeoConfig();
String seoTitle = scfg.getProperty("seotitle");
String seoKeywords = scfg.getProperty("seokeywords");
String seoDescription = scfg.getProperty("seodescription");
String seoHead = scfg.getProperty("seohead");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD><TITLE><%=boardname%> - <%=Global.AppName%> <%=seoTitle%></TITLE>
<%=seoHead%>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<META name="keywords" content="<%=seoKeywords%>">
<META name="description" content="<%=seoDescription%>">
<LINK href="<%=skinPath%>/css.css" type=text/css rel=stylesheet>
<script src="../inc/common.js"></script>
<SCRIPT>
var curpage = 1;
function loadonline(boardcode, CPages){
	var targetImg = eval("document.all.followImg000");
	var targetDiv = eval("document.all.followDIV000");
	if (targetImg.src.indexOf("nofollow")!=-1){return false;}
	if ("object"==typeof(targetImg)){
		if (curpage!=CPages || targetDiv.style.display!='block')
		{
			curpage = CPages;
			targetDiv.style.display="block";
			targetImg.src="images/minus.gif";
			advance.innerText="<lt:Label res="res.label.forum.listtopic" key="close_online"/>";
			document.frames["hiddenframe"].location.replace("online.jsp?boardcode="+boardcode+"&CPages="+CPages);
		}
		else
		{
			targetDiv.style.display="none";
			targetImg.src="images/plus.gif";
			advance.innerText="<lt:Label res="res.label.forum.listtopic" key="show_online"/>"
		}
	}
}

// 展开帖子
function loadThreadFollow(b_id,t_id,getstr){
	var targetImg2=$("followImg" + t_id);
	var targetTR2=$("follow" + t_id);
	if (targetImg2.src.indexOf("nofollow")!=-1){return false;}
	if ("object"==typeof(targetImg2)){
		if (targetTR2.style.display!="")
		{
			targetTR2.style.display="";
			targetImg2.src="<%=skinPath%>/images/minus.gif";
		}else{
			targetTR2.style.display="none";
			targetImg2.src="<%=skinPath%>/images/plus.gif";
		}
	}
}
</SCRIPT>
</HEAD>
<BODY>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<%@ include file="inc/position.jsp"%>
<jsp:useBean id="Topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<jsp:useBean id="userservice" scope="page" class="com.redmoon.forum.person.userservice" />
<%
String querystring = StrUtil.getNullString(request.getQueryString());
String privurl=request.getRequestURL()+"?"+StrUtil.UrlEncode(querystring,"utf-8");

// 登记访客
try {
	privilege.enrolGuest(request,response);
}
catch (UserArrestedException e) {
	response.sendRedirect("info.jsp?info=" + StrUtil.UrlEncode(e.getMessage()));
	return;
}

// 刷新在位时间
userservice.refreshStayTime(request,response);

String timelimit = request.getParameter("timelimit");
if (timelimit==null)
	timelimit = "all";

String brule = StrUtil.getNullString(curleaf.getBoardRule());
if (!brule.equals("")) {%>
<div class="boardRules">
  <div class="boardRulesTitle"><lt:Label res="res.label.forum.listtopic" key="rule"/></div>
  <div class="boardRulesLine"></div>
  <div class="boardRulesContent"><%=brule.trim()%></div>
</div>
<%}%>
<%	
PluginMgr pm = new PluginMgr();
Vector vplugin = pm.getAllPluginUnitOfBoard(boardcode);
if (vplugin.size()>0) {
	Iterator irplugin = vplugin.iterator();
	while (irplugin.hasNext()) {
		PluginUnit pu = (PluginUnit)irplugin.next();
		IPluginUI ipu = pu.getUI(request, response, out);
		IPluginViewListThread pv = ipu.getViewListThread(boardcode);
		String rule = pv.render(UIListThread.POS_RULE);
		if (!rule.equals("")) {
%>
<div class="pluginRules">
  <div class="pluginRulesTitle"><%=pu.getName(request)%></div>
  <div class="pluginRulesLine"></div>
  <div class="pluginRulesContent"><%=rule%></div>
</div>
<%		}
	}
}%>
<%
MsgMgr mm = new MsgMgr();
EntranceMgr em = new EntranceMgr();
com.redmoon.forum.Config cfg1 = com.redmoon.forum.Config.getInstance();
int pagesize = cfg1.getIntProperty("forum.showTopicPageSize");
com.redmoon.forum.person.UserMgr um = new com.redmoon.forum.person.UserMgr();
if (curleaf.getChildCount()>0) {
	if (curleaf.getDisplayStyle()==Leaf.DISPLAY_STYLE_VERTICAL) {
%>
<div class="boardArea" id="boardArea_0">
  <div class="boardAreaTitle">
    <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
      <tr>
        <td align="left"><%=curleaf.getName()%></td>
        <td align="right"><img src="<%=skinPath%>/images/icon/boardliststyle_0.jpg" onclick="collapse('<%=curleaf.getCode()%>', 'true')" /><img src="<%=skinPath%>/images/icon/quick.jpg" /></td>
      </tr>
    </table>
  </div>
  <div class="content">
    <table width="100%" border="0" cellpadding="0" cellspacing="0" class="boardlist">
      <%
						MsgDb md = null;

						LeafChildrenCacheMgr dl = new LeafChildrenCacheMgr(curleaf.getCode());
						java.util.Vector v = dl.getChildren();
						Iterator ir1 = v.iterator();
						while (ir1.hasNext()) {
							Leaf lf = (Leaf) ir1.next();
							md = mm.getMsgDb(lf.getAddId());
							if (!lf.isDisplay(request, privilege))
								continue;
						%>
      <tbody>
        <tr>
          <td width="70" align="center"><%if (lf.isLocked()) {%>
              <img alt="<lt:Label res="res.label.forum.index" key="board_lock"/>" src="<%=skinPath%>/images/board_lock.gif" />
              <%}else{%>
              <%if (lf.getTodayCount()>0) {%>
              <img alt="<lt:Label res="res.label.forum.index" key="board_new"/>" src="<%=skinPath%>/images/board_new.gif" />
              <%}else{%>
              <img alt="<lt:Label res="res.label.forum.index" key="board_nonew"/>" src="<%=skinPath%>/images/board_nonew.gif" />
              <%}%>
              <%}%>
          </td>
          <td align="left"><a href="<%=ForumPage.getListTopicPage(request, lf.getCode())%>">
            <%
			String lfName = lf.getName();
			if (!lf.getColor().equals("")) {
				lfName = "<font color=" + lf.getColor() + ">" + lfName + "</font>";
			}
			if (lf.isBold()) {
				lfName = "<strong>" + lfName + "</strong>";
			}
			%>
            <%=lfName%></a>
              <%
			int chcount = lf.getChildCount();
			Vector ventrance = em.getAllEntranceUnitOfBoard(lf.getCode());
			if (ventrance.size()>0) {
				Iterator irpluginent = ventrance.iterator();
				while (irpluginent.hasNext()) {
					EntranceUnit eu = (EntranceUnit)irpluginent.next();
					out.print("<img src='" + skinPath + "/images/passport.gif' alt='" + eu.getDesc(request) + "'>&nbsp;");
				}		
			}						
			%>
            (今日:&nbsp;<%=lf.getTodayCount()%>)<br />
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
            <a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(user.getName())%>"><%=user.getNick()%></a>&nbsp;
            <%}
			}%>
            &nbsp;&nbsp;主题:&nbsp;<%=lf.getTopicCount()%>&nbsp;&nbsp;贴数:&nbsp;<%=lf.getPostCount()%> <br />
            <%
			MsgDb mdb = mm.getMsgDb(md.getRootid());
			%>
            <lt:Label res="res.label.forum.index" key="topic"/>
            <%if (md.isLoaded()) {
					if (md.getReplyid()==-1){%>
            <a title="<%=StrUtil.toHtml(mdb.getTitle())%>" href="<%=ForumPage.getShowTopicPage(request, mdb.getId())%>"><%=DefaultRender.RenderTitle(request, mdb, 60)%></a>
            <%}else{
				int CPages = (int)Math.ceil((double)md.getOrders()/pagesize);  
			%>
            <a title="<%=StrUtil.toHtml(mdb.getTitle())%>" href="<%=ForumPage.getShowTopicPage(request, md.getRootid(), CPages, ""+md.getId())%>"><%=DefaultRender.RenderTitle(request, mdb, 60)%></a>
            <%}
			}%>
            &nbsp;&nbsp;
            <%if (md.isLoaded()) {%>
            <%if (md.getReplyid()==-1) {%>
            <lt:Label res="res.label.forum.index" key="topic_post"/>
            <%}else{%>
            <lt:Label res="res.label.forum.index" key="topic_reply"/>
            <%}%>
            <%if (!md.getName().equals("")) {%>
            <a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(md.getName())%>"><%=um.getUser(md.getName()).getNick()%></a>
            <%}else{%>
            <lt:Label res="res.label.forum.showtopic" key="anonym"/>
            <%}
								}%>
            <lt:Label res="res.label.forum.index" key="topic_date"/>
            <%=com.redmoon.forum.ForumSkin.formatDateTimeShort(request, md.getAddDate())%>
            <%if (chcount>0) {%>
            <div>
              <%
				LeafChildrenCacheMgr lfc = new LeafChildrenCacheMgr(lf.getCode()); 
				Vector chv = lfc.getLeafChildren();
				Iterator chir = chv.iterator();
				while (chir.hasNext()) {
					Leaf chlf = (Leaf) chir.next();
					if (chlf.isDisplay(request, privilege)) {
				%>
              <a href="<%=ForumPage.getListTopicPage(request, chlf.getCode())%>"><font color="<%=chlf.getColor()%>"><%=chlf.getName()%></font></a>&nbsp;
              <%}
				}%>
            </div>
            <%}%>
          </td>
          <td width="110" align="center"><%
		  	String logo = StrUtil.getNullString(lf.getLogo());
		  	if (!logo.equals("")) {
		  	%>
              <img src="images/board_logo/<%=logo%>" align="absmiddle" />
              <%}%>
          </td>
        </tr>
      </tbody>
      <tbody>
        <tr>
          <td class="line" colspan="3"></td>
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
        <td align="left"><%=curleaf.getName()%></td>
        <td align="right"><img src="<%=skinPath%>/images/icon/boardliststyle_0.jpg" onclick="collapse('<%=curleaf.getCode()%>', 'false')" /><img src="<%=skinPath%>/images/icon/quick.jpg" /></td>
      </tr>
    </table>
  </div>
  <div class="content">
    <table width="100%" border="0" cellpadding="0" cellspacing="0" class="boardlist">
      <%
		MsgDb md = new MsgDb();
		LeafChildrenCacheMgr lccm = new LeafChildrenCacheMgr(curleaf.getCode());
		java.util.Vector v3 = lccm.getChildren();
		Iterator ir3 = v3.iterator();
		int row = 0;
		while (ir3.hasNext()) {
			row ++;
			Leaf lf = (Leaf) ir3.next();
			if (!lf.isDisplay(request, privilege)) {
				continue;
			}
			md = md.getMsgDb(lf.getAddId());
		%>
      <tr>
        <td width="70" align="center"><%if (lf.isLocked()) {%>
            <img alt="<lt:Label res="res.label.forum.index" key="board_lock"/>" src="<%=skinPath%>/images/board_lock.gif" />
            <%}else{%>
            <%if (lf.getTodayCount()>0) {%>
            <img alt="<lt:Label res="res.label.forum.index" key="board_new"/>" src="<%=skinPath%>/images/board_new.gif" />
            <%}else{%>
            <img alt="<lt:Label res="res.label.forum.index" key="board_nonew"/>" src="<%=skinPath%>/images/board_nonew.gif" />
            <%}%>
            <%}%>
        </td>
        <td align="left"> 『&nbsp;<a href="<%=ForumPage.getListTopicPage(request, lf.getCode())%>"><%=lf.getName()%></a>(今日:&nbsp;<%=lf.getTodayCount()%>)&nbsp;』<br />
            <img src="<%=skinPath%>/images/icon/arrowhead.jpg" />&nbsp;&nbsp;社区站务，提供社区服务<br />
          &nbsp;
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
          <a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(user.getName())%>"><%=user.getNick()%></a>&nbsp;
          <%}
		}%>
        </td>
        <%if (ir3.hasNext()) {
				lf = (Leaf)ir3.next();
			%>
        <td width="70" align="center"><%if (lf.isLocked()) {%>
            <img alt="<lt:Label res="res.label.forum.index" key="board_lock"/>" src="<%=skinPath%>/images/board_lock.gif" />
            <%}else{%>
            <%if (lf.getTodayCount()>0) {%>
            <img alt="<lt:Label res="res.label.forum.index" key="board_new"/>" src="<%=skinPath%>/images/board_new.gif" />
            <%}else{%>
            <img alt="<lt:Label res="res.label.forum.index" key="board_nonew"/>" src="<%=skinPath%>/images/board_nonew.gif" />
            <%}%>
            <%}%></td>
        <td align="left"> 『&nbsp;<a href="<%=ForumPage.getListTopicPage(request, lf.getCode())%>"><%=lf.getName()%></a>&nbsp;』<br />
            <img src="<%=skinPath%>/images/icon/arrowhead.jpg" />&nbsp;&nbsp;社区站务，提供社区服务<br />
          &nbsp;
          <lt:Label res="res.label.forum.index" key="board_manager"/>
          <%
			  managers = mm.getBoardManagers(lf.getCode());
			  irmgr = managers.iterator();
			  while (irmgr.hasNext()) {
				UserDb user = (UserDb) irmgr.next();
				bmd = bmd.getBoardManagerDb(lf.getCode(), user.getName());
				if (!bmd.isHide()) {
			  %>
          <a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(user.getName())%>"><%=user.getNick()%></a>&nbsp;
          <%}
			  }%></td>
        <%}%>
      </tr>
      <%}%>
    </table>
  </div>
</div>
<%}
}%>
<%
if (curleaf.getType()==Leaf.TYPE_BOARD) {
%>
	<div style="padding-top:10px">
	<span class="addTopic">
	<%
		String addpage = "addtopic_new.jsp";
		if (com.redmoon.forum.Config.getInstance().getBooleanProperty("forum.isWebeditTopicEnabled") && curleaf.getWebeditAllowType()==Leaf.WEBEDIT_ALLOW_TYPE_REDMOON_FIRST)
			addpage = "addtopic_we.jsp";
		%>
	  <span id="buttonSpan" style="display:none">
		<A href="<%=addpage%>?isvote=1&boardcode=<%=boardcode%>&threadType=<%=threadType%>&privurl=<%=privurl%>"> 
        <IMG src="<%=skinPath%>/images/votenew_<%=SkinUtil.getLocale(request)%>.gif" border=0 alt="<lt:Label res="res.label.forum.listtopic" key="vote_btn"/>">&nbsp;&nbsp;<lt:Label res="res.label.forum.listtopic" key="vote_btn"/></A>	  
	  <%
		if (vplugin.size()>0) {
			Iterator irplugin = vplugin.iterator();
			while (irplugin.hasNext()) {
				PluginUnit pu = (PluginUnit)irplugin.next();				
				if (pu.getType().equals(pu.TYPE_TOPIC) && !pu.getButton().equals("")) {%><a href="<%=addpage%>?pluginCode=<%=pu.getCode()%>&boardcode=<%=StrUtil.UrlEncode(boardcode)%>&threadType=<%=threadType%>&privurl=<%=privurl%>"><img src="<%=skinPath + "/" + pu.getButton()%>_<%=SkinUtil.getLocale(request)%>.gif" border="0">&nbsp;&nbsp;<%=pu.getName(request)%></a><%}
			}
		}

		Vector vplugin2 = curleaf.getAllPlugin2();
		Iterator irplugin2 = vplugin2.iterator();
		while (irplugin2.hasNext()) {
			com.redmoon.forum.plugin2.Plugin2Unit p2u = (com.redmoon.forum.plugin2.Plugin2Unit)irplugin2.next();
		%><a href="<%=addpage%>?plugin2Code=<%=p2u.getCode()%>&boardcode=<%=StrUtil.UrlEncode(boardcode)%>&threadType=<%=threadType%>&privurl=<%=privurl%>"><img src="<%=skinPath + "/images/" + p2u.getButton()%>_<%=SkinUtil.getLocale(request)%>.gif" border="0"><%=p2u.getName(request)%></a><%}%></span>		
		<A href="<%=addpage%>?boardcode=<%=boardcode%>&threadType=<%=threadType%>&privurl=<%=privurl%>"> 
          <IMG onmouseover='menuOffY=29;if(buttonSpan.innerHTML!="") showmenu(event, buttonSpan.innerHTML, 0);menuOffY=18' src="<%=skinPath%>/images/post_<%=SkinUtil.getLocale(request)%>.gif" border=0 alt="<lt:Label res="res.label.forum.listtopic" key="post_btn"/>"></A>
	</span><span class="op">
	&nbsp;<a href="<%=ForumPage.getListTopicPage(request, boardcode)%>" title="<lt:Label res="res.label.forum.showtopic" key="flat_view"/>"><img border=0 src="images/flatview.gif"></a>&nbsp;<a href="listtopic.jsp?op=showelite&boardcode=<%=boardcode%>">
	<lt:Label res="res.label.forum.listtopic" key="elite"/>
	</a>
    <SELECT onChange="if(this.options[this.selectedIndex].value!=''){location=this.options[this.selectedIndex].value;}" name=select2>
	  <OPTION selected><lt:Label res="res.label.forum.listtopic" key="manager"/></OPTION>
	  <OPTION>-------</OPTION>
	  <%
	  BoardManagerDb bmd = new BoardManagerDb();	  
	  Vector managers = mm.getBoardManagers(boardcode);
	  Iterator irmgr = managers.iterator();
	  boolean isUserBoardManager = false;
	  while (irmgr.hasNext()) {
	  	UserDb user = (UserDb) irmgr.next();
		if (user.getName().equals(username))
			isUserBoardManager = true;
		if (!bmd.isHide()) {			
	  %>
	  <OPTION value="../userinfo.jsp?username=<%=StrUtil.UrlEncode(user.getName())%>"><%=user.getNick()%></OPTION>
	  <%}
	  }
	  // 祖先节点上的版主
	  String parentCode = curleaf.getParentCode();
	  while (!parentCode.equals("-1")) {
	  	Leaf tmplf = curleaf.getLeaf(parentCode);
		if (tmplf==null)
			break;
		else {
		  managers = mm.getBoardManagers(tmplf.getCode());
		  if (managers.size()>0) {
		  	  out.print("<option>-------</option>");
			  irmgr = managers.iterator();
			  while (irmgr.hasNext()) {
				UserDb user = (UserDb) irmgr.next();
				bmd = bmd.getBoardManagerDb(tmplf.getCode(), user.getName());				
				if (!bmd.isHide()) {								
			  %>
				<OPTION value="../userinfo.jsp?username=<%=StrUtil.UrlEncode(user.getName())%>">
				<%if (tmplf.getCode().equals(Leaf.CODE_ROOT)){%>
				<lt:Label res="res.label.forum.listtopic" key="superManager"/>				
				<%}%>  <%=user.getNick()%>
				</OPTION>
			  <%}
			  }
		  }
		}
		parentCode = tmplf.getParentCode();
	  }
	  %>	  
    </SELECT>&nbsp; <SELECT onChange="if(this.options[this.selectedIndex].value!=''){location=this.options[this.selectedIndex].value+'&op=<%=op%>';}" 
      name=seltimelimit>
		<OPTION value="listtopic.jsp?boardcode=<%=boardcode%>&timelimit=all" selected><lt:Label res="res.label.forum.listtopic" key="topic_all"/></OPTION>
		<OPTION value="listtopic.jsp?boardcode=<%=boardcode%>&timelimit=1"><lt:Label res="res.label.forum.listtopic" key="topic_one_day"/></OPTION>
		<OPTION value="listtopic.jsp?boardcode=<%=boardcode%>&timelimit=2"><lt:Label res="res.label.forum.listtopic" key="topic_two_day"/></OPTION>
		<OPTION value="listtopic.jsp?boardcode=<%=boardcode%>&timelimit=7"><lt:Label res="res.label.forum.listtopic" key="topic_week"/></OPTION>
		<OPTION value="listtopic.jsp?boardcode=<%=boardcode%>&timelimit=15"><lt:Label res="res.label.forum.listtopic" key="topic_half_month"/></OPTION>
		<OPTION value="listtopic.jsp?boardcode=<%=boardcode%>&timelimit=30"><lt:Label res="res.label.forum.listtopic" key="topic_one_month"/></OPTION>
		<OPTION value="listtopic.jsp?boardcode=<%=boardcode%>&timelimit=60"><lt:Label res="res.label.forum.listtopic" key="topic_two_month"/></OPTION>
		<OPTION value="listtopic.jsp?boardcode=<%=boardcode%>&timelimit=180"><lt:Label res="res.label.forum.listtopic" key="topic_half_year"/></OPTION>
		</SELECT>
		<script language=javascript>
		</script>
		<script language=javascript><!--
		seltimelimit.value = "listtopic.jsp?boardcode=<%=boardcode%>&timelimit=<%=timelimit%>"
		//-->
		</script>
		&nbsp;		
		<A href="javascript:this.location.reload()" title="<lt:Label res="res.label.forum.listtopic" key="refresh"/>"><IMG src="images/refresh.gif" border=0></A>
	  &nbsp;<a href="rss.jsp?boardCode=<%=StrUtil.UrlEncode(boardcode)%>"><img src="../images/rss.gif" alt="rss<lt:Label res="res.label.forum.listtopic" key="subscription"/>" border="0" align="absmiddle"></a></TD>
      </span>
	</div>
<%
ThreadTypeDb ttd = new ThreadTypeDb();
Vector ttv = ttd.getThreadTypesOfBoard(boardcode);
if (ttv.size()>0) {
%>
  <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td>
	<div class="thread_type">
	<ul>
<%
Iterator ir = ttv.iterator();
String className = "";
while (ir.hasNext()) {
	ttd = (ThreadTypeDb)ir.next();
	if (ttd.getId()==threadType)
		className = "td_hover";
	else
		className = "";
	String ttName = ttd.getName();
	if (!ttd.getColor().equals(""))
		ttName = "<font color='" + ttd.getColor() + "'>" + ttName + "</font>";		
%>
	<li class="<%=className%>" onMouseOver="this.className='thread_type_hover'" onMouseOut="this.className='<%=className%>'">
	<div>
	<a href="<%=ForumPage.getListTopicPage(request, boardcode, 1, 1, ttd.getId())%>"><%=ttName%></a><br />
	</div>
	</li>
    <%}
	if (threadType==ThreadTypeDb.THREAD_TYPE_NONE)
		className = "td_hover";
	else
		className = "";
%>
	<li class="<%=className%>" onMouseOver="this.className='thread_type_hover'" onMouseOut="this.className='<%=className%>'">
   	<div><a href="<%=ForumPage.getListTopicPage(request, boardcode, 1, 1, ThreadTypeDb.THREAD_TYPE_NONE)%>">
	<lt:Label res="res.label.forum.listtopic" key="topic_all"/>
	</a></div>
	</li></ul>
    </div></td>
    </tr>
  </table>
<%}%>
<%
String sql = SQLBuilder.getListtopicSql(request, response, out, boardcode, op, timelimit, threadType);

MsgDb msgdb = new MsgDb();
int total = msgdb.getThreadsCount(sql, boardcode);

pagesize = cfg1.getIntProperty("forum.listTopicPageSize");
int showPageSize = cfg1.getIntProperty("forum.showTopicPageSize");
		
ForumPaginator paginator = new ForumPaginator(request, total, pagesize);
int curpage = paginator.getCPage(request);
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}
		
long start = (curpage-1)*pagesize;
long end = curpage*pagesize;
		
ThreadBlockIterator irmsg = msgdb.getThreads(sql, boardcode, start, end);
%>
<div class="listTopicWrapper" id="listTopic">
  <TABLE width="100%" cellpadding="0" cellspacing="0">
    <thead>
      <TR> 
        <th colSpan=4 align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="topis_list"/></th>
        <th width=91 height="26" align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="author"/></th>
        <th width=55 height="26" align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="reply"/></th>
        <th width=55 height="26" align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="hit"/></th>
        <th width=146 height="26" align=middle noWrap class="list_title_date"><lt:Label res="res.label.forum.listtopic" key="reply_date"/></th>
        <th width=14 align=middle noWrap class="list_title_date">&nbsp;</th>
      </TR>
    </thead>
<%		
String id="",name="",lydate="",rename="",redate="";
int level=0,iselite=0,islocked=0,expression=0;
int i = 0,recount=0,hit=0,type=0;
// 取出论坛置顶的贴子
ForumDb forum = new ForumDb();
long[] topmsgs = forum.getTopMsgs();
int tlen = topmsgs.length;
int hotReplyCount = cfg1.getIntProperty("forum.hotReplyCount");
UserDb ud = null;
UserDb reUserDb = null;
boolean isTopTopicFirstPage = cfg1.getBooleanProperty("forum.isTopTopicFirstPage");
if (isTopTopicFirstPage && curpage!=1)
	tlen = 0; // 仅首页显示总置顶贴
while (i<tlen) {
	 	  msgdb = msgdb.getMsgDb((int)topmsgs[i]);
		  i++;
		  id = ""+msgdb.getId();
		  name = msgdb.getName();
		  ud = um.getUser(name);
		  lydate = com.redmoon.forum.ForumSkin.formatDateTime(request, msgdb.getAddDate());
		  recount = msgdb.getRecount();
		  hit = msgdb.getHit();
		  expression = msgdb.getExpression();
		  type = msgdb.getType();
		  iselite = msgdb.getIsElite();
		  islocked = msgdb.getIsLocked();
		  level = msgdb.getLevel();
		  rename = StrUtil.getNullString(msgdb.getRename());
		  if (!rename.equals(""))
			  reUserDb = um.getUser(rename);
		  redate = com.redmoon.forum.ForumSkin.formatDateTime(request, msgdb.getRedate());
	  %>
    <tbody>
      <tr>
        <td noWrap align=middle width=17>&nbsp;</td> 
        <td noWrap align=middle width=53> 
		<%if (recount>=hotReplyCount){ %>
			<img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_hot"/>" src="<%=skinPath%>/images/f_hot.gif"> 
		<%}
		else if (recount>0) {%>
			<img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_reply"/>" src="<%=skinPath%>/images/f_new.gif">
		<%}
		else {%>
			<img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_no_reply"/>" src="<%=skinPath%>/images/f_norm.gif">
		<%}%>		</td>
        <td align=middle width=36> <% String urlboardname = StrUtil.UrlEncode(boardname,"utf-8"); %> <a href="<%=ForumPage.getShowTopicPage(request, 1, msgdb.getId(), msgdb.getId(), 1, "")%>" target=_blank> 
          <% 
		  if (islocked==1) { %>
          <IMG height=15 alt="" src="<%=skinPath%>/images/f_locked.gif" width=17 border=0> 
          <%}
		  else {
			if (type==1) { %>
          	<IMG height=15 alt="" src="<%=skinPath%>/images/f_poll.gif" width=17 border=0> 
          <%}else {
		  	if (expression!=MsgDb.EXPRESSION_NONE) {		  
		  %>
          		<img src="images/brow/<%=expression%>.gif" border=0> 
          <%}
		  	else
				out.print("&nbsp;");		  
		  	}
		  } %>
          </a></td>
        <td width="490" align=left><%
		if (recount>0) {
		%>
          <img id=followImg<%=id%> title="<lt:Label res="res.label.forum.listtopic" key="collapse_reply"/>" style="CURSOR: hand" onClick="loadThreadFollow(<%=id%>,<%=id%>,'&boardcode=<%=boardcode%>')" src="<%=skinPath%>/images/minus.gif" loaded="no">
          <% }else { %>
    <img id=followImg<%=id%> title="<lt:Label res="res.label.forum.listtopic" key="no_reply"/>" src="<%=skinPath%>/images/minus.gif" loaded="no">
    <% } %>
    <strong><lt:Label res="res.label.forum.listtopic" key="top"/></strong>
    <%
		  String attIcon = MsgUtil.getIconImg(msgdb);
		  if (!attIcon.equals("")) {
		  	out.print("<img src='../images/fileicon/" + attIcon + "'>");
		  }
		  %>
    <a title="<%=StrUtil.toHtml(msgdb.getTitle())%>" href="<%=ForumPage.getShowTopicPage(request, msgdb.getId())%>">
    <%
		String color = StrUtil.getNullString(msgdb.getColor());
		String tp = DefaultRender.RenderTitle(request, msgdb, 68);
		if (!color.equals(""))
			tp = "<font color='" + color + "'>" + tp + "</font>";
		if (msgdb.isBold())
			tp = "<B>" + tp + "</B>";
	%>
    <%=tp%></a>
    <%if (iselite==1) { %>
    <IMG src="images/topicgood.gif">
    <%}%>
    <%
		// 计算共有多少页回贴
		int allpages = Math.round((float)recount/showPageSize+0.5f);
		if (allpages>1)
		{
			int pg = allpages;
			if (allpages>10)
				pg = 10;
		 	out.print("[");
			for (int m=1; m<=pg; m++)
			{%>
    <a href="<%=ForumPage.getShowTopicPage(request, msgdb.getId(), m)%>"><%=m%></a>
    <%}
			if (allpages>10) {%>
...<a href="<%=ForumPage.getShowTopicPage(request, msgdb.getId(), allpages)%>"><%=allpages%></a>
<%}
		  	out.print("]");
		}%></td>
        <td class="list_td" align=middle width=91>
	  <%if (privilege.getUser(request).equals(name)) { %><IMG height=14 src="<%=skinPath%>/images/my.gif" width=14><%}%>
	  <%if (name.equals("")) {%>
		<lt:Label res="res.label.forum.showtopic" key="anonym"/>	  
	  <%}else{%>			
			<a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>"><%=ud.getNick()%></a>
	  <%}%></td>
        <td align=middle width=55><%=recount%></td>
        <td align=middle width=55 class="list_td"><%=hit%></td>
        <td align=left width=146 class="list_date">&nbsp;
          <%if (rename.equals("")) {%>
          <%=lydate%>
          <%}else{%>
          <%=redate%>
		  &nbsp;|&nbsp;
          <a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(rename,"utf-8")%>" title="<lt:Label res="res.label.forum.listtopic" key="topic_date"/><%=lydate%>"><%=reUserDb.getNick()%></a>
        <%}%></td>
        <td align=left width=14 class="list_date">&nbsp;</td>
      </tr>
<%	  
String sql2 = "select id from sq_message where rootid="+msgdb.getId()+" ORDER BY orders";
long totalMsg = msgdb.getMsgCount(sql2, boardcode, msgdb.getId());
if (totalMsg>1) {
%>
      <tr id=follow<%=id%>>
        <td noWrap align=middle width=17>&nbsp;</td> 
        <td noWrap align=middle width=53>&nbsp;</td>
        <td align=middle width=36>&nbsp;</td>
        <td align=left colspan="6"> 
<%		
int layer = 1;
MsgBlockIterator irmsg2 = msgdb.getMsgs(sql2, boardcode, msgdb.getId(), 0, totalMsg);
if (irmsg2.hasNext()) {
	// 跳过根贴
	irmsg2.next();
}
// 写跟贴
while (irmsg2.hasNext()) {
	  i++;
	  MsgDb md = (MsgDb)irmsg2.next();
	  id = "" + md.getId();
	  name = md.getName();
	  layer = md.getLayer();
	  lydate = DateUtil.format(md.getAddDate(), "MM-dd HH:mm");
	  ud = um.getUser(name);
 %>
<table cellspacing=0 cellpadding=0 width="100%" align=center border=0>
  <tbody> 
  <tr>
    <td height="13" align=left noWrap> 
      <%
		int pagesize2 = 10;
		int CPages = (int)Math.ceil((double)i/pagesize2);
		layer = layer-1;
		for (int k=1; k<=layer-1; k++){%>
		<img src="" width=18 height=1>
		<%}%>
      <img src="images/join.gif" width="18" height="16">
	  <%if (name.equals("")) {%>
		<lt:Label res="res.label.forum.showtopic" key="anonym"/>	  
	  <%}else{%>
		<a href="<%=ForumPage.getShowTopicPage(request, msgdb.getId(), CPages, id)%>"><%=DefaultRender.RenderFullTitle(request, md)%></a>&nbsp;&nbsp;<a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>"><%=ud.getNick()%></a>
	  <%}%>&nbsp;&nbsp;[<%=com.redmoon.forum.ForumSkin.formatDateTime(request, md.getAddDate())%>]
	  </td>
    </tr>
  </tbody> 
</table>
	<%}%>  
<%
}
%>        </td>
      </tr>
      <tr>
	  <td class="separaterBlank"></td>
      <td class="separater" colspan="7"></td>
	  <td class="separaterBlank"></td>
      </tr>	  
    </tbody>
<%}%>
<%
if (!isTopTopicFirstPage || (isTopTopicFirstPage && curpage==1)) {
%>
<tbody>
  <tr>
    <td></td>
    <td colspan="7" align="center">==&nbsp;普通主题&nbsp;==</td>
    <td></td>
  </tr>
  <tr>
  <td class="separaterBlank"></td>
  <td class="separater" colspan="7"></td>
  <td class="separaterBlank"></td>
  </tr>  
</tbody>
<%}%>
<%
while (irmsg.hasNext()) {
  msgdb = (MsgDb) irmsg.next(); 
  i++;
  id = ""+msgdb.getId();
  name = msgdb.getName();
  ud = um.getUser(name);
  lydate = com.redmoon.forum.ForumSkin.formatDateTime(request, msgdb.getAddDate());
  recount = msgdb.getRecount();
  hit = msgdb.getHit();
  expression = msgdb.getExpression();
  type = msgdb.getType();
  iselite = msgdb.getIsElite();
  islocked = msgdb.getIsLocked();
  level = msgdb.getLevel();
  rename = msgdb.getRename();
  if (!rename.equals(""))
	  reUserDb = um.getUser(rename);
  redate = com.redmoon.forum.ForumSkin.formatDateTime(request, msgdb.getRedate());
%>
    <tbody>
      <tr>
        <td noWrap align=middle width=17>&nbsp;</td> 
        <td noWrap align=middle width=53>
		<% if (level==MsgDb.LEVEL_TOP_BOARD) { %>
		<IMG alt="" src="<%=skinPath%>/images/f_top.gif" border=0> 
        <% } 
		else {
				if (recount>=hotReplyCount){ %> <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_hot"/>" src="<%=skinPath%>/images/f_hot.gif"> <%}
	  			else if (recount>0) {%> <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_reply"/>" src="<%=skinPath%>/images/f_new.gif"> <%}
	  			else {%> <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_no_reply"/>" src="<%=skinPath%>/images/f_norm.gif"> <%}
	 	}%>		</td>
        <td align=middle width=36> <% String urlboardname = StrUtil.UrlEncode(boardname,"utf-8"); %> <a href="<%=ForumPage.getShowTopicPage(request, 1, msgdb.getId(), msgdb.getId(), 1, "")%>" target=_blank> 
          <% 
		  if (islocked==1) { %>
          <IMG height=15 alt="" src="<%=skinPath%>/images/f_locked.gif" width=17 border=0> 
          <% }
		  else {
			  if (type==1) { %>
          <IMG height=15 alt="" src="<%=skinPath%>/images/f_poll.gif" width=17 border=0> 
          <%}else{
		  		if (expression!=MsgDb.EXPRESSION_NONE) {		  
		  %>
          <img src="images/brow/<%=expression%>.gif" border=0> 
          <%	}
		  		else
					out.print("&nbsp;");		  
		  	}
		  } %>
          </a></td>
        <td align=left> <%
		if (recount>0) {
		%>
          <img id=followImg<%=id%> title="<lt:Label res="res.label.forum.listtopic" key="collapse_reply"/>" style="CURSOR: hand" onClick="loadThreadFollow(<%=id%>,<%=id%>,'&boardcode=<%=boardcode%>')" src="<%=skinPath%>/images/minus.gif" loaded="no">
          <% }else { %>
          <img id=followImg<%=id%> title="<lt:Label res="res.label.forum.listtopic" key="no_reply"/>" src="<%=skinPath%>/images/minus.gif" loaded="no">
          <%}%>
		  <%
		  String attIcon = MsgUtil.getIconImg(msgdb);
		  if (!attIcon.equals("")) {
		  	out.print("<img src='../images/fileicon/" + attIcon + "'>");
		  }
		  %>
		<a title="<%=StrUtil.toHtml(msgdb.getTitle())%>" href="<%=ForumPage.getShowTopicPage(request, msgdb.getId())%>"> 
		<%
		String color = StrUtil.getNullString(msgdb.getColor());
		String tp = DefaultRender.RenderTitle(request, msgdb, 76);
		if (!color.equals(""))
			tp = "<font color='" + color + "'>" + tp + "</font>";
		if (msgdb.isBold())
			tp = "<B>" + tp + "</B>";
		%>
		<%=tp%>		</a>
			<%if (iselite==1) { %>
				<IMG src="images/topicgood.gif">
			<%}%>
		  <%
		// 计算共有多少页回贴
		int allpages = Math.round((float)recount/showPageSize+0.5f);
		if (allpages>1)
		{
			int pg = allpages;
			if (allpages>10)
				pg = 10;
		 	out.print("[");
			for (int m=1; m<=pg; m++)
			{%> 
				<a href="<%=ForumPage.getShowTopicPage(request, msgdb.getId(), m)%>"><%=m%></a> 
			<%}
			if (allpages>10) {%>
				...<a href="<%=ForumPage.getShowTopicPage(request, msgdb.getId(), allpages)%>"><%=allpages%></a>
			<%}
		  	out.print("]");
		}%>	    </td>
        <td class="list_td" align=middle width=91>
		<% if (privilege.getUser(request).equals(name)) { %> <IMG height=14 src="<%=skinPath%>/images/my.gif" width=14><% } %>
	  <%if (name.equals("")) {%>
		<lt:Label res="res.label.forum.showtopic" key="anonym"/>	  
	  <%}else{%>			
			<a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>"><%=ud.getNick()%></a>
	  <%}%>		</td>
        <td align=middle width=55><%=recount%></td>
        <td align=middle width=55 class="list_td"><%=hit%></td>
        <td align=left width=146 class="list_date"> 
		  &nbsp;
		  <%if (rename==null || rename.equals("")) {
				  	if (lydate!=null) {
					%>
          <%=lydate%>
          <%}
				  }else{%>
          <%=redate%>
		  &nbsp;|&nbsp;
          <a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(rename,"utf-8")%>" title="<lt:Label res="res.label.forum.listtopic" key="topic_date"/><%=lydate%>"><%=reUserDb.getNick()%></a>
        <%}%></td>
        <td align=left width=14 class="list_date">&nbsp;</td>
      </tr>
<%	  
String sql2 = "select id from sq_message where rootid="+msgdb.getId()+" ORDER BY orders";
long totalMsg = msgdb.getMsgCount(sql2, boardcode, msgdb.getId());
if (totalMsg>1) {
%>
      <tr id=follow<%=id%>>
        <td noWrap align=middle width=17>&nbsp;</td> 
        <td noWrap align=middle width=53>&nbsp;</td>
        <td align=middle width=36>&nbsp;</td>
        <td align=left colspan="6">
<%		
int layer = 1;
MsgBlockIterator irmsg2 = msgdb.getMsgs(sql2, boardcode, msgdb.getId(), 0, totalMsg);
if (irmsg2.hasNext()) {
	// 跳过根贴
	irmsg2.next();
}
// 写跟贴
i=0;
while (irmsg2.hasNext()) {
	  i++;
	  MsgDb md = (MsgDb)irmsg2.next();
	  id = "" + md.getId();
	  name = md.getName();
	  layer = md.getLayer();
	  lydate = DateUtil.format(md.getAddDate(), "MM-dd HH:mm");
	  ud = um.getUser(name);	  
 %>
<table cellspacing=0 cellpadding=0 width="100%" align=center border=0>
  <tbody> 
  <tr> 
    <td noWrap align=left height="13"> 
      <%
		int pagesize2 = 10;
		int CPages = (int)Math.ceil((double)i/pagesize2);
		layer = layer-1;
		for (int k=1; k<=layer-1; k++)
		{%>
		<img src="" width=18 height=1>
		<%}%>
      <img src="images/join.gif" width="18" height="16">
	  <%if (name.equals("")) {%>
		<lt:Label res="res.label.forum.showtopic" key="anonym"/>	  
	  <%}else{%>	  
	  <a href="<%=ForumPage.getShowTopicPage(request, msgdb.getId(), CPages, id)%>"><%=DefaultRender.RenderFullTitle(request, md)%></a> <a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>"><%=ud.getNick()%></a>
	  <%}%>
	  &nbsp;&nbsp;[<%=com.redmoon.forum.ForumSkin.formatDateTime(request, md.getAddDate())%>]</td>
  </tr>
  </tbody> 
</table>
<%
}
%>	</td>
      </tr>
<%}%>
      <tr>
	  <td class="separaterBlank"></td>
      <td class="separater" colspan="7"></td>
	  <td class="separaterBlank"></td>
      </tr>
    </tbody>
<%}%>
</table>
</div>
  <table width="100%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
    <tr>
      <td width="11%" align="left"><select name="selboard" onChange="if(this.options[this.selectedIndex].value!='not'){location='listtopic_tree.jsp?boardcode=' + this.options[this.selectedIndex].value;}">
        <option value="not" selected>
        <lt:Label res="res.label.forum.showtopic" key="sel_board"/>
        </option>
        <%
				Directory dir = new Directory();
				Leaf leaf = dir.getLeaf(Leaf.CODE_ROOT);
				com.redmoon.forum.DirectoryView dv = new com.redmoon.forum.DirectoryView(leaf);
				dv.ShowDirectoryAsOptions(request, privilege, out, leaf, leaf.getLayer());
			%>
      </select></td> 
      <td width="60" height="23" align="left"> 
          <%if (privilege.isManager(request, boardcode)) {%>
          <a href="manager/boardRule.jsp?boardcode=<%=StrUtil.UrlEncode(boardcode)%>"><lt:Label res="res.label.forum.listtopic" key="manage_board"/></a>
          <%}%>          &nbsp;</td>
      <td width="74%" align="right"><%
				String querystr = "boardcode=" + boardcode + "&op=" + op + "&threadType=" + threadType;
				if (op.equals(""))
					out.print(paginator.getListTopicCurPageBlock(request, boardcode, 1, threadType));
				else
					out.print(paginator.getCurPageBlock(request, "listtopic.jsp?"+querystr));
				%></td>
    </tr>
</table>
<%}%> 
  <div id="onlineInfo">
      <table width="100%" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td colspan="9" id="OLInfoTitle"><span class="online"><span>
            <%
OnlineInfo oli = new OnlineInfo();
int boardcount = oli.getBoardCount(boardcode);
int boardusercount = oli.getBoardUserCount(boardcode);
int boardguestcount = boardcount - boardusercount;
%>
            <lt:Label res="res.label.forum.listtopic" key="online"/>
            <%=oli.getAllCount()%>
            <lt:Label res="res.label.forum.listtopic" key="ren"/>
            &nbsp;
            <lt:Label res="res.label.forum.listtopic" key="cur_board"/>
            <%=boardcount%>
            <lt:Label res="res.label.forum.listtopic" key="ren"/>
            &nbsp;
            <lt:Label res="res.label.forum.listtopic" key="regist_user"/>
            <%=boardusercount%>
            <lt:Label res="res.label.forum.listtopic" key="ren"/>
            &nbsp;
            <lt:Label res="res.label.forum.listtopic" key="guest"/>
            <%=boardguestcount%>
            <lt:Label res="res.label.forum.listtopic" key="ren"/>
            &nbsp;<span>
            <lt:Label res="res.label.forum.listtopic" key="today_post"/>
            </span><%=curleaf.getTodayCount()%>&nbsp;</span>
			<img alt="<lt:Label res="res.label.forum.listtopic" key="show_online"/>" id="followImg000" style="CURSOR:pointer" src="images/plus.gif" border="0" onclick="loadonline('<%=boardcode%>', 1)" loaded="no" />&nbsp;<span id="advance"><lt:Label res="res.label.forum.listtopic" key="show_online"/>
          </span></span></td>
        </tr>
        <tr>
          <td colspan="9" class="line"></td>
        </tr>
        <tr>
          <td width="60"><img src="<%=skinPath%>/images/group.jpg" /></td>
          <td width="26"><img src="<%=skinPath%>/images/group_admin.jpg" /></td>
          <td width="50">管理员</td>
          <td width="26"><img src="<%=skinPath%>/images/group_boardmgr.jpg" /></td>
          <td width="50">版主</td>
          <td width="26"><img src="<%=skinPath%>/images/group_member.jpg" /></td>
          <td width="50">会员</td>
          <td width="26"><img src="<%=skinPath%>/images/group_everyone.jpg" /></td>
          <td width="640">游客</td>
        </tr>
        <tr>
          <td colspan="9"><DIV id="followDIV000" name="followDIV000">
            <div style="display:none; BORDER-RIGHT: black 1px solid; PADDING-RIGHT: 2px; BORDER-TOP: black 1px solid; PADDING-LEFT: 2px; PADDING-BOTTOM: 2px; MARGIN-LEFT: 18px; BORDER-LEFT: black 1px solid; WIDTH: 240px; COLOR: black; PADDING-TOP: 2px; BORDER-BOTTOM: black 1px solid; BACKGROUND-COLOR: lightyellow" 
      onclick="loadonline('<%=boardcode%>', 1)"><lt:Label res="res.label.forum.listtopic" key="wait_online"/></DIV>
        </div></td>
        </tr>
      </table>
    </div>
  <TABLE width="100%" border=0 align="center" 
      cellPadding=0 cellSpacing=4 borderColor=#111111 style="BORDER-COLLAPSE: collapse">
      <TBODY>
        <TR>
          <TD noWrap width=200><IMG height=12 alt="" 
            src="<%=skinPath%>/images/f_new.gif" width=18 border=0>&nbsp;<lt:Label res="res.label.forum.listtopic" key="topic_reply"/></TD>
          <TD noWrap width=100><IMG height=12 alt="" 
            src="<%=skinPath%>/images/f_hot.gif" width=18 border=0>&nbsp;<lt:Label res="res.label.forum.listtopic" key="topic_hot"/> </TD>
          <TD noWrap width=100><IMG height=15 alt="" 
            src="<%=skinPath%>/images/f_locked.gif" width=17 border=0>&nbsp;<lt:Label res="res.label.forum.listtopic" key="topic_lock"/></TD>
          <TD noWrap width=150><IMG src="images/topicgood.gif"> <lt:Label res="res.label.forum.listtopic" key="topic_elite"/></TD>
          <TD noWrap width=150><IMG height=15 alt="" src="images/top_forum.gif" width=15 border=0>&nbsp;<lt:Label res="res.label.forum.listtopic" key="topic_all_top"/></TD>
        </TR>
        <TR>
          <TD noWrap width=200><IMG height=12 alt="" 
            src="<%=skinPath%>/images/f_norm.gif" width=18 border=0>&nbsp;<lt:Label res="res.label.forum.listtopic" key="topic_no_reply"/></TD>
          <TD noWrap width=100><IMG height=15 alt="" 
            src="<%=skinPath%>/images/f_poll.gif" width=17 border=0>&nbsp;<lt:Label res="res.label.forum.listtopic" key="topic_vote"/></TD>
          <TD noWrap width=100><IMG height=15 alt="" 
            src="<%=skinPath%>/images/f_top.gif" width=15 border=0>&nbsp;<lt:Label res="res.label.forum.listtopic" key="topic_top"/></TD>
          <TD noWrap width=150><IMG height=14 src="<%=skinPath%>/images/my.gif" 
            width=14> <lt:Label res="res.label.forum.listtopic" key="topic_my"/></TD>
          <TD noWrap width=150>&nbsp;</TD>
        </TR>
      </TBODY>
    </TABLE>
	<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="center">
	<%
long pageEndTime =  System.currentTimeMillis();
long t = pageEndTime - pageBeginTime;
	%>
	<lt:Label res="res.label.forum.listtopic" key="page_run"/><%=t%><lt:Label res="res.label.forum.listtopic" key="mili_second"/>	</td>
  </tr>
</table>
<%
int msgCount = 0;
if (privilege.isUserLogin(request)) {
%>
	<jsp:useBean id="Msg" scope="page" class="com.redmoon.forum.message.MessageMgr"/>
	<%
	msgCount = Msg.getNewMsgCount(request);
	request.setAttribute("msgCount", new Integer(msgCount));
	%>
	<%if (msgCount>0) {%>
	   <%@ include file="inc/msg_popup.jsp"%>
	<%
	}
}
%>
</div>
<jsp:include page="inc/footer.jsp" />
</div>
</BODY>
</HTML>
