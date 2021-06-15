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
<title><lt:Label res="res.label.forum.search" key="search_result"/> - <%=Global.AppName%></title>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
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
<jsp:useBean id="Topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<%
// 检查是否处于可搜索时间段
TimeConfig tcsearch = new TimeConfig();
if (tcsearch.isSearchForbidden(request)) {
    out.print(SkinUtil.makeErrMsg(request, StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.search", "time_forbid_search"), 
           new Object[] {tcsearch.getProperty("forbidSearchTime")})));
	return;
}

com.redmoon.forum.Config cfg1 = com.redmoon.forum.Config.getInstance();
MsgMgr mm = new MsgMgr();
%>
  <TABLE height=25 cellSpacing=0 cellPadding=1 width="98%" align=center border=1 class="tableCommon">
  <TBODY>
  <TR>
        <TD>&nbsp;<img src="images/userinfo.gif" width="9" height="9">&nbsp;<a>
          <lt:Label res="res.label.forum.inc.position" key="cur_position"/>
        </a>&nbsp;<a href="<%=request.getContextPath()%>/forum/index.jsp"><lt:Label res="res.label.forum.inc.position" key="forum_home"/></a>&nbsp;&nbsp;<B>&raquo;</B> 
        <lt:Label res="res.label.forum.listtopic" key="search_file_result"/>&nbsp;  </TD>
    </TR></TBODY></TABLE>
  <BR>
  <%
  		String type = ParamUtil.get(request, "type");
		String sql = "select id,msgId,name,upload_date,ext,download_count from sq_message_attach";
		String op = ParamUtil.get(request, "op");
		String what = ParamUtil.get(request, "what");
		String sBeginDate = ParamUtil.get(request, "beginDate");
		String sEndDate = ParamUtil.get(request, "endDate");	
		
		if (op.equals("search")) {
			sql += " where name like " + StrUtil.sqlstr("%" + what + "%");
			if (!sBeginDate.equals("") && sEndDate.equals("")) {
				java.util.Date beginDate = DateUtil.parse(sBeginDate, "yyyy-MM-dd");
				long lBeginDate = beginDate.getTime();
				sql += " and UPLOAD_DATE>=" + lBeginDate;			
			}
			else if (sBeginDate.equals("") && !sEndDate.equals("")) {
				java.util.Date endDate = DateUtil.parse(sEndDate, "yyyy-MM-dd");
				long lEndDate = endDate.getTime() + 24*60*60*1000;
				sql += " and UPLOAD_DATE<" + lEndDate;			
			}
			else if (!sBeginDate.equals("") && !sEndDate.equals("")) {
				java.util.Date beginDate = DateUtil.parse(sBeginDate, "yyyy-MM-dd");
				java.util.Date endDate = DateUtil.parse(sEndDate, "yyyy-MM-dd");
				long lBeginDate = beginDate.getTime();
				long lEndDate = endDate.getTime() + 24*60*60*1000;
				sql += " and UPLOAD_DATE>=" + lBeginDate + " and UPLOAD_DATE<" + lEndDate;			
			}
			
			String filter = "";
			if (type.equals("img")) {
				filter = "('gif','jpg','png','bmp')";
			}
			else if (type.equals("audio")) {
				filter = "('wma','mp3')";
			}
			else if (type.equals("video")) {
				filter = "('rmvb','rm','avi','wmv')";
			}
			else if (type.equals("wps")) {
				filter = "('wps','doc')";
			}
			else if (type.equals("excel")) {
				filter = "('xls')";
			}		
			else if (type.equals("ppt")) {
				filter = "('ppt')";
			}
			
			if (!filter.equals("")) {
				sql += " and ext in " + filter;
			}
		}
		
		sql += " order by upload_date desc";

		int pagesize = 20;
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
<form id=form1 name=form1 action="attachment_list.jsp?op=search" method="post">
  <table width="98%" border="0" align="center" class="per100">
  <tr>
      <td width="68%" align="left"><lt:Label res="res.label.forum.listtopic" key="file_type"/><select name="type">
	  <option value="">全部</option>
	  <option value="img">图片</option>
	  <option value="audio">音乐</option>
	  <option value="video">视频</option>
	  <option value="wps">Word/Wps</option>
	  <option value="excel">excel</option>
	  <option value="ppt">ppt</option>
	  </select><lt:Label res="res.label.forum.topic_batch_m" key="from"/>
	  <input size="10" name="beginDate" value="<%=sBeginDate%>">
	<script type="text/javascript">
    Calendar.setup({
        inputField     :    "beginDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "B1",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
	</script>
	<lt:Label res="res.label.forum.topic_batch_m" key="to"/>
	<input size="10" name="endDate" value="<%=sEndDate%>">
	  	<script type="text/javascript">
    Calendar.setup({
        inputField     :    "endDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "B1",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
	</script>
	<lt:Label res="res.label.forum.listtopic" key="file_name"/><input name="what" value="<%=StrUtil.toHtml(what)%>">&nbsp;
	<input type="submit" class="singleboarder" value="<lt:Label res="res.label.forum.admin.user_m" key="search"/>">      </td>
	<script>
	form1.type.value = "<%=type%>";
	form1.what.value = "<%=what%>";
	</script>
    <td align="right"><%=paginator.getPageStatics(request)%></td>
  </tr>
</table>
</form>
<TABLE class="tableCommon" width="98%" border=0 align=center cellPadding=3 cellSpacing=0>
  <thead>
    <TR align=center>
      <TD width=20% height=23><lt:Label res="res.label.forum.listtopic" key="file_name"/></TD>
      <TD width=33% height=23><lt:Label res="res.label.forum.listtopic" key="topic"/></TD>
      <TD width=17%><lt:Label res="res.label.forum.listtopic" key="nick"/></TD>
      <TD width=8%><lt:Label res="res.label.forum.listtopic" key="download_count"/></TD>
      <TD width=14%><lt:Label res="res.label.forum.listtopic" key="upload_date"/></TD>
      <TD width=8% height=23><strong>
        <lt:Label key="op"/>
        </strong></TD>
    </TR>
	</thead>
<%
UserMgr um = new UserMgr();
while (ri.hasNext()) {
    rr = (ResultRecord)ri.next();
	MsgDb md = mm.getMsgDb(rr.getLong("msgId"));
	UserDb user = new UserDb();
	if (md.isLoaded())
		user = um.getUser(md.getName());
%>	<TBODY>
    <TR align=center>
      <TD height=23 align="left"><a href="getfile.jsp?msgId=<%=rr.getLong("msgId")%>&attachId=<%=rr.getLong("id")%>"><%=rr.getString("name")%></a></TD>
      <TD width=33% height=23 align=left><a target=_blank href="showtopic_tree.jsp?showid=<%=md.getId()%>"><%=StrUtil.toHtml(md.getTitle())%></a></TD>
      <TD width=17%><a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(md.getName())%>" target="_blank"><%=user.getNick()%></a></TD>
      <TD width=8%><%=rr.getInt("download_count")%></TD>
      <TD width=14%><%=ForumSkin.formatDateTime(request, DateUtil.parse(rr.getString("upload_date")))%></TD>
      <TD width=8% height=23><a href="getfile.jsp?msgId=<%=rr.getLong("msgId")%>&attachId=<%=rr.getLong("id")%>" target="_blank">
        <lt:Label res="res.label.forum.listtopic" key="download"/>
      </a></TD>
    </TR>
  </TBODY>
<%}%>
</TABLE>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="per100">
  <tr>
    <td height="23" align="right">
      <%
	  String querystr = "op=" + op + "&what=" + StrUtil.UrlEncode(what) + "&type=" + type;
 	  out.print(paginator.getCurPageBlock("attachment_list.jsp?"+querystr));
	%>    </td>
  </tr>
</table>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</BODY></HTML>
