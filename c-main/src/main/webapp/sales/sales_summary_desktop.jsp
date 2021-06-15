<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.crm.*" %>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ include file="../inc/nocache.jsp"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
int id = ParamUtil.getInt(request, "id");
UserDesktopSetupDb udsd = new UserDesktopSetupDb();
udsd = udsd.getUserDesktopSetupDb(id);

String userName = privilege.getUser(request);
%>
<div id="drag_<%=id%>" dragTitle="<%=udsd.getTitle()%>" count="<%=udsd.getCount()%>" wordCount="<%=udsd.getWordCount()%>" class="portlet drag_div bor" >

<div class="portlet_content" style="margin:0px; padding:0px">

<table id="tabSummary" width="100%" class="tabStyle_1" style="width:100%; padding:0px" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td colspan="4">
    	<div id="drag_<%=id%>_h" class="box">
    	<!--<span class="titletxt"><img src="<%=SkinMgr.getSkinPath(request)%>/images/titletype.png" width="8" height="12" />销售工作概况</span>-->
       	<!--<div class="opbut-2"><img onclick="minSummary()" title="最小化" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/minimization.png" align="absmiddle" width="19" height="19"/></div>-->
      	<!--<div class="opbut-3"><img onclick="clo('<%=udsd.getId()%>')" title="关闭" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/close.png" align="absmiddle" width="19" height="19"/></div>-->
      	<div class="titleimg">
        <!--<img src="images/desktop/sales.summary.png" width="40" height="40" />-->
        <i class="fa <%=udsd.getIcon()%>"></i>
        &nbsp;&nbsp;</div>
        <div class="titletxt">&nbsp;&nbsp;<%=udsd.getTitle()%></div>
      <script>
	  function minSummary() {
		jQuery('#tabSummary').find('tr').each(function (i) {
			if (i!=0) {
				if (jQuery(this).css("display")=="none")
					jQuery(this).show();
				else
					jQuery(this).hide();
			}
		});   
		  
	  }
	  </script>
	  </div>
    </td>
    </tr>
<tbody>
  <tr><td width="26%"><%
String sql = "select count(o.id) from form_table_sales_order o, form_table_sales_ord_product p where o.cws_creator=? and o.id=p.cws_id and o.order_date>=? and o.order_date<?";
JdbcTemplate jt = new JdbcTemplate();
java.util.Date[] dateAry = DateUtil.getDateSectOfCurMonth();
ResultIterator ri = jt.executeQuery(sql, new Object[]{userName, dateAry[0], dateAry[1]});
int curMonOrderCount = 0;
if (ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	curMonOrderCount = rr.getInt(1);
}
%>
订单数</td>
    <td width="26%"><%=curMonOrderCount%></td>
    <td width="23%"><%
sql = "select sum(real_sum) from form_table_sales_order o, form_table_sales_ord_product p where o.cws_creator=? and o.id=p.cws_id and o.order_date>=? and o.order_date<?";
dateAry = DateUtil.getDateSectOfCurMonth();
ri = jt.executeQuery(sql, new Object[]{userName, dateAry[0], dateAry[1]});
double curMonSum = 0.0;
if (ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	curMonSum = rr.getDouble(1);
}
dateAry = DateUtil.getDateSectOfLastMonth();
ri = jt.executeQuery(sql, new Object[]{userName, dateAry[0], dateAry[1]});
double lastMonSum = 0.0;
if (ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	lastMonSum = rr.getDouble(1);
}
%>
销售额</td>
    <td width="25%"><%=NumberUtil.round(curMonSum, 2)%></td>
  </tr>
  <tr>
    <td>与上月相比</td>
    <td><%if (lastMonSum>0) {%>
      <%=NumberUtil.round((double)curMonSum/lastMonSum*100, 2)%>%
      <%}%></td>
    <td>上月销售额</td>
    <td><%=NumberUtil.round(lastMonSum, 2)%></td>
  </tr>
  <tr>
    <td><%
sql = "select sum(p.num) from form_table_sales_order o, form_table_sales_ord_product p where o.cws_creator=? and o.id=p.cws_id and o.order_date>=? and o.order_date<?";
dateAry = DateUtil.getDateSectOfCurMonth();
ri = jt.executeQuery(sql, new Object[]{userName, dateAry[0], dateAry[1]});
int curMonProCount = 0;
if (ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	curMonProCount = rr.getInt(1);
	if (curMonProCount==-1)
		curMonProCount = 0;
}
%>
售出产品数</td>
    <td><%=curMonProCount%></td>
    <td><%
sql = "select sum(zj) from form_table_sales_chance c, form_table_sales_cha_product p where c.cws_creator=? and c.id=p.cws_id and c.pre_date>=? and c.pre_date<?";
dateAry = DateUtil.getDateSectOfCurMonth();
ri = jt.executeQuery(sql, new Object[]{userName, dateAry[0], dateAry[1]});
double curMonSaleSum = 0.0;
if (ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	curMonSaleSum = rr.getDouble(1);
}
%>
本月预计销售额</td>
    <td><%=curMonSaleSum%></td>
  </tr>
  <tr>
    <td><%
sql = "select count(*) from form_table_sales_customer where cws_creator=? and find_date>=? and find_date<?";
dateAry = DateUtil.getDateSectOfCurMonth();
ri = jt.executeQuery(sql, new Object[]{userName, dateAry[0], dateAry[1]});
int curMonCustomer = 0;
if (ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	curMonCustomer = rr.getInt(1);
}
%>
新增客户</td>
    <td><%=curMonCustomer%></td>
    <td><%
sql = "select sum(zj) from form_table_sales_chance c, form_table_sales_cha_product p where c.cws_creator=? and c.id=p.cws_id and c.pre_date>=? and c.pre_date<?";
dateAry = DateUtil.getDateSectOfNextMonth();
ri = jt.executeQuery(sql, new Object[]{userName, dateAry[0], dateAry[1]});
double nextMonSaleSum = 0.0;
if (ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	nextMonSaleSum = rr.getDouble(1);
}
%>
下月预计销售额</td>
    <td><%=nextMonSaleSum%></td>
  </tr>
  <tr>
    <td><%
sql = "select count(*) from form_table_day_lxr where cws_creator=? and visit_date>=? and visit_date<?";
dateAry = DateUtil.getDateSectOfCurMonth();
ri = jt.executeQuery(sql, new Object[]{userName, dateAry[0], dateAry[1]});
int curMonAction = 0;
if (ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	curMonAction = rr.getInt(1);
}
%>
行动次数</td>
    <td><%=curMonAction%></td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
</tbody>
</table>
</div>
</div>
