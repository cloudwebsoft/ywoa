<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*" %>
<%@ page import="com.redmoon.forum.robot.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Msg Operate List</title>
<link href="default.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script type="text/javascript" src="../../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../../util/jscalendar/calendar-win2k-2.css"); </style>
<style type="text/css">
<!--
.style4 {
	color: #FFFFFF;
	font-weight: bold;
}
-->
</style>
</head>
<body bgcolor="#FFFFFF" text="#000000">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td width="56%" class="head"><lt:Label res="res.label.forum.admin.message_op_list" key="op_list"/></td>
      <td width="44%" class="head">&nbsp;</td>
    </tr>
  </tbody>
</table>
<%
MsgOperateDb mod = new MsgOperateDb();

String op = ParamUtil.get(request, "op");

int total = 0;
int pagesize = 20;
int curpage = StrUtil.toInt(ParamUtil.get(request, "CPages"), 1);

String sql = "select id from " + mod.getTable().getName() + " order by op_date desc";

String action = ParamUtil.get(request, "action");
String searchType = ParamUtil.get(request, "searchType");
String opType = ParamUtil.get(request, "opType");
String what = ParamUtil.get(request, "what");
com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();	
try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, pvg, "what", what, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "what", what, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

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
	else if (searchType.equals("managerName")) {
		UserDb ud = new UserDb();
		ud = ud.getUserDbByNick(what.trim());
		if (ud==null || !ud.isLoaded()) {
			out.print(StrUtil.Alert_Back(StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.admin.message_op_list", "manager_lost"), new Object[] {what})));
			return;
		}
		sql = "select id from " + mod.getTable().getName() + " where user_name=" + StrUtil.sqlstr(ud.getName());
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
	
	sql += " order by op_date desc";
}

if (op.equals("del")) {
	long id = ParamUtil.getLong(request, "id");
	mod = mod.getMsgOperateDb(id);
	if (mod!=null) {
		if (mod.del())
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"res.common", "info_op_success"), "message_op_list.jsp"));
		else
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request,"res.common", "info_op_fail")));
		return;
	}
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
<table width="65%" border="0" align="center" cellpadding="0" cellspacing="0" class="p9">
  <form name="form1" action="message_op_list.jsp?action=search" method="post">
    <tr>
      <td align="center">
<lt:Label res="res.label.forum.admin.message_op_list" key="begin_date"/>
<input id="beginDate" name="beginDate" size="10" value="<%=beginDate%>">	  
<lt:Label res="res.label.forum.admin.message_op_list" key="end_date"/>
<input id="endDate" name="endDate" size="10" value="<%=endDate%>">
&nbsp;<script type="text/javascript">
    Calendar.setup({
        inputField     :    "beginDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
</script>
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "endDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
</script>
<select id="searchType" name="searchType">
<option value="title" selected="selected"><lt:Label res="res.label.forum.admin.message_op_list" key="title"/></option>
<option value="managerName"><lt:Label res="res.label.forum.admin.message_op_list" key="managerName"/></option>
<option value="userName"><lt:Label res="res.label.forum.admin.message_op_list" key="userName"/></option>
<option value="boardcode"><lt:Label res="res.label.forum.admin.message_op_list" key="boardcode"/></option>
</select>
<select id="opType" name="opType">
	  <option value=""><lt:Label res="res.label.forum.admin.message_op_list" key="op_type"/></option>
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
        &nbsp;
        <input name="Submit" type="submit" value="<lt:Label res="res.label.forum.admin.message_op_list" key="search"/>"></td>
    </tr>
  </form>
</table>
<table width="98%" border="0" align="center" class="p9">
  <tr>
    <td align="right"><%=paginator.getPageStatics(request)%></td>
  </tr>
</table>
<table style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" cellSpacing="1" cellPadding="3" width="98%" align="center">
  <tbody>
    <tr>
      <td class="thead" noWrap width="32%">
      <lt:Label res="res.label.forum.admin.message_op_list" key="topic"/>      </td>
      <td class="thead" noWrap width="11%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
        <lt:Label res="res.label.forum.admin.message_op_list" key="userName"/></td>
      <td class="thead" noWrap width="8%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
      <lt:Label res="res.label.forum.admin.message_op_list" key="manager"/>      </td>
      <td class="thead" noWrap width="5%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
      <lt:Label res="res.label.forum.admin.message_op_list" key="type"/></td>
      <td class="thead" noWrap width="10%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
      <lt:Label res="res.label.forum.admin.message_op_list" key="op_date"/></td>
      <td class="thead" noWrap width="6%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
      <lt:Label res="res.label.forum.admin.message_op_list" key="board"/></td>
      <td class="thead" noWrap width="7%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
      <lt:Label res="res.label.forum.admin.message_op_list" key="topic_status"/></td>
      <td width="12%" noWrap class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15">IP</td>
      <td width="9%" noWrap class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
      <lt:Label key="op"/></td>
    </tr>
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
		t = StrUtil.getNullStr(mod.getString("msg_title"));
	}
    if (!userName.equals(MsgOperateDb.OPERATOR_MASTER)) {
        UserDb ud = um.getUser(userName);
        userName = "<a target=_blank href='../../userinfo.jsp?username=" + StrUtil.UrlEncode(ud.getName()) + "'>" + ud.getNick() + "</a>";
    }
    else
        userName = "Administrator";	
	%>
    <tr class="row" style="BACKGROUND-COLOR: #ffffff">
      <td><%=t%></td>
      <td>
	  <%
	  UserDb user = null;
	  if (md.isLoaded())
	  	user = um.getUser(md.getName());
	  else {
	  	user = um.getUser(StrUtil.getNullStr(mod.getString("msg_user")));
	  }
	  if (user!=null) {
	  	out.print("<a target=_blank href='../../userinfo.jsp?username=" + StrUtil.UrlEncode(user.getName()) + "'>" + user.getNick() + "</a>");
	  }
	  %>	  </td>
      <td><%=userName%></td>
      <td><%=mod.getOperate(request)%></td>
      <td><span title="<%=ForumSkin.formatDateTime(request, mod.getDate("op_date"))%>"><%=ForumSkin.formatDate(request, mod.getDate("op_date"))%></span></td>
      <td>
	  <%
	  String boardcode = "";
	  if (md.isLoaded())
	  	boardcode = md.getboardcode();
	  else {
	  	boardcode = StrUtil.getNullStr(mod.getString("boardcode"));
	  }
	  if (!boardcode.equals("")) {
	  	Leaf lf = dir.getLeaf(boardcode);
		if (lf!=null)
			out.print("<a target=_blank href='../listtopic.jsp?boardcode=" + StrUtil.UrlEncode(lf.getCode()) + "'>" + lf.getName() + "</a>");
	  }
	  %>	  </td>
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
	  %>	  </td>
      <td align="center"><%=StrUtil.getNullStr(mod.getString("ip"))%></td>
      <td align="center"><a href="message_op_list.jsp?op=del&id=<%=mod.getLong("id")%>"><lt:Label key="op_del"/></a></td>
    </tr>
<%}%>
  </tbody>
</table>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
        <tr>
          <td height="23" align="right"><%
				String querystr = "action=" + action + "&searchType=" + searchType + "&opType=" + opType + "&what=" + StrUtil.UrlEncode(what) + "&beginDate=" + beginDate + "&endDate=" + endDate;
				out.print(paginator.getPageBlock(request,"?"+querystr));
				%></td>
        </tr>
</table>
</body>
</html>