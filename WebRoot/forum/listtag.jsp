<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="org.jdom.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);

String userName = ParamUtil.get(request, "userName");
String tagName = "";
TagDb td = new TagDb();
long tagId = -1;
if (userName.equals("")) {
	tagId = ParamUtil.getLong(request, "tagId");
	td = td.getTagDb(tagId);
	tagName = td.getString("name");
}
else
	tagName = SkinUtil.LoadString(request, "res.label.forum.showtopic", "tag_mine");
com.redmoon.forum.Config cfg1 = com.redmoon.forum.Config.getInstance();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=tagName%> - <lt:Label res="res.label.forum.showtopic" key="tag"/> - <%=Global.AppName%></title>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
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
</head>
<BODY>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<%@ include file="inc/position.jsp"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="Topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<TABLE class="tableCommon">
  <TBODY>
  <TR>
    <TD><img src="images/userinfo.gif" width="9" height="9">&nbsp;
          <lt:Label res="res.label.forum.inc.position" key="cur_position"/>
        &nbsp;<a href="<%=request.getContextPath()%>/forum/tag.jsp"><lt:Label res="res.label.forum.showtopic" key="tag"/></a>&nbsp;&nbsp;<B>&raquo;</B>&nbsp;<%=tagName%>  </TD>
    </TR></TBODY></TABLE>
<BR>
  <%
  		TagMsgDb tmd = new TagMsgDb();

		int showPageSize = cfg1.getIntProperty("forum.showTopicPageSize");

		int pagesize = showPageSize;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		
		ListResult lr = null;
		QObjectBlockIterator qi = null;
		long total = 0;
		if (userName.equals("")) {
			String sql = "select tag_id, msg_id from " + tmd.getTable().getName() + " where tag_id=" + tagId + " order by create_date desc";
			total = tmd.getQObjectCount(sql);
			qi = tmd.getQObjects(sql, (curpage-1)*pagesize, curpage*pagesize);
		}
		else {
			String sql = "select tag_id, msg_id from " + tmd.getTable().getName() + " where user_name=" + StrUtil.sqlstr(userName) + " order by create_date desc";
			total = tmd.getQObjectCount(sql);
			qi = tmd.getQObjects(sql, (curpage-1)*pagesize, curpage*pagesize);
		}
		paginator.init(total, pagesize);
				
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
%>
  <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="per100">
    <tr>
      <td width="13%" height="23">&nbsp;</td>
      <td height="23" align="right" valign="baseline"><%
	  String querystr = "?userName=" + StrUtil.UrlEncode(userName) + "&tagId=" + tagId;
 	  out.print(paginator.getCurPageBlock(request, "listtag.jsp" + querystr, "up"));
	  %>
      </td>
    </tr>
  </table>
  <TABLE class="tableCommon" cellSpacing=0 cellPadding=1 width="98%" align=center>
  <thead>
  <TR>
    <TD colSpan=3 align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="topis_list"/></TD>
    <TD width=103 align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="author"/></TD>
    <TD width=42 align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="reply"/></TD>
    <TD width=35 align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="hit"/></TD>
    <TD width=105 align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="reply_date"/></TD>
    <TD width=158 align=middle noWrap><lt:Label res="res.label.forum.mytopic" key="board"/></TD>
  </TR>
  </thead>
<%
String topic = "",name="",lydate="",myboardcode="", myboardname="";
int expression = 0;
long id = -1;
int i = 0,recount=0,hit=0,type=0;
MsgDb md = new MsgDb();
Leaf myleaf = new Leaf();
Directory dir = new Directory();
UserMgr um = new UserMgr();
while (qi.hasNext()) {
	 	  tmd = (TagMsgDb)qi.next(); 
		  i++;
		  id = tmd.getLong("msg_id");
		  md = md.getMsgDb(id);
		  topic = md.getTitle();
		  name = md.getName();
		  lydate = com.redmoon.forum.ForumSkin.formatDateTime(request, md.getAddDate());
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
    <tbody> 
    <tr> 
      <td noWrap align=middle width=30> 
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
        <td align=middle width=36> 
          <% String urlboardname = StrUtil.UrlEncode(myboardname,"utf-8"); %>
		   <a href="showtopic_tree.jsp?boardcode=<%=myboardcode%>&hit=<%=(hit+1)%>&rootid=<%=id%>" target=_blank> 
          <% if (type==1) { %>
          <IMG height=15 alt="" src="images/f_poll.gif" width=17 border=0>
		  <%}else{
		  		if (expression!=MsgDb.EXPRESSION_NONE) {		  
		  %>
				  <img src="images/brow/<%=expression%>.gif" border=0>
		  <%	}
		  		else
					out.print("&nbsp;");
		  }%>
		  </a></td>
        <td width="413" align=left> 
        <%
		if (recount==0) {
		%>
          <img id=followImg<%=id%> title="<lt:Label res="res.label.forum.listtopic" key="no_reply"/>" src="images/minus.gif" loaded="no"> 
          <% }else { %>
          <img id=followImg<%=id%> title=<lt:Label res="res.label.forum.listtopic" key="extend_reply"/> style="CURSOR: hand" 
      onclick="loadThreadFollow(<%=id%>,<%=id%>,'&boardcode=<%=myboardcode%>&hit=<%=hit+1%>&boardname=<%=urlboardname%>')" src="images/plus.gif" loaded="no"> 
          <% } %>
		  <%if (md.isBlog()) {%>
          <a href="../blog/showblog.jsp?rootid=<%=id%>"><%=StrUtil.toHtml(topic)%></a>
		  <%}else{%>
          <a href="showtopic.jsp?rootid=<%=id%>"><%=StrUtil.toHtml(topic)%></a>
		  <%}%>
          <%
		// 计算共有多少页回贴
		int allpages = (int)Math.ceil((double)recount/showPageSize);
		if (allpages>1)	{
		 	out.print("[");
			for (int m=1; m<=allpages; m++)
			{ %>
          <a href="showtopic.jsp?boardcode=<%=myboardcode%>&hit=<%=(hit+1)%>&boardname=<%=urlboardname%>&rootid=<%=id%>&CPages=<%=m%>"><%=m%></a> 
          <%}
		  	out.print("]");
		 }%>
        </td>
      <td align=middle> 
	  <% if (privilege.getUser(request).equals(name)) {%>
          <IMG height=14 src="images/my.gif" width=14>
	  <%}%>
	  <a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>"><%=um.getUser(name).getNick()%></a> 
      </td>
      <td align=middle>[<%=recount%>]</td>
      <td align=middle><%=hit%></td>
      <td align=left><%=lydate%></td>
      <td align=middle>&nbsp;
	  <%if (!myboardcode.equals(Leaf.CODE_BLOG)) {%>
	  <a href="listtopic.jsp?boardcode=<%=StrUtil.UrlEncode(myboardcode)%>"><%=myboardname%></a>&nbsp;
	  <%}else{%>
	  <a href="../blog/myblog.jsp?blogId=<%=md.getBlogId()%>"><%=myboardname%></a>
	  <%}%>
	  </td>
    </tr>
    <tr id=follow<%=id%> style="DISPLAY: none"> 
      <td noWrap align=middle>&nbsp;</td>
      <td align=middle>&nbsp;</td>
      <td align=left colspan="6">
	 <div id="followDIV<%=id%>" onClick="loadThreadFollow(<%=id%>,<%=id%>,'&hit=<%=hit+1%>&boardname=<%=urlboardname%>')">
	   <lt:Label res="res.label.forum.listtopic" key="wait"/>
	 </div>
	</td>
    </tr>
    </tbody> 
<%}%>
</table>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="per100">
    <tr> 
      <td width="13%" height="23">&nbsp;</td>
      <td height="23" align="right" valign="baseline"> 
	  <%
 	  out.print(paginator.getCurPageBlock(request, "listtag.jsp" + querystr, "down"));
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
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</BODY></HTML>
