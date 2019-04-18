<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.SkinUtil"%>
<%@ page import = "com.redmoon.oa.account.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>帐号管理</title>
<meta http-equiv="X-UA-Compatible"content="IE=9; IE=8; IE=7; IE=EDGE" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
</head>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
if (!privilege.isUserPrivValid(request, "admin.user")) {
    out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String searchUnitCode = ParamUtil.get(request, "searchUnitCode");
String unitCode = searchUnitCode;
if (unitCode.equals(""))
	unitCode = privilege.getUserUnitCode(request);
	
// out.print("unitCode=" + unitCode);
String by = ParamUtil.get(request, "by");
if (by.equals(""))
	by = "userName";
String what = ParamUtil.get(request, "what");

String op = ParamUtil.get(request, "op");
if (op.equals("del")) {
	AccountMgr am = new AccountMgr();
	boolean re = false;
	try {
		  re = am.del(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	if (re) {
		out.print(StrUtil.Alert_Redirect("操作成功！", "account_list.jsp?op=search&by=" + by + "&what=" + StrUtil.UrlEncode(what) + "&searchUnitCode=" + searchUnitCode));
	}
	return;
}
%>
<body>
<%@ include file="account_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<form action="?op=search" name=form1 method=post>
  <table width="80%" border="0" align="center">
  <tr>
    <td width="100%" align="center"><%if (privilege.getUserUnitCode(request).equals(DeptDb.ROOTCODE)) {%>
      <select id="searchUnitCode" name="searchUnitCode">
        <option value="">全部</option>
        <%
        if (License.getInstance().isPlatformSrc()) {
			DeptDb dd = new DeptDb();
			DeptView dv = new DeptView(request, dd);
			StringBuffer sb = new StringBuffer();
			dd = dd.getDeptDb(DeptDb.ROOTCODE);
		%>
        <%=dv.getUnitAsOptions(sb, dd, dd.getLayer())%>
        <%
				}
			  %>
      </select>
      <script>
			  o("searchUnitCode").value = "<%=searchUnitCode%>";
			  </script>
      <%}%>
      按&nbsp;
      <select id="by" name="by">
      <option value='userName'>姓名</option>
      <option value='account'>工号</option>
    </select>
    &nbsp;
    <input id="what" name="what" value="<%=what%>" size="20" />
    &nbsp;
    <input name="submit" type="submit" class="btn" value="搜索" />
    <script>
	o("by").value = "<%=by%>";
	</script>
    </td>
    </tr>
</table>
</form>
<%
		String sql = "select name from account a where 1=1";
		if (op.equals("search")) {
			if (by.equals("userName")) {
				sql = "select a.name from account a, users u where a.userName=u.name and u.realName like " + StrUtil.sqlstr("%" + what + "%");
			}
			else if (by.equals("account")) {
				sql = "select name from account a where name like " + StrUtil.sqlstr("%" + what + "%");			
			}
		}
		
		if (!searchUnitCode.equals("")) {
			sql += " and a.unit_code=" + StrUtil.sqlstr(searchUnitCode);
		}
		else {
			boolean isAdmin = privilege.isUserPrivValid(request, "admin.user") && unitCode.equals(DeptDb.ROOTCODE);
			if (!isAdmin)
				sql += " and a.unit_code=" + StrUtil.sqlstr(unitCode);
		}
		
		sql += " order by a.name asc";
		// out.print(sql);
		
		String querystr = "op=" + op + "&by=" + by + "&what=" + StrUtil.UrlEncode(what) + "&searchUnitCode=" + searchUnitCode;
		int pagesize = 20;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		AccountDb adb = new AccountDb();
		ListResult lr = adb.listResult(sql, curpage, pagesize);
		int total1 = lr.getTotal();
		Vector v = lr.getResult();
	    Iterator ir1 = null;
		if (v!=null)
			ir1 = v.iterator();
		paginator.init(total1, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}

%>
<table width="98%" border="0" cellpadding="0" cellspacing="0" class="percent98">
  <tr><td align="right">
&nbsp;找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b>
</td></tr></table>
<table id="mainTable" width="100%" border="0" align="center" cellspacing="0" class="tabStyle_1">
  <tr align="center">
    <td width="145" class="tabStyle_1_title">工号</td>
    <td width="214" class="tabStyle_1_title">姓名</td>
    <td width="434" class="tabStyle_1_title">部门</td>
    <td width="158" class="tabStyle_1_title">操作</td>
  </tr>
    <%
	  	String userRealName = "";
		String userName = "";
		DeptUserDb dud = new DeptUserDb();
		while (ir1!=null && ir1.hasNext()) {
		  adb = (AccountDb)ir1.next();
		  UserDb user = new UserDb();
		  userName = StrUtil.getNullString(adb.getUserName());
		  userRealName = "";
		  if (!userName.equals("")) {
			  user = user.getUserDb(adb.getUserName());
			  if (user!=null && user.isLoaded()) {
				userRealName = user.getRealName();
			  }
		  }
	%>
  <tr align="center">	
    <td bgcolor="#FFFFFF"><%=adb.getName()%></td>
        <td bgcolor="#FFFFFF"><%=userRealName%></td>
        <td bgcolor="#FFFFFF"><%
		DeptMgr dm = new DeptMgr();
		Vector v2 = dud.getDeptsOfUser(userName);
		Iterator ir2 = v2.iterator();
		while (ir2.hasNext()) {
			DeptDb dd = (DeptDb)ir2.next();
			if (!dd.getParentCode().equals(DeptDb.ROOTCODE)) {
				out.print(dm.getDeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName() + "&nbsp;&nbsp;");
			}
			else
				out.print(dd.getName() + "&nbsp;&nbsp;");
		}
	%></td>
    <td bgcolor="#FFFFFF"><a href="account_edit.jsp?name=<%=StrUtil.UrlEncode(adb.getName())%>">修改</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="#" onClick="if (confirm('您确定要删除工号<%=adb.getName()%>吗？\r\n提示：删除工号并不会删除用户！')) window.location.href='?op=del&name=<%=StrUtil.UrlEncode(adb.getName())%>'">删除</a></td>
  </tr>
  <%}%>
</table>
<table width="98%" class="percent98">
      <tr>
        <td align="right"><%
			   out.print(paginator.getCurPageBlock("?"+querystr));
			 %></td>
      </tr>
</table>
</body>
<script>
$(document).ready( function() {
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
});
</script>
</html>
