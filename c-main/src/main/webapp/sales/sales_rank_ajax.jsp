<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%><%@ page import = "cn.js.fan.db.*"%><%@ page import = "cn.js.fan.util.*"%><%@ page import = "com.cloudwebsoft.framework.db.*"%><%@ page import = "com.redmoon.oa.dept.*"%><%@ page import = "com.redmoon.oa.basic.*"%><%@ page import = "com.redmoon.oa.ui.*"%><%@ page import = "com.redmoon.oa.person.*"%><%@ page import = "com.redmoon.oa.visual.*"%><%@ page import = "com.redmoon.oa.flow.FormDb"%><%@ page import = "com.redmoon.oa.flow.FormField"%><%@ page import = "org.json.*"%><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
response.reset();

String formCode = "sales_order";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

String op = ParamUtil.get(request, "op");

String priv = "sales.user";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String mode = ParamUtil.get(request, "mode");

String preDate = ParamUtil.get(request, "preDate");
try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "preDate", preDate, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");

java.util.Date beginDate = null;
java.util.Date endDate = null;

if (!preDate.equals("") && !preDate.equals("*")) {
	String[] ary = StrUtil.split(preDate, "\\|");
	strBeginDate = ary[0];
	strEndDate = ary[1];
	beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
	endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
}
else {
	beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
	endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
}

String unitCode = privilege.getUserUnitCode(request);

String sql = "select p.cws_creator, sum(p.real_sum) as z from form_table_sales_order o, form_table_sales_ord_product p where o.unit_code=" + StrUtil.sqlstr(unitCode) + " and o.id=p.cws_id";

if (beginDate!=null) {
	sql += " and o.order_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
}
if (endDate!=null) {
	sql += " and o.order_date<" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
}

sql += " group by p.cws_creator";

String sidx = ParamUtil.get(request, "sidx");
// System.out.println(getClass() + " sidx=" + sidx);
sidx = "2";
String sord = ParamUtil.get(request, "sord");
if (sord.equals(""))
	sord = "desc";

sord = "desc";
sql += " order by " + sidx + " " + sord;

// System.out.println(getClass() + " " + sidx + " sord= " + sord);

int pagesize = ParamUtil.getInt(request, "rows", 20);

// System.out.println(getClass() + " sql222=" + sql);

Paginator paginator = new Paginator(request);
int curpage = ParamUtil.getInt(request, "page", 1); // paginator.getCurPage();

FormDAO fdao = new FormDAO();

JdbcTemplate jt = new JdbcTemplate();

ResultIterator ri = jt.executeQuery(sql, curpage, pagesize);
long total = ri.getTotal();
paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}
JSONObject jsonObj = new JSONObject();   

// 根据jqGrid对JSON的数据格式要求给jsonObj赋值   
jsonObj.put("page", "" + curpage);     // 当前页   
jsonObj.put("total", "" + totalpages);    // 总页数   
jsonObj.put("records", "" + ri.getTotal());  // 总记录数

// 定义rows，存放数据   
JSONArray rows = new JSONArray();              

UserMgr um = new UserMgr();
DeptMgr dm = new DeptMgr();		
DeptUserDb du = new DeptUserDb();

int customerCountAll = 0;
int actionCountAll = 0;
int chanceCountAll = 0;
double chanceRateAll = 0.0;
int orderCountAll = 0;
int sellCountAll = 0;
double sellSumAll = 0;

SelectMgr sm = new SelectMgr();
SelectDb sd = sm.getSelect("sales_customer_category");

Map map = new HashMap();

int i = (curpage-1)*pagesize;
while (ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	i++;
	
	double zj = rr.getDouble(2);
	sellSumAll += zj;

	UserDb user = um.getUserDb(rr.getString(1));
	String realName = user.getRealName();
	
	sql = "select count(*) from form_table_day_lxr where cws_creator=?";
	if (beginDate!=null) {
		sql += " and visit_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and visit_date<=" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}			
	ResultIterator ri2 = jt.executeQuery(sql, new Object[]{user.getName()});
	int actionCount = 0;
	if (ri2.hasNext()) {
		ResultRecord rr2 = (ResultRecord)ri2.next();
		actionCount = rr2.getInt(1);
		
		actionCountAll += actionCount;
	}
	
	sql = "select count(*) from form_table_sales_customer where cws_creator=?";
	if (beginDate!=null) {
		sql += " and find_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and find_date<=" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}
	ri2 = jt.executeQuery(sql, new Object[]{user.getName()});
	int customerCount = 0;
	if (ri2.hasNext()) {
		ResultRecord rr2 = (ResultRecord)ri2.next();
		customerCount = rr2.getInt(1);
		
		customerCountAll += customerCount;
	}
	
	// 存放一条记录的对象   
	JSONObject cell = new JSONObject();
	 	
	sql = "select count(*) from form_table_sales_chance where cws_creator=?";
	if (beginDate!=null) {
		sql += " and find_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and find_date<=" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}			
	ri2 = jt.executeQuery(sql, new Object[]{user.getName()});
	int chanceCount = 0;
	if (ri2.hasNext()) {
		ResultRecord rr2 = (ResultRecord)ri2.next();
		chanceCount = rr2.getInt(1);
		
		chanceCountAll += chanceCount;
	}
	sql = "select count(*) from form_table_sales_order where cws_creator=?";
	if (beginDate!=null) {
		sql += " and order_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and order_date<=" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}						
	ri2 = jt.executeQuery(sql, new Object[]{user.getName()});
	int orderCount = 0;
	if (ri2.hasNext()) {
		ResultRecord rr2 = (ResultRecord)ri2.next();
		orderCount = rr2.getInt(1);
		
		orderCountAll += orderCount;
	}
	
	sql = "select sum(p.num) from form_table_sales_order o, form_table_sales_ord_product p where o.id=p.cws_id and p.cws_creator=?";
	if (beginDate!=null) {
		sql += " and o.order_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and o.order_date<=" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}						
	ri2 = jt.executeQuery(sql, new Object[]{user.getName()});
	int productCount = 0;
	if (ri2.hasNext()) {
		ResultRecord rr2 = (ResultRecord)ri2.next();
		productCount = rr2.getInt(1);
		sellCountAll += productCount;		
	}

	Iterator ir2 = du.getDeptsOfUser(user.getName()).iterator();
	String dept = "";
	while (ir2.hasNext()) {
		DeptDb dd = (DeptDb)ir2.next();
		String deptName = "";
		if (!dd.getParentCode().equals(DeptDb.ROOTCODE) && !dd.getCode().equals(DeptDb.ROOTCODE)) {					
			deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName();
		}
		else
			deptName = dd.getName();
		if (dept.equals("")) {
			dept = deptName;
		}
		else {
			dept += "，" + deptName;
		}
	}
	cell.put("idx", ""+i);  
	cell.put("realName", realName);   
	cell.put("dept", dept);
	
	if (mode.equals("grouping"))
		cell.put("customerCount", "" + customerCount);
	else
		cell.put("customerCount", "<a href='javascript:;' onclick=\"addTab('客户', 'sales/customer_list.jsp?op=search&find_date_cond=0&userName=" + StrUtil.UrlEncode(user.getName()) + "&find_dateFromDate=" + strBeginDate + "&find_dateToDate=" + DateUtil.format(DateUtil.addDate(endDate, -1), "yyyy-MM-dd") + "')\">" + customerCount + "</a>");
	cell.put("actionCount", "" + actionCount);
	cell.put("chanceCount", "" + chanceCount);   
	cell.put("chanceRate", NumberUtil.round((double)orderCount/chanceCount*100, 1));   
	cell.put("orderCount", "" + orderCount);   
	cell.put("sellCount", "" + productCount);
	cell.put("sellSum", "" + zj);
	cell.put("op", ""); // "<a href=\"customer_sales_chance_show.jsp?customerId=" + fdao.getCwsId() + "&parentId=" + fdao.getCwsId() + "&formCodeRelated=sales_chance&formCode=sales_customer&isShowNav=1\" target=\"_blank\">查看</a>");   
	   
	// 将该记录放入rows中
	rows.put(cell);

}

// 将rows放入json对象中   
jsonObj.put("rows", rows);

JSONObject userdata = new JSONObject();
userdata.put("actionCount", actionCountAll);
userdata.put("customerCount", customerCountAll);
userdata.put("chanceCount", chanceCountAll);
userdata.put("orderCount", orderCountAll);
userdata.put("sellCount", sellCountAll);
userdata.put("sellSum", sellSumAll);

userdata.put("dept", "总计");

Vector vsd = sd.getOptions();
Iterator irsd = vsd.iterator();
while (irsd.hasNext()) {
	SelectOptionDb sod = (SelectOptionDb)irsd.next();
	Integer obj = (Integer)map.get(sod.getValue());
	int val = 0;
	if (obj!=null)
		val = obj.intValue();
	userdata.put(sod.getValue(), "" + val);
}

jsonObj.put("userdata", userdata);

// response.setContentType("application/x-json");
response.setCharacterEncoding("UTF-8");
out.print(jsonObj);
%>