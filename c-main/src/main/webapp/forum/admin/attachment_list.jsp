<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="org.jdom.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("del")) {
	int msgId = ParamUtil.getInt(request, "msgId");
	int attach_id = ParamUtil.getInt(request, "attachId");
	MsgDb msgDb = new MsgDb();
	msgDb = msgDb.getMsgDb(msgId);
	int CPages = ParamUtil.getInt(request, "CPages", 1);
	boolean re = msgDb.delAttachment(attach_id);
	if (re) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "attachment_list.jsp?CPages=" + CPages));
	}
	else
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
	return;
}
if (op.equals("delBatch")) {
	String strIds = ParamUtil.get(request, "ids");
	String[] idsary = StrUtil.split(strIds, ",");
	int CPages = ParamUtil.getInt(request, "CPages", 1);
	if (idsary!=null) {
		int len = idsary.length;
		Attachment att = new Attachment();
		for (int i=0; i<len; i++) {
			Attachment att2 = att.getAttachment(StrUtil.toLong(idsary[i]));
			att2.del();
		}
	}
	out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "attachment_list.jsp?CPages=" + CPages));
	return;	
}
String type = ParamUtil.get(request, "type");
String sql = "select id,msgId,name,upload_date,ext,download_count,is_remote,diskname,visualpath from sq_message_attach";
String what = ParamUtil.get(request, "what");
String sBeginDate = ParamUtil.get(request, "beginDate");
String sEndDate = ParamUtil.get(request, "endDate");
int downloadCount1 = ParamUtil.getInt(request, "downloadCount1", -1);
int downloadCount2 = ParamUtil.getInt(request, "downloadCount2", -1);
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "upload_date";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<title><lt:Label res="res.label.forum.search" key="search_result"/> - <%=Global.AppName%></title>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<LINK href="default.css" type=text/css rel=stylesheet>
<script type="text/javascript" src="../../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../../util/jscalendar/calendar-win2k-2.css"); </style>
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
<script src="../../inc/common.js"></script>
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

var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
	window.location.href = "attachment_list.jsp?op=<%=op%>&what=<%=StrUtil.UrlEncode(what)%>&type=<%=type%>&beginDate=<%=sBeginDate%>&endDate=<%=sEndDate%>&downloadCount1=<%=downloadCount1%>&downloadCount2=<%=downloadCount2%>&orderBy=" + orderBy + "&sort=" + sort;
}

function doDelSearch() {
	if (confirm("<lt:Label key="confirm_del"/>"))		
		window.location.href = "attachment_list.jsp?op=delSearch&what=<%=StrUtil.UrlEncode(what)%>&type=<%=type%>&beginDate=<%=sBeginDate%>&endDate=<%=sEndDate%>&downloadCount1=<%=downloadCount1%>&downloadCount2=<%=downloadCount2%>&orderBy=<%=orderBy%>&sort=<%=sort%>";
}
</SCRIPT>
<META content="MSHTML 6.00.2600.0" name=GENERATOR></HEAD>
<BODY>
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
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><lt:Label res="res.label.forum.listtopic" key="attachment_m"/></td>
  </tr>
</table>
<BR>
<%
if (op.equals("search") || op.equals("delSearch")) {
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
	
	if (downloadCount1>=0 && downloadCount2<0) {
		sql += " and download_count>=" + downloadCount1;
	}
	else if (downloadCount1<0 && downloadCount2>=0) {
		sql += " and download_count<=" + downloadCount2;
	}
	else if (downloadCount1>=0 && downloadCount2>=0) {
		sql += " and download_count>=" + downloadCount1 + " and download_count<=" + downloadCount2;
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

sql += " order by " + orderBy + " " + sort;

int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
PageConn pageconn = new PageConn(Global.getDefaultDB(), curpage, pagesize);
ResultIterator ri = pageconn.getResultIterator(sql);
if (op.equals("delSearch"))
	pagesize = (int)pageconn.getTotal();
paginator.init(pageconn.getTotal(), pagesize);

ResultRecord rr = null;

// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}
%>
  <table width="98%" border="0" align="center" class="p9">
  <tr>
    <form id=form1 name=form1 action="attachment_list.jsp?op=search" method="post">
      <td align="left"><lt:Label res="res.label.forum.listtopic" key="file_type"/>
        <select name="type">
	  <option value="">全部</option>
	  <option value="img">图片</option>
	  <option value="audio">音乐</option>
	  <option value="video">视频</option>
	  <option value="wps">WORD/WPS</option>
	  <option value="excel">excel</option>
	  <option value="ppt">ppt</option>
	  </select>
        <lt:Label res="res.label.forum.listtopic" key="date"/>
        <lt:Label res="res.label.forum.topic_batch_m" key="from"/>
	  <input size="8" id="beginDate" name="beginDate" value="<%=sBeginDate%>">
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
	<input size="8" id="endDate" name="endDate" value="<%=sEndDate%>">
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
	  	<lt:Label res="res.label.forum.listtopic" key="file_name"/>
	  	<input name="what" value="<%=StrUtil.toHtml(what)%>" size="15">
      <lt:Label res="res.label.forum.listtopic" key="download_count"/>
      <lt:Label res="res.label.forum.topic_batch_m" key="from"/>
	  <input name="downloadCount1" size="3" value="<%=downloadCount1==-1?"":""+downloadCount1%>">
	  <lt:Label res="res.label.forum.topic_batch_m" key="to"/>
	  <input name="downloadCount2" size="3" value="<%=downloadCount2==-1?"":""+downloadCount2%>">
      <input name="Submit" type="submit" class="singleboarder" value="<lt:Label res="res.label.forum.admin.user_m" key="search"/>">      </td>
    </form>
	<script>
	form1.type.value = "<%=type%>";
	form1.what.value = "<%=what%>";
	</script>
    </tr>
  <tr>
    <td align="right"><%=paginator.getPageStatics(request)%></td>
    </tr>
</table>
<TABLE width="99%" 
border=0 align=center cellPadding=3 cellSpacing=1 bgcolor="#edeced">
  <TBODY>
    <TR align=center bgColor=#f8f8f8 class="td_title">
      <TD width=20% height=23 class="thead"><lt:Label res="res.label.forum.listtopic" key="file_name"/></TD>
      <TD width=32% height=23 class="thead"><lt:Label res="res.label.forum.listtopic" key="topic"/>
	  </TD>
      <TD width=15% class="thead"><lt:Label res="res.label.forum.listtopic" key="nick"/></TD>
      <TD width=10% class="thead" style="cursor:hand" onClick="doSort('download_count')">
	  <lt:Label res="res.label.forum.listtopic" key="download_count"/>
            <%if (orderBy.equals("download_count")) {
			if (sort.equals("asc")) 
				out.print("<img src='images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='images/arrow_down.gif' width=8px height=7px>");
		}%>
	  </TD>
      <TD width=14% class="thead" style="cursor:hand" onClick="doSort('upload_date')">
	  <lt:Label res="res.label.forum.listtopic" key="upload_date"/>
        <%if (orderBy.equals("upload_date")) {
			if (sort.equals("asc")) 
				out.print("<img src='images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='images/arrow_down.gif' width=8px height=7px>");
		}%>
      </TD>
      <TD width=9% height=23 class="thead"><strong>
        <lt:Label key="op"/>
      </strong></TD>
    </TR>
<%
UserMgr um = new UserMgr();
Attachment att = new Attachment();
while (ri.hasNext()) {
    rr = (ResultRecord)ri.next();
	MsgDb md = mm.getMsgDb(rr.getLong("msgId"));
	UserDb user = new UserDb();
	if (md.isLoaded())
		user = um.getUser(md.getName());
%>
    <TR align=center bgColor=#f8f8f8>
      <TD height=23 align="left">
		<input name="ids" value="<%=rr.getLong("id")%>" type="checkbox">
		<%if (rr.getInt("is_remote")==1) {%>
		<a target=_blank href="<%=com.redmoon.forum.Config.getInstance().getProperty("forum.ftpUrl") + "/" + rr.getString("visualpath")%>/<%=rr.getString("diskname")%>"><%=rr.getString("name")%></a>
		<%}else{%>
		<a target=_blank href="<%=Global.getRootPath() + "/" + com.redmoon.forum.Config.getInstance().getAttachmentPath() + "/" + rr.getString("visualpath")%>/<%=rr.getString("diskname")%>"><%=rr.getString("name")%></a>
		<%}%>
	  </TD>
      <TD width=32% height=23 align=left><a target=_blank href="../showtopic_tree.jsp?showid=<%=md.getId()%>"><%=StrUtil.toHtml(md.getTitle())%></a></TD>
      <TD width=15%><a href="../../userinfo.jsp?username=<%=StrUtil.UrlEncode(md.getName())%>" target="_blank"><%=user.getNick()%></a></TD>
      <TD width=10%><%=rr.getInt("download_count")%></TD>
      <TD width=14%><%=ForumSkin.formatDateTime(request, DateUtil.parse(rr.getString("upload_date")))%></TD>
      <TD width=9% height=23>
	  <%if (op.equals("delSearch")) {
	  	Attachment att2 = att.getAttachment(rr.getLong("id"));
		att2.del();
	  %>
	  <lt:Label res="res.label.forum.listtopic" key="already_del"/>
	  <%}else{%>
	  <a href="javascript:if (confirm('<lt:Label key="confirm_del"/>')) window.location.href='attachment_list.jsp?op=del&msgId=<%=rr.getLong("msgId")%>&CPages=<%=curpage%>&attachId=<%=rr.getLong("id")%>'">
	  <lt:Label key="op_del"/>
	  </a>
	  <%}%>
	  </TD>
    </TR>
<%}%>
  </TBODY>
</TABLE>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
  <tr>
    <td width="57%" height="23" align="left"><input name="button" type="button" onClick="selAllCheckBox('ids')" value="<lt:Label res="res.label.forum.topic_m" key="sel_all"/>">
    &nbsp;&nbsp;<input name="button" type="button" onClick="clearAllCheckBox('ids')" value="<lt:Label res="res.label.forum.topic_m" key="clear_all"/>">
	&nbsp;&nbsp;<input name="button2" type="button" onClick="doDel()" value="<lt:Label key="op_del"/>">
	&nbsp;&nbsp;<input value="<lt:Label res="res.label.forum.listtopic" key="del_search"/>" type="button" onClick="doDelSearch()">
	</td>
    <td width="43%" align="right"><%
	  String querystr = "op=" + op + "&what=" + StrUtil.UrlEncode(what) + "&type=" + type + "&beginDate=" + sBeginDate + "&endDate=" + sEndDate + "&downloadCount1=" + downloadCount1 + "&downloadCount2=" + downloadCount2 + "&orderBy=" + orderBy + "&sort=" + sort;
 	  out.print(paginator.getCurPageBlock("attachment_list.jsp?"+querystr));
	%></td>
  </tr>
</table>
</BODY>
<script>
function doDel() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请选择附件！");
		return;
	}
	if (confirm('<lt:Label key="confirm_del"/>'))
		window.location.href = "attachment_list.jsp?op=delBatch&ids=" + ids;
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
