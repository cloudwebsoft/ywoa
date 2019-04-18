  <%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@page import="com.redmoon.oa.sale.Chart"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="com.redmoon.oa.questionnaire.QuestionnaireSubitemDb"%>
<%@page import="java.sql.SQLException"%>
<%@page import="com.redmoon.oa.questionnaire.QuestionnaireFormSubitemDb"%>
<%@page import="com.redmoon.oa.questionnaire.QuestionnaireFormDb"%>
<%@page import="com.redmoon.oa.questionnaire.QuestionnaireItemDb"%>
<%@page import="com.redmoon.oa.questionnaire.QuestionnaireFormItemDb"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>统计</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="../js/highcharts/highcharts.js" ></script>
<script type="text/javascript" src="../js/highcharts/highcharts-3d.js" ></script>
<script type="text/javascript" src="../js/my_highcharts.js" ></script>
<script type="text/javascript" src="../js/highcharts/funnel.js"></script>
<%
	 QuestionnaireSubitemDb qsb = new QuestionnaireSubitemDb();
	 int form_id = ParamUtil.getInt(request,"form_id",0);
	 int item_id= ParamUtil.getInt(request,"item_id",0);
	 QuestionnaireFormDb qfd = new QuestionnaireFormDb(form_id);
	 QuestionnaireFormItemDb qfid = new QuestionnaireFormItemDb(item_id);
	 
	 String title = "";
	 String subTitle = "";
	 if( qfd != null && qfd.isLoaded()){
		 title = StrUtil.getNullStr(qfd.getFormName());
	 }
	 if(qfid != null && qfid.isLoaded()){
		 subTitle = StrUtil.getNullStr(qfid.getItemName());
	 }
	 
	 String sql = "select count(subitem_value),subitem_value from oa_questionnaire_subitem where questionnaire_form_id="+form_id+" and item_id="+item_id+"  group by subitem_value  order by count(subitem_value) desc limit 10"  ;
     JdbcTemplate jt = null;
	 jt = new JdbcTemplate();
	 JSONArray arr = new JSONArray();
	 try {
		ResultIterator ri = jt.executeQuery(sql);
		while (ri.hasNext()) {
			ResultRecord record = (ResultRecord) ri.next();
			int count = record.getInt(1);
			int value = record.getInt(2);
			QuestionnaireFormSubitemDb qfsd = new QuestionnaireFormSubitemDb(value);
			String name = qfsd.getName();
			JSONArray itemArr = new JSONArray();
			itemArr.add(name+"-"+count+"票");
			itemArr.add(count);
			arr.add(itemArr);
		}
	 } catch (SQLException e) {
		
	 }
	 


%>
</head>
<script>
$(function () {
    $('#container').highcharts({
        chart: {
            type: 'pie',
            options3d: {
                enabled: true,
                alpha: 45,
                beta: 0
            }
        },
        title: {
            text: '<%=title%>'
        },
        subtitle: {
            text: '<%=subTitle%>'
        },
        tooltip: {
            pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                depth: 35,
                dataLabels: {
                    enabled: true,
                    format: '{point.name}'
                }
            }
        },
        series: [{
            type: 'pie',
            name: '<%=subTitle%>',
            data: <%=arr%>
        }]
    });
});

</script>
<body>
<div id="container"></div>

</body>
</html>
