<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD><TITLE><%=Global.AppName%></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<SCRIPT>
function loadonline(boardcode){
	var targetImg =eval("document.all.followImg000");
	var targetDiv =eval("document.all.followDIV000");
	if (targetImg.src.indexOf("nofollow")!=-1){return false;}
	if ("object"==typeof(targetImg)){
		if (targetDiv.style.display!='block')
		{
			targetDiv.style.display="block";
			targetImg.src="images/minus.gif";
			advance.innerText='<lt:Label res="res.label.forum.listtopic" key="close_online"/>';
			if (targetImg.loaded=="no")
				document.frames["hiddenframe"].location.replace("online.jsp?boardcode="+boardcode);
		}
		else
		{
			targetDiv.style.display="none";
			targetImg.src="images/plus.gif";
			advance.innerText='<lt:Label res="res.label.forum.listtopic" key="show_online"/>';
		}
	}
}
////////////////////展开帖子
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
<BODY>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
String querystring = StrUtil.getNullString(request.getQueryString());
// 安全验证
if (!privilege.isUserLogin(request)) {
	response.sendRedirect("../door.jsp");
	return;
}
String username = privilege.getUser(request);
%>
<TABLE height=25 cellSpacing=0 cellPadding=1 width="98%" align=center border=1 class="tableCommon">
  <TBODY>
  <TR>
     <TD>
        <lt:Label res="res.label.forum.inc.position" key="cur_position"/>
        &nbsp;<a href="<%=request.getContextPath()%>/forum/index.jsp"><lt:Label res="res.label.forum.inc.position" key="forum_home"/></a>&nbsp;&nbsp;<B>&raquo;</B> 
        &nbsp;<a href="<%=request.getContextPath()%>/usercenter.jsp"><lt:Label res="res.label.forum.menu" key="user_center"/></a>&nbsp;&nbsp;<B>&raquo;</B> 
		<%
		String action = StrUtil.getNullString(request.getParameter("action"));
		if (action.equals("mytopic"))
			out.print(SkinUtil.LoadString(request, "res.label.forum.mytopic", "mytopic"));
		else
			out.print(SkinUtil.LoadString(request, "res.label.forum.mytopic", "myattendtopic"));
		%>
		</TD>
    </TR></TBODY></TABLE>
<BR>
<%
		String sql = "";
		String myboardname = "", myboardcode = "";
		if ( action.equals("mytopic"))
		{
			sql = "select id from sq_thread where name=" + StrUtil.sqlstr(username) + " and isBlog=0 and check_status=" + MsgDb.CHECK_STATUS_PASS;
		}
		else if ( action.equals("myreply"))
		{
			sql = "select id from sq_message where name=" + StrUtil.sqlstr(username) + " and isBlog=0 and check_status=" + MsgDb.CHECK_STATUS_PASS;
		}
		String orderby = " ORDER BY msg_level desc,lydate desc";
		sql += orderby;
		
		int pagesize = 10;
		ResultRecord rr = null;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		PageConn pageconn = new PageConn(Global.getDefaultDB(), curpage, pagesize);
		ResultIterator ri = pageconn.getResultIterator(sql);
		paginator.init(pageconn.getTotal(), pagesize);
		//设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}
%>
<TABLE class="tableCommon" cellSpacing=0 cellPadding=1 width="98%" align=center border=1>
  <thead>
  <TR>
    <TD colspan="3" align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="topis_list"/></TD>
    <TD width=91 align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="author"/></TD>
    <TD width=55 align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="reply"/></TD>
    <TD width=55 align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="hit"/></TD>
    <TD width=105 align=middle noWrap>
      <lt:Label res="res.label.forum.listtopic" key="reply_date"/>    </TD>
    <TD width=91 align=middle noWrap>
    <lt:Label res="res.label.forum.mytopic" key="board"/>    </TD>
  </TR></thead>
  <%		
String title = "",name="",lydate="";
int expression=0;
int id = -1;
int i = 0,recount=0,hit=0,type=0;
MsgDb md = new MsgDb();
Leaf myleaf = new Leaf();
UserMgr um = new UserMgr();
UserDb user = null;
while (ri.hasNext()) {
 	      rr = (ResultRecord)ri.next(); 
		  i++;
		  id = rr.getInt("id");
		  md = md.getMsgDb(id);
		  title = md.getTitle();
		  name = md.getName();
		  user = um.getUser(name);
		  lydate = com.redmoon.forum.ForumSkin.formatDateTimeShort(request, md.getAddDate());
		  recount = md.getRecount();
		  hit = md.getHit();
		  expression = md.getExpression();
		  type = md.getType();
		  myboardcode = md.getboardcode();
		  myleaf = myleaf.getLeaf(myboardcode);
		  myboardname = myleaf.getName();
	  %>
    <tbody> 
    <tr> 
        <td noWrap align=middle> 
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
        <td align=middle> 
          <% String urlboardname = StrUtil.UrlEncode(myboardname,"utf-8"); %>
		   <a href="showtopic_tree.jsp?boardcode=<%=myboardcode%>&hit=<%=(hit+1)%>&boardname=<%=urlboardname%>&showid=<%=id%>&rootid=<%=md.getRootid()%>" target=_blank> 
          <% if (type==1) { %>
          <IMG height=15 alt="" src="images/f_poll.gif" width=17 border=0>
		  <%}else { 
		  	if (expression!=MsgDb.EXPRESSION_NONE) {
		  %>
		  <img src="images/brow/<%=expression%>.gif" border=0>
		  <%}
		  }%>
		  </a></td>
        <td onMouseOver="this.style.backgroundColor='#ffffff'" 
    onMouseOut="this.style.backgroundColor=''" align=left> 
        <%
		if (recount==0) {
		%>
          <img id=followImg<%=id%> title="<lt:Label res="res.label.forum.listtopic" key="no_reply"/>" src="images/minus.gif" loaded="no"> 
          <%}else{%>
          <img id=followImg<%=id%> title=<lt:Label res="res.label.forum.listtopic" key="extend_reply"/> style="CURSOR: hand" 
      onclick="loadThreadFollow(<%=id%>,<%=id%>,'&boardcode=<%=myboardcode%>&hit=<%=hit+1%>&boardname=<%=urlboardname%>')" src="images/plus.gif" loaded="no"> 
          <%}%>
          <a href="showtopic_tree.jsp?boardcode=<%=myboardcode%>&hit=<%=(hit+1)%>&showid=<%=id%>&rootid=<%=md.getRootid()%>"><%=title%></a>
          <%
		//计算共有多少页回贴
		int allpages = (int)Math.ceil((double)recount/pagesize);
		if (allpages>1)	{
		 	out.print("[");
			for (int m=1; m<=allpages; m++)	{%>
          <a href="showtopic.jsp?boardcode=<%=myboardcode%>&hit=<%=(hit+1)%>&rootid=<%=id%>&CPages=<%=m%>"><%=m%></a> 
          <%}
		  	out.print("]");
		 }%>
        </td>
      <td align=middle width=91> 
	  	  <% if (privilege.getUser(request).equals(name)) { %>
          <IMG height=14 src="images/my.gif" 
            width=14>
	  <% } %>
	  <a href="../userinfo.jsp?username=<%=name%>"><%=user.getNick()%></a> 
      </td>
        <td align=middle>[<%=recount%>]</td>
        <td align=middle><%=hit%></td>
      <td align=left> 
        <%=lydate%>
      </td>
      <td align=middle><a href="listtopic.jsp?boardcode=<%=myboardcode%>&boardname=<%=StrUtil.UrlEncode(myboardname,"utf-8")%>"><%=myboardname%></a>&nbsp;</td>
    </tr>
    <tr id=follow<%=id%> style="DISPLAY: none"> 
      <td noWrap align=middle width=30>&nbsp;</td>
      <td align=middle width=17>&nbsp;</td>
      <td align=left colspan="6">
	 <div id=followDIV<%=id%> 
      style="WIDTH: 100%" 
      onclick="loadThreadFollow(<%=id%>,<%=id%>,'&hit=<%=hit+1%>&boardname=<%=urlboardname%>')"><span style="WIDTH: 100%">
	   <lt:Label res="res.label.forum.listtopic" key="wait"/>
	 </span></div>
	</td>
    </tr>
    </tbody> 
<%}%>
  </table>
  <table class="per98" border="0" cellspacing="1" cellpadding="3" align="center">
    <tr> 
      <td height="23" align="right"> 
	  <%
	  String querystr = "action="+action;
 	  out.print(paginator.getCurPageBlock(request, "mytopic.jsp?"+querystr));
	  %>
	  </td>
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
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</BODY></HTML>
