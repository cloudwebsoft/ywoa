<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ include file="inc/inc.jsp"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>日程列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script language=javascript>
<!--
function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}
//-->
</script>
<script src="inc/common.js"></script>
</head>
<body>
<%@ include file="plan_inc_menu_top.jsp"%>
<script>
$("menu2").className="current";
</script>
<%
		if (!privilege.isUserLogin(request)) {
			out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
		int pagesize = 10;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();		
		String action = ParamUtil.get(request, "action");
		String what = ParamUtil.get(request, "what");
		
		String op = ParamUtil.get(request, "op");
		if (op.equals("del")) {
			PlanMgr pm = new PlanMgr();
			boolean re = false;
			try {
				re = pm.del(request);
			}
			catch (ErrMsgException e) {
				out.print(fchar.Alert_Back(e.getMessage()));
			}
			if (re) {
				out.print(fchar.Alert_Redirect("删除成功！", "plan_list.jsp?action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&CPages=" + curpage));
			}
			return;
		}
		
		String sql;
		String myname = privilege.getUser(request);
		sql = "select id from user_plan where username=" + fchar.sqlstr(privilege.getUser(request));
		
		String y = ParamUtil.get(request, "year");
		String m = ParamUtil.get(request, "month");
		String d = ParamUtil.get(request, "day");
		if (!y.equals("")) {
			sql += " and " + SQLFilter.year("myDate") + "=" + y + " and " + SQLFilter.month("myDate") + "=" + m + " and " + SQLFilter.day("myDate") + "=" + d;
		}
		
		if (action.equals("search") && !what.equals("")) {
			sql += " and title like " + StrUtil.sqlstr("%" + what + "%");
		}
		
		sql += " order by myDate desc";
		// out.print(sql);
			
		PlanDb pd = new PlanDb();
		
		ListResult lr = pd.listResult(sql, curpage, pagesize);
		int total = lr.getTotal();
		Vector v = lr.getResult();
	    Iterator ir = null;
		if (v!=null)
			ir = v.iterator();
		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
%>
<form action="plan_list.jsp" method="get" name="formSearch" id="formSearch">
<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr>
	<td align="center">&nbsp;&nbsp;标题&nbsp;&nbsp;
	  <input name="what" value="<%=what%>" />
		<input name="action" value="search" type="hidden" />
		<input name="submit" type="submit" value="搜索" />	</td>
	</tr>
</table>
</form>
<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right"> 找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<table width="98%" align="center" class="tabStyle_1 percent98">
  <tr>
    <td class="tabStyle_1_title">安排日期</td>
    <td class="tabStyle_1_title">标题</td>
    <td width="17%" class="tabStyle_1_title">操作</td>
  </tr>
<%	
	    int id;
		String title, mydate;
		while (ir!=null && ir.hasNext()) {
			pd = (PlanDb)ir.next();
			id = pd.getId();
			title = pd.getTitle();
			mydate = DateUtil.format(pd.getMyDate(), "yyyy-MM-dd HH:mm:ss");
		%>
  <tr>
    <td width="18%" align="center"><%=mydate%></td>
    <td width="65%"><a href="plan_show.jsp?id=<%=id%>"><%=title%></a></td>
    <td align="center"><a href="plan_edit.jsp?id=<%=id%>">编辑</a>&nbsp;&nbsp;&nbsp;&nbsp;<a onClick="return confirm('您确定要删除么？')" href="plan_list.jsp?op=del&id=<%=id%>&action=<%=action%>&what=<%=StrUtil.UrlEncode(what)%>&CPages=<%=curpage%>">删除</a></td>
  </tr>
<%}%>
</table>
<br />
<table width="98%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td height="23" align="right">&nbsp;
        <%
				String querystr = "action=" + action + "&what=" + StrUtil.UrlEncode(what);
				out.print(paginator.getCurPageBlock("plan_list.jsp?"+querystr));
				%>
      &nbsp;&nbsp;</td>
  </tr>
</table>
<br />
<br />
<p><br />
</p>
</body>
</html>
