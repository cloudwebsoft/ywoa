<%@ page contentType="text/html;charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.questionnaire.*"%> 
<%@ page import="java.lang.Exception"%> 
<%@ page import="com.redmoon.oa.questionnaire.QuestionnaireFormDb"%> 
<%@ page import="java.math.BigDecimal"%> 
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate"%> 
<%@ page import="cn.js.fan.db.ResultIterator"%> 
<%@ page import="cn.js.fan.db.ResultRecord"%> 
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<title>图表</title>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="../js/highcharts/highcharts.js" ></script>
<script type="text/javascript" src="../js/highcharts/highcharts-3d.js" ></script>
</head>
<body>
<%
int formId = ParamUtil.getInt(request,"form_id");
int itemId = ParamUtil.getInt(request,"item_id");
%>
<script>
$(function () {
	$.ajax({
		type:"post",
		url:"questionnaire_item_showchart_do.jsp",
		data:{
			form_id: <%=formId%>, 
			item_id: <%=itemId%>
		},
		dataType:"html",
		success: function(data, status){
			data = $.parseJSON(data.trim());
			$('#container').highcharts({
        chart: {
            type: 'pie',
            options3d: {
                enabled: true,
                alpha: 55,
                beta: 0
            }
        },
        title: {
            text: data.name
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
            name: data.itemName+"占比",
            data: data.attr,
        }]
    });
		
		},
		complete: function(XMLHttpRequest, status){
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});
});
</script>
<div class="spacerH"></div>
<div id="container"></div>

<div style="text-align:center; margin-bottom:10px">
参加测评人数：<%=QuestionnaireStatistics.getNumOfJoin(formId, itemId)%>
<br/>
<%
// 取出kind置于数组
String sql = "select distinct kind from oa_questionnaire_priv" + " where quest_id = " + formId + " order by id asc";
JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = jt.executeQuery(sql);
String[] kindAry = new String[ri.size()]; 
int n = 0;
while (ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	kindAry[n] = rr.getString(1);
	n++;
}
%>
<table width="611" border="0" align="center" class="percent80 tabStyle_1">
  <thead>
    <tr>
      <td width="34">编号</td>
      <td width="157">题目</td>
      <td width="56">票数</td>
      <td width="80">票数*权重</td>
      <%
      for (String kind:kindAry) {
      	%>
      	<td width="60"><%=kind %></td>
      	<%
      }
      %>      
    </tr>
  </thead>
  <tbody>
<%
String[] ary = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "k"};

QuestionnaireFormItemDb qfid = new QuestionnaireFormItemDb();
qfid = qfid.getQuestionnaireFormItemDb(itemId);

Vector itemOptions = qfid.getSubItems();
QuestionnaireStatistics qs = new QuestionnaireStatistics();
int [] itemValueStatistics = qs.itemValueStatistics(qfid.getItemId());
int [] itemValueRealStatistics = qs.itemValueRealStatistics(qfid.getItemId());
for(int i=0;i<itemOptions.size();i++) {
	QuestionnaireFormSubitemDb qfsd = ((QuestionnaireFormSubitemDb)itemOptions.elementAt(i));
%>
    <tr>
      <td><%=ary[i]%>、</td>
      <td><%=qfsd.getName()%></td>
      <td><%=itemValueRealStatistics[i]%></td>
      <td><%=itemValueStatistics[i]%></td>
      <%
      for (String kind:kindAry) {
      	int c = 0;
		sql = "select count(subitem_value) from oa_questionnaire_item a, oa_questionnaire_subitem b" 
			+ " where a.item_id=b.item_id and a.item_id=" + itemId + " and b.subitem_value=" + qfsd.getId() + " and a.kind=" + StrUtil.sqlstr(kind);     
		ri = jt.executeQuery(sql);  
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			c = rr.getInt(1);
		}
      	%>
      	<td width="60"><%=c%></td>
      	<%
      }
      %>      
    </tr>
<%}%>
  </tbody>
</table>
</div>
</body>
</html>