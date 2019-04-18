<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>回收站</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<%
if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String sql = "select class1,title,id,isHome,examine,modifiedDate,color,isbold,expire_date,type from document where examine=" + Document.EXAMINE_DUSTBIN;
String op = StrUtil.getNullString(request.getParameter("op"));
String dir_code = ParamUtil.get(request, "dir_code");
Leaf leaf = dir.getLeaf(dir_code);
String dir_name = "";
if (leaf!=null)
	dir_name = leaf.getName();
if (op.equals("del")) {
	int id = ParamUtil.getInt(request, "id");
	try {
		if (docmanager.del(request, id, privilege, false))
			out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_operate_success")));
		else 
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	op = "";
}
else if (op.equals("clear")) {
	try {
		docmanager.clearDustbin(request);
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_operate_success")));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}
else if (op.equals("delBatch")) {
	try {
		docmanager.delBatch(request, false);
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "dustbin.jsp"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}
else if (op.equals("resumeBatch")) {
	try {
		docmanager.resumeBatch(request);
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_operate_success")));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}
else if (op.equals("resume")) {
	try {
		int id = ParamUtil.getInt(request, "id");
		if (docmanager.resume(request, id))
			out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_operate_success")));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">回收站</td>
    </tr>
  </tbody>
</table>
<%
String what = "";
String kind = "";
if (op.equals("search")) {
	kind = ParamUtil.get(request, "kind");
	what = StrUtil.UnicodeToUTF8(StrUtil.getNullString(request.getParameter("what")));
	if (kind.equals("title"))
		sql += " and title like "+StrUtil.sqlstr("%"+what+"%");
	// else if (kind.equals("content"))
	// 	sql += " and content like " + StrUtil.sqlstr("%" + what + "%");
	else
		sql += " and keywords like " + StrUtil.sqlstr("%" + what + "%");
}
else {
	if (!dir_code.equals(""))
		sql += " and class1=" + StrUtil.sqlstr(dir_code);
}

sql += " order by examine asc, isHome desc, modifiedDate desc";

String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	out.print(StrUtil.makeErrMsg(StrUtil.Alert_Back(SkinUtil.LoadString(request, "err_id"))));
	return;
}
int pagesize = 15;
int curpage = Integer.parseInt(strcurpage);
PageConn pageconn = new PageConn(Global.getDefaultDB(), Integer.parseInt(strcurpage), pagesize);
ResultIterator ri = pageconn.getResultIterator(sql);
ResultRecord rr = null;

Paginator paginator = new Paginator(request, pageconn.getTotal(), pagesize);
//设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}
%>
<br>
<table width="98%"  border="0" align="center" cellpadding="0" cellspacing="0" class="p9">
  <form name="form1" action="dustbin.jsp?op=search" method="post">
    <tr>
      <td align="center"><lt:Label res="res.label.cms.doc" key="an"/>
        <select name="kind">
          <option value="title"><lt:Label res="res.label.cms.doc" key="title"/></option>
          <option value="keywords"><lt:Label res="res.label.cms.doc" key="key_words"/></option>
        </select>
&nbsp;
<input name=what size=20>
&nbsp;
<input class="btn" name="Submit" type="submit" value=<%=SkinUtil.LoadString(request, "res.label.cms.doc","search")%>></td>
    </tr>
  </form>
</table>
<table width="92%" border="0" align="center" class="p9">
  <tr>
    <td height="24" align="right"><lt:Label res="res.label.cms.doc" key="found_right_list"/><b><%=paginator.getTotal() %></b><lt:Label res="res.label.cms.doc" key="page_list"/><b><%=paginator.getPageSize() %></b><lt:Label res="res.label.cms.doc" key="page"/><b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
  </tr>
</table>
<table class="tabStyle_1" cellSpacing="0" cellPadding="3" width="98%" align="center">
  <tbody>
    <tr>
      <td width="8%" align="center" noWrap class="tabStyle_1_title" style="PADDING-LEFT: 10px">编号</td>
      <td width="32%" align="center" noWrap class="tabStyle_1_title" style="PADDING-LEFT: 10px"><lt:Label res="res.label.cms.doc" key="title"/></td>
      <td width="15%" align="center" noWrap class="tabStyle_1_title">栏目</td>
      <td width="11%" align="center" noWrap class="tabStyle_1_title"><lt:Label res="res.label.cms.doc" key="type"/></td>
      <td width="12%" align="center" noWrap class="tabStyle_1_title"><lt:Label res="res.label.cms.doc" key="modify_date"/></td>
      <td width="22%" align="center" noWrap class="tabStyle_1_title"><lt:Label res="res.label.cms.doc" key="mgr"/></td>
    </tr>
    <%
while (ri.hasNext()) {
 	rr = (ResultRecord)ri.next(); 
	boolean isHome = rr.getBoolean("isHome");
	String color = StrUtil.getNullStr(rr.getString("color"));
	boolean isBold = rr.getInt("isBold")==1;
	java.util.Date expireDate = DateUtil.parse(rr.getString("expire_date"));
	%>
    <tr onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1">
      <td><input name="ids" type="checkbox" value="<%=rr.getInt("id")%>">
      <%=rr.getInt("id")%></td>
      <td style="PADDING-LEFT: 10px"><%if (DateUtil.compare(new java.util.Date(), expireDate)==2) {%>
        <a href="../doc_show.jsp?id=<%=rr.getInt("id")%>" title="<%=rr.getString(2)%>">
        <%
		if (isBold)
			out.print("<B>");
		if (!color.equals("")) {
		%>
        <font color="<%=color%>">
        <%}%>
        <%=(String)rr.get(2)%>
        <%if (!color.equals("")) {%>
        </font>
        <%}%>
        <%
		if (isBold)
			out.print("</B>");
		%>
        </a>
        <%}else{%>
        <a target="_blank" href="../doc_show.jsp?id=<%=rr.getInt("id")%>" title="<%=rr.getString(2)%>"><%=(String)rr.get(2)%></a>
      <%}%></td>
      <td align="center">
	  <%
	  Leaf lf6 = dir.getLeaf(rr.getString("class1"));
	  if (lf6!=null)
		  out.print("<a href='document_list_m.jsp?dir_code=" + StrUtil.UrlEncode(lf6.getCode()) + "'>" + lf6.getName() + "</a>");
	  %>	  </td>
      <td align="center">
	  <%
	  if (rr.getString("type").equals("0"))
	  	out.print("文章");
	  else if (rr.getString("type").equals("1"))
	  	out.print("投票");
	  else
	  	out.print("链接");
	  %>	  </td>
      <td align="center"><%
	  java.util.Date d = DateUtil.parse(rr.getString("modifiedDate"));
	  if (d!=null)
	  	out.print(DateUtil.format(d, "yy-MM-dd HH:mm"));
	  %></td>
      <td align="center"><a href="../fwebedit.jsp?op=edit&id=<%=rr.getInt("id")%>&dir_code=<%=StrUtil.UrlEncode((String)rr.get(1))%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>">[<lt:Label res="res.label.cms.doc" key="edit"/>]</a> <a onClick="return confirm('您确定要删除吗？')" href="?op=del&id=<%=rr.getString(3)%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>">[<lt:Label res="res.label.cms.doc" key="del"/>]</a> <a target="_blank" href="../doc_show.jsp?id=<%=rr.getInt("id")%>">[<lt:Label res="res.label.cms.doc" key="view"/>]</a> <a href="?op=resume&id=<%=rr.getInt("id")%>">[恢复]</a></td>
    </tr>
    <%}%>
  </tbody>
</table>
<table width="96%"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td colspan="2" align="right">&nbsp;</td>
  </tr>
  <tr>
    <td width="49%" align="left"><input class="btn" name="button3" type="button" onClick="selAllCheckBox('ids')" value="<lt:Label res="res.label.forum.topic_m" key="sel_all"/>">
&nbsp;&nbsp;
<input class="btn" name="button3" type="button" onClick="clearAllCheckBox('ids')" value="<lt:Label res="res.label.forum.topic_m" key="clear_all"/>">
&nbsp;
<input class="btn" name="button3" type="button" onClick="doDel()" value="<lt:Label key="op_del"/>">
&nbsp;
<input class="btn" name="button" type="button" onClick="doResume()" value="恢复">
&nbsp;
<input class="btn" type="button" onClick="window.location.href='?op=clear'" value="清空回收站"></td>
    <td width="51%" align="right"><%
	String querystr = "op=" + op + "&dir_code=" + StrUtil.UrlEncode(dir_code) + "&op=" + op + "&kind=" + kind + "&what=" + StrUtil.UrlEncode(what);
    out.print(paginator.getCurPageBlock("dustbin.jsp?"+querystr));
%></td>
  </tr>
</table>
</body>
<script src="../inc/common.js"></script>
<script>
function doResume() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("<lt:Label res="res.label.forum.topic_m" key="need_id"/>");
		return;
	}
	window.location.href = "?op=resumeBatch&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&ids=" + ids;
}

function doDel() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("<lt:Label res="res.label.forum.topic_m" key="need_id"/>");
		return;
	}
	window.location.href = "?op=delBatch&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&ids=" + ids;
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
</html>