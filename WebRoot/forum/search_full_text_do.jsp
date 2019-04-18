<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="org.jdom.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.search.Indexer"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="org.apache.lucene.search.*,org.apache.lucene.document.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String queryString = ParamUtil.get(request, "queryString");
if(queryString.equals("")){
	out.print(StrUtil.Alert_Redirect("请填写关键字！","search.jsp"));
	return;
}
	
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<title><lt:Label res="res.label.forum.search" key="search_result"/> - <%=Global.AppName%></title>
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
</HEAD>
<BODY>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="Topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<%
if (!privilege.canUserDo(request, "", "search")) {
	response.sendRedirect("../info.jsp?info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String qstr = StrUtil.getNullString(request.getQueryString());
String privurl = request.getRequestURL()+"?"+StrUtil.UrlEncode(qstr,"utf-8");

String fieldName = ParamUtil.get(request, "fieldName");
%>
<TABLE cellSpacing=0 width="98%" align=center class="tableCommon">
 <TBODY>
  <TR>
    <TD>&nbsp;<img src="images/userinfo.gif" width="9" height="9">&nbsp;<a>
          <lt:Label res="res.label.forum.inc.position" key="cur_position"/>
        </a>&nbsp;<a href="<%=request.getContextPath()%>/forum/index.jsp"><lt:Label res="res.label.forum.inc.position" key="forum_home"/></a>&nbsp;&nbsp;<B>&raquo;</B> 
     <a href="search.jsp"><lt:Label res="res.label.forum.listtopic" key="search_result"/></a></TD>
    </TR>
 </TBODY>
</TABLE>
<BR>
<%
        Indexer indexer = new Indexer();
	    Hits hits = indexer.seacher(queryString, fieldName);
		if (hits==null || hits.length()==0) {
			out.print(SkinUtil.makeInfo(request, "未找到符合条件的记录！"));
			return;
		}
		int pagesize = 20;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		paginator.init(hits.length(), pagesize);
		
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
%>
<TABLE class="tableCommon" cellSpacing=0 cellPadding=1 width="98%" align=center>
  <thead>
  <TR>
    <TD colSpan=3 align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="topis_list"/></TD>
    <TD width=102 height="26" align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="author"/></TD>
    <TD width=51 height="26" align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="reply"/></TD>
    <TD width=51 height="26" align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="hit"/></TD>
    <TD width=118 height="26" align=middle noWrap><lt:Label res="res.label.forum.listtopic" key="reply_date"/></TD>
    <TD width=91 height="26" align=middle noWrap><lt:Label res="res.label.forum.mytopic" key="board"/></TD>
  </TR>
  </thead>
<%
String name="",lydate="",myboardcode="", myboardname="";
int expression;
long id = -1;
int i = 0,recount=0,hit=0,type=0;
MsgMgr mm = new MsgMgr();
MsgDb md = null;
Leaf myleaf = new Leaf();
Directory dir = new Directory();
UserMgr um = new UserMgr();
i = (curpage-1)*pagesize;
if (i>hits.length()-1)
	i = hits.length() - 1;
int end = curpage*pagesize;
if (end>hits.length())
	end = hits.length();

while (i < end) {
	org.apache.lucene.document.Document doc = hits.doc(i);
	i++;
	id = Long.parseLong(doc.get("id"));
	md = mm.getMsgDb(id);
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
        <td align=middle width=36> 
          <% String urlboardname = StrUtil.UrlEncode(myboardname,"utf-8"); %>
		   <a href="showtopic_tree.jsp?rootid=<%=id%>" target=_blank> 
          <% if (type==1) { %>
          <IMG height=15 alt="" src="images/f_poll.gif" width=17 border=0>
		  <%}else {
				if (expression!=MsgDb.EXPRESSION_NONE) {		  
		  %>
		  			<img src="images/brow/<%=expression%>.gif" border=0>
		  <%	}
		  		else
					out.print("&nbsp;");
		  }%>
		  </a></td>
        <td width="443" align=left style="padding-left:3px" onMouseOver="this.style.backgroundColor='#ffffff'" onMouseOut="this.style.backgroundColor=''"> 
          <a href="showtopic_tree.jsp?rootid=<%=md.getRootid()%>&showid=<%=id%>"><%=DefaultRender.RenderFullTitle(request, md)%></a>
        </td>
      <td align=middle width=102> 
	  	  <% if (privilege.getUser(request).equals(name)) { %>
          <IMG height=14 src="images/my.gif" width=14>
	  	  <%}%>
	  <a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>"><%=um.getUser(name).getNick()%></a> 
      </td>
        <td align=middle width=51>[<%=recount%>]</td>
        <td align=middle width=51><%=hit%></td>
      <td align=left width=118> 
        <%=lydate%>
      </td>
      <td align=middle width=91>&nbsp;
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
      <td align=left colspan="6">
	 <div id="followDIV<%=id%>" style="WIDTH: 100%;" onClick="loadThreadFollow(<%=id%>,<%=id%>,'&hit=<%=hit+1%>&boardname=<%=urlboardname%>')">
	 <lt:Label res="res.label.forum.listtopic" key="wait"/>
	 </div>
	</td>
    </tr>
    </tbody> 
<%}%>
</table>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
    <tr> 
      <td width="13%" height="23"><select name="selboard" onChange="if(this.options[this.selectedIndex].value!=''){location='listtopic.jsp?' + this.options[this.selectedIndex].value;}">
        <option value="" selected>
          <lt:Label res="res.label.forum.listtopic" key="sel_board"/>
          </option>
<%
String boardcode="";
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
	  String querystr = "queryString="+StrUtil.UrlEncode(queryString,"utf-8")+"&fieldName="+StrUtil.UrlEncode(fieldName,"utf-8");
	  out.print(paginator.getCurPageBlock(request, "search_full_text_do.jsp?"+querystr));	  
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
