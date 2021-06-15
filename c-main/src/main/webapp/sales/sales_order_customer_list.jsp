<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String formCode = "sales_order";

int productId = ParamUtil.getInt(request, "productId", -1);

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

// 取得皮肤路径
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))
	skincode = UserSet.defaultSkin;

SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>订单 - 客户</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../inc/sortabletable.js"></script>
<script type="text/javascript" src="../inc/columnlist.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
<%@ include file="../inc/nocache.jsp"%>
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">订单&nbsp;-&nbsp;客户</td>
    </tr>
  </tbody>
</table>
<%
String op = ParamUtil.get(request, "op");
if (!privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String analysisType = ParamUtil.get(request, "analysisType");
if (analysisType.equals(""))
	analysisType = "sales_customer_type";
	
Map map = new HashMap();
map.put("sales_customer_type", "customer_type");
map.put("customer_jyms", "jyms");
map.put("customer_rygm", "rygm");
map.put("qyxz", "enterType");

String analysisTypeValue = ParamUtil.get(request, "analysisTypeValue");

FormDb productfd = new FormDb();
productfd = productfd.getFormDb("sales_product_info");
FormDb customerfd = new FormDb();
customerfd = customerfd.getFormDb("sales_customer");

String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");
java.util.Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
java.util.Date endDate = DateUtil.parse(ParamUtil.get(request, "endDate"), "yyyy-MM-dd");

try {	
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "analysisTypeValue", analysisTypeValue, getClass().getName());

	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "analysisTypeValue", analysisTypeValue, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strBeginDate", strBeginDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strEndDate", strEndDate, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String sql = "select o.id from form_table_sales_customer c, form_table_sales_order o where c.id=o.cws_id and c." + map.get(analysisType) + "=" + StrUtil.sqlstr(analysisTypeValue);

if (beginDate!=null) {
	sql += " and o.order_date>=" + SQLFilter.getDateStr(DateUtil.format(beginDate, "yyyy-MM-dd"), "yyyy-MM-dd");
}
if (endDate!=null) {
	sql += " and o.order_date<" + SQLFilter.getDateStr(DateUtil.format(endDate, "yyyy-MM-dd"), "yyyy-MM-dd");
}

sql += " order by o.id asc";

// out.print(sql);

int pagesize = 20;
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
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}
%>
<form action="?" method="get">
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="center">
		从
        <input readonly type="text" id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>">
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "beginDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
</script>
	至
        <input readonly type="text" id="endDate" name="endDate" size="10" value="<%=strEndDate%>">
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "endDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
</script>
	<input type="submit" value="查询" class="btn" />
    <input name="analysisType" value="<%=analysisType%>" type="hidden" />
    <input name="analysisTypeValue" value="<%=analysisTypeValue%>" type="hidden" />
	</td>
  </tr>
</table>
</form>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
	<tr> 
	  <td align="right" backgroun="images/title1-back.gif">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></td>
	</tr>
</table>
      <table width="98%" align="center" cellspacing="0" cellpadding="0" class="tabStyle_1 percent98">
        <tr>
          <td width="15%" class="tabStyle_1_title">订单编号</td>
          <td width="14%" class="tabStyle_1_title">客户名称</td>
          <td width="12%" class="tabStyle_1_title">订单来源</td>
          <td width="10%" class="tabStyle_1_title">支付方式</td>
          <td width="12%" class="tabStyle_1_title">促成时间</td>
          <td width="12%" class="tabStyle_1_title">订单金额</td>
          <td width="13%" class="tabStyle_1_title">操作</td>
        </tr>
      <%
	  	SelectOptionDb sod = new SelectOptionDb();
	  	UserMgr um = new UserMgr();
	  	int i = 0;
		FormDAO fdaopro = new FormDAO();
		FormDAO fdaoCustomer = new FormDAO();
		while (ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
			
			fdaopro = fdaopro.getFormDAO(StrUtil.toLong(fdao.getFieldValue("product")), productfd);
			fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toLong(fdao.getCwsId()), customerfd);
			
			String realName = "";
			UserDb user = um.getUserDb(fdao.getCreator());
			realName = user.getRealName();
			
			JdbcTemplate jt = new JdbcTemplate();
			sql = "select sum(real_sum) from form_table_sales_ord_product where cws_id=?";
			ResultIterator ri = jt.executeQuery(sql, new Object[]{new Long(fdao.getId())});
			double real_sum = 0.0;
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				real_sum = rr.getDouble(1);
			}
		%>
        <tr>
		  <td><%=fdao.getId()%></td>
          <td><%=fdaoCustomer.getFieldValue("customer")%></td>
          <td><%=fdao.getFieldValue("source")%></td>
          <td><%=sod.getOptionName("pay_type", fdao.getFieldValue("pay_type"))%></td>
          <td><%=fdao.getFieldValue("order_date")%></td>
          <td align="center"><%=real_sum%></td>
          <td align="center"><a target="_blank" href="customer_sales_order_show.jsp?customerId=<%=fdaoCustomer.getId()%>&parentId=<%=fdaoCustomer.getId()%>&id=<%=fdao.getId()%>&formCodeRelated=sales_order&formCode=sales_customer&isShowNav=1">查看</a></td>
        </tr>
      <%
		}
%>
      </table>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
<tr> 
		<td align="right"><%
String querystr = "analysisType=" + analysisType + "&analysisTypeValue=" + StrUtil.UrlEncode(analysisTypeValue) + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate;
out.print(paginator.getCurPageBlock("sales_order_customer_list.jsp?" + querystr));
%></td>
  </tr>
</table>
</body>
</html>
