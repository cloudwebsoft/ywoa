<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Date"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*" %>
<%@ page import="com.redmoon.forum.robot.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.forum.listtopic" key="message_op"/> - <%=Global.AppName%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="x-ua-compatible" content="ie=7" />
<script type="text/javascript" src="../../js/jquery1.7.2.min.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../../util/jscalendar/calendar-win2k-2.css"); </style>
<link href="../<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<link rel="stylesheet" type="text/css" href="../../js/datepicker/jquery.datetimepicker.css"/>
<script src="../../js/datepicker/jquery.datetimepicker.js"></script>
</head>
<body>
<div id="wrapper">
<%@ include file="../inc/header.jsp"%>
<div id="main">
<%@ include file="../inc/position.jsp"%>
<br>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
MsgOperateDb mod = new MsgOperateDb();

String op = ParamUtil.get(request, "op");
String boardcode = ParamUtil.get(request, "boardcode");

int total = 0;
int pagesize = 20;
int curpage = StrUtil.toInt(ParamUtil.get(request, "CPages"), 1);

String sql = "select id from " + mod.getTable().getName() + " order by op_date desc";

String action = ParamUtil.get(request, "action");
String searchType = ParamUtil.get(request, "searchType");
String opType = ParamUtil.get(request, "opType");
String what = ParamUtil.get(request, "what");
String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");

Date bdate = DateUtil.parse(beginDate, "yyyy-MM-dd");
Date edate = DateUtil.parse(endDate, "yyyy-MM-dd");

if (action.equals("search")) {
	if (searchType.equals("title")) {
		sql = "select id from " + mod.getTable().getName() + " where msg_title like " + StrUtil.sqlstr("%" + what + "%");
	}
	else if (searchType.equals("boardcode")) {
		sql = "select id from " + mod.getTable().getName() + " where boardcode=" + StrUtil.sqlstr(what);
	}
	else if (searchType.equals("userName")) {
		UserDb ud = new UserDb();
		ud = ud.getUserDbByNick(what.trim());
		if (ud==null || !ud.isLoaded()) {
			out.print(StrUtil.Alert_Back(StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.admin.message_op_list", "user_lost"), new Object[] {what})));
			return;
		}
		sql = "select id from " + mod.getTable().getName() + " where msg_user=" + StrUtil.sqlstr(ud.getName());
	}
	
	if (!opType.equals("")) {
		sql += " and op_type=" + opType;
	}
	
	if (bdate!=null && edate!=null) {
		sql += " and op_date between ? and ?";
	}
	else if (bdate!=null) {
		sql += " and op_date>=?";
	}
	else if (edate!=null) {
		sql += " and op_date<=?";
	}
	
	sql += " and user_name=" + StrUtil.sqlstr(privilege.getUser(request)) + " order by op_date desc";
}

ListResult lr = null;
if (bdate!=null && edate!=null) {
	lr = mod.listResult(sql, new Object[] {bdate,edate}, curpage, pagesize);
}
else if (bdate!=null) {
	lr = mod.listResult(sql, new Object[] {bdate}, curpage, pagesize);
}
else if (edate!=null) {
	lr = mod.listResult(sql, new Object[] {edate}, curpage, pagesize);
}
else
	lr = mod.listResult(sql, curpage, pagesize);
total = lr.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0){
	curpage = 1;
	totalpages = 1;
}
%>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="p9">
  <form name="form1" action="message_op_list.jsp?action=search" method="post">
    <tr>
      <td align="center"><lt:Label res="res.label.forum.admin.message_op_list" key="begin_date"/>
          <input name="beginDate" id="beginDate" size="10" value="<%=beginDate%>" />
          <lt:Label res="res.label.forum.admin.message_op_list" key="end_date"/>
          <input name="endDate" id="endDate" size="10" value="<%=endDate%>" />
          <script type="text/javascript">
          $(function(){
		    $('#beginDate').datetimepicker({
                 lang:'ch',
                 timepicker:false,
                 format:'Y-m-d',
                 formatDate:'Y/m/d'
             });
		     $('#endDate').datetimepicker({
                  lang:'ch',
                  timepicker:false,
                  format:'Y-m-d',
                  formatDate:'Y/m/d'
              });
 		  })
 			 </script>
          <select id="searchType" name="searchType">
            <option value="title" selected="selected">
              <lt:Label res="res.label.forum.admin.message_op_list" key="title"/>
            </option>
            <option value="userName">
              <lt:Label res="res.label.forum.admin.message_op_list" key="userName"/>
            </option>
            <option value="boardcode">
              <lt:Label res="res.label.forum.admin.message_op_list" key="boardcode"/>
            </option>
          </select>
          <select id="opType" name="opType">
            <option value="">
              <lt:Label res="res.label.forum.admin.message_op_list" key="op_type"/>
            </option>
            <%for (int i=0; i<12; i++) {%>
            <option value="<%=i%>"><%=mod.getOperate(request, i)%></option>
            <%}%>
          </select>
          <input id="what" name="what" type="text" />
          <%if (!searchType.equals("")) {%>
          <script>
		form1.searchType.value = "<%=searchType%>";
		form1.what.value = "<%=what%>";
		form1.opType.value = "<%=opType%>";
		</script>
          <%}%>
		<input name="boardcode" value="<%=boardcode%>" type="hidden">
        &nbsp;
        <input name="Submit" type="submit" value="<lt:Label res="res.label.forum.admin.message_op_list" key="search"/>">
      </td>
    </tr>
  </form>
</table>
<table width="98%" border="0" align="center" class="per100">
  <tr>
    <td align="right"><%=paginator.getPageStatics(request)%></td>
  </tr>
</table>
<table class="tableCommon" width="98%" align="center" cellPadding="3" cellSpacing="0">
  <thead>
    <tr>
      <td width="38%" align="center" noWrap><lt:Label res="res.label.forum.admin.message_op_list" key="topic"/></td>
      <td width="15%" align="center" noWrap><lt:Label res="res.label.forum.admin.message_op_list" key="userName"/></td>
      <td width="13%" align="center" noWrap><lt:Label res="res.label.forum.admin.message_op_list" key="type"/></td>
      <td width="11%" align="center" noWrap><lt:Label res="res.label.forum.admin.message_op_list" key="board"/></td>
      <td width="12%" align="center" noWrap><lt:Label res="res.label.forum.admin.message_op_list" key="op_date"/></td>
      <td width="11%" align="center" noWrap><lt:Label res="res.label.forum.admin.message_op_list" key="topic_status"/></td>
    </tr>
  </thead>
  <tbody>
<%
java.util.Iterator ir = lr.getResult().iterator();
UserMgr um = new UserMgr();
MsgMgr mm = new MsgMgr();
Directory dir = new Directory();
while (ir.hasNext()) {
 	mod = (MsgOperateDb)ir.next();
    String userName = StrUtil.getNullStr(mod.getString("user_name"));
	MsgDb md = mm.getMsgDb(mod.getLong("msg_id"));
	String t = "";
	if (md.isLoaded()) {
		t = "<a target=_blank href='../showtopic_tree.jsp?rootid=" + md.getRootid() + "&showid=" + md.getId() + "'>" + StrUtil.toHtml(md.getTitle()) + "</a>";
	}
	else {
		t = StrUtil.toHtml(StrUtil.getNullStr(mod.getString("msg_title")));
	}
    if (!userName.equals(MsgOperateDb.OPERATOR_MASTER)) {
        UserDb ud = um.getUser(userName);
        userName = "<a target=_blank href='../../userinfo.jsp?username=" + StrUtil.UrlEncode(ud.getName()) + "'>" + ud.getNick() + "</a>";
    }
    else
        userName = "Administrator";	
	%>
    <tr>
      <td height="24"><%=t%></td>
      <td class="row"><%
	  UserDb user = null;
	  if (md.isLoaded())
	  	user = um.getUser(md.getName());
	  else {
	  	user = um.getUser(StrUtil.getNullStr(mod.getString("msg_user")));
	  }
	  if (user!=null) {
	  	out.print("<a target=_blank href='../../userinfo.jsp?username=" + StrUtil.UrlEncode(user.getName()) + "'>" + user.getNick() + "</a>");
	  }
	  %>
      </td>
      <td><%=mod.getOperate(request)%></td>
      <td class="row"><%
	  String boardcode2 = "";
	  if (md.isLoaded())
	  	boardcode2 = md.getboardcode();
	  else {
	  	boardcode2 = StrUtil.getNullStr(mod.getString("boardcode"));
	  }
	  if (!boardcode2.equals("")) {
	  	Leaf lf = dir.getLeaf(boardcode2);
		if (lf!=null)
			out.print("<a target=_blank href='../listtopic.jsp?boardcode=" + StrUtil.UrlEncode(lf.getCode()) + "'>" + lf.getName() + "</a>");
	  }
	  %>      </td>
      <td><span title="<%=ForumSkin.formatDateTime(request, mod.getDate("op_date"))%>"><%=ForumSkin.formatDate(request, mod.getDate("op_date"))%></span></td>
      <td align="center">
	  <%
	  if (!md.isLoaded()) {
	  	out.print("<font color=red>" + SkinUtil.LoadString(request, "res.label.forum.admin.message_op_list", "deleted") + "</font>");
	  }
	  else {
          if (md.getCheckStatus()==MsgDb.CHECK_STATUS_PASS) {%>
                  <lt:Label res="res.label.forum.topic_m" key="check_pass"/>
          <%}else if (md.getCheckStatus()==MsgDb.CHECK_STATUS_NOT) {%>
                  <lt:Label res="res.label.forum.topic_m" key="check_not"/>
          <%}else if (md.getCheckStatus()==MsgDb.CHECK_STATUS_DUSTBIN) {%>
                  <lt:Label res="res.label.forum.topic_m" key="check_dustbin"/>
      <%}
	  }
	  %></td>
    </tr>
<%}%>
  </tbody>
</table>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="per100">
        <tr>
          <td height="23" align="right"><%
				String querystr = "boardcode=" + StrUtil.UrlEncode(boardcode) + "&action=" + action + "&searchType=" + searchType + "&opType=" + opType + "&what=" + StrUtil.UrlEncode(what) + "&beginDate=" + beginDate + "&endDate=" + endDate;
				out.print(paginator.getPageBlock(request,"?"+querystr));
				%></td>
        </tr>
</table>
</div>
<%@ include file="../inc/footer.jsp"%>
</div>
</body>
</html>