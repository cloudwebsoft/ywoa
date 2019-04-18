<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>订单选择</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script>
function sel(id,sth) {
	window.opener.setIntpuObjValue(id,sth);
	window.close();
}
</script>
</head>
<body>
<%
String priv = "read";
if (!privilege.isUserPrivValid(request, priv))
{
	// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}

String op = ParamUtil.get(request, "op");
String formCode = "sales_order";

String querystr = "";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

String unitCode = privilege.getUserUnitCode(request);

String sql = "select o.id from form_table_sales_order o where o.unit_code=" + StrUtil.sqlstr(unitCode);
String customer = ParamUtil.get(request, "customer");
String state = ParamUtil.get(request, "state");
String orderSource = ParamUtil.get(request, "orderSource");

try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "state", state, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "orderSource", orderSource, getClass().getName());
	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "state", state, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "orderSource", orderSource, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "customer", customer, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if (op.equals("search")) {
	if (!customer.equals("")) {
		sql = "select o.id from form_table_sales_order o, form_table_sales_customer c where c.id=o.cws_id";
		sql += " and c.unit_code=" + StrUtil.sqlstr(unitCode) + " and c.customer like " + StrUtil.sqlstr("%" + customer + "%");
	}
	if (!state.equals("")) {
		sql += " and o.status=" + state;
	}
	if (!orderSource.equals("")) {
		sql += " and o.source=" + orderSource;
	}
}
sql += " order by o.id desc";

querystr = "customer=" + StrUtil.UrlEncode(customer) + "&state=" + state + "&orderSource=" + orderSource;
%>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td height="23" valign="middle" class="tdStyle_1">&nbsp;选择<%=fd.getName()%></td>
  </tr>
  <tr> 
    <td valign="top">
  <form id="form2" name="form2" action="?op=search" method="post">
    <table width="100%" border="0" align="center" cellPadding="2" cellSpacing="0">
      <tbody>
        <tr>
          <td align="center">
		  客户：
            <input name="customer" size="20" value="<%=customer%>">
        来源
        <select id="orderSource" name="orderSource">
          <option value="">不限</option>
          <%
SelectMgr sm = new SelectMgr();
SelectDb sd = sm.getSelect("sales_order_source");
Vector vsd = sd.getOptions();
Iterator irsd = vsd.iterator();
while (irsd.hasNext()) {
	SelectOptionDb sod = (SelectOptionDb)irsd.next();
	String clr = "";
	if (!sod.getColor().equals(""))
		clr = " style='color:" + sod.getColor() + "' ";	
	%>
          <option value="<%=sod.getValue()%>" <%=clr%>><%=sod.getName()%></option>
          <%	
}
%>
        </select>            
            状态：
        <select id="state" name="state">
          <option value="">不限</option>
          <%
		sd = sm.getSelect("sales_order_state");
		vsd = sd.getOptions();
		irsd = vsd.iterator();
		while (irsd.hasNext()) {
			SelectOptionDb sod = (SelectOptionDb)irsd.next();
			String clr = "";
			if (!sod.getColor().equals(""))
				clr = " style='color:" + sod.getColor() + "' ";			
			%>
				  <option value="<%=sod.getValue()%>" <%=clr%>><%=sod.getName()%></option>
				  <%	
		}
		%>
        </select>
        <script>
		o("orderSource").value = "<%=orderSource%>";
		o("state").value = "<%=state%>";
		</script>
        <input class="btn" type="submit" value="查  询"></td>
        </tr>
      </tbody>
    </table>
  </form>      </td>
  </tr>
  <tr>
    <td width="72%" valign="top">
	<%
		int pagesize = 10;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		
		FormDAO fdao = new FormDAO();
		ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
		int total = lr.getTotal();
		Vector v = lr.getResult();
		Iterator ir = v.iterator();
		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
%>
      <table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
        <tr align="center">
          <td width="14%" class="tabStyle_1_title">编号</td>
          <td width="22%" class="tabStyle_1_title">客户</td>
          <td width="17%" class="tabStyle_1_title">日期</td>
          <td width="18%" class="tabStyle_1_title">来源</td>
          <td width="16%" class="tabStyle_1_title">状态</td>
          <td width="13%" class="tabStyle_1_title">操作</td>
        </tr>
        <%	
	  	int i = 0;
		
		FormDb customerfd = new FormDb();
		customerfd = customerfd.getFormDb("sales_customer");		
	  	FormDAO fdaoCustomer = new FormDAO();
		SelectOptionDb sod = new SelectOptionDb();
		
		while (ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
			
			fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toLong(fdao.getFieldValue("customer")), customerfd);
		%>
        <tr align="center" class="highlight">
          <td align="left"><a href="customer_sales_order_show.jsp?customerId=<%=fdaoCustomer.getId()%>&parentId=<%=fdaoCustomer.getId()%>&id=<%=fdao.getId()%>&formCodeRelated=sales_order&formCode=sales_customer&isShowNav=1" target="_blank"><%=fdao.getFieldValue("code")%></a></td>
          <td align="left"><a href="customer_show.jsp?id=<%=fdao.getFieldValue("customer")%>&formCode=sales_customer" target="_blank"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
          <td align="left"><%=fdao.getFieldValue("order_date")%></td>
          <td align="left"><%=sod.getOptionName("sales_order_source", fdao.getFieldValue("source"))%></td>
          <td align="left"><%=sod.getOptionName("sales_order_state", fdao.getFieldValue("status"))%></td>
          <td><a href="javascript:sel('<%=id%>', '<%=fdao.getFieldValue("code")%>')">选择</a></td>
        </tr>
        <%
		}
%>
      </table>
      <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
        <tr>
          <td height="23" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b>&nbsp;
          <%
			out.print(paginator.getCurPageBlock("?"+querystr));
			%></td></tr>
    </table></td>
  </tr>
</table>
</body>
</html>
