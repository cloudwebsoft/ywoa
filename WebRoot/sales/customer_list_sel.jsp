<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "read";
if (!privilege.isUserPrivValid(request, priv)) {
	// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}

String op = ParamUtil.get(request, "op");
String formCode = "sales_customer";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=fd.getName()%>选择</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script><script>
function selCustomer(id, customer) {
	window.opener.setIntpuObjValue(id, customer);
	window.close();
}
</script>
<%@ include file="../inc/nocache.jsp"%>
</head>
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0">
<%
	String sql = "select id from " + fd.getTableNameByForm() + " where 1=1";		

	if (op.equals("search")) {
		Iterator ir = fd.getFields().iterator();
		// String cond = "sales_person=" + StrUtil.sqlstr(privilege.getUser(request));
		String cond = "";
		while (ir.hasNext()) {
			FormField ff = (FormField)ir.next();
			String value = ParamUtil.get(request, ff.getName());
			String name_cond = ParamUtil.get(request, ff.getName() + "_cond");
			if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
				String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
				String tDate = ParamUtil.get(request, ff.getName() + "ToDate");
				if (!fDate.equals("")) {
					if (cond.equals(""))
						cond += ff.getName() + ">=" + StrUtil.sqlstr(fDate);
					else
						cond += " and " + ff.getName() + ">=" + StrUtil.sqlstr(fDate);
				}
				if (!tDate.equals("")) {
					if (cond.equals(""))
						cond += ff.getName() + "<=" + StrUtil.sqlstr(tDate);
					else
						cond += " and " + ff.getName() + "<=" + StrUtil.sqlstr(tDate);
				}
			}
			else {
				if (name_cond.equals("0")) {
					if (!value.equals("")) {
						if (cond.equals(""))
							cond += ff.getName() + " like " + StrUtil.sqlstr("%" + value + "%");
						else
							cond += " and " + ff.getName() + " like " + StrUtil.sqlstr("%" + value + "%");
					}
				}
				else if (name_cond.equals("1")) {
					if (!value.equals("")) {
						if (cond.equals(""))
							cond += ff.getName() + "=" + StrUtil.sqlstr(value);
						else
							cond += " and " + ff.getName() + "=" + StrUtil.sqlstr(value);
					}
				}
			}
		}

		// out.print("cond=" + cond + "<br>");
		if (!cond.equals("")) {
			sql += " and " + cond;
		}
	}
	
	if (!privilege.isUserPrivValid(request, "sales"))
		sql += " and sales_person=" + StrUtil.sqlstr(privilege.getUser(request));
	
	sql += " order by id desc";

%>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td height="23" valign="middle" class="tdStyle_1">&nbsp;选择<%=fd.getName()%></td>
  </tr>
</table>
<%
		int pagesize = 10;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
			
		FormDAO fdao = new FormDAO();
		
		ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
		int total = lr.getTotal();
		Vector v = lr.getResult();
	    Iterator ir = null;
		if (v!=null)
			ir = v.iterator();
		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}
%>
<form id="form2" name="form2" action="?op=search" method="post">
  <table class="tabStyle_1 percent98" width="100%" border="0" align="center" cellpadding="2" cellspacing="0">
    <tbody>
      <tr>
        <td class="tabStyle_1_title" colspan="6">查询条件</td>
      </tr>
      <tr>
        <td width="11%">客户</td>
        <td nowrap="nowrap" width="10%"><select name="customer_cond">
          <option value="1">等于</option>
          <option value="0" selected="selected">包含</option>
        </select>
        </td>
        <td width="29%"><input name="customer" size="20" /></td>
        <td width="11%">电话</td>
        <td width="10%"><select name="tel_cond">
          <option value="1">等于</option>
          <option value="0" selected="selected">包含</option>
        </select></td>
        <td width="29%"><input name="tel" size="20" />
        <%
        String customerCond = ParamUtil.get(request, "customer_cond").equals("1")?"1":"0";
        String customer = ParamUtil.get(request, "customer");
        String telCond = ParamUtil.get(request, "tel_cond").equals("1")?"1":"0";
        String tel = ParamUtil.get(request, "tel");
        %>        
        <script>
        o("customer_cond").value = "<%=customerCond%>";
        o("customer").value = "<%=customer%>";
        o("tel_cond").value = "<%=telCond%>";
        o("tel").value = "<%=tel%>";
        </script>
        </td>
      </tr>
      <tr>
        <td width="11%">地址</td>
        <td nowrap="nowrap" width="10%"><select name="address_cond">
          <option value="1">等于</option>
          <option value="0" selected="selected">包含</option>
        </select></td>
        <td width="29%"><input name="address" size="20" /></td>
        <td width="11%">邮件</td>
        <td width="10%"><select name="email_cond">
          <option value="1">等于</option>
          <option value="0" selected="selected">包含</option>
        </select></td>
        <td width="29%"><input name="email" size="20" />
        <%
        String addressCond = ParamUtil.get(request, "address_cond").equals("0")?"0":"1";
        String address = ParamUtil.get(request, "address");
        String emailCond = ParamUtil.get(request, "email_cond").equals("0")?"0":"1";
        String email = ParamUtil.get(request, "email");
        
        String queryStr = "op=" + op + "&customer_cond=" + customerCond + "&customer=" + StrUtil.UrlEncode(customer);
        queryStr += "&tel_cond=" + telCond + "&tel=" + tel;
        queryStr += "&address_cond=" + addressCond + "&address=" + StrUtil.UrlEncode(address);
        queryStr += "&email_cond=" + emailCond + "&email=" + email;
        %>        
        <script>
        o("address_cond").value = "<%=addressCond%>";
        o("address").value = "<%=address%>";
        o("email_cond").value = "<%=emailCond%>";
        o("email").value = "<%=email%>";
        </script>        
        </td>
      </tr>
      <tr>
        <td colspan="6" align="center"><input class="btn"  type="submit" value="查  询" name="submit" /></td>
      </tr>
    </tbody>
  </table>
</form>
<table class="percent98" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
  <tr align="center">
    <td class="tabStyle_1_title" width="48%">客户</td>
    <td class="tabStyle_1_title" width="32%">地址</td>
    <td class="tabStyle_1_title" width="20%">操作</td>
  </tr>
  <%	
	  	int i = 0;
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
		%>
  <tr>
    <td width="48%" align="left"><a target="_blank" href="customer_show.jsp?id=<%=id%>&amp;formCode=<%=formCode%>"><%=fdao.getFieldValue("customer")%></a></td>
    <td width="32%"><%=StrUtil.getNullStr(fdao.getFieldValue("address"))%></td>
    <td width="20%" align="center"><a href="javascript:selCustomer('<%=id%>', '<%=fdao.getFieldValue("customer")%>')">选择</a></td>
  </tr>
  <%
		}
%>
</table>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="percent98">
  <tr>
    <td height="23" align="right">
    <%
		out.print(paginator.getCurPageBlock("?"+queryStr));
	%>
    </td>
  </tr>
</table>
</body>
</html>
