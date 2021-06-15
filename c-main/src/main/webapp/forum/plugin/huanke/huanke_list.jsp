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
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.huanke.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>换客列表</title>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link href="../../<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
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
			targetImg2.src="../../images/minus.gif";
			if (targetImg2.loaded=="no"){
				document.frames["hiddenframe"].location.replace("../../listtree.jsp?id="+b_id+getstr);
			}
		}else{
			targetTR2.style.display="none";
			targetImg2.src="../../images/plus.gif";
		}
	}
}
</SCRIPT>
</head>
<BODY>
<div id="wrapper">
<%@ include file="../../inc/header.jsp"%>
<div id="main">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="Topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<%
if (!privilege.isUserLogin(request)) {
	response.sendRedirect("../../../door.jsp");
	return;
}

com.redmoon.forum.Config cfg1 = com.redmoon.forum.Config.getInstance();

String querystring = StrUtil.getNullString(request.getQueryString());
String privurl = request.getRequestURL()+"?"+StrUtil.UrlEncode(querystring,"utf-8");

String boardcode = ParamUtil.get(request, "boardcode");
String boardname = ParamUtil.get(request, "boardname");
String timelimit = request.getParameter("timelimit");
if (timelimit==null)
	timelimit = "all";
%>
  <TABLE height=25 cellSpacing=0 cellPadding=1 rules=rows width="98%" align=center class="tableCommon">
  <TBODY>
  <TR>
        <TD>&nbsp;<img src="../../images/userinfo.gif" width="9" height="9">&nbsp;<a>
          <lt:Label res="res.label.forum.inc.position" key="cur_position"/>
        </a>&nbsp;<a href="<%=request.getContextPath()%>/forum/index.jsp"><lt:Label res="res.label.forum.inc.position" key="forum_home"/></a>&nbsp;&nbsp;<B>&raquo;</B> 
        换客列表&nbsp;  </TD>
    </TR></TBODY></TABLE>
  <BR>
<%
		String myboardname = "", myboardcode = "";
  		String userName = privilege.getUser(request);
		String sql = "";
		String huankeType = ParamUtil.get(request, "huankeType");
		String boardCode = ParamUtil.get(request, "boardCode");
		if (boardCode.equals(""))
			boardCode = "allboard";
		if (huankeType.equals("all")) {
			sql = "select msg_root_id from plugin_huanke_goods where user_name=" + StrUtil.sqlstr(userName);
		}
		else if (huankeType.equals("stocks"))
		{
			sql = "select msg_root_id from plugin_huanke_goods where status=" +HuankeGoodsDb.HUANKE_GOOD_STATUS_STOCKS + " and user_name=" + StrUtil.sqlstr(userName);
		}
		else if (huankeType.equals("exchanged"))
		{
			sql = "select msg_root_id from plugin_huanke_goods where status=" +HuankeGoodsDb.HUANKE_GOOD_STATUS_EXCHANGED + " and user_name=" + StrUtil.sqlstr(userName);
		}
		else{
			sql = "select msg_root_id from plugin_huanke_goods where status=" +HuankeGoodsDb.HUANKE_GOOD_STATUS_EXCHANGE + " and user_name=" + StrUtil.sqlstr(userName);
		}

		int pagesize = 10;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		PageConn pageconn = new PageConn(Global.getDefaultDB(), curpage, pagesize);
		ResultIterator ri = pageconn.getResultIterator(sql);
		paginator.init(pageconn.getTotal(), pagesize);
		
		ResultRecord rr = null;
		
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
		
%>
<TABLE class="tableCommon" cellSpacing=0 cellPadding=1 width="98%" align=center border=1>
  <thead>
  <TR>
    <TD colSpan=3 align=middle noWrap>主题列表 (点 <IMG src="../../images/plus.gif"> 即可展开贴子列表)</TD>
    <TD width=91 align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="author"/></TD>
    <TD width=55 align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="reply"/></TD>
    <TD width=55 align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="hit"/></TD>
    <TD width=80 align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="reply_date"/></TD>
    <TD width=91 align=middle noWrap><lt:Label res="res.label.forum.mytopic" key="board"/></TD>
  </TR>
  </thead>
<%
String topic = "",name="",lydate="",expression="";
int id = -1;
int i = 0,recount=0,hit=0,type=0;
MsgMgr mm = new MsgMgr();
MsgDb md = null;
Leaf myleaf = new Leaf();
com.redmoon.forum.Directory dir = new com.redmoon.forum.Directory();
UserMgr um = new UserMgr();
int showPageSize = cfg1.getIntProperty("forum.showTopicPageSize");
while (ri.hasNext()) {
	 	  rr = (ResultRecord)ri.next(); 
		  i++;
		  id = rr.getInt("msg_root_id");
		  md = mm.getMsgDb(id);
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
    <tbody> 
    <tr> 
        <td noWrap align=middle width=30 bgcolor=#f8f8f8> 
      <%if (recount>20){ %>
          <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_hot"/>" src="../../images/f_hot.gif"> 
          <%}
	  else if (recount>0) {%>
          <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_reply"/>" src="../../images/f_new.gif"> 
          <%}
	  else {%>
          <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_no_reply"/>" src="../../images/f_norm.gif"> 
          <%}%>	    </td>
        <td align=middle width=17 bgcolor=#ffffff> 
          <% String urlboardname = StrUtil.UrlEncode(myboardname,"utf-8"); %>
		   <a href="../../showtopic_tree.jsp?boardcode=<%=myboardcode%>&hit=<%=(hit+1)%>&rootid=<%=id%>" target=_blank> 
          <% if (type==1) { %>
          <IMG height=15 alt="" src="../../images/f_poll.gif" width=17 border=0>
		  <%}else { %>
		  <img src="../../images/brow/<%=expression%>.gif" border=0>
		  <%}%>
		  </a></td>
        <td onMouseOver="this.style.backgroundColor='#ffffff'" onMouseOut="this.style.backgroundColor=''" align=left bgcolor=#f8f8f8> 
        <%
		if (recount==0) {
		%>
          <img id=followImg<%=id%> title="<lt:Label res="res.label.forum.listtopic" key="no_reply"/>" src="../../images/minus.gif" loaded="no"> 
          <% }else { %>
          <img id=followImg<%=id%> title=<lt:Label res="res.label.forum.listtopic" key="extend_reply"/> style="CURSOR: hand" 
      onclick="loadThreadFollow(<%=id%>,<%=id%>,'&boardcode=<%=myboardcode%>&hit=<%=hit+1%>&boardname=<%=urlboardname%>')" src="../../images/plus.gif" loaded="no">
          <% } %>
          <a href="../../showtopic_tree.jsp?boardcode=<%=myboardcode%>&hit=<%=(hit+1)%>&rootid=<%=id%>"><%=StrUtil.toHtml(topic)%></a>
          <%
		// 计算共有多少页回贴
		int allpages = (int)Math.ceil((double)recount/showPageSize);
		if (allpages>1)
		{
		 	out.print("[");
			for (int m=1; m<=allpages; m++)
			{ %>
          <a href="../../showtopic.jsp?boardcode=<%=myboardcode%>&hit=<%=(hit+1)%>&boardname=<%=urlboardname%>&rootid=<%=id%>&CPages=<%=m%>"><%=m%></a> 
          <% }
		  	out.print("]");
		 }%>        </td>
      <td align=middle width=91 bgcolor=#ffffff> 
	  	  <% if (privilege.getUser(request).equals(name)) { %>
          <IMG height=14 src="../../images/my.gif" width=14>
	  <% } %>
	  <a href="../../../userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>"><%=StrUtil.toHtml(um.getUser(name).getNick())%></a>      </td>
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
        </table>      </td>
      <td align=middle width=91 bgcolor=#ffffff>&nbsp;
	  <%if (!myboardcode.equals(Leaf.CODE_BLOG)) {%>
	  <a href="../../listtopic.jsp?boardcode=<%=StrUtil.UrlEncode(myboardcode)%>"><%=myboardname%></a>&nbsp;
	  <%}else{%>
	  <a href="../../../blog/myblog.jsp?userName=<%=StrUtil.UrlEncode(md.getName())%>"><%=myboardname%></a>
	  <%}%></td>
    </tr>
    <tr id=follow<%=id%> style="DISPLAY: none"> 
      <td noWrap align=middle width=30>&nbsp;</td>
      <td align=middle width=17 bgcolor=#ffffff>&nbsp;</td>
      <td onMouseOver="this.style.backgroundColor='#ffffff'" onMouseOut="this.style.backgroundColor=''" align=left bgcolor=#f8f8f8 colspan="6">
	 <div id=followDIV<%=id%> 
      style="WIDTH: 100%;BACKGROUND-COLOR: lightyellow" 
      onclick="loadThreadFollow(<%=id%>,<%=id%>,'&hit=<%=hit+1%>&boardname=<%=urlboardname%>')"><span style="WIDTH: 100%;">
	   <lt:Label res="res.label.forum.listtopic" key="wait"/>
	 </span></div></td>
    </tr>
    </tbody> 
<%}%>
</table>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
    <tr> 
      <td width="13%" height="23"><select name="selboard" onChange="if(this.options[this.selectedIndex].value!=''){location='../../listtopic.jsp?' + this.options[this.selectedIndex].value;}">
        <option value="" selected>
          <lt:Label res="res.label.forum.listtopic" key="sel_board"/>
          </option>
        <%
com.redmoon.forum.LeafChildrenCacheMgr dlcm = new com.redmoon.forum.LeafChildrenCacheMgr("root");
java.util.Vector vt = dlcm.getChildren();
Iterator ir = vt.iterator();
while (ir.hasNext()) {
	Leaf leaf = (Leaf) ir.next();
	String parentCode = leaf.getCode();
	if (leaf.isDisplay(request, privilege)) {
%>
        <option style="BACKGROUND-COLOR: #f8f8f8" value="">╋ <%=leaf.getName()%></option>
        <%
	com.redmoon.forum.LeafChildrenCacheMgr dl = new com.redmoon.forum.LeafChildrenCacheMgr(parentCode);
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
	  String querystr = "huankeType="+huankeType+"&boardCode="+StrUtil.UrlEncode(boardcode,"utf-8");
 	  out.print(paginator.getCurPageBlock(request, "huanke_list.jsp?" + querystr));
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
            src="../../<%=skinPath%>/images/f_new.gif" width=18 border=0>&nbsp;
              <lt:Label res="res.label.forum.listtopic" key="topic_reply"/></TD>
          <TD noWrap width=100><IMG height=12 alt="" 
            src="../../<%=skinPath%>/images/f_hot.gif" width=18 border=0>&nbsp;
              <lt:Label res="res.label.forum.listtopic" key="topic_hot"/>
          </TD>
          <TD noWrap width=100><IMG height=15 alt="" 
            src="../../<%=skinPath%>/images/f_locked.gif" width=17 border=0>&nbsp;
              <lt:Label res="res.label.forum.listtopic" key="topic_lock"/></TD>
          <TD noWrap width=150><IMG src="../../images/topicgood.gif">
              <lt:Label res="res.label.forum.listtopic" key="topic_elite"/></TD>
          <TD noWrap width=150><IMG height=15 alt="" src="../../images/top_forum.gif" width=15 border=0>&nbsp;
              <lt:Label res="res.label.forum.listtopic" key="topic_all_top"/></TD>
        </TR>
        <TR>
          <TD noWrap width=200><IMG height=12 alt="" 
            src="../../<%=skinPath%>/images/f_norm.gif" width=18 border=0>&nbsp;
              <lt:Label res="res.label.forum.listtopic" key="topic_no_reply"/></TD>
          <TD noWrap width=100><IMG height=15 alt="" 
            src="../../<%=skinPath%>/images/f_poll.gif" width=17 border=0>&nbsp;
              <lt:Label res="res.label.forum.listtopic" key="topic_vote"/></TD>
          <TD noWrap width=100><IMG height=15 alt="" 
            src="../../<%=skinPath%>/images/f_top.gif" width=15 border=0>&nbsp;
              <lt:Label res="res.label.forum.listtopic" key="topic_top"/></TD>
          <TD noWrap width=150><IMG height=14 src="../../<%=skinPath%>/images/my.gif" 
            width=14>
              <lt:Label res="res.label.forum.listtopic" key="topic_my"/></TD>
          <TD noWrap width=150>&nbsp;</TD>
        </TR>
      </TBODY>
    </TABLE></TD>
    </TR></TBODY></TABLE>
</div>
<%@ include file="../../inc/footer.jsp"%>
</div>
</BODY></HTML>
