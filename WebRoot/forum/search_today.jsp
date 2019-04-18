<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="org.jdom.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.redmoon.forum.OnlineInfo"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="com.redmoon.forum.person.UserSet"%>
<%@ page import="com.redmoon.forum.ui.*"%>
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
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<title><%=Global.AppName%> - <lt:Label res="res.label.forum.search" key="search_result"/></title>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<%@ include file="../inc/nocache.jsp"%>
<link href="<%=skinPath%>/skin.css" rel="stylesheet" type="text/css">
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
</SCRIPT>
<META content="MSHTML 6.00.2600.0" name=GENERATOR></HEAD>
<BODY>
<%@ include file="inc/header.jsp"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="Topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<%
if (!privilege.isUserLogin(request)) {
	response.sendRedirect("../door.jsp");
	return;
}

String querystring = StrUtil.getNullString(request.getQueryString());
String privurl = request.getRequestURL()+"?"+StrUtil.UrlEncode(querystring,"utf-8");

String boardcode = ParamUtil.get(request, "boardcode");
String boardname = ParamUtil.get(request, "boardname");
String timelimit = request.getParameter("timelimit");
if (timelimit==null)
	timelimit = "all";
%>
<CENTER>
  <TABLE borderColor=#edeced height=25 cellSpacing=0 cellPadding=1 rules=rows 
width="98%" align=center bgColor=#ffffff border=1 class="table_normal">
  <TBODY>
  <TR>
        <TD>&nbsp;<img src="images/userinfo.gif" width="9" height="9">&nbsp;<a>
          <lt:Label res="res.label.forum.inc.position" key="cur_position"/>
        </a>&nbsp;<a href="<%=request.getContextPath()%>/forum/index.jsp"><lt:Label res="res.label.forum.inc.position" key="forum_home"/></a>&nbsp;&nbsp;<B>&raquo;</B> 
        <lt:Label res="res.label.forum.listtopic" key="search_result"/> &nbsp;</TD>
    <TD align=right>
	
  </TD></TR></TBODY></TABLE><BR>
  <%
		String sql = "";
		String searchtype = "";
		String searchwhat = "";
		String selboard = "";
		String selauthor = "";
		String myboardname = "", myboardcode = "";
				String t1 = "";
		/*	
		String searchtype = StrUtil.getNullString(request.getParameter("searchtype"));
		String searchwhat = ParamUtil.get(request, "searchwhat");
		String selboard = ParamUtil.get(request, "selboard");
		if (selboard.equals(""))
			selboard = "allboard";
		String selauthor = ParamUtil.get(request, "selauthor");
		String myboardname = "", myboardcode = "";
		if (searchtype.equals("byauthor"))
		{
			if ( selauthor.equals("topicname"))
				sql = "select id from sq_thread where name like " + StrUtil.sqlstr("%"+searchwhat+"%");
			else if ( selauthor.equals("replyname"))
				sql = "select id from sq_thread where id in (select rootid from sq_message where replyid<>-1 and name like " + StrUtil.sqlstr("%"+searchwhat+"%") + ")";
			else
				sql = "select id from sq_thread where name like " + StrUtil.sqlstr("%"+searchwhat+"%");
		}
		else if (searchtype.equals("bykey"))
		{
			sql = "select id from sq_message where replyid=-1 and title like " + StrUtil.sqlstr("%"+searchwhat+"%");
		}
		else
			sql = "select id from sq_thread";
		String sb="";
		if (selboard.equals("allboard"))
			sb = "";
		else
			sb = " and boardcode=" + StrUtil.sqlstr(selboard);
		sql += sb;

		*/
		sql = "select id from sq_message where ";
		//if (!timelimit.equals("all"))
		//{
			// tl = " and TO_DAYS(NOW()) - TO_DAYS(lydate) <=" + timelimit;
		    long cur = System.currentTimeMillis();	
		
			java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			java.util.Date date1 = format.parse("2006-11-20 00:00:00");
            long dlt = date1.getTime();

			out.print(cur);
			out.print(dlt);
			//long afterDay = cur - dlt;
			t1 = "lydate>" + StrUtil.sqlstr("" + dlt);
		//}
		//else
			//t1 = "";
		sql += t1;
		String orderby = "";
		if (selboard.equals("allboard"))
			orderby = " ORDER BY lydate desc";
		else
			orderby = " ORDER BY msg_level desc,lydate desc";
		sql += orderby;

        out.print(sql);
		int pagesize = 10;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		PageConn pageconn = new PageConn(Global.getDefaultDB(), curpage, pagesize);
		ResultIterator ri = pageconn.getResultIterator(sql);
		paginator.init(pageconn.getTotal(), pagesize);
		
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
  <TR height=25>
    <TD height="26" colSpan=3 align=middle noWrap background="<%=skinPath%>/images/bg1.gif" class="text_title"><lt:Label res="res.label.forum.listtopic" key="topis_list"/></TD>
    <TD width=91 height="26" align=middle noWrap background="<%=skinPath%>/images/bg1.gif" class="text_title"><lt:Label res="res.label.forum.listtopic" key="author"/></TD>
    <TD width=55 height="26" align=middle noWrap background="<%=skinPath%>/images/bg1.gif" class="text_title"><lt:Label res="res.label.forum.listtopic" key="reply"/></TD>
    <TD width=55 height="26" align=middle noWrap background="<%=skinPath%>/images/bg1.gif" class="text_title"><lt:Label res="res.label.forum.listtopic" key="hit"/></TD>
    <TD width=80 height="26" align=middle noWrap background="<%=skinPath%>/images/bg1.gif" class="text_title"><lt:Label res="res.label.forum.listtopic" key="reply_date"/></TD>
        <TD width=91 height="26" align=middle noWrap background="<%=skinPath%>/images/bg1.gif" class="text_title"><lt:Label res="res.label.forum.mytopic" key="board"/></TD>
  </TR>
  </TBODY></TABLE>
<%
String topic = "",name="",lydate="",expression="";
int id = -1;
int i = 0,recount=0,hit=0,type=0;
MsgDb md = new MsgDb();
Leaf myleaf = new Leaf();
Directory dir = new Directory();
while (ri.hasNext()) {
	 	  rr = (ResultRecord)ri.next(); 
		  i++;
		  id = rr.getInt("id");
		  md = md.getMsgDb(id);
		  topic = md.getTitle();
		  name = md.getName();
		  lydate = com.redmoon.forum.ForumSkin.formatDateTime(request, md.getAddDate());
		  recount = md.getRecount();
		  hit = md.getHit();
		  expression = "" + md.getExpression();
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
        <td noWrap align=middle width=30 bgcolor=#f8f8f8> 
      <%if (recount>20){ %>
          <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_hot"/>" src="images/f_hot.gif"> 
          <%}
	  else if (recount>0) {%>
          <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_reply"/>" src="images/f_new.gif"> 
          <%}
	  else {%>
          <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_no_reply"/>" src="images/f_norm.gif"> 

          <%}%>
	    </td>
        <td align=middle width=17 bgcolor=#ffffff> 
          <% String urlboardname = StrUtil.UrlEncode(myboardname,"utf-8"); %>
		   <a href="showtopictree.jsp?boardcode=<%=myboardcode%>&hit=<%=(hit+1)%>&rootid=<%=id%>" target=_blank> 
          <% if (type==1) { %>
          <IMG height=15 alt="" src="images/f_poll.gif" width=17 border=0>
		  <%}else { %>
		  <img src="images/brow/<%=expression%>.gif" border=0>
		  <%}%>
		  </a></td>
        <td onMouseOver="this.style.backgroundColor='#ffffff'" onMouseOut="this.style.backgroundColor=''" align=left bgcolor=#f8f8f8> 
        <%
		if (recount==0) {
		%>
          <img id=followImg<%=id%> title="<lt:Label res="res.label.forum.listtopic" key="no_reply"/>" src="images/minus.gif" loaded="no"> 
          <% }else { %>
          <img id=followImg<%=id%> title=<lt:Label res="res.label.forum.listtopic" key="extend_reply"/> style="CURSOR: hand" 
      onclick="loadThreadFollow(<%=id%>,<%=id%>,'&boardcode=<%=myboardcode%>&hit=<%=hit+1%>&boardname=<%=urlboardname%>')" src="images/plus.gif" loaded="no"> 
          <% } %>
          <a href="showtopictree.jsp?boardcode=<%=myboardcode%>&hit=<%=(hit+1)%>&rootid=<%=id%>"><%=topic%></a>
          <%
		// 计算共有多少页回贴
		int allpages = (int)Math.ceil((double)recount/pagesize);
		if (allpages>1)
		{
		 	out.print("[");
			for (int m=1; m<=allpages; m++)
			{ %>
          <a href="showtopic.jsp?boardcode=<%=myboardcode%>&hit=<%=(hit+1)%>&boardname=<%=urlboardname%>&rootid=<%=id%>&CPages=<%=m%>"><%=m%></a> 
          <% }
		  	out.print("]");
		 }%>
        </td>
      <td align=middle width=91 bgcolor=#ffffff> 
	  	  <% if (privilege.getUser(request).equals(name)) { %>
          <IMG height=14 src="images/my.gif" width=14>
	  <% } %>
	  <a href="../userinfo.jsp?username=<%=name%>"><%=name%></a> 
      </td>
        <td align=middle width=55 bgcolor=#f8f8f8><font color=red>[<%=recount%>]</font></td>
        <td align=middle width=55 bgcolor=#ffffff><%=hit%></td>
      <td align=left width=80 bgcolor=#f8f8f8> 
        <table cellspacing=0 cellpadding=2 width="100%" align=center border=0>
          <tbody> 
          <tr> 
            <td width="10%">&nbsp;</td>
            <td><%=lydate%></td>
          </tr>
          </tbody> 
        </table>
      </td>
      <td align=middle width=91 bgcolor=#ffffff>&nbsp;
	  <%if (!myboardcode.equals(Leaf.CODE_BLOG)) {%>
	  <a href="listtopic.jsp?boardcode=<%=StrUtil.UrlEncode(myboardcode)%>"><%=myboardname%></a>&nbsp;
	  <%}else{%>
	  <a href="../blog/myblog.jsp?userName=<%=StrUtil.UrlEncode(md.getName())%>"><%=myboardname%></a>
	  <%}%>
	  </td>
    </tr>
    <tr id=follow<%=id%> style="DISPLAY: none"> 
      <td noWrap align=middle width=30 bgcolor=#f8f8f8>&nbsp;</td>
      <td align=middle width=17 bgcolor=#ffffff>&nbsp;</td>
      <td onMouseOver="this.style.backgroundColor='#ffffff'" 
    onMouseOut="this.style.backgroundColor=''" align=left bgcolor=#f8f8f8 colspan="6">
	 <div id=followDIV<%=id%> 
      style="WIDTH: 100%;BACKGROUND-COLOR: lightyellow" 
      onclick="loadThreadFollow(<%=id%>,<%=id%>,'&hit=<%=hit+1%>&boardname=<%=urlboardname%>')"><span style="WIDTH: 100%;">
	   <lt:Label res="res.label.forum.listtopic" key="wait"/>
	 </span></div>
</td>
    </tr>
    <tr> 
      <td 
    style="PADDING-RIGHT: 0px; PADDING-LEFT: 0px; PADDING-BOTTOM: 0px; PADDING-TOP: 0px" 
    colspan=5> 
      </td>
    </tr>
    </tbody> 
  </table>
<%}%>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
    <tr> 
      <td width="13%" height="23"><select name="selboard" onChange="if(this.options[this.selectedIndex].value!=''){location='listtopic.jsp?' + this.options[this.selectedIndex].value;}">
        <option value="" selected>
          <lt:Label res="res.label.forum.listtopic" key="sel_board"/>
          </option>
        <%
LeafChildrenCacheMgr dlcm = new LeafChildrenCacheMgr("root");
java.util.Vector vt = dlcm.getChildren();
Iterator ir = vt.iterator();
while (ir.hasNext()) {
	Leaf leaf = (Leaf) ir.next();
	String parentCode = leaf.getCode();
	if (leaf.getIsHome()) {
%>
        <option style="BACKGROUND-COLOR: #f8f8f8" value="">╋ <%=leaf.getName()%></option>
        <%
	LeafChildrenCacheMgr dl = new LeafChildrenCacheMgr(parentCode);
	java.util.Vector v = dl.getChildren();
	Iterator ir1 = v.iterator();
	while (ir1.hasNext()) {
		Leaf lf = (Leaf) ir1.next();
%>
        <option value="boardcode=<%=StrUtil.UrlEncode(lf.getCode(),"utf-8")%>">　├『<%=lf.getName()%>』</option>
        <%}
	}
}%>
      </select>
	  <%if (!boardcode.equals("")) {%>
		 <script language=javascript>
		<!--
		var v = "boardcode=<%=StrUtil.UrlEncode(boardcode,"utf-8")%>";
		selboard.value = v;
		//-->
		</script>
	  <%}%>
	  </td>
      <td height="23" align="right" valign="baseline"> 
	  <%
	  String querystr = "&searchtype="+searchtype+"&searchwhat="+StrUtil.UrlEncode(searchwhat,"utf-8");
	  querystr += "&selboard="+StrUtil.UrlEncode(selboard,"utf-8");
	  querystr += "&selauthor="+StrUtil.UrlEncode(selauthor,"utf-8")+"&timelimit="+timelimit;
 	  out.print(paginator.getCurPageBlock(request, "search_do.jsp?boardcode=" + boardcode + "&boardname=" + StrUtil.UrlEncode(boardname,"utf-8") + querystr));
	  %>   </td>
    </tr>
</table>            
  <TABLE cellSpacing=0 cellPadding=0 width="98%" border=0>
  <TBODY>
  <TR>
    <TD><TABLE width="100%" border=0 align="center" 
      cellPadding=0 cellSpacing=4 borderColor=#111111 style="BORDER-COLLAPSE: collapse">
      <TBODY>
        <TR>
          <TD noWrap width=200><IMG height=12 alt="" 
            src="<%=skinPath%>/images/f_new.gif" width=18 border=0>&nbsp;
              <lt:Label res="res.label.forum.listtopic" key="topic_reply"/></TD>
          <TD noWrap width=100><IMG height=12 alt="" 
            src="<%=skinPath%>/images/f_hot.gif" width=18 border=0>&nbsp;
              <lt:Label res="res.label.forum.listtopic" key="topic_hot"/>
          </TD>
          <TD noWrap width=100><IMG height=15 alt="" 
            src="<%=skinPath%>/images/f_locked.gif" width=17 border=0>&nbsp;
              <lt:Label res="res.label.forum.listtopic" key="topic_lock"/></TD>
          <TD noWrap width=150><IMG src="images/topicgood.gif">
              <lt:Label res="res.label.forum.listtopic" key="topic_elite"/></TD>
          <TD noWrap width=150><IMG height=15 alt="" src="images/top_forum.gif" width=15 border=0>&nbsp;
              <lt:Label res="res.label.forum.listtopic" key="topic_all_top"/></TD>
        </TR>
        <TR>
          <TD noWrap width=200><IMG height=12 alt="" 
            src="<%=skinPath%>/images/f_norm.gif" width=18 border=0>&nbsp;
              <lt:Label res="res.label.forum.listtopic" key="topic_no_reply"/></TD>
          <TD noWrap width=100><IMG height=15 alt="" 
            src="<%=skinPath%>/images/f_poll.gif" width=17 border=0>&nbsp;
              <lt:Label res="res.label.forum.listtopic" key="topic_vote"/></TD>
          <TD noWrap width=100><IMG height=15 alt="" 
            src="<%=skinPath%>/images/f_top.gif" width=15 border=0>&nbsp;
              <lt:Label res="res.label.forum.listtopic" key="topic_top"/></TD>
          <TD noWrap width=150><IMG height=14 src="<%=skinPath%>/images/my.gif" 
            width=14>
              <lt:Label res="res.label.forum.listtopic" key="topic_my"/></TD>
          <TD noWrap width=150>&nbsp;</TD>
        </TR>
      </TBODY>
    </TABLE></TD>
    </TR></TBODY></TABLE>
</CENTER>
<%@ include file="inc/footer.jsp"%>
</BODY></HTML>
