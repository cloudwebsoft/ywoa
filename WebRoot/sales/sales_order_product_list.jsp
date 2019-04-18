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
String formCode = "sales_ord_product";

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
<title>订单 - 产品</title>
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
      <td class="tdStyle_1">订单产品</td>
    </tr>
  </tbody>
</table>
<%
String op = ParamUtil.get(request, "op");
if (!privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

FormDb productfd = new FormDb();
productfd = productfd.getFormDb("sales_product_info");
FormDb orderfd = new FormDb();
orderfd = orderfd.getFormDb("sales_order");

String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");
java.util.Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
java.util.Date endDate = DateUtil.parse(ParamUtil.get(request, "endDate"), "yyyy-MM-dd");

try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strBeginDate", strBeginDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strEndDate", strEndDate, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String sql = "select p.id from " + fd.getTableNameByForm() + " p, form_table_sales_order o where p.cws_id=o.id and p.product=" + productId;

if (beginDate!=null) {
	sql += " and o.order_date>=" + SQLFilter.getDateStr(DateUtil.format(beginDate, "yyyy-MM-dd"), "yyyy-MM-dd");
}
if (endDate!=null) {
	sql += " and o.order_date<" + SQLFilter.getDateStr(DateUtil.format(endDate, "yyyy-MM-dd"), "yyyy-MM-dd");
}

sql += " order by p.id asc";

// out.print(sql);

int pagesize = 20;
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
    <input name="productId" value="<%=productId%>" type="hidden" />
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
          <td width="15%" class="tabStyle_1_title">产品</td>
          <td width="14%" class="tabStyle_1_title">单价</td>
          <td width="12%" class="tabStyle_1_title">数量</td>
          <td width="10%" class="tabStyle_1_title">总价</td>
          <td width="12%" class="tabStyle_1_title">实际销售额</td>
          <td width="12%" class="tabStyle_1_title">订单</td>
          <td width="12%" class="tabStyle_1_title">销售员</td>
          <td width="13%" class="tabStyle_1_title">操作</td>
        </tr>
      <%
	  	UserMgr um = new UserMgr();
	  	int i = 0;
		FormDAO fdaopro = new FormDAO();
		FormDAO fdaoOrder = new FormDAO();
		while (ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
			
			fdaopro = fdaopro.getFormDAO(StrUtil.toLong(fdao.getFieldValue("product")), productfd);
			fdaoOrder = fdaoOrder.getFormDAO(StrUtil.toLong(fdao.getCwsId()), orderfd);
			
			String realName = "";
			UserDb user = um.getUserDb(fdao.getCreator());
			realName = user.getRealName();
		%>
        <tr>
		  <td><%=fdaopro.getFieldValue("product_name")%></td>
          <td><%=fdao.getFieldValue("price")%></td>
          <td><%=fdao.getFieldValue("num")%></td>
          <td><%=fdao.getFieldValue("zj")%></td>
          <td><%=fdao.getFieldValue("real_sum")%></td>
          <td align="center"><a target="_blank" href="customer_sales_order_show.jsp?customerId=<%=fdaoOrder.getCwsId()%>&parentId=<%=fdaoOrder.getCwsId()%>&id=<%=fdao.getCwsId()%>&formCodeRelated=sales_order&formCode=sales_customer&isShowNav=1">查看订单</a></td>
          <td><%=realName%></td>
          <td align="center"><a target="_blank" href="product_show.jsp?id=<%=fdaopro.getId()%>&amp;formCode=sales_product_info">查看</a></td>
        </tr>
      <%
		}
%>
      </table>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
<tr> 
		<td align="right"><%
String querystr = "beginDate=" + strBeginDate + "&endDate=" + strEndDate;
out.print(paginator.getCurPageBlock("sales_order_product_list.jsp?" + querystr));
%></td>
    </tr>
</table>
</body>
</html>
