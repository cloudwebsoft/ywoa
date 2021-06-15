<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "sales.sell";
if (!privilege.isUserPrivValid(request, priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
String formCode = "sales_product_sell";

String querystr = "";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>
</head>
<body>
<%@ include file="product_sell_inc_menu_top.jsp"%>
<script>
o("menu1").className="current"; 
</script>
<div class="spacerH"></div>
<%
String sql = "select id from " + fd.getTableNameByForm();		

String query = ParamUtil.get(request, "query");
if (!query.equals(""))
	sql = query;
else
	if (op.equals("search")) {
		Iterator ir = fd.getFields().iterator();
		String cond = "";
		while (ir.hasNext()) {
			FormField ff = (FormField)ir.next();
			String value = ParamUtil.get(request, ff.getName());
			String name_cond = ParamUtil.get(request, ff.getName() + "_cond");
			if (ff.getType().equals(ff.TYPE_DATE) || ff.getType().equals(ff.TYPE_DATE_TIME)) {
				if (name_cond.equals("0")) {
					// 时间段
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
					// 时间点
					String d = ParamUtil.get(request, ff.getName());
					if (!d.equals("")) {
						cond = SQLFilter.concat(cond, "and", ff.getName() + "=" + StrUtil.sqlstr(d));
					}
				}
			}
			else if (ff.getType().equals(FormField.TYPE_SELECT)) {
				String[] ary = ParamUtil.getParameters(request, ff.getName());
				if (ary!=null) {
					int len = ary.length;
					if (len==1) {
						if (!ary[0].equals("")) {
							if (cond.equals(""))
								cond += ff.getName() + "=" + StrUtil.sqlstr(ary[0]);
							else
								cond += " and " + ff.getName() + "=" + StrUtil.sqlstr(ary[0]);
						}
					}
					else {
						String orStr = "";
						for (int n=0; n<len; n++) {
							if (!ary[n].equals(""))
								orStr = SQLFilter.concat(orStr, "or", ff.getName() + "=" + StrUtil.sqlstr(ary[n]));
						}
						if (!orStr.equals(""))						
							cond = SQLFilter.concat(cond, "and", orStr);
					}
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
		if (!cond.equals(""))
			sql = sql + " where " + cond;	
	}
	
	querystr = "query=" + StrUtil.UrlEncode(sql);

		int pagesize = 10;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
			
		FormDAO fdao = new FormDAO();
		
		ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
		long total = lr.getTotal();
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
<table class="percent98" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></td>
  </tr>
</table>
<table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="1">
  <tr align="center">
    <td class="tabStyle_1_title" width="26%">产品名称</td>
    <td class="tabStyle_1_title" width="20%">售出日期</td>
    <td class="tabStyle_1_title" width="21%">客户名称</td>
    <td class="tabStyle_1_title" width="16%">销售员</td>
    <td class="tabStyle_1_title" width="17%">操作</td>
  </tr>
  <%	
	  	int i = 0;
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
		%>
  <tr align="center">
    <td width="26%"><a href="product_sell_show.jsp?id=<%=id%>&amp;formCode=<%=formCode%>"><%=fdao.getFieldValue("product_name")%></a></td>
    <td width="20%"><%=fdao.getFieldValue("record_date")%></td>
    <td width="21%"><%=fdao.getFieldValue("customer_name")%></td>
    <td width="16%"><%=fdao.getFieldValue("sales_man")%></td>
    <td width="17%"><a href="product_sell_edit.jsp?id=<%=id%>&amp;formCode=<%=StrUtil.UrlEncode(formCode)%>">编辑</a>&nbsp;&nbsp;<a onClick="if (!confirm('您确定要删除么？')) return false" href="../visual_del.jsp?id=<%=id%>&amp;formCode=<%=formCode%>&amp;privurl=<%=StrUtil.getUrl(request)%>">删除</a>&nbsp;&nbsp; </td>
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
			%>    </td>
  </tr>
</table>
</body>
</html>
