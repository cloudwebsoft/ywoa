<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
String formCode = "sales_chance";

String unitCode = privilege.getUserUnitCode(request);

String querystr = "";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

FormDb customerfd = new FormDb();
customerfd = customerfd.getFormDb("sales_customer");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>选择</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script><script>
function selChance(id, customer) {
	window.opener.setIntpuObjValue(id, customer);
	window.close();
}
</script>
<%@ include file="../inc/nocache.jsp"%>
</head>
<body>
<%
int customerId = ParamUtil.getInt(request, "customerId", -1);

FormDAO fdao = new FormDAO();

String sql;
if (customerId!=-1) {
	fdao = fdao.getFormDAO(customerId, customerfd);
	sql = "select id from " + fd.getTableNameByForm() + " where cws_id=" + customerId + " order by find_date desc";
}
else {
	sql = "select id from " + fd.getTableNameByForm() + " order by find_date desc";
}
%>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td height="23" valign="middle" class="tdStyle_1">&nbsp;选择<%=fd.getName()%>
    <%if (customerId!=-1) {%>
    (<%=fdao.getFieldValue("customer")%>)
    <%}%>
    </td>
  </tr>
</table>
<%
	int pagesize = 10;
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();
	
	ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
	long total = lr.getTotal();
	Vector v = lr.getResult();
	Iterator ir = null;
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
<table class="percent98" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
  <tr align="center">
    <td class="tabStyle_1_title" width="18%">编号</td>
    <td class="tabStyle_1_title" width="21%">发现时间</td>
    <td class="tabStyle_1_title" width="27%">商机阶段</td>
    <td class="tabStyle_1_title" width="17%">状态</td>
    <td class="tabStyle_1_title" width="17%">操作</td>
  </tr>
  <%	
	  	int i = 0;
		
		// FormDb customerfd = new FormDb();
		// customerfd = customerfd.getFormDb("sales_customer");
		SelectOptionDb sod = new SelectOptionDb();
		
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
		%>
  <tr align="center">
    <td width="18%"><%=fdao.getFieldValue("code")%></td>
    <td width="21%">
    <a target="_blank" href="customer_sales_chance_show.jsp?customerId=<%=fdao.getCwsId()%>&parentId=<%=fdao.getCwsId()%>&id=<%=id%>&formCodeRelated=sales_chance&formCode=sales_customer&isShowNav=1"><%=fdao.getFieldValue("find_date")%></a>
    </td>
    <td width="27%"><%=sod.getOptionName("sales_chance_state", fdao.getFieldValue("state"))%></td>
    <td width="17%"><%=sod.getOptionName("sales_chance_status", fdao.getFieldValue("sjzt"))%></td>
    <td width="17%"><a href="javascript:;" onclick="selChance('<%=id%>', '<%=fdao.getFieldValue("code")%>')">选择</a>&nbsp;&nbsp;&nbsp;&nbsp; </td>
  </tr>
  <%
		}
%>
</table>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="percent98">
  <tr>
    <td height="23" align="right">
    <%
		out.print(paginator.getCurPageBlock("?"+querystr));
	%>
    </td>
  </tr>
</table>
</body>
</html>
