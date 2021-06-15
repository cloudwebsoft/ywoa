<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>联系人列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
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
String formCode = "sales_linkman";

String[] deptArr1 = null;
String deptstrs = "";
String userName = privilege.getUser(request);
if (privilege.isUserPrivValid(request, "sales.manager")&&!privilege.isUserPrivValid(request, "sales")) {
	UserDb udb1 = new UserDb();
	udb1 = udb1.getUserDb(userName);
	deptArr1 = udb1.getAdminDepts();
	if(deptArr1.length>0){
		for(int t=0;t<deptArr1.length;t++){
			if("".equals(deptstrs)){
				deptstrs = "'"+deptArr1[t]+"'";
			}else{
				deptstrs += ",'"+deptArr1[t]+"'";
			}
		}
	}
}


String querystr = "";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

String sql = "select l.id from form_table_sales_linkman l, form_table_sales_customer c where c.id=l.customer";

if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && !privilege.isUserPrivValid(request, "sales.manager")){
	sql += " and c.sales_person=" + StrUtil.sqlstr(userName);
} else if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && privilege.isUserPrivValid(request, "sales.manager")){
	if (!deptstrs.equals("")) {
		sql += " and c.dept_code in ("+deptstrs+")";
	}
}

String customer = ParamUtil.get(request, "customer");
String linkmanName = ParamUtil.get(request, "linkmanName");

FormDAO fdao = new FormDAO();

long customerId = ParamUtil.getLong(request, "customerId", -1);
if (customerId!=-1) {
	FormDb fdCustomer = new FormDb();
	fdCustomer = fdCustomer.getFormDb("sales_customer");
	fdao = fdao.getFormDAO(customerId, fdCustomer);
	customer = fdao.getFieldValue("customer");
	op = "search";
}

try {	
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "linkmanName", linkmanName, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "customer", customer, getClass().getName());

	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "linkmanName", linkmanName, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "customer", customer, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if (op.equals("search")) {
	if (!customer.equals("")) {
		sql += " and c.customer like " + StrUtil.sqlstr("%" + customer + "%");
	}
	if (!linkmanName.equals("")) {
		sql += " and l.linkmanName like " + StrUtil.sqlstr("%" + linkmanName + "%");
	}
	sql += " union select l.id from form_table_sales_linkman l,form_table_sales_customer c, customer_share s where l.customer=s.customerId and c.id=l.customer and s.sharePerson=" + StrUtil.sqlstr(privilege.getUser(request));
	if (!customer.equals("")) {
        sql += " and c.customer like " + StrUtil.sqlstr("%" + customer + "%");
    }
    if (!linkmanName.equals("")) {
        sql += " and l.linkmanName like " + StrUtil.sqlstr("%" + linkmanName + "%");
    }
}else{
	sql += " union select l.id from form_table_sales_linkman l,form_table_sales_customer c, customer_share s where l.customer=s.customerId and c.id=l.customer   and s.sharePerson=" + StrUtil.sqlstr(privilege.getUser(request));
}
sql += " order by id desc";

querystr = "customer=" + StrUtil.UrlEncode(customer) + "&linkmanName=" + linkmanName + "&customerId=" + customerId;
%>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td height="23" valign="middle" class="tdStyle_1">&nbsp;选择<%=fd.getName()%></td>
  </tr>
  <tr> 
    <td valign="top">
  <%
  String dis = "";
  if (customerId!=-1) {
	  dis = "display:none";
  }
  %>
  <form id="form2" name="form2" action="?op=search" method="post" style="<%=dis%>">
    <table width="100%" border="0" align="center" cellPadding="2" cellSpacing="0">
      <tbody>
        <tr>
          <td align="center">
		  <select name="customer2" onchange="if (this.value!='') {form2.customer.value=this.value;form2.submit()}">
		  <option value="">无</option>
		<%
		Iterator ir = fdao.list("sales_customer", "select id from form_table_sales_customer where sales_person=" + StrUtil.sqlstr(privilege.getUser(request)) + " order by id desc").iterator();
		while (ir.hasNext()) {
			fdao = (FormDAO)ir.next();
		%>
		  <option value="<%=fdao.getFieldValue("customer")%>"><%=fdao.getFieldValue("customer")%></option>
		  <%}%>
		  </select>
		  <script>
		  form2.customer2.value = "<%=customer%>";
		  </script>
		  客户名称：
            <input name="customer" size="20" value="<%=customer%>">
            联系人：
            <input name="linkmanName" size="20" value="<%=linkmanName%>">
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
		
		ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
		long total = lr.getTotal();
		Vector v = lr.getResult();
		if (v!=null)
			ir = v.iterator();
		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
%>
	<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
        <tr>
          <td height="23" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b>&nbsp;
          <%
			out.print(paginator.getCurPageBlock("?"+querystr));
			%></td></tr>
    </table>
      <table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
        <tr align="center">
          <td width="23%" class="tabStyle_1_title">联系人</td>
          <td width="35%" class="tabStyle_1_title">客户</td>
          <td width="27%" class="tabStyle_1_title">电话</td>
          <td width="15%" class="tabStyle_1_title">操作</td>
        </tr>
        <%	
	  	int i = 0;
		
		FormDb customerfd = new FormDb();
		customerfd = customerfd.getFormDb("sales_customer");		
	  	FormDAO fdaoCustomer = new FormDAO();
		
		while (ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
			
			fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toLong(fdao.getFieldValue("customer")), customerfd);
		%>
        <tr align="center" class="highlight">
          <td align="left"><a href="customer_show.jsp?id=<%=id%>&formCode=<%=formCode%>" target="_blank"><%=fdao.getFieldValue("linkmanName")%></a></td>
          <td align="left"><a href="customer_show.jsp?id=<%=fdao.getFieldValue("customer")%>&formCode=sales_customer" target="_blank"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
          <td align="left"><%=fdao.getFieldValue("mobile")%></td>
          <td><a href="javascript:sel('<%=id%>', '<%=fdao.getFieldValue("linkmanName")%>')">选择</a></td>
        </tr>
        <%
		}
%>
      </table>
      </td>
  </tr>
</table>
</body>
</html>
