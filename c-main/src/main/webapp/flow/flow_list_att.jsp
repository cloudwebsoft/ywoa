<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

String typeCode = ParamUtil.get(request, "typeCode");
typeCode = "fawen";
String typeName = "";
if (!typeCode.equals("")) {
	Leaf lf = new Leaf();
	lf = lf.getLeaf(typeCode);
	if (lf!=null)
		typeName = "&nbsp;-&nbsp;"+lf.getName()+"&nbsp;";
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>发文</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../inc/common.js"></script>
</head>
<body>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">发文</td>
    </tr>
  </tbody>
</table>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	out.print(StrUtil.makeErrMsg("标识非法！"));
	return;
}
int pagesize = 20;
int curpage = Integer.parseInt(strcurpage);

WorkflowDb wf = new WorkflowDb();

String sql = "select id from flow where type_code=" + StrUtil.sqlstr(typeCode) + " and status=" + WorkflowDb.STATUS_FINISHED;
sql += " order by begin_date desc";

ListResult lr = wf.listResult(sql, curpage, pagesize);
long total = lr.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}

Vector v = lr.getResult();
Iterator ir = v.iterator();
%>
<br>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td height="24" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %><%
	String querystr = "op=" + op + "&typeCode=" + StrUtil.UrlEncode(typeCode);
    out.print(paginator.getCurPageBlock("?"+querystr, "up"));%></b></td>
  </tr>
</table>
<table width="98%" class="tabStyle_1 percent98">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" noWrap width="32%">文件标题</td>
      <td class="tabStyle_1_title" noWrap width="14%">时间</td>
    </tr>
<%
Leaf ft = new Leaf();
UserMgr um = new UserMgr();
DocumentMgr dm = new DocumentMgr();
while (ir.hasNext()) {
 	WorkflowDb wfd = (WorkflowDb)ir.next();
	Document doc = dm.getDocument(wfd.getDocId());
	java.util.Vector attachments = doc.getAttachments(1);
	if (attachments.size()==0)
		continue;
	UserDb user = null;
	if (wfd.getUserName()!=null)
		user = um.getUserDb(wfd.getUserName());
	String userRealName = "";
	if (user!=null)
		userRealName = user.getRealName();
		
	java.util.Iterator ir2 = attachments.iterator();
	if (ir2.hasNext()) {
		Attachment am = (Attachment) ir2.next();
		%>
    <tr class="highlight">
      <td><a href="../flow_getfile.jsp?attachId=<%=am.getId()%>&amp;flowId=<%=wfd.getId()%>" target="_blank"><span id="spanAttName<%=am.getId()%>"><%=am.getName()%></span></a></td>
      <td align="center"><%=DateUtil.format(wfd.getEndDate(), "yy-MM-dd HH:mm:ss")%> </td>
    </tr>		
<%
	}
}%>
  </tbody>
</table>
<table width="98%" border="0" cellpadding="0" cellspacing="0" align="center">
  <tr>
    <td width="48%" align="left">&nbsp;</td>
    <td width="52%" align="right"><%
    out.print(paginator.getCurPageBlock("?"+querystr, "down"));
%></td>
  </tr>
</table>
</body>
</html>