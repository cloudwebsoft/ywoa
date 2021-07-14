<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ include file="../inc/nocache.jsp"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html>
<html>
<head>
<title>请假</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">当前请假人员</td>
    </tr>
  </tbody>
</table>
<%
// STATUS_FINISHED说明已经销假，流程已完毕
String now = SQLFilter.getDateStr(DateUtil.format(new java.util.Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
String sql = "select f.flowId from form_table_qjsqd f, flow fl where f.flowId=fl.id and " + now + ">=qjkssj" + " and " + now + "<=qjjssj and f.flowTypeCode='qj' and f.result='1' and fl.status=" + WorkflowDb.STATUS_STARTED + " order by fl.mydate desc";

FormDb fd = new FormDb();
fd = fd.getFormDb("qjsqd");
%>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	out.print(StrUtil.makeErrMsg("标识非法！"));
	return;
}
int pagesize = 30;
int curpage = Integer.parseInt(strcurpage);

String op = ParamUtil.get(request, "op");

// out.print(sql);

WorkflowDb wf = new WorkflowDb();
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
Iterator ir = null;
if (v!=null)
	ir = v.iterator();
%>
<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td height="24" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
  </tr>
</table>
<table width="62%" class="tabStyle_1 percent98">
  <tbody>
    <tr>
      <td width="16%" class="tabStyle_1_title">请假人</td>
      <td width="13%" class="tabStyle_1_title">部门</td>
      <td width="13%" class="tabStyle_1_title">开始日期</td>
      <td width="12%" class="tabStyle_1_title">结束日期</td>
      <td width="16%" class="tabStyle_1_title">请假时间</td>
      <!--<td width="18%" class="tabStyle_1_title">审批者</td>-->
    </tr>
<%
Leaf ft = new Leaf();
UserMgr um = new UserMgr();
FormDAO fdao = new FormDAO();
DeptMgr dm = new DeptMgr();
while (ir.hasNext()) {
 	WorkflowDb wfd = (WorkflowDb)ir.next();
	fdao = fdao.getFormDAO(wfd.getId(), fd);
	String strBeginDate = fdao.getFieldValue("qjkssj");
	String strEndDate = fdao.getFieldValue("qjjssj");
	DeptDb dd = dm.getDeptDb(fdao.getFieldValue("dept"));
	String deptName = "";
	if (dd!=null)
		deptName = dd.getName();
	String applier = fdao.getFieldValue("applier");
	%>
    <tr class="highlight">
      <td align="center"><%=um.getUserDb(applier).getRealName()%></td>
      <td align="center"><%=deptName%></td>
      <td align="center"><%=strBeginDate%></td>
      <td align="center"><%=strEndDate%></td>
      <td align="center"><%=DateUtil.format(wfd.getMydate(), "yy-MM-dd HH:mm:ss")%> </td>
      <!-- <td align="center"></td> -->
    </tr>
<%}%>
  </tbody>
</table>
<table width="98%"  border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right"><%
	String querystr = "op="+op;
    out.print(paginator.getCurPageBlock("?"+querystr));
%></td>
  </tr>
</table>
</body>
</html>