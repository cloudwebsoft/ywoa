<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.sale.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@page import="net.sf.json.JSONObject"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
// 取得皮肤路径
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))
	skincode = UserSet.defaultSkin;

SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

String unitCode = privilege.getUserUnitCode(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>跟踪频率设置</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="../js/highcharts/highcharts.js" ></script>
<script type="text/javascript" src="../js/highcharts/highcharts-3d.js" ></script>
<script type="text/javascript" src="../js/my_highcharts.js" ></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
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
      <td class="tdStyle_1">跟踪频率</td>
    </tr>
  </tbody>
</table>
<%
if (!privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

SelectMgr sm = new SelectMgr();
SelectDb sd = sm.getSelect("sales_customer_type");
Vector vsd = sd.getOptions();
Iterator irsd = vsd.iterator();

String op = ParamUtil.get(request, "op");
ActionSetupDb asd;	
ActionSetupDb asd2 = new ActionSetupDb();	
if (op.equals("save")) {
	while (irsd.hasNext()) {
		SelectOptionDb sod = (SelectOptionDb)irsd.next();
		int custType = StrUtil.toInt(sod.getValue());
		int remindDays = ParamUtil.getInt(request, "remind_days_"+custType, 0);
		int expireDays = ParamUtil.getInt(request, "expire_days_"+custType, 0);
		asd = (ActionSetupDb)asd2.getActionSetupDb(custType, unitCode);
		if (asd==null) {
			asd2.create(new JdbcTemplate(), new Object[]{new Integer(custType), new Integer(remindDays), new Integer(expireDays), unitCode});
		}
		else {
			asd.save(new JdbcTemplate(), new Object[]{new Integer(remindDays), new Integer(expireDays), new Integer(custType), unitCode});
		}
	}
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "sales_action_setup.jsp"));
	return;
}
Chart chart = new Chart();
JSONObject lineParams = chart.salesActionLineDatas(unitCode);


%>
<script>
	$(function(){
		lineCharts('#line',<%=lineParams%>);
	
	})

</script>

<table width="98%" align="center"><tr><td align="center">
   <div style="width: 80%;height: 300px" id="line" ></div>
</td></tr></table>    
<form action="sales_action_setup.jsp?op=save" method="post">  
<table width="98%" align="center" cellspacing="0" cellpadding="0" class="tabStyle_1 percent60">
    <tr>
      <td width="15%" class="tabStyle_1_title">客户类型</td>
      <td width="14%" class="tabStyle_1_title">跟踪频率(天)</td>
      <td width="12%" class="tabStyle_1_title">回落天数(天)</td>
    </tr>
    <%
	irsd = vsd.iterator();
	while (irsd.hasNext()) {
		SelectOptionDb sod = (SelectOptionDb)irsd.next();
		asd = (ActionSetupDb)asd2.getActionSetupDb(StrUtil.toInt(sod.getValue()), unitCode);
	%>
    <tr>
      <td><%=sod.getName()%></td>
      <td><input type="text" name="remind_days_<%=sod.getValue()%>" value="<%=asd==null?0:asd.getInt("remind_days")%>" /></td>
      <td><input type="text" name="expire_days_<%=sod.getValue()%>" value="<%=asd==null?0:asd.getInt("expire_days")%>" /></td>
    </tr>
    <%}%>
    <tr>
      <td colspan="3" align="center"><input class="btn" type="submit" value="确定" />
      <br />
      (注：0表示不需跟踪或不回落)</td>
    </tr>
</table>
</form>
</body>
</html>
