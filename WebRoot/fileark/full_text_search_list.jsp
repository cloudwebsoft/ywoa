<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.db.Paginator"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.security.*"%>
<%@ page import = "com.redmoon.oa.fileark.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="org.apache.lucene.search.*,org.apache.lucene.document.*" %>
<%@page import="com.redmoon.oa.person.UserDb"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String queryString = ParamUtil.get(request, "queryString");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "queryString", queryString, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
	
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>全文检索</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
String fieldName = ParamUtil.get(request, "fieldName");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "fieldName", fieldName, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if (fieldName.equals(""))
	fieldName = "content";
%>
<br>
<FORM name=form1 action="full_text_search_list.jsp" method=get>
<TABLE align="center" cellPadding=2 cellSpacing=1 id=AutoNumber1>
    <TBODY>
      <TR> 
        <TD width="758" height=23 align="center">
        请选择&nbsp;<SELECT size=1 name="fieldName">
		    <OPTION value="content" selected>内容</OPTION>
            <OPTION value="title">标题</OPTION>
        </SELECT>&nbsp;&nbsp;
        <input type="text" size=40 name="queryString" value="<%=queryString%>">
        <script>
		o("fieldName").value = "<%=fieldName%>";
		</script>
        &nbsp;
        <input type=submit value="全文检索" class="btn"/></TD>
      </TR></TBODY>
</TABLE>
</FORM>
<%
	if(queryString.equals("")){
		// out.print(StrUtil.Alert_Redirect("请填写关键字！","search.jsp"));
		return;
	}
	
	Indexer indexer = new Indexer();
	Hits hits = indexer.seacher(queryString, fieldName);
	if (hits==null || hits.length()==0) {
		out.print(SkinUtil.makeInfo(request, "未找到符合条件的记录！"));
		return;
	}	

	int pagesize = 10;
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();
	paginator.init(hits.length(), pagesize);
	
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0) {
		curpage = 1;
		totalpages = 1;
	}
%>
      <table width="92%" border="0" align="center" class="p9">
        <tr>
          <td height="24" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
        </tr>
      </table>
      <table class="tabStyle_1 percent98" width="96%" border="0" align="center" cellpadding="1" cellspacing="1">
      	<thead>
        <tr>
          <td class="tabStyle_1_title" width="54%" height="21" align="center">标题</td>
          <td class="tabStyle_1_title" width="17%" align="center">作者</td>
          <td class="tabStyle_1_title" width="22%" align="center">修改日期</td>
          <td class="tabStyle_1_title" width="7%" align="center">点击率</td>
        </tr>
        </thead>
<%
	String title = "", modifiedDate="";
	int id = -1;
	int hit=0, i=0;
	int j = (curpage-1)*pagesize;
	
	if (j>hits.length()-1)
		j = hits.length() - 1;
	int end = curpage*pagesize;
	
	if (end>hits.length())
		end = hits.length();
	
	com.redmoon.oa.fileark.Document document = null;
	com.redmoon.oa.fileark.DocumentMgr dm = new com.redmoon.oa.fileark.DocumentMgr();
	while (j < end) {
		org.apache.lucene.document.Document doc = hits.doc(j);
		id = Integer.parseInt(doc.get("id"));
		document = dm.getDocument(id);
		if (document==null || !document.isLoaded()) {
			j++;
			continue;
		}
		title = document.getTitle();
		hit = document.getHit();
		modifiedDate = DateUtil.format(document.getModifiedDate(), "yyyy-MM-dd");		
		String bgcolor = "";
		if (i==1)
			bgcolor = "#F2F2F2";
		else if (i==0)
			bgcolor = "";
		i++;
		if (i==2)
			i = 0;
		UserDb ud = new UserDb(document.getAuthor());
		%>
		<tr>
          <td width="54%" height=23 align="left" bgcolor="<%=bgcolor%>">&nbsp;
			  <a href="javascript:;" onclick="addTab('<%=title%>', '<%=request.getContextPath()%>/doc_show.jsp?id=<%=id%>')"><%=title%></a>
          </td>
          <td width="17%" align="center" bgcolor="<%=bgcolor%>"><%=ud.getRealName()%></td>
          <td width="22%" align="center" bgcolor="<%=bgcolor%>"><%=modifiedDate%></td>
		  <td width="7%" align="center" bgcolor="<%=bgcolor%>"><%=hit%></td>
		</tr>
<%
		j++;
	}
%>
      </table>
      <table width="92%"  border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
          <td height="26" align="right">
<%
	String querystr = "queryString=" + StrUtil.UrlEncode(queryString) + "&fieldName=" + StrUtil.UrlEncode(fieldName);
    out.print(paginator.getCurPageBlock("full_text_search_list.jsp?"+querystr));
%>       </td>
        </tr>
      </table>
</body>
</html>