<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>文章列表</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<%
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
<br />
<table width="98%" align="center" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td height="25" valign="middle" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
  </tr>
</table>
<table class="tabStyle_1 percent98">
  <tr>
    <td width="70%" class="tabStyle_1_title" align="center">标&nbsp;&nbsp;&nbsp;&nbsp;题</td>
    <td width="20%" class="tabStyle_1_title" align="center">日&nbsp;&nbsp;&nbsp;&nbsp;期</td>
    <td width="10%" class="tabStyle_1_title" align="center">点击率</td>
  </tr>
  <%@ taglib uri="/WEB-INF/tlds/DocListTag.tld" prefix="dl" %>
  <%int i = 0;%>
  <dl:DocListTag action="list" query="" dirCode="<%=dir_code%>" start="<%=(curpage-1)*pagesize%>" end="<%=curpage*pagesize%>">
    <dl:DocListItemTag field="title" mode="detail">
      <tr class="highlight">
        <td><a href="doc_show.jsp?id=$id">$title</a></td>
        <td align="center">[$modifiedDate]</td>
        <td align="center">$hit</td>
      </tr>
    </dl:DocListItemTag>
  </dl:DocListTag>
</table>
<table width="98%" align="center" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right"><%
	String querystr = "dir_code=" + StrUtil.UrlEncode(dir_code);
    out.print(paginator.getCurPageBlock("doc_list.jsp?"+querystr));
%>
    </td>
  </tr>
</table>
</body>
</html>