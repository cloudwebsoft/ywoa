<%@ page contentType="text/html; charset=GB2312"%><%@ page import="com.redmoon.oa.person.*"%><%@ page import="com.redmoon.oa.dept.*"%><%@ page import="com.redmoon.oa.flow.*"%><%@ page import="java.io.*"%><%@ page import="jxl.*"%><%@ page import="jxl.write.*"%><%@ page import="com.cloudwebsoft.framework.db.*"%><%@ page import="cn.js.fan.db.*"%><%@ page import="java.util.*"%><%@ page import="cn.js.fan.web.*"%><%@ page import="cn.js.fan.util.*"%><%@ page import="cn.js.fan.security.*"%><%@ page import="jxl.*"%><%@ page import="jxl.write.*"%><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
if (!privilege.isUserPrivValid(request, "leave")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "fl.mydate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String op = ParamUtil.get(request, "op");
String what = ParamUtil.get(request, "what");

String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");

String now = SQLFilter.getDateStr(DateUtil.format(new java.util.Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
// String sql = "select f.flowId from form_table_jbsqd f, flow fl where f.flowId=fl.id and f.flowTypeCode='jbsq' and (fl.status=" + WorkflowDb.STATUS_STARTED + " or fl.status=" + WorkflowDb.STATUS_FINISHED + ")";
String sql = "select f.flowId from form_table_jbsqd f, flow fl,users u where f.flowId=fl.id and f.flowTypeCode='jbsq' and (fl.status=" + WorkflowDb.STATUS_STARTED + " or fl.status=" + WorkflowDb.STATUS_FINISHED + ")"+" and u.name = f.applier ";

if (op.equals("search")) {
	if (!what.equals("")) {
		sql += " and f.applier=" + StrUtil.sqlstr(what);
	}
	if (!beginDate.equals("") && !endDate.equals("")) {
		java.util.Date d = DateUtil.parse(endDate, "yyyy-MM-dd");
		sql += " and ((f.kssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + " and f.kssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + ") or (f.jssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + " and f.jssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + "))";
	}
	else if (!beginDate.equals("")) {
		sql += " and f.kssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
	}
	else if (beginDate.equals("") && !endDate.equals("")) {
		sql += " and f.kssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
	}
}
// and " + now + ">=kssj" + " and " + now + "<=jssj and xjrq is null
int result = ParamUtil.getInt(request, "result", -1);
if (result!=-1)
	sql += " and f.result='" + result + "'";
sql += " order by " + orderBy + " " + sort;

// out.print(sql);

FormDb fd = new FormDb();
fd = fd.getFormDb("jbsqd");

WorkflowDb wf = new WorkflowDb();

int pageSize = 10;
JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = jt.executeQuery(SQLFilter.getCountSql(sql));
if (ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	pageSize = rr.getInt(1);
}

ListResult lr = wf.listResult(sql, 1, pageSize);
Vector v = lr.getResult();

response.setContentType("application/vnd.ms-excel");
response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode("加班记录.xls"));  

OutputStream os = response.getOutputStream();

try {
	File file = new File(Global.realPath + "attendance/jb_list_excel.xls");

	Workbook wb = Workbook.getWorkbook(file);

	// 打开一个文件的副本，并且指定数据写回到原文件
	WritableWorkbook wwb = Workbook.createWorkbook(os, wb);
	WritableSheet ws = wwb.getSheet(0);
		
	Iterator ir = v.iterator();
	UserMgr um = new UserMgr();
	FormDAO fdao = new FormDAO();
	DeptMgr dm = new DeptMgr();

	int j = 1;
	while (ir.hasNext()) {
		WorkflowDb wfd = (WorkflowDb)ir.next();
		fdao = fdao.getFormDAO(wfd.getId(), fd);
		String strBeginDate = fdao.getFieldValue("kssj");
		String strEndDate = fdao.getFieldValue("jssj");
		DeptDb dd = dm.getDeptDb(fdao.getFieldValue("dept"));
		String deptName = "";
		if (dd!=null)
			deptName = dd.getName();
		String checker = fdao.getFieldValue("checker");
		if (!checker.equals(""))
			checker = um.getUserDb(checker).getRealName();
		
		Label a0 = new Label(0, j, um.getUserDb(fdao.getFieldValue("applier")).getRealName());
		Label a1 = new Label(1, j, deptName);
		Label a2 = new Label(2, j, fdao.getFieldValue("jblb"));
		Label a3 = new Label(3, j, fdao.getFieldValue("day_count"));
		Label a4 = new Label(4, j, strBeginDate);
		Label a5 = new Label(5, j, strEndDate);
		Label a6 = new Label(6, j, DateUtil.format(wfd.getMydate(), "yy-MM-dd HH:mm:ss"));
		Label a7 = new Label(7, j, fdao.getFieldValue("result").equals("1")?"通过":"不通过");
		Label a8 = new Label(8, j, wfd.getStatusDesc());
		Label a9 = new Label(9, j, checker);
		
		ws.addCell(a0);
		ws.addCell(a1);
		ws.addCell(a2);
		ws.addCell(a3);
		ws.addCell(a4);
		ws.addCell(a5);
		ws.addCell(a6);
		ws.addCell(a7);
		ws.addCell(a8);
		ws.addCell(a9);
		
		j++;
	}
	
	wwb.write();
	wwb.close();
	wb.close();	
}
catch (Exception e) {
	out.println(e.toString());
}
finally {
	os.close();
}
%>