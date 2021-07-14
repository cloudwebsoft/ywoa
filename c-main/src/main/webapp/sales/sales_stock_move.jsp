<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.sale.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
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
<title>库存明细</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
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
      <td class="tdStyle_1">库存明细</td>
    </tr>
  </tbody>
</table>
<%
String op = ParamUtil.get(request, "op");

String priv = "sales.stock";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

FormDb stockfd = new FormDb();
stockfd = stockfd.getFormDb("sales_stock");
FormDb productfd = new FormDb();
productfd = productfd.getFormDb("sales_product_info");

long stockFrom = ParamUtil.getLong(request, "stockFrom", -1);
long stockTo = ParamUtil.getLong(request, "stockTo", -1);

if (op.equals("move")) {
	String[] productIds = ParamUtil.getParameters(request, "productId");
	String[] counts = ParamUtil.getParameters(request, "count");
	if (productIds==null) {
		out.print(StrUtil.jAlert_Back("产品不能为空！","提示"));
		return;
	}
	int len = productIds.length;
	SalesStockProductDb sspd = new SalesStockProductDb();
	for (int i=0; i<len; i++) {
		int count = StrUtil.toInt(counts[i], -1);
		if (count>0) {
			long productId = StrUtil.toLong(productIds[i]);
			SalesStockProductDb sspdTo = sspd.getSalesStockProductDb(new Long(stockTo), new Long(productId));
			SalesStockProductDb sspdFrom = sspd.getSalesStockProductDb(new Long(stockFrom), new Long(productId));
			if (sspdTo!=null) {
				sspdTo.updateNum(count);
			}
			else {
				String unitCode = privilege.getUserUnitCode(request);
				sspd.create(new JdbcTemplate(), new Object[]{new Long(stockTo), new Long(productId), new Integer(count), unitCode});
			}
			sspdFrom.updateNum(-count);
		}
	}
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "sales_stock_move.jsp?stockFrom=" + stockFrom + "&stockTo=" + stockTo));
	return;
}

String unitCode = privilege.getUserUnitCode(request);

FormDAO fdao = new FormDAO();
Vector vstock = fdao.list("sales_stock", "select id from form_table_sales_stock where unit_code=" + StrUtil.sqlstr(unitCode) + " order by id");
if (vstock.size()==0) {
  out.print(SkinUtil.makeInfo(request, "请先建立仓库！"));
  return;
}
fdao = (FormDAO)vstock.elementAt(0);
if (stockFrom==-1)
	stockFrom = fdao.getId();
if (stockTo==-1)
	stockTo = fdao.getId();
	
int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

String sql = "select id from oa_sales_stock_product where stock_id=" + stockFrom;

SalesStockProductDb sspd = new SalesStockProductDb();
Iterator ir = sspd.list(sql).iterator();
%>
<form action="sales_stock_move.jsp">
  <table width="70%" align="center" class="percent98">
    <tr>
      <td width="100%" align="center"><input name="op" value="move" type="hidden" />
        选择出库的仓库
          <select id="stockFrom" name="stockFrom" onchange="window.location.href='sales_stock_move.jsp?stockFrom=' + this.value + '&stockTo=<%=stockTo%>'">
          <%
		  Iterator irstock = vstock.iterator();
		  while (irstock.hasNext()) {
		  	fdao = (FormDAO)irstock.next();
			%>
			<option value="<%=fdao.getId()%>"><%=fdao.getFieldValue("name")%></option>
			<%
		  }
		  %>
        </select>
        选择入库的仓库
        <select id="stockTo" name="stockTo" >
          <%
		  irstock = vstock.iterator();
		  while (irstock.hasNext()) {
		  	fdao = (FormDAO)irstock.next();
			%>
          	<option value="<%=fdao.getId()%>"><%=fdao.getFieldValue("name")%></option>
       	  <%
		  }
		  %>
        </select>
        <script>
		o("stockFrom").value = "<%=stockFrom%>";
		o("stockTo").value = "<%=stockTo%>";
		</script></td>
    </tr>
  </table>
<table width="83%" align="center" cellspacing="0" cellpadding="0" class="tabStyle_1 percent60">
<tr>
          <td width="34%" class="tabStyle_1_title">产品名称</td>
          <td width="34%" class="tabStyle_1_title">数量</td>
          <td width="32%" class="tabStyle_1_title">移库数量</td>
        </tr>
      <%
		FormDAO fdaoProduct = new FormDAO();
	  	int i = 0;
		SelectOptionDb sod = new SelectOptionDb();
		while (ir.hasNext()) {
			sspd = (SalesStockProductDb)ir.next();
			i++;
			long id = sspd.getLong("id");
			fdaoProduct = fdaoProduct.getFormDAO(sspd.getLong("product_id"), productfd);
		%>
        <tr>
		  <td><a href="product_show.jsp?id=<%=sspd.getLong("product_id")%>&formCode=sales_product_info" target="_blank"><%=fdaoProduct.getFieldValue("product_name")%></a></td>
          <td><%=sspd.getInt("num")%></td>
          <td><input name="count" size="2" />
          <input name="productId" value="<%=sspd.getLong("product_id")%>" type="hidden" />
          </td>
        </tr>
      <%
		}
%>
      </table>
<table width="98%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td align="center">
    <input type="submit" class="btn" value="确定" />
    </td>
  </tr>
</table>
</form>
</body>
</html>
