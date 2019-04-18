<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="org.jdom.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.redmoon.forum.OnlineInfo"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skincode = UserSet.getSkin(request);
if (skincode.equals(""))
	skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
if (skin==null)
	skin = skm.getSkin(UserSet.defaultSkin);
String skinPath = skin.getPath();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<title>
<lt:Label res="res.label.forum.search" key="search_result"/> - <%=Global.AppName%></title>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<meta http-equiv="x-ua-compatible" content="ie=7" />
<link href="admin/default.css" rel="stylesheet" type="text/css">
<STYLE>
TABLE {
	BORDER-TOP: 0px; BORDER-LEFT: 0px; BORDER-BOTTOM: 1px
}
TD {
	BORDER-RIGHT: 0px; BORDER-TOP: 0px
}
body {
	margin-top: 0px;
}
</STYLE>
<SCRIPT>
// 展开帖子
function loadThreadFollow(b_id,t_id,getstr){
	var targetImg2 =eval("document.all.followImg" + t_id);
	var targetTR2 =eval("document.all.follow" + t_id);
	if (targetImg2.src.indexOf("nofollow")!=-1){return false;}
	if ("object"==typeof(targetImg2)){
		if (targetTR2.style.display!="")
		{
			targetTR2.style.display="";
			targetImg2.src="images/minus.gif";
			if (targetImg2.loaded=="no"){
				document.frames["hiddenframe"].location.replace("listtree.jsp?id="+b_id+getstr);
			}
		}else{
			targetTR2.style.display="none";
			targetImg2.src="images/plus.gif";
		}
	}
}

function form1_onsubmit()
{
	if (form1.selboard.value=="")
	{
		alert('<lt:Label res="res.label.forum.search" key="alert_board"/>');
		return false;
	}
}
</SCRIPT>
<script src="../inc/common.js"></script>
</HEAD><BODY>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="Topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<%
if (!privilege.isMasterLogin(request)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.PVG_INVALID)));
	return;
}

int pageSize = ParamUtil.getInt(request, "pageSize", 20);

String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action");
String checkStatus = ParamUtil.get(request, "checkStatus");
if (checkStatus.equals(""))
	checkStatus = "1";
String searchItem = ParamUtil.get(request, "searchItem");
if (searchItem.equals(""))
	searchItem = "topic";	

MsgMgr mm = new MsgMgr();
if (op.equals("del")) {
	String strIds = ParamUtil.get(request, "ids");
	String[] idsary = StrUtil.split(strIds, ",");
	if (idsary!=null) {
		int len = idsary.length;
		for (int i=0; i<len; i++) {
			mm.delTopic(application, request, Long.parseLong(idsary[i]));
		}
	}
	out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "topic_m.jsp?pageSize=" + pageSize + "&checkStatus=" + checkStatus));
}

if (op.equals("check")) {
	String strIds = ParamUtil.get(request, "ids");
	String[] idsary = StrUtil.split(strIds, ",");
	if (idsary!=null) {
		int len = idsary.length;
		for (int i=0; i<len; i++) {
			mm.checkMsg(request, Long.parseLong(idsary[i]), MsgDb.CHECK_STATUS_PASS);
		}
	}
	out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "topic_m.jsp?pageSize=" + pageSize + "&checkStatus=" + checkStatus + "&action=" + action));
}

if (op.equals("resume")) {
	String strIds = ParamUtil.get(request, "ids");
	String[] idsary = StrUtil.split(strIds, ",");
	if (idsary!=null) {
		int len = idsary.length;
		for (int i=0; i<len; i++) {
			mm.checkMsg(request, Long.parseLong(idsary[i]), MsgDb.CHECK_STATUS_PASS);
		}
	}
	out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "topic_m.jsp?pageSize=" + pageSize + "&checkStatus=" + checkStatus + "&action=" + action));
}

String querystring = StrUtil.getNullString(request.getQueryString());
String privurl = request.getRequestURL()+"?"+StrUtil.UrlEncode(querystring,"utf-8");

String boardcode = ParamUtil.get(request, "boardcode");
String boardname = ParamUtil.get(request, "boardname");
String timelimit = request.getParameter("timelimit");
if (timelimit==null)
	timelimit = "all";

String selboard = ParamUtil.get(request, "selboard");
String searchtype = ParamUtil.get(request, "searchtype");
String searchwhat = ParamUtil.get(request, "searchwhat");
if (selboard.equals(""))
	selboard = "allboard";
String selauthor = ParamUtil.get(request, "selauthor");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "redate";
	
try {
	com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();		
	// 防XSS
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "searchwhat", searchwhat, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "searchItem", searchItem, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "boardname", boardname, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "boardcode", boardcode, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "action", action, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}	
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><lt:Label res="res.label.forum.topic_m" key="top_manage"/></td>
  </tr>
</table>  
<CENTER>
<br>
<TABLE width="98%" border="0" align="center" cellPadding=0 cellSpacing=1 class="frame_gray" id=AutoNumber1>
    <FORM name=form1 action="topic_m.jsp" method=post onSubmit="return form1_onsubmit()">
      <TBODY>
        <TR>
          <TD height=22 colSpan=2 class="thead">
              <lt:Label res="res.label.forum.search" key="input_keywards"/>
          </TD>
        </TR>
        <TR>
          <TD height=24 align="right" bgcolor="#FFFFFF"><lt:Label res="res.label.forum.search" key="search_content"/>
          &nbsp;&nbsp;</TD>
          <TD height=24 align="left" 
    vAlign=top bgcolor="#FFFFFF">&nbsp;
            <input size=40 name=searchwhat value="<%=searchwhat%>">
            <input name=boardcode value="<%=boardcode%>" type=hidden>
            <input name=boardname value="<%=boardname%>" type=hidden>
          <input name=action value="<%=action%>" type=hidden>			</TD>
        </TR>
        <TR>
          <TD 
    height=22 bgcolor="#FFFFFF"><P align=right><SPAN style="FONT-SIZE: 9pt">
              <lt:Label res="res.label.forum.search" key="search_keywords"/>
              </SPAN>
              <INPUT type=radio value=bykey name=searchtype checked>
          &nbsp; </P></TD>
          <TD height=22 align="left" 
    vAlign=top bgcolor="#FFFFFF">&nbsp;
            <SELECT size=1 name=searchItem>
              <OPTION value=topic selected><lt:Label res="res.label.forum.search" key="search_topic_keywards"/></OPTION>
              <OPTION value=content><lt:Label res="res.label.forum.search" key="search_content_keywards"/></OPTION>
            </SELECT>
		  <script>
			form1.searchItem.value = "<%=searchItem%>";
			</script>          </TD>
        </TR>
        <TR>
          <TD 
    
    width=210 height=24 bgcolor="#FFFFFF"><P align=right><FONT style="FONT-SIZE: 9pt">
              <lt:Label res="res.label.forum.search" key="search_author"/>
              </FONT>
              <INPUT type=radio value=byauthor name=searchtype <%=searchtype.equals("byauthor")?"checked":""%>>
          &nbsp; </P></TD>
          <TD height=24 align="left" 
    vAlign=top bgcolor="#FFFFFF" 
   >&nbsp;
            <SELECT size=1 name=selauthor>
              <OPTION value=topicname selected>
              <lt:Label res="res.label.forum.search" key="topic_author"/>
              </OPTION>
              <OPTION value=replyname>
              <lt:Label res="res.label.forum.search" key="reply_author"/>
              </OPTION>
          </SELECT>          </TD>
        </TR>
        <TR>
          <TD height=24 align="right" bgcolor="#FFFFFF" 
   >按ID搜索
            <INPUT type=radio value=byId name=searchtype <%=searchtype.equals("byId")?"checked":""%>>
          &nbsp;</TD>
          <TD height=24 align="left" 
    vAlign=top bgcolor="#FFFFFF" 
   >&nbsp;</TD>
        </TR>
        <TR>
          <TD 
    
    width=210 height=23 bgcolor="#FFFFFF"><P align=right><FONT style="FONT-SIZE: 9pt" 
      color=#000000>
              <lt:Label res="res.label.forum.search" key="scope_date"/>
          &nbsp;</FONT>&nbsp; </P></TD>
          <TD height=23 align="left" 
    vAlign=top bgcolor="#FFFFFF" 
   >&nbsp;
            <SELECT size=1 name=timelimit>
              <OPTION value="all">
              <lt:Label res="res.label.forum.search" key="all_date"/>
              </OPTION>
              <OPTION value=1>
              <lt:Label res="res.label.forum.search" key="after_yestoday"/>
              </OPTION>
              <OPTION value=5 selected>
              <lt:Label res="res.label.forum.search" key="after_five_today"/>
              </OPTION>
              <OPTION value=10>
              <lt:Label res="res.label.forum.search" key="after_ten_today"/>
              </OPTION>
              <OPTION value=30>
              <lt:Label res="res.label.forum.search" key="after_30_today"/>
              </OPTION>
            </SELECT>
          <script>
			  form1.timelimit.value = "<%=timelimit%>";
			  </script></TD>
        </TR>
        <TR>
          <TD 
    
    width=210 height=23 bgcolor="#FFFFFF"><P align=right><FONT style="FONT-SIZE: 9pt" 
      color=#000000>
              <lt:Label res="res.label.forum.topic_m" key="check_status"/>
          </FONT>&nbsp;&nbsp; </P></TD>
          <TD height=23 align="left" 
    vAlign=top bgcolor="#FFFFFF" 
   >&nbsp;
            <SELECT size=1 name="checkStatus">
              <OPTION value="<%=MsgDb.CHECK_STATUS_PASS%>" selected>
              <lt:Label res="res.label.forum.topic_m" key="check_pass"/>
              </OPTION>
              <OPTION value="<%=MsgDb.CHECK_STATUS_NOT%>">
              <lt:Label res="res.label.forum.topic_m" key="check_not"/>
              </OPTION>
              <OPTION value="<%=MsgDb.CHECK_STATUS_DUSTBIN%>">
              <lt:Label res="res.label.forum.topic_m" key="check_dustbin"/>
              </OPTION>
            </SELECT>
          <script>
			  form1.checkStatus.value = "<%=checkStatus%>";
			  </script>          </TD>
        </TR>
        <TR>
          <TD height=23 align="right" bgcolor="#FFFFFF" 
   ><lt:Label res="res.label.forum.topic_batch_m" key="page_size"/>
          <FONT style="FONT-SIZE: 9pt" 
      color=#000000>&nbsp; </FONT>&nbsp;&nbsp; </TD>
          <TD height=23 align="left" 
    vAlign=top bgcolor="#FFFFFF" 
   >&nbsp;
          <input type="text" name="pageSize" value="<%=pageSize%>"/>
		  &nbsp;排序
		  <select name="orderBy">
		  <option value="lydate">发贴时间</option>
		  <option value="redate">回复时间</option>
		  </select>
		  <script>
		  form1.orderBy.value = "<%=orderBy%>";
		  </script>
		  </TD>
        </TR>
        <TR>
          <TD width=210 height=26 
    
    align=right bgcolor="#FFFFFF"><FONT 
      style="FONT-SIZE: 9pt" color=#000000>
            <lt:Label res="res.label.forum.search" key="sel_board"/>
          &nbsp;&nbsp;</FONT></TD>
          <TD height=26 align="left" 
    vAlign=center bgcolor="#FFFFFF" 
   >&nbsp;
            <select name="selboard">
              <option value="cwBlogTopic" selected>博客贴</option>
			  <%
			  Leaf groupLf = new Leaf();
			  groupLf = groupLf.getLeaf("group");
			  if (groupLf!=null) {
			  %>
			  <option value="group">朋友圈</option>
			  <%}%>
              <option value="allboard" selected>
              <lt:Label res="res.label.forum.search" key="all_board"/>
              </option>
              <%
				com.redmoon.forum.Directory dir = new com.redmoon.forum.Directory();
				com.redmoon.forum.Leaf leaf = dir.getLeaf(Leaf.CODE_ROOT);
				com.redmoon.forum.DirectoryView dv = new com.redmoon.forum.DirectoryView(leaf);
				dv.ShowDirectoryAsOptions(request, privilege, out, leaf, leaf.getLayer());
			%>
            </select>
            <script language=javascript>
		<!--
		var v = "<%=selboard%>";
		if (v!="")
			form1.selboard.value = v;
		//-->
		</script>
            &nbsp;
          <INPUT type=submit value=<lt:Label res="res.label.forum.search" key="begin_search"/> name=submit1>          </TD>
        </TR>
        <TR>
          <TD height=22 
    colSpan=2 bgcolor="#FFFFFF"></TD>
        </TR>
    </FORM>
    </TBODY>
  </TABLE>
  <BR>
  <%
String sql = "";
String myboardname = "", myboardcode = "";
if (searchtype.equals("byauthor")) {
	UserDb ud = new UserDb();
	String nicks = ud.getNicksLike(searchwhat);
	if ( selauthor.equals("topicname"))
		sql = "select id from sq_thread where name in (" + nicks + ")";
	else if ( selauthor.equals("replyname"))
		sql = "select id from sq_thread where id in (select rootid from sq_message where name in (" + nicks + ") and replyid<>-1)";
	else
		sql = "select id from sq_thread where name in (" + nicks + ")";
}
else if (searchtype.equals("bykey"))
{
	if (searchItem.equals("topic")) {
		sql = "select id from sq_message where title like " + StrUtil.sqlstr("%"+searchwhat+"%");
	}
	else {
		sql = "select id from sq_message where content like " + StrUtil.sqlstr("%"+searchwhat+"%");
	}
}
else if (searchtype.equals("byId")) {
	long searchId = StrUtil.toLong(searchwhat, -1);
	sql = "select id from sq_message where id=" + searchId;
}
else
	sql = "select id from sq_thread";
String sb="";
if (selboard.equals("allboard")) {
	if (searchtype.equals(""))
		sb = " where check_status=" + checkStatus;
	else
		sb += " and check_status=" + checkStatus;
}
else if (selboard.equals("cwBlogTopic")) {
	if (searchtype.equals(""))
		sb = " where isBlog=1";
	else
		sb += " and isBlog=1";			
}
else
	sb = " and boardcode=" + StrUtil.sqlstr(selboard) + " and check_status=" + checkStatus;;
sql += sb;
String t1 = "";
if (!timelimit.equals("all")) {
	long cur = System.currentTimeMillis();
	long dlt = (long)Integer.parseInt(timelimit)*24*60*60000;
	long afterDay = cur - dlt;
	t1 = " and lydate>" + StrUtil.sqlstr("" + afterDay);
}
else
	t1 = "";
sql += t1;
String orderby = "";
if (selboard.equals("allboard"))
	orderby = " ORDER BY " + orderBy + " desc";
else
	orderby = " ORDER BY msg_level desc," + orderBy + " desc";
sql += orderby;

Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
PageConn pageconn = new PageConn(Global.getDefaultDB(), curpage, pageSize);
ResultIterator ri = pageconn.getResultIterator(sql);
paginator.init(pageconn.getTotal(), pageSize);
ResultRecord rr = null;

//设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}
%>
  <TABLE borderColor=#edeced cellSpacing=0 cellPadding=1 width="98%" align=center 
border=1>
    <TBODY>
      <TR height=25 class="td_title">
        <TD height="26" colSpan=3 align=middle noWrap class="thead"><lt:Label res="res.label.forum.listtopic" key="topis_list"/></TD>
        <TD width=91 height="26" align=middle noWrap class="thead"><lt:Label res="res.label.forum.listtopic" key="author"/></TD>
        <TD width=55 height="26" align=middle noWrap class="thead"><lt:Label res="res.label.forum.listtopic" key="reply"/></TD>
        <TD width=55 height="26" align=middle noWrap class="thead"><lt:Label res="res.label.forum.listtopic" key="hit"/></TD>
        <TD width=80 height="26" align=middle noWrap class="thead"><lt:Label res="res.label.forum.listtopic" key="reply_date"/></TD>
        <TD width=91 height="26" align=middle noWrap class="thead"><lt:Label res="res.label.forum.mytopic" key="board"/></TD>
      </TR>
    </TBODY>
  </TABLE>
  <%
String topic = "",name="",lydate="";
int expression;
int id = -1;
int i = 0,recount=0,hit=0,type=0;
MsgDb md = new MsgDb();
Leaf myleaf = new Leaf();
com.redmoon.forum.person.UserMgr um = new com.redmoon.forum.person.UserMgr();
while (ri.hasNext()) {
	rr = (ResultRecord)ri.next(); 
	i++;
	id = rr.getInt("id");
	md = md.getMsgDb(id);
	if (!md.isLoaded()) {
	out.print("Thread " + id + " is not exist.<BR>");
		continue;
	}
	topic = md.getTitle();
	name = md.getName();
	if (orderBy.equals("lydate"))
		lydate = com.redmoon.forum.ForumSkin.formatDate(request, md.getAddDate());
	else
		lydate = com.redmoon.forum.ForumSkin.formatDate(request, md.getRedate());
	recount = md.getRecount();
	hit = md.getHit();
	expression = md.getExpression();
	type = md.getType();
	myboardcode = md.getboardcode();
	myleaf = dir.getLeaf(myboardcode);
	myboardname = "";
	if (myleaf!=null)
	  myboardname = myleaf.getName();
	  %>
  <table bordercolor=#edeced cellspacing=0 cellpadding=1 width="98%" align=center border=1>
    <tbody>
      <tr>
        <td width=30 height="22" align=middle noWrap bgcolor=#f8f8f8><input name="ids" value="<%=id%>" type="checkbox"></td>
        <td noWrap align=left width=50 bgcolor=#f8f8f8><%=md.getId()%></td>
        <td noWrap align=middle width=30 bgcolor=#f8f8f8><%if (recount>20){ %>
          <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_hot"/>" src="<%=skinPath%>/images/f_hot.gif">
          <%}
	  else if (recount>0) {%>
          <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_reply"/>" src="<%=skinPath%>/images/f_new.gif">
          <%}
	  else {%>
          <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_no_reply"/>" src="<%=skinPath%>/images/f_norm.gif">
          <%}%>
        </td>
        <td align=middle width=17 bgcolor=#ffffff><% String urlboardname = StrUtil.UrlEncode(myboardname,"utf-8"); %>
          <a href="showtopic_tree.jsp?boardcode=<%=myboardcode%>&hit=<%=(hit+1)%>&rootid=<%=id%>" target=_blank>
          <% if (type==1) { %>
          <IMG height=15 alt="" src="images/f_poll.gif" width=17 border=0>
          <%}else {
				if (expression!=MsgDb.EXPRESSION_NONE) {		  
		  %>
          <img src="images/brow/<%=expression%>.gif" border=0>
          <%	}
		  		else
					out.print("&nbsp;");
		  }
		  %>
          </a></td>
        <td onMouseOver="this.style.backgroundColor='#ffffff'" onMouseOut="this.style.backgroundColor=''" align=left bgcolor=#f8f8f8><%
		if (recount==0) {
		%>
          <img id=followImg<%=id%> title="<lt:Label res="res.label.forum.listtopic" key="no_reply"/>" src="<%=skinPath%>/images/minus.gif" loaded="no">
          <% }else { %>
          <img id=followImg<%=id%> title=<lt:Label res="res.label.forum.listtopic" key="extend_reply"/> style="CURSOR: hand" onclick="loadThreadFollow(<%=id%>,<%=id%>,'&boardcode=<%=myboardcode%>&hit=<%=hit+1%>&boardname=<%=urlboardname%>')" src="<%=skinPath%>/images/plus.gif" loaded="no">
          <% } %>
          <a target="_blank" title="<%=StrUtil.toHtml(md.getContent())%>" href="showtopic_tree.jsp?boardcode=<%=myboardcode%>&rootid=<%=md.getRootid()%>&showid=<%=md.getId()%>"><%=StrUtil.toHtml(topic)%></a>
          <%
		// 计算共有多少页回贴
		int allpages = (int)Math.ceil((double)recount/pageSize);
		if (allpages>1)
		{
		 	out.print("[");
			for (int m=1; m<=allpages; m++)
			{ %>
          <a target="_blank" title="<%=StrUtil.toHtml(md.getContent())%>" href="showtopic.jsp?boardcode=<%=myboardcode%>&hit=<%=(hit+1)%>&boardname=<%=urlboardname%>&rootid=<%=id%>&CPages=<%=m%>"><%=m%></a>
          <% }
		  	out.print("]");
		 }%>
        </td>
        <td align=middle width=91 bgcolor=#ffffff>
		<%if (name.equals("")) {%>
          <lt:Label res="res.label.forum.showtopic" key="anonym"/>
        <%}else{%>
          <a href="../userinfo.jsp?username=<%=name%>"><%=um.getUser(name).getNick()%></a>
        <%}%>
        </td>
        <td align=middle width=55 bgcolor=#f8f8f8><font color=red>[<%=recount%>]</font></td>
        <td align=middle width=55 bgcolor=#ffffff><%=hit%></td>
        <td align=left width=80 bgcolor=#f8f8f8><table cellspacing=0 cellpadding=2 width="100%" align=center border=0>
            <tbody>
              <tr>
                <td width="10%">&nbsp;</td>
                <td><%=lydate%></td>
              </tr>
            </tbody>
          </table></td>
        <td align=middle width=91 bgcolor=#ffffff>&nbsp;&nbsp;
          <%if (!myboardcode.equals(Leaf.CODE_BLOG)) {%>
          <a target=_blank href="listtopic.jsp?boardcode=<%=StrUtil.UrlEncode(myboardcode)%>"><%=myboardname%></a>&nbsp;
          <%}else{%>
          <a target=_blank href="../blog/myblog.jsp?userName=<%=StrUtil.UrlEncode(md.getName())%>"><%=myboardname%></a>
          <%}%></td>
      </tr>
      <tr id=follow<%=id%> style="DISPLAY: none">
        <td noWrap align=middle width=30 bgcolor=#f8f8f8></td>
        <td noWrap align=middle width=30 bgcolor=#f8f8f8></td>
        <td noWrap align=middle width=30 bgcolor=#f8f8f8></td>
        <td align=middle width=17 bgcolor=#ffffff></td>
        <td onMouseOver="this.style.backgroundColor='#ffffff'" 
    onMouseOut="this.style.backgroundColor=''" align=left bgcolor=#f8f8f8 colspan="6"><div id=followDIV<%=id%> 
      style="WIDTH: 100%;BACKGROUND-COLOR: lightyellow" onclick="loadThreadFollow(<%=id%>,<%=id%>,'&hit=<%=hit+1%>&boardname=<%=urlboardname%>')"><span style="WIDTH: 100%;">
            <lt:Label res="res.label.forum.listtopic" key="wait"/>
            </span></div></td>
      </tr>
      <tr>
        <td 
    style="PADDING-RIGHT: 0px; PADDING-LEFT: 0px; PADDING-BOTTOM: 0px; PADDING-TOP: 0px" 
    colspan=7></td>
      </tr>
    </tbody>
  </table>
  <%}%>
  <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
    <tr>
      <td width="51%" height="23" align="left"><input value="<lt:Label res="res.label.forum.topic_m" key="sel_all"/>" type="button" onClick="selAllCheckBox('ids')">
        &nbsp;&nbsp;
        <input value="<lt:Label res="res.label.forum.topic_m" key="clear_all"/>" type="button" onClick="clearAllCheckBox('ids')">
        &nbsp;&nbsp;
        <%if (!action.equals("sel")) {%>
        <input value="<lt:Label key="op_del"/>" type="button" onClick="doDel()">
        <%}else{%>
        <input type="button" value="<lt:Label key="ok"/>" onClick="sel()">
        <%}%>
        &nbsp;
        <input name="button" type="button" onClick="doCheck()" value="<lt:Label res="res.label.forum.topic_m" key="check_pass"/>">
        &nbsp;
        <input name="button" type="button" onClick="doResume()" value="<lt:Label res="res.label.forum.topic_m" key="resume"/>">
      </td>
      <td width="49%" align="right"><%
	  String querystr = "&pageSize=" + pageSize + "&searchtype="+searchtype+"&searchwhat="+StrUtil.UrlEncode(searchwhat,"utf-8");
	  querystr += "&selboard="+StrUtil.UrlEncode(selboard,"utf-8");
	  querystr += "&selauthor="+StrUtil.UrlEncode(selauthor,"utf-8")+"&timelimit="+timelimit + "&action=" + action;
	  querystr += "&checkStatus=" + checkStatus+"&searchItem=" + searchItem + "&orderBy=" + orderBy;
 	  out.print(paginator.getCurPageBlock(request, "?boardcode=" + boardcode + querystr));
	  %></td>
    </tr>
  </table>
  <iframe width=0 height=0 src="" id="hiddenframe"></iframe>
</CENTER>
</BODY>
<script>
function doDel() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("<lt:Label res="res.label.forum.topic_m" key="need_id"/>");
		return;
	}
	if (confirm("您确定要删除么？"))
		window.location.href = "topic_m.jsp?op=del&pageSize=<%=pageSize%>&checkStatus=<%=checkStatus%>&ids=" + ids;
}

function doCheck() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("<lt:Label res="res.label.forum.topic_m" key="need_id"/>");
		return;
	}
	window.location.href = "topic_m.jsp?op=check&action=<%=action%>&pageSize=<%=pageSize%>&checkStatus=<%=checkStatus%>&ids=" + ids;
}

function doResume() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("<lt:Label res="res.label.forum.topic_m" key="need_id"/>");
		return;
	}
	window.location.href = "topic_m.jsp?op=resume&action=<%=action%>&pageSize=<%=pageSize%>&checkStatus=<%=checkStatus%>&ids=" + ids;
}

function sel() {
	var ids = getCheckboxValue("ids");
	window.opener.selTopic(ids);
	window.close();
}

function selAllCheckBox(checkboxname){
	var checkboxboxs = document.all.item(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = true;
		}
	}
}

function clearAllCheckBox(checkboxname) {
	var checkboxboxs = document.all.item(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = false;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = false;
		}
	}
}
</script>
</HTML>