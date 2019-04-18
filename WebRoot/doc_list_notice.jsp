<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<html>
<head>
<title>通知列表</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="common.css" type="text/css">
</head>
<body bgcolor="#FFFFFF" text="#000000" style="overflow:auto">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%
String dir_code = ParamUtil.get(request, "dir_code");
if (dir_code.equals("")) {
	out.print(fchar.makeErrMsg("类别编码不能为空！"));
	return;
}
Leaf lf = new Leaf();
lf = lf.getLeaf(dir_code);
if (!lf.isLoaded()) {
	out.print(fchar.makeErrMsg("节点 " + dir_code + " 不存在！"));
	return;
}
Document document = new Document();
%>
<table width="100%" height="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe">
  <tr>
    <td width="100%" height="21" class="right-title">&nbsp;<img src="images/left/icon-notice.gif" align="absmiddle">&nbsp;通知</td>
  </tr>
  <tr>
    <td valign="top" bgcolor="#FFFFFF"><%
		String sql="select id from document where class1=" + StrUtil.sqlstr(dir_code);
		
		if (!SecurityUtil.isValidSql(sql)) {
			out.print(fchar.p_center("标识非法！"));
			return;
		}
		
		int pagesize = 20;
		
	    int total = document.getDocCount(sql);

		int curpage,totalpages;
		Paginator paginator = new Paginator(request, total, pagesize);
        //设置当前页数和总页数
	    totalpages = paginator.getTotalPages();
		curpage	= paginator.getCurrentPage();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}
%>
      <table width="92%" border="0" align="center" class="p9">
        <tr>
          <td height="24" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
        </tr>
      </table>
      <table width="100%"  border="0" align="center" cellpadding="1" cellspacing="1">
        <tr>
          <td width="71%" height="21" align="center" bgcolor="#C4DAFF" class="line6">标&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 题</td>
          <td width="19%" align="center" bgcolor="#C4DAFF" class="line6">日 期</td>
          <td width="10%" align="center" bgcolor="#C4DAFF" class="line6">点击率</td>
        </tr>
      </table>
      <table width=100% height="28" border=0 align="center" cellpadding="0" cellspacing="1" class="p9">
        <%@ taglib uri="/WEB-INF/tlds/DocListTag.tld" prefix="dl" %>
		<%int i = 0;%>
        <dl:DocListTag action="list" query="" dirCode="<%=dir_code%>" start="<%=(curpage-1)*pagesize%>" end="<%=curpage*pagesize%>">
		<%
		String bgcolor = "";
		if (i==1)
			bgcolor = "#F2F2F2";
		if (i==0)
			bgcolor = "";
		i++;
		if (i==2)
			i = 0;
		%>
        <dl:DocListItemTag field="title" mode="detail">
		<tr>
          <td width="71%" height=23 align="left" bgcolor="<%=bgcolor%>">&nbsp;<a href="doc_show_notice.jsp?id=$id">$title</a></td>
          <td width="19%" align="center" bgcolor="<%=bgcolor%>">[$modifiedDate]</td>
        <td width="10%" align="center" bgcolor="<%=bgcolor%>">$hit</td>
		</tr>
		</dl:DocListItemTag>
        </dl:DocListTag>
      </table>
      <table width="96%"  border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
          <td height="26" align="right">
<%
	String querystr = "dir_code=" + StrUtil.UrlEncode(dir_code);
    out.print(paginator.getCurPageBlock("?"+querystr));
%>       </td>
        </tr>
    </table></td>
  </tr>
</table>
</body>
</html>