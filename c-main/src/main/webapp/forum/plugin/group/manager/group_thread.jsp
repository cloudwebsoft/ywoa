<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.blog.*"%>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="org.jdom.*"%>
<%@ page import="java.util.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
long groupId = ParamUtil.getLong(request, "id", -1);

if (!GroupPrivilege.isMember(request, groupId)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String skinPath = "skin/default";

if (!privilege.isUserLogin(request)) {
	response.sendRedirect("../../../../door.jsp");
	return;
}

GroupDb gd = new GroupDb();
gd = (GroupDb)gd.getQObjectDb(new Long(groupId));
if (gd==null) {
	out.print(SkinUtil.makeErrMsg(request, "该圈子不存在!"));
	return;
}
		
String op = ParamUtil.get(request, "op");
if (op.equals("setLocked")) {
	if (!GroupPrivilege.isManager(request, groupId)) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	boolean re = false;
	long id = ParamUtil.getLong(request, "msgId");
	int value = ParamUtil.getInt(request, "value");
	MsgMgr mm = new MsgMgr();
	MsgDb md = mm.getMsgDb(id);
	if (privilege.canEdit(request, md)) {
		try {
			re = mm.setLocked(request,id,value);
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
			return;
		}		
	}
	else {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request,"pvg_invalid")));
		return;
	}
	if (re) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"info_operate_success"), "group_thread.jsp?id=" + groupId));
		return;
	}
	else {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request,"info_operate_fail")));
		return;
	}
}

if (op.equals("del")) {
	if (!GroupPrivilege.isManager(request, groupId)) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	MsgMgr mm = new MsgMgr();
	long delId = ParamUtil.getLong(request, "delId");
	boolean re = false;
	try {
		re = mm.delTopicAbsolutely(application, request, delId);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	if (re) {
		String privurl = ParamUtil.get(request, "privurl");
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"info_operate_success"), privurl));
		return;
	}
	else {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request,"info_operate_fail")));
		return;
	}
}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML><HEAD><TITLE><%=Global.AppName%></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<%@ include file="../../../inc/nocache.jsp"%>
<LINK href="../../../../common.css" type=text/css rel=stylesheet>
<STYLE>
TABLE {
	BORDER-TOP: 0px; BORDER-LEFT: 0px; BORDER-BOTTOM: 1px
}
TD {
	BORDER-RIGHT: 0px; BORDER-TOP: 0px
}
.style1 {color: #FFFFFF}
.style3 {color: #FFFFFF; font-weight: bold; }
</STYLE>
<SCRIPT>
function openWin(url,width,height) {
  var newwin = window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width="+width+",height="+height);
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
			targetImg2.src="<%=request.getContextPath()%>/forum/images/minus.gif";
			if (targetImg2.loaded=="no"){
				document.frames["hiddenframe"].location.replace("<%=request.getContextPath()%>/forum/listtree.jsp?id="+b_id+getstr);
			}
		}else{
			targetTR2.style.display="none";
			targetImg2.src="<%=request.getContextPath()%>/forum/images/plus.gif";
		}
	}
}
</SCRIPT>
<META content="MSHTML 6.00.2600.0" name=GENERATOR></HEAD>
<BODY leftmargin="0" topMargin=0>
<iframe src="" id="hiddenframe" width="0" height="0"></iframe>
<jsp:useBean id="Topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<table width="100%" border="0">
  <tr>
    <td>
	&nbsp;管理文章</td>
  </tr>
      <tr>
        <td height="1" align="center" background="../../images/comm_dot.gif"></td>
      </tr>
</table>
<%		
		GroupThreadDb gtd = new GroupThreadDb();
		String sql = gtd.getListThreadSql(groupId, "add_date");

	    long total = gtd.getQObjectCount(sql, "" + groupId);
		int pagesize = 20;
		Paginator paginator = new Paginator(request, total, pagesize);
		int curpage = paginator.getCurPage();

		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
		
		int start = (curpage-1)*pagesize;
		int end = curpage*pagesize;
		
        QObjectBlockIterator irthread = gtd.getQObjects(sql,""+groupId, start, end);
%>
  <table width="98%" border="0" class="p9">
    <tr> 
      <td width="44%" align="left"> 
	  </td>
      <td width="56%" align="right"><%=paginator.getPageStatics(request)%></td>
    </tr>
  </table>
  <TABLE borderColor=#edeced cellSpacing=0 cellPadding=1 width="98%" align=center 
border=1>
    <TBODY>
      <TR height=25> 
        <TD height="26" colSpan=3 align=middle noWrap bgcolor="#617AA9" class="text_title style1">主题列表</TD>
        <TD width=55 align=middle noWrap bgcolor="#617AA9" class="text_title style1">用户</TD>
        <TD width=55 height="26" align=middle noWrap bgcolor="#617AA9" class="text_title style1"><lt:Label res="res.label.forum.listtopic" key="reply"/></TD>
        <TD width=55 height="26" align=middle noWrap bgcolor="#617AA9" class="text_title style1"><lt:Label res="res.label.forum.listtopic" key="hit"/></TD>
        <TD width=80 height="26" align=middle noWrap bgcolor="#617AA9" class="text_title style1"><lt:Label res="res.label.forum.listtopic" key="reply_date"/></TD>
        <TD width=80 align=middle noWrap bgcolor="#617AA9" class="text_title style1"><lt:Label key="op"/></TD>
      </TR>
    </TBODY>
</TABLE>
<%		
String id="",topic = "",name="",lydate="",rename="",redate="";
int level=0,iselite=0,islocked=0,expression=0;
int i = 0,recount=0,hit=0,type=0;
ForumDb forum = new ForumDb();
UserMgr um = new UserMgr();
MsgMgr mm = new MsgMgr();
%>
<%
while (irthread.hasNext()) {
	 	  gtd = (GroupThreadDb) irthread.next(); 
		  i++;
		  MsgDb msgdb = mm.getMsgDb(gtd.getLong("msg_id"));
		  id = ""+msgdb.getId();
		  topic = msgdb.getTitle();
		  name = msgdb.getName();
		  lydate = com.redmoon.forum.ForumSkin.formatDateTime(request, msgdb.getAddDate());
		  recount = msgdb.getRecount();
		  hit = msgdb.getHit();
		  expression = msgdb.getExpression();
		  type = msgdb.getType();
		  iselite = msgdb.getIsElite();
		  islocked = msgdb.getIsLocked();
		  level = msgdb.getLevel();
		  rename = msgdb.getRename();
		  redate = com.redmoon.forum.ForumSkin.formatDateTime(request, msgdb.getRedate());
		  if (redate!=null && redate.length()>=19)
		  	redate = redate.substring(5,16);
	  %>
<table bordercolor=#edeced cellspacing=0 cellpadding=1 width="98%" align=center border=1>
    <tbody>
      <tr> 
        <td noWrap align=middle width=30 bgcolor=#f8f8f8> <% if (level==MsgDb.LEVEL_TOP_BOARD) { %> <IMG alt="" src="<%=request.getContextPath()%>/forum/<%=skinPath%>/images/f_top.gif" border=0> 
        <%}
		else {
				if (recount>20){ %> <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_hot"/>" src="<%=request.getContextPath()%>/forum/<%=skinPath%>/images/f_hot.gif"> <%}
	  			else if (recount>0) {%> <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_reply"/>" src="<%=request.getContextPath()%>/forum/<%=skinPath%>/images/f_new.gif"> <%}
	  			else {%> <img alt="<lt:Label res="res.label.forum.listtopic" key="open_topic_no_reply"/>" src="<%=request.getContextPath()%>/forum/<%=skinPath%>/images/f_norm.gif"> <%}
	 	}%>		</td>
        <td align=middle width=17 bgcolor=#ffffff><a href="<%=request.getContextPath()%>/forum/showtopic.jsp?rootid=<%=id%>" target=_blank> 
          <% 
		  if (islocked==1) { %>
          <IMG height=15 alt="" src="<%=request.getContextPath()%>/forum/<%=skinPath%>/images/f_locked.gif" width=17 border=0> 
          <% }
		  else {
			  if (type==1) { %>
          <IMG height=15 alt="" src="<%=request.getContextPath()%>/forum/<%=skinPath%>/images/f_poll.gif" width=17 border=0> 
          <%}else {
		  		if (expression!=MsgDb.EXPRESSION_NONE) {
		  %>
		          <img src="<%=request.getContextPath()%>/forum/images/brow/<%=expression%>.gif" border=0> 
          <%	}
		  		else
					out.print("&nbsp;");
		  	}
		  } %>
          </a></td>
        <td onMouseOver="this.style.backgroundColor='#ffffff'" 
    onMouseOut="this.style.backgroundColor=''" align=left bgcolor=#f8f8f8> <%
		if (recount==0) {
		%> <img id=followImg<%=id%> title="<lt:Label res="res.label.forum.listtopic" key="no_reply"/>" src="<%=request.getContextPath()%>/forum/images/minus.gif" loaded="no"> 
          <% }else { %> <img id=followImg<%=id%> title="<lt:Label res="res.label.forum.listtopic" key="extend_reply"/>" style="CURSOR: hand" 
      onClick="loadThreadFollow(<%=id%>,<%=id%>,'&hit=<%=hit+1%>')" src="<%=request.getContextPath()%>/forum/images/plus.gif" loaded="no"> 
          <% } %>
		<a href="<%=request.getContextPath()%>/forum/showtopic.jsp?rootid=<%=id%>" target=_blank> 
		<%
		String color = StrUtil.getNullString(msgdb.getColor());
		String tp = topic;
		if (!color.equals(""))
			tp = "<font color='" + color + "'>" + tp + "</font>";
		if (msgdb.isBold())
			tp = "<B>" + tp + "</B>";
		%>
		<%=tp%>		</a>
			<%if (iselite==1) { %>
				<IMG src="<%=request.getContextPath()%>/forum/images/topicgood.gif">
			<%}%>
		  <%
		// 计算共有多少页回贴
		int allpages = Math.round((float)recount/10+0.5f);
		if (allpages>1)
		{
		 	out.print("[");
			for (int m=1; m<=allpages; m++)
			{ %> <a href="<%=request.getContextPath()%>/forum/showtopic.jsp?rootid=<%=id%>&CPages=<%=m%>" target=_blank><%=m%></a>
		  <% }
		  	out.print("]");
		 }%></td>
        <td align=middle width=55 bgcolor=#f8f8f8>
		<a href="../../../../userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>" target="_blank"><%=um.getUser(name).getNick()%></a>
		</td>
        <td align=middle width=55 bgcolor=#f8f8f8><font color=red>[<%=recount%>]</font></td>
        <td align=middle width=55 bgcolor=#ffffff><%=hit%></td>
        <td align=left width=80 bgcolor=#f8f8f8> <table cellspacing=0 cellpadding=2 width="100%" align=center border=0>
            <tbody>
              <tr> 
                <td align="center"> 
                  <%if (rename.equals("")) {%>
				  <%=lydate%>
				  <%}else{%>
				<a href="<%=request.getContextPath()%>/userinfo.jsp?username=<%=StrUtil.UrlEncode(rename,"utf-8")%>" target="_blank" title="<lt:Label res="res.label.forum.listtopic" key="topic_date"/><%=lydate%>"><%=um.getUser(rename).getNick()%></a><br>
				<%=redate%>
				<%}%>
				</td>
              </tr>
            </tbody>
          </table></td>
        <td align=center width=80 bgcolor=#f8f8f8>
		<%if (msgdb.getIsWebedit()==msgdb.WEBEDIT_REDMOON) {%>
		<a href="<%=request.getContextPath()%>/forum/edittopic_we.jsp?editFlag=plugin&boardcode=<%=com.redmoon.forum.Leaf.CODE_BLOG%>&editid=<%=msgdb.getId()%>&privurl=<%=StrUtil.getUrl(request)%>"><lt:Label key="op_edit"/></a>
		<%} else if (msgdb.getIsWebedit()==msgdb.WEBEDIT_UBB) {%>
		<a href="<%=request.getContextPath()%>/forum/edittopic.jsp?editFlag=plugin&boardcode=<%=com.redmoon.forum.Leaf.CODE_BLOG%>&editid=<%=msgdb.getId()%>&privurl=<%=StrUtil.getUrl(request)%>"><lt:Label key="op_edit"/></a>
		<%} else {%>
		<a href="<%=request.getContextPath()%>/forum/edittopic_new.jsp?editFlag=plugin&boardcode=<%=com.redmoon.forum.Leaf.CODE_BLOG%>&editid=<%=msgdb.getId()%>&privurl=<%=StrUtil.getUrl(request)%>"><lt:Label key="op_edit"/></a>
		<%}%>
		<a href="javascript:if (confirm('<lt:Label key="confirm_del"/>')) window.location.href='group_thread.jsp?op=del&id=<%=groupId%>&delId=<%=msgdb.getId()%>&privurl=<%=StrUtil.getUrl(request)%>'"><lt:Label key="op_del"/></a><BR>
	    <%if (GroupPrivilege.isManager(request, groupId)) {%>
		<%if (msgdb.getIsLocked()==0) {%>
	  		<a href="group_thread.jsp?id=<%=groupId%>&op=setLocked&msgId=<%=msgdb.getId()%>&value=1"><lt:Label res="res.label.forum.showtopic" key="lock"/></a>
		<%}else{%>
	  		<a href="group_thread.jsp?id=<%=groupId%>&op=setLocked&msgId=<%=msgdb.getId()%>&value=0"><lt:Label res="res.label.forum.showtopic" key="unlock"/></a>
		<%}%>
		<%}%>		</td>
      </tr>
      <tr id=follow<%=id%> style="DISPLAY: none"> 
        <td noWrap align=middle width=30 bgcolor=#f8f8f8>&nbsp;</td>
        <td align=middle width=17 bgcolor=#ffffff>&nbsp;</td>
        <td onMouseOver="this.style.backgroundColor='#ffffff'" 
    onMouseOut="this.style.backgroundColor=''" align=left bgcolor=#f8f8f8 colspan="6"> 
          <div id=followDIV<%=id%> 
      style="WIDTH: 100%;BACKGROUND-COLOR: lightyellow" 
      onclick="loadThreadFollow(<%=id%>,<%=id%>,'&hit=<%=hit+1%>')"><lt:Label res="res.label.forum.listtopic" key="wait"/></div></td>
      </tr>
      <tr> 
        <td 
    style="PADDING-RIGHT: 0px; PADDING-LEFT: 0px; PADDING-BOTTOM: 0px; PADDING-TOP: 0px" 
    colspan=5> </td>
      </tr>
    </tbody>
</table>
<%}%>
  <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
    <tr> 
      <td height="23" align="right"> 
         
          <%
				String querystr = "op="+op+"&id=" + groupId;
				out.print(paginator.getCurPageBlock("group_thread.jsp?"+querystr));
				%>
      &nbsp;&nbsp;</td>
    </tr>
</table> 
  <TABLE cellSpacing=0 cellPadding=0 width="98%" border=0>
  <TBODY>
  <TR>
    <TD width="70%">&nbsp;</TD>
    <TD width="40%">&nbsp;</TD></TR></TBODY></TABLE></CENTER>
</BODY></HTML>
