<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.crm.*" %>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ include file="../inc/nocache.jsp"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<SCRIPT LANGUAGE= "Javascript" SRC="FusionCharts/FusionCharts.js"></SCRIPT>
<%
int id = ParamUtil.getInt(request, "id");
UserDesktopSetupDb udsd = new UserDesktopSetupDb();
udsd = udsd.getUserDesktopSetupDb(id);

String userName = privilege.getUser(request);
%>
<div id="drag_<%=id%>" class="portlet drag_div bor" >
<div id="drag_<%=id%>_h" class="box">
		<!--<span class="titletxt"><img src="<%=SkinMgr.getSkinPath(request)%>/images/titletype.png" width="8" height="12" />本年总业绩</span>-->
       	<!--<div class="opbut-2"><img onclick="minSummary()" title="最小化" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/minimization.png" align="absmiddle" width="19" height="19"/></div>-->
       	<!--<div class="opbut-3"><img onclick="clo('<%=udsd.getId()%>')" title="关闭" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/close.png" align="absmiddle" width="19" height="19"/></div>-->
       	<div class="titleimg">
        <!--<img src="images/desktop/sales.achievement.png" width="40" height="40" />-->
        <i class="fa <%=udsd.getIcon()%>"></i>
        &nbsp;&nbsp;</div>
        <div class="titletxt">&nbsp;&nbsp;<%=udsd.getTitle()%></div>
</div>
<div class="portlet_content" style="margin:0px; padding:0px" id="archievementContent">
<%
String strXML = "<graph caption='" + StrUtil.UrlEncode("本年总业绩") + "' yAxisNam='Units' baseFontSize='12' showNames='1' showValues='1' decimalPrecision='0' formatNumberScale='0' formatNumber='0'>";
SelectMgr sm = new SelectMgr();
SelectDb sd = sm.getSelect("sales_chance_state");

String sql = "select sum(p.real_sum) from form_table_sales_order o, form_table_sales_ord_product p where o.id=p.cws_id and o.cws_creator=? and o.order_date>=? and o.order_date<?";
Calendar cal = Calendar.getInstance();
int y = cal.get(Calendar.YEAR);
int m = cal.get(Calendar.MONTH);
for (int i=1; i<=12; i++)  {
	java.util.Date[] ary = DateUtil.getDateSectOfMonth(y, i-1);
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = jt.executeQuery(sql, new Object[]{userName, ary[0], ary[1]});
	String c = "";
	if (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		if (rr.getDouble(1)!=0)
			c = "" + rr.getDouble(1);
	}
	strXML += "<set name='" + i + StrUtil.UrlEncode("月") + "' value='" + c + "' link='n-sales_chance_list.jsp?state=" + i + "' />";
}
strXML += "</graph>";
%>
<jsp:include page="../FusionCharts/Includes/FusionChartsRenderer.jsp" flush="true">
<jsp:param name="chartSWF" value= "FusionCharts/FCF_Column2D.swf" />      
<jsp:param name="strURL" value= "" />      
<jsp:param name="strXML" value= "<%=strXML%>" />      
<jsp:param name="chartId" value= "myFirst" />    
<jsp:param name="chartWidth" value= "400" />      
<jsp:param name="chartHeight" value= "200" />      
<jsp:param name="debugMode" value= "false" />
<jsp:param name="registerWithJS" value= "false" />      
</jsp:include>

</div>
<script type="text/javascript">
function minSummary() {
		jQuery('#archievementContent').toggle();
	  }
</script>
</div>
