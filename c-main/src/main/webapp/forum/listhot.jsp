<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="org.jdom.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.canUserDo(request, "", "search")) {
	response.sendRedirect("../info.jsp?info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<title><lt:Label res="res.label.forum.index" key="hot_topic"/> - <%=Global.AppName%></title>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<script src="../inc/common.js"></script>
<SCRIPT>
// 展开帖子
function loadThreadFollow(b_id,t_id,getstr){
	var targetImg2=$("followImg" + t_id);
	var targetTR2=$("follow" + t_id);
	if (targetImg2.src.indexOf("nofollow")!=-1){return false;}
	if ("object"==typeof(targetImg2)){
		if (targetTR2.style.display!=""){
			targetTR2.style.display="";
			targetImg2.src="images/minus.gif";
			if (targetImg2.getAttribute("loaded")=="no"){
				var frm = document.getElementById("hiddenframe");
				frm.contentWindow.location.replace("listtree.jsp?id="+b_id+getstr);
			}
		}else{
			targetTR2.style.display="none";
			targetImg2.src="images/plus.gif";
		}
	}
}
</SCRIPT>
</HEAD>
<BODY>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="Topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<%
// 检查是否处于可搜索时间段
TimeConfig tcsearch = new TimeConfig();
if (tcsearch.isSearchForbidden(request)) {
    out.print(SkinUtil.makeErrMsg(request, StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.search", "time_forbid_search"), 
           new Object[] {tc.getProperty("forbidSearchTime")})));
	return;
}

com.redmoon.forum.Config cfg1 = com.redmoon.forum.Config.getInstance();

Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

// 检查搜索次数
int topicSearchInterval = cfg1.getIntProperty("forum.topicSearchInterval");

String result = ParamUtil.get(request, "result");

if (topicSearchInterval>0 && !result.equals("y")) {
	Long ltime = (Long)session.getAttribute("lastSearchTime");
	if (ltime!=null) {
		long lastSearchTime = ltime.longValue();
		if ((System.currentTimeMillis()-lastSearchTime)/1000 <= topicSearchInterval) {
			out.print(SkinUtil.makeErrMsg(request, StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.search", "search_too_quick"), new Object[]{"" + topicSearchInterval})));
			out.print(SkinUtil.LoadString(request, "link_back"));
			return;
		}
	}
	session.setAttribute("lastSearchTime", new Long(System.currentTimeMillis()));
}

String querystring = StrUtil.getNullString(request.getQueryString());
String privurl = request.getRequestURL()+"?"+StrUtil.UrlEncode(querystring,"utf-8");
%>
<TABLE height=25 width="98%" class="tableCommon">
  <TBODY>
  <TR>
	<TD>&nbsp;<img src="images/userinfo.gif" width="9" height="9">&nbsp;<a>
	  <lt:Label res="res.label.forum.inc.position" key="cur_position"/>
	</a>&nbsp;<a href="<%=request.getContextPath()%>/forum/index.jsp"><lt:Label res="res.label.forum.inc.position" key="forum_home"/>
	</a>&nbsp;<B>&raquo;</B>&nbsp;
	<lt:Label res="res.label.forum.index" key="hot_topic"/></TD>
  </TR></TBODY></TABLE>
  <%
	VisitTopicLogDb bvld = new VisitTopicLogDb();
	String sql = "select topic_id, count(*) as s from " + bvld.getTable().getName() + " where boardcode<>" + StrUtil.sqlstr(Leaf.CODE_BLOG);
	sql += " and add_date>=? group by topic_id order by s desc";

	java.util.Date d = DateUtil.addDate(new java.util.Date(), -30);
	
	int pagesize = 10;
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = jt.executeQuery(sql, new Object[]{d});
	long total = ri.size();
	ri = jt.executeQuery(sql, new Object[] {d}, total, curpage, pagesize);

	paginator.init(total, pagesize);
	
	ResultRecord rr = null;
	
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0) {
		curpage = 1;
		totalpages = 1;
	}
%><br />
<TABLE class="tableCommon" width="98%">
  <thead>
  <TR>
    <TD colspan="3" height="26" align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="topis_list"/></TD>
    <TD width=105 height="26" align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="author"/></TD>
    <TD width=51 height="26" align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="reply"/></TD>
    <TD width=36 height="26" align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="hit"/></TD>
    <TD width=114 height="26" align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="reply_date"/></TD>
    <TD width=120 height="26" align=middle noWrap><lt:Label res="res.label.forum.mytopic" key="board"/></TD>
  </TR>
  </thead>
<%
String name="",lydate="";
int expression;
long id = -1;
int i = 0,recount=0,hit=0,type=0;
String myboardcode,myboardname;
MsgDb md = new MsgDb();
Leaf myleaf = new Leaf();
Directory dir = new Directory();
UserMgr um = new UserMgr();
int showPageSize = cfg1.getIntProperty("forum.showTopicPageSize");
while (ri.hasNext()) {
	rr = (ResultRecord)ri.next();
	i++;
	id = rr.getLong(1);
	md = md.getMsgDb(id);
	if (!md.isLoaded())
		continue;
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
        <td align=center width=36> 
          <% String urlboardname = StrUtil.UrlEncode(myboardname,"utf-8"); %>
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
        <td width="414" align=left style="padding-left:3px" onMouseOver="this.style.backgroundColor='#ffffff'" onMouseOut="this.style.backgroundColor=''"> 
        <%
		if (recount==0) {
		%>
          <img id=followImg<%=id%> title="<lt:Label res="res.label.forum.listtopic" key="no_reply"/>" src="images/minus.gif" loaded="no"> 
          <% }else { %>
          <img id=followImg<%=id%> title="<lt:Label res="res.label.forum.listtopic" key="extend_reply"/>" style="CURSOR: pointer" 
      onclick="loadThreadFollow(<%=id%>,<%=id%>,'&boardcode=<%=myboardcode%>')" src="images/plus.gif" loaded="no"> 
          <% } %>
          <a href="showtopic_tree.jsp?rootid=<%=id%>"><%=DefaultRender.RenderFullTitle(request, md)%></a>
          <%
		// 计算共有多少页回贴
		int allpages = (int)Math.ceil((double)recount/showPageSize);
		if (allpages>1) {
		 	out.print("[");
			for (int m=1; m<=allpages; m++)
			{ %>
          <a href="showtopic.jsp?rootid=<%=id%>&CPages=<%=m%>"><%=m%></a> 
          <% }
		  	out.print("]");
		 }%>
        </td>
      <td align=middle width=105> 
	  	  <% if (privilege.getUser(request).equals(name)) { %>
          <IMG height=14 src="images/my.gif" width=14>
	  <%}%>
	  <%if (name.equals("")) {%>
          <lt:Label res="res.label.forum.showtopic" key="anonym"/>
      <%}else{%>
	  <a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>"><%=um.getUser(name).getNick()%></a>
	  <%}%>
      </td>
        <td align=middle width=51><font color=red>[<%=recount%>]</font></td>
        <td align=middle width=36><%=hit%></td>
      <td align=left width=114> 
		<%=lydate%>
      </td>
      <td align=middle width=120>&nbsp;
	  <%if (!myboardcode.equals(Leaf.CODE_BLOG)) {%>
	  <a href="listtopic.jsp?boardcode=<%=StrUtil.UrlEncode(myboardcode)%>"><%=myboardname%></a>&nbsp;
	  <%}else{%>
	  <a href="../blog/myblog.jsp?userName=<%=StrUtil.UrlEncode(md.getName())%>"><%=myboardname%></a>
	  <%}%>
	  </td>
    </tr>
    <tr id=follow<%=id%> style="DISPLAY: none"> 
      <td noWrap align=middle width=30>&nbsp;</td>
      <td align=middle width=36>&nbsp;</td>
      <td onMouseOver="this.style.backgroundColor='#ffffff'" 
    onMouseOut="this.style.backgroundColor=''" align=left colspan="6">
	 <div id=followDIV<%=id%> onclick="loadThreadFollow(<%=id%>,<%=id%>,'')"><span style="WIDTH: 100%;">
	   <lt:Label res="res.label.forum.listtopic" key="wait"/>
	 </span></div>
	</td>
    </tr>
    </tbody> 
<%}%>
</table>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
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
	if (leaf.isDisplay(request, privilege)) {
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
	  </td>
      <td height="23" align="right" valign="baseline"> 
	  <%
	  String querystr = "result=y";
 	  out.print(paginator.getCurPageBlock(request, "listhot.jsp?" + querystr));
	  %></td>
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
