<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.worklog.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script language="JavaScript" type="text/JavaScript">
<!--
function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}
//-->
</script>
<script language=javascript>
<!--
function openWin(url,width,height) {
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}
//-->
</script>
</head>
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0">
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String userName = ParamUtil.get(request, "userName");
if(userName.equals("")){
	userName = privilege.getUser(request);
}
%>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="100%" height="23" class="tdStyle_1">&nbsp;<span>工作报告 &nbsp;<a href="mywork.jsp?userName=<%=StrUtil.UrlEncode(userName)%>">本月</a></span></td>
  </tr>
  <tr>
    <td valign="top">
<%
String op = ParamUtil.get(request, "op");

if (op.equals("add"))
{
	boolean re = false;
	try {
		WorkLogMgr wlm = new WorkLogMgr();
		re = wlm.create(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re)
		out.print(StrUtil.Alert("操作成功！"));
}

String what = ParamUtil.get(request, "what");
%>
<table width="98%" border="0" align="center">
          <form method="post" action="?op=search&userName=<%=StrUtil.UrlEncode(userName)%>">
        <tr> 
          <td align="center">搜索&nbsp;
            <input name="what" value="<%=what%>">
          &nbsp;
          <input type="submit" value="确定"></td>
        </tr>
          </form>		  
      </table>
<%
String sql = "select id from work_log where userName="+StrUtil.sqlstr(userName)+" order by myDate desc";
if (op.equals("search")) {
	sql = "select id from work_log where userName="+StrUtil.sqlstr(userName)+" and content like " + StrUtil.sqlstr("%" + what + "%") + " order by myDate desc";
}

String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	out.print(StrUtil.makeErrMsg("标识非法！"));
	return;
}
int pagesize = 20;
int curpage = Integer.parseInt(strcurpage);
WorkLogDb wld = new WorkLogDb();
ListResult lr = wld.listResult(sql, curpage, pagesize);

long total = lr.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}

Iterator ir = lr.getResult().iterator();
%>
      <table width="92%" border="0" align="center" class="p9">
        <tr>
          <td height="24" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
        </tr>
      </table>
      <table width="98%" class="tabStyle_1 percent98" align="center" cellpadding="2" cellspacing="0">
        <tr align="center" bgcolor="#C4DAFF"> 
          <td width="12%" class="tabStyle_1_title">日期</td>
          <td width="88%" class="tabStyle_1_title">内 容</td>
        </tr>
      <%
		while (ir.hasNext()) {
			wld = (WorkLogDb)ir.next();
		%>
        <tr align="center"> 
          <td width="12%"><%=DateUtil.format(wld.getMyDate(), "yyyy-MM-dd")%></td>
          <td width="88%" align="left"><a title="查看" href="mywork_show.jsp?id=<%=wld.getId()%>&userName=<%=StrUtil.UrlEncode(userName)%>"><%=StrUtil.getAbstract(request, wld.getContent(), 120)%></a></td>
          </tr>
<%}%>
        </table>
<table width="96%"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right">&nbsp;</td>
  </tr>
  <tr>
    <td align="right"><%
	String querystr = "op=" + op + "&userName=" + StrUtil.UrlEncode(userName) + "&what=" + StrUtil.UrlEncode(what);
    out.print(paginator.getCurPageBlock("?"+querystr));
%></td>
  </tr>
</table></td>
  </tr>
  <tr>
    <td height="9">&nbsp;</td>
  </tr>
</table>
</body>
</html>
