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
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String formCode = "sales_chance";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

// 取得皮肤路径
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))
	skincode = UserSet.defaultSkin;

SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

String userName = ParamUtil.get(request, "userName");
if (userName.equals(""))
	userName = privilege.getUser(request);
UserDb userDb = new UserDb();
userDb = userDb.getUserDb(userName);	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="../js/highcharts/highcharts.js" ></script>
<script type="text/javascript" src="../js/highcharts/highcharts-3d.js" ></script>
<script type="text/javascript" src="../inc/sortabletable.js"></script>
<script type="text/javascript" src="../inc/columnlist.js"></script>
<SCRIPT LANGUAGE="Javascript" SRC="../FusionCharts/FusionCharts.js"></SCRIPT>
<%@ include file="../inc/nocache.jsp"%>
</head>
<body>
<%@ include file="sales_user_inc_menu_top.jsp"%>
<script>
o("menu1").className="current"; 
		
</script>
<div class="spacerH"></div>
<table width="98%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="50%" valign="top">
    <div id="container" style="height:300px;">
    </div>
   
      <%
String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action"); // action为manage时表示为销售总管理员方式
if (action.equals("manage")) {
	if (!privilege.isUserPrivValid(request, "sales"))
	{
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

String priv = "sales.user";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

if (!userName.equals(privilege.getUser(request))) {
	if (!privilege.isUserPrivValid(request, "sales")) {
		if (!privilege.canAdminUser(request, userName)) {
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "您对该用户没有管理权限！"));
			return;
		}
	}
}
String strXML = "<graph caption='" + StrUtil.UrlEncode("本年总业绩") + "' yAxisNam='Units' baseFontSize='12' showNames='1' showValues='1' decimalPrecision='0' formatNumberScale='0' formatNumber='0'>";
SelectMgr sm = new SelectMgr();
SelectDb sd = sm.getSelect("sales_chance_state");

String sql = "select sum(p.real_sum) from form_table_sales_order o, form_table_sales_ord_product p where o.id=p.cws_id and o.cws_creator=? and o.order_date>=? and o.order_date<?";
Calendar cal = Calendar.getInstance();
int y = cal.get(Calendar.YEAR);
int m = cal.get(Calendar.MONTH);
ArrayList<String> al = new ArrayList<String>();
for (int i=1; i<=12; i++)  {
	java.util.Date[] ary = DateUtil.getDateSectOfMonth(y, i-1);
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = jt.executeQuery(sql, new Object[]{userName, ary[0], ary[1]});
	String c = "";
	if (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		if (rr.getDouble(1)!=0)
			c = "" + rr.getDouble(1);
			if(c.equals("")){
				c = "0";
			}
			al.add(c);
	}
	strXML += "<set name='" + i + StrUtil.UrlEncode("月") + "' value='" + c + "' link='n-sales_chance_list.jsp?state=" + i + "' />";
}
strXML += "</graph>";
%>
 <script >
	    $(function () {
	    // Set up the chart
	    var chart = new Highcharts.Chart({
	        chart: {
	            renderTo: 'container',
	            type: 'column',
	            margin: 75,
	            options3d: {
	                enabled: true,
	                alpha: 0,
	                beta: 0,
	                depth: 50,
	                viewDistance: 25
	            }
	        },
	        title: {
	            text: '本年总业绩'
	        },
	         legend: {
                layout: 'vertical',
                backgroundColor: '#FFFFFF',
                align: 'left',
                verticalAlign: 'top',
                x: 100,
                y: 70,
                floating: true,
                shadow: true,
                enabled: false
            },
	        subtitle: {
	            text: ''
	        },
	        plotOptions: {
	            column: {
	                depth: 25
	            }
	        },
	        xAxis: {
	            //categories: Highcharts.getOptions().lang.shortMonths
	            categories:["一月","二月","三月","四月","五月","六月","七月","八月","九月","十月","十一月","十二月"]
	        },
	        yAxis:{
	        	title:{	        		
	        		text:"订单数"
	        	}
	        },
	        series: [{
	            data: [<%=al.get(0)%>,<%=al.get(1)%>,<%=al.get(2)%>,<%=al.get(3)%>,<%=al.get(4)%>,<%=al.get(5)%>,<%=al.get(6)%>,<%=al.get(7)%>,<%=al.get(8)%>,<%=al.get(9)%>,<%=al.get(10)%>,<%=al.get(11)%>]
	        }]
	    });
	    
	});
 </script>
<table width="94%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent98">
<thead>
  <tr>
    <td class="tabStyle_1_title" colspan="4">本月业绩简报</td>
    </tr>
</thead>
  <tr><td width="27%"><%
sql = "select count(o.id) from form_table_sales_order o, form_table_sales_ord_product p where o.cws_creator=? and o.id=p.cws_id and o.order_date>=? and o.order_date<?";
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
    <td width="25%"><%=curMonOrderCount%></td>
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
</table>
</td>
    <td valign="top"><%
FormDb customerfd = new FormDb();
customerfd = customerfd.getFormDb("sales_customer");

sql = "select chance.id from " + fd.getTableNameByForm() + " chance, " + customerfd.getTableNameByForm() + " cust where cust.id=chance.cws_id and cust.sales_person=" + StrUtil.sqlstr(userName) + " order by chance.find_date desc" ;		

// out.print(sql);

int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
	
FormDAO fdao = new FormDAO();

ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
Vector v = lr.getResult();
Iterator ir = v.iterator();
%>
<table width="100%" align="center" class="percent98"><tr><td align="left"><b><a href="sales_user_chance_list.jsp?userName=<%=StrUtil.UrlEncode(userName)%>">最新商机</a></b></td></tr></table>
      <table width="98%" align="center" cellspacing="0" cellpadding="0" class="tabStyle_1 percent98">
      <thead>
        <tr>
          <td class="tabStyle_1_title" width="24%">客户</td>
          <td class="tabStyle_1_title" width="19%">商机阶段</td>
          <td class="tabStyle_1_title" width="19%">预计金额</td>
          <td class="tabStyle_1_title" width="19%">发现日期</td>
          <td class="tabStyle_1_title" width="19%">可能性</td>
        </tr>
        </thead>
        <%
	  	UserMgr um = new UserMgr();
	  	FormDAO fdaoCustomer = new FormDAO();
	  	int i = 0;
		SelectOptionDb sod = new SelectOptionDb();
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
			fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toLong(fdao.getFieldValue("customer")), customerfd);
			
			String realName = "";
			UserDb user = um.getUserDb(fdao.getCreator());
			realName = user.getRealName();
			
			sql = "select sum(zj) from form_table_sales_chance c, form_table_sales_cha_product p where c.id=p.cws_id and c.id=" + id;
			ri = jt.executeQuery(sql);
			double sum = 0.0;
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				sum = rr.getDouble(1);
			}
		%>
        <tr>
          <td><a href="customer_sales_chance_show.jsp?customerId=<%=fdao.getCwsId()%>&amp;parentId=<%=fdao.getCwsId()%>&amp;id=<%=id%>&amp;formCodeRelated=sales_chance&amp;formCode=sales_customer&amp;isShowNav=1" target="_blank"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
          <td><%=sod.getOptionName("sales_chance_state", fdao.getFieldValue("state"))%></td>
          <td><%=sum%></td>
          <td><%=fdao.getFieldValue("find_date")%></td>
          <td><div class="progressBar" style="">
            <div class="progressBarFore" style="width:<%=fdao.getFieldValue("possibility")%>%;"></div>
            <div class="progressText"> <%=fdao.getFieldValue("possibility")%>% </div>
          </div></td>
        </tr>
        <%
		}
%>
    </table>
      <%
sql = "select id from form_table_sales_customer where sales_person=" + StrUtil.sqlstr(userName) + " order by id desc" ;		
formCode = "sales_customer";
lr = fdao.listResult(formCode, sql, 1, 10);
v = lr.getResult();
ir = v.iterator();
%>
      <table width="100%" align="center" class="percent98">
        <tr>
          <td align="left"><b><a href="javascript:;" onclick="addTab('<%=userDb.getRealName()%>的客户', '<%=request.getContextPath()%>/sales/customer_list.jsp?userName=<%=StrUtil.UrlEncode(userName)%>')">最新客户</a></b></td>
        </tr>
      </table>
      <table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
        <thead>
          <tr align="center">
            <td class="tabStyle_1_title" width="24%">客户</td>
            <td class="tabStyle_1_title" width="19%">销售方式</td>
            <td class="tabStyle_1_title" width="19%">性质</td>
            <td class="tabStyle_1_title" width="19%">电话</td>
            <td class="tabStyle_1_title" width="19%">发现日期</td>
          </tr>
        </thead>
        <%	
	  	int k = 0;
		while (ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			k++;
			long id = fdao.getId();
		%>
        <tr align="center">
          <td align="left"><a href="customer_show.jsp?id=<%=id%>&amp;formCode=sales_customer" target="_blank"><%=fdao.getFieldValue("customer")%></a></td>
          <td align="left"><%=sod.getOptionName("xsfs", fdao.getFieldValue("sellMode"))%></td>
          <td align="left"><%=sod.getOptionName("qyxz", fdao.getFieldValue("enterType"))%></td>
          <td align="left"><%=fdao.getFieldValue("tel")%></td>
          <td align="left"><%=fdao.getFieldValue("find_date")%></td>
        </tr>
        <%
  }
%>
    </table>
      <%
sql = "select id from form_table_sales_order where cws_creator=" + StrUtil.sqlstr(userName) + " order by id desc" ;		
formCode = "sales_order";
lr = fdao.listResult(formCode, sql, 1, 10);
v = lr.getResult();
ir = v.iterator();
%>
      <table width="100%" align="center" class="percent98">
        <tr>
          <td align="left"><b><a href="sales_user_order_list.jsp?userName=<%=StrUtil.UrlEncode(userName)%>">最新订单</a></b></td>
        </tr>
      </table>
      <table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
        <thead>
          <tr align="center">
            <td class="tabStyle_1_title" width="24%">客户</td>
            <td class="tabStyle_1_title" width="19%">订单来源</td>
            <td class="tabStyle_1_title" width="19%">订单状态</td>
            <td class="tabStyle_1_title" width="19%">促成人员</td>
            <td class="tabStyle_1_title" width="19%">促成日期</td>
          </tr>
        </thead>
        <%	
	  	k = 0;
		while (ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			k++;
			long id = fdao.getId();
			
			fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toLong(fdao.getCwsId()), customerfd);			
		%>
        <tr align="center">
          <td align="left"><a href="javascript:;" onclick="addTab('<%=fdaoCustomer.getFieldValue("customer")%>订单', '<%=request.getContextPath()%>/visual/module_mode1_show.jsp?parentId=<%=id%>&id=<%=id%>&formCode=sales_order')"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
          <td align="left"><%=sod.getOptionName("sales_order_source", fdao.getFieldValue("source"))%></td>
          <td align="left"><%=sod.getOptionName("sales_order_state", fdao.getFieldValue("status"))%></td>
          <td align="left">&nbsp;</td>
          <td align="left"><%=fdao.getFieldValue("order_date")%></td>
        </tr>
        <%
  }
%>
    </table></td>
  </tr>
</table>
</body>
</html>
