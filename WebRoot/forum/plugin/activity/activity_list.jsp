<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.sql.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.setup.*"%>
<%@ page import="com.redmoon.forum.setup.*"%>
<%@ page import="com.redmoon.forum.plugin.activity.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "expire_date";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
	
String op = ParamUtil.get(request, "op");
String kind = ParamUtil.get(request, "kind");
String value = ParamUtil.get(request, "value");	

String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="../../<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<title>活动列表 - <%=Global.AppName%></title>
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "activity_list.jsp?op=<%=op%>&value=<%=StrUtil.UrlEncode(value)%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<div id="wrapper">
<%@ include file="../../inc/header.jsp"%>
<div id="main">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<div class="tableTitle">
活动列表
</div>
<%
	int pagesize = 10;
	Paginator paginator = new Paginator(request);
	
	ActivityDb ad = new ActivityDb();
	String sql = "select msg_id from " + ad.getTableName() + " order by " + orderBy + " " + sort;
	if (op.equals("search")) {
		if (kind.equals("msgId"))
			sql = "select msg_id from " + ad.getTableName() + " where msg_id=" + value;
		else {
			sql = "select a.msg_id from " + ad.getTableName() + " a left join sq_message m on m.id=a.msg_id where m.title like " +StrUtil.sqlstr("%" + value + "%") + " order by " + orderBy + " " + sort;
		}
	}

	int total = ad.getObjectCount(sql);
	paginator.init(total, pagesize);
	int curpage = paginator.getCurPage();
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0)
	{
		curpage = 1;
		totalpages = 1;
	}
%>
<form action="activity_list.jsp?op=search" method="post">
  <table width="98%" border="0" align="center" class="p9">
    <tr>
      <td width="46%" align="left">
	  <input name="value">&nbsp;
	  <select name="kind">
	     <option value="title">活动标题</option>
	     <option value="msgId">活动编号</option>
	  </select>
	  &nbsp;<input name="Submit" type="submit" class="singleboarder" value="<lt:Label res="res.label.forum.admin.user_m" key="search"/>">
	  </td>

      <td width="54%" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
    </tr>
  </table> 
</form>
  <TABLE width="98%" border=0 align=center class="tableCommon">
  <thead>
      <TR> 
        <TD width=7% height=23>编号</TD>
        <TD width=42% height=23>活动标题</TD>
        <TD width=7% height=23>发布者</TD>
        <TD width=8%>组织者</TD>
        <TD width=9%>联系方式</TD>
        <TD width=13%>参与者等级要求</TD>
        <TD width=14% height=23 onClick="doSort('expire_date')" style="cursor:pointer">
          截止时间
			<%if (orderBy.equals("expire_date")) {
				if (sort.equals("asc")) 
					out.print("<img src='../../admin/images/arrow_up.gif' width=8px height=7px>");
				else
					out.print("<img src='../../admin/images/arrow_down.gif' width=8px height=7px>");
			}%>
		</TD>
      </TR>
	</thead>
<%
	Vector v = ad.list(sql, (curpage-1)*pagesize, curpage*pagesize-1);
	Iterator ir = v.iterator();
	com.redmoon.forum.person.UserMgr um = new com.redmoon.forum.person.UserMgr();
	MsgMgr mm = new MsgMgr();
	int i = 0;
	while (ir.hasNext()) {
		ad = (ActivityDb)ir.next();
		MsgDb md = mm.getMsgDb(ad.getMsgId());
		int level = ad.getUserLevel();
		UserLevelDb uld = new UserLevelDb();
		uld = uld.getUserLevelDb(level);
		i++;
%>
    <TBODY>
      <TR> 
        <TD height=23><%=ad.getMsgId()%></TD>
        <TD width=42% height=23 align="left">&nbsp;<a href="<%=request.getContextPath()%>/forum/showtopic.jsp?rootid=<%=ad.getMsgId()%>" target="_blank"><%=md.getTitle()%></a></TD>
        <TD width=7% height=23><a target="_blank" href="<%=request.getContextPath()%>/userinfo.jsp?username=<%=md.getName()%>"><%=um.getUser(md.getName()).getNick()%></a></TD>
        <TD width=8%><a target="_blank" href="<%=request.getContextPath()%>/userinfo.jsp?username=<%=StrUtil.UrlEncode(ad.getOrganizer())%>"><%=StrUtil.getNullStr(um.getUser(ad.getOrganizer()).getNick())%></a></TD>
        <TD width=9%><%=ad.getTel()%></TD>
        <TD width=13%><%=uld.getDesc()%></TD>
        <TD width=14% height=23><%=DateUtil.format(ad.getExpireDate(), "yyyy-MM-dd")%></TD>
      </TR>
    </TBODY>
<%
	}
%>
  </TABLE> 
  <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="per100">
    <tr> 
      <td height="23" align="right">
      <%
	  String querystr = "op=" + op + "&kind=" + kind + "&value=" + StrUtil.UrlEncode(value) + "&orderBy=" + orderBy + "&sort=" + sort;
 	  out.print(paginator.getCurPageBlock("activity_list.jsp?"+querystr));
	%>
    </td>
    </tr>
  </table>
</div>
<%@ include file="../../inc/footer.jsp"%>
</div>
</body>
</html>