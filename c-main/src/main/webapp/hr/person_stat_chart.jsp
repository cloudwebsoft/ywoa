<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import = "java.io.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "java.sql.SQLException"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>人事信息统计</title>
<style>
iframe {
width:100%;
height:100%;
}
</style>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="../js/highcharts/highcharts.js" ></script>
<script type="text/javascript" src="../js/highcharts/highcharts-3d.js" ></script>
<script language="javascript">
</script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<%
   String term = ParamUtil.get(request,"term");
%>
<%@ include file="person_stat_inc_menu_top.jsp"%>
<script>
<%if (term.equals("age")) {%>
o("menu1").className="current"; 
<%}else if(term.equals("people")){%>
o("menu2").className="current";
<%}else if (term.equals("zzmm")){%>
o("menu3").className="current";
<%}else  if (term.equals("zgxl")){%>
o("menu4").className="current";
<%}else{%>
o("menu1").className="current";
<%}%>
</script>
<div class="spacerH"></div>
<%
String priv="archive.user";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
String type = ParamUtil.get(request, "type");
if(type.equals("")){
	type = "bt";
}
Vector vec = new Vector();
Iterator itr = null;
Vector vecs = new Vector();
Iterator itrs = null;
Vector vecss = new Vector();
Iterator itrss = null;
int countAll = 0;

// 防SQL注入
if ("".equals(term) || !cn.js.fan.db.SQLFilter.isValidSqlParam(term)) {
	com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ hr/person_stat_chart.jsp term=" + term);
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "param_invalid")));
	return;
}

ResultIterator it = null;
String sql = "";
if("age".equals(term)&&term!=null){
   sql = "select count(id) c,(YEAR(SYSDATE())- YEAR(csrq)+1) a from form_table_personbasic where zzqk='1' group by a ;";
} else {
   sql = "select count("+term+") ,"+term+" from form_table_personbasic where zzqk='1' group by "+term+";";
}
JdbcTemplate jt = new JdbcTemplate();
int otherCount = 0;
try {
	it = jt.executeQuery(sql);
	while (it.hasNext()) {
		ResultRecord rr = (ResultRecord) it.next();
		String termValue = "";
		int countValue = 0;
		if ("age".equals(term)&&term!=null){
			termValue = rr.getString("a");
			countValue = rr.getInt("c");
		} else {
		   termValue = StrUtil.getNullStr(rr.getString(term));
		   countValue = rr.getInt("count("+term+")") ;
		   
		   if (termValue.equals("")) {
			   otherCount += countValue;
		   }
		}
		countAll += countValue;
		if(!termValue.equals("") && !termValue.equals("people") && !termValue.equals("polity") && !termValue.equals("mostgrade")){
			vec.addElement(term + "|" + termValue + "|" + countValue);
		}
	}
} catch (SQLException ex) {
	throw ex;
} finally {
	if (jt != null) {
		jt.close();
	}
}
if (otherCount > 0) {
	vec.addElement(term + "|其他|" + otherCount);
}
if(vec !=null){
	itr = vec.iterator();	
}
%>
<div style="margin:0px">
<form name="form1" action="?op=set&term=<%=term %>" method="post">
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td valign="top" background="images/tab-b-back.gif">

	<table class="tabStyle_1 percent98" width="97%" border="0" align="center" cellpadding="2" cellspacing="0" >
          <tr>
            <td height="24" class="tabStyle_1_title" >查询统计</td>
          </tr>
          <tr>
            <td height="24" align="left" >
            <input <%=type.equals("bt")?"checked":"" %> type="radio" name=type value="bt" />饼图<input <%=type.equals("zzt")?"checked":"" %>  type="radio" name=type  value="zzt"/>柱状图
			</td>
          </tr>
          <tr>
            <td height="24" align="center" >
			<input class="btn" type="submit" value="确定" />
			</td>
          </tr>
      </table>
    </td>
  </tr>
</table>
 </form>
</div>
<div id="container" class="container"></div>

<script type="text/javascript">
var chart;
function highChartts(){
 var type = '<%=type%>';
 var sql = "<%=ThreeDesUtil.encrypt2hex("cloudwebcloudwebcloudweb",sql)%>";
 <%
 if(type.equals("bt")){%>
 	//$.ajax({
    		//type:"get",
    		//url:"../js/highcharts/highcharts_do.jsp",
    		////data:{"op":type,"sql":sql,term:"<%=term%>"},
    		//success:function(data,status){
    		//	alert("sd");
    		//},
    		//error:function(XMLHttpRequest, textStatus){
			//	alert(XMLHttpRequest.responseText);
			//}
			
    	//})
 <%}else if(type.equals("zzt")){%>
 	
 <%}
 %>
 
 
}

<%
if(type.equals("bt")){
%>
    $(document).ready(function() {
 		//highChartts();
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
                text: ''
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
                name: 'Browser share',
                data: [
<%
int val1 = 0;
int val2 = 0;
int val3 = 0;
int val4 = 0;
int val5 = 0;
int val6 = 0;
int val7 = 0;
int val8 = 0;
while(itr.hasNext()){
	String value = itr.next().toString();
	String val[] = value.split("\\|");
	String terms = "";
	 if(val[0].equals("age")){
		 terms = "年龄";
     }else if(val[0].equals("people")){
    	 terms = "民族";
     }else if(val[0].equals("zzmm")){
    	 terms = "政治面貌";
     }else if(val[0].equals("zgxl")){
    	 terms = "学历";
     }
	 if(!term.equals("age")){
%> 
				['<%=terms+":"+val[1]%>',  <%=val[2]%>,],
 <%}else{
	 if(StrUtil.toInt(val[1],-1)>0&&StrUtil.toInt(val[1],-1)<=10){
		 val1 += StrUtil.toInt(val[2],-1);			
	 }else if(StrUtil.toInt(val[1],-1)>10&&StrUtil.toInt(val[1],-1)<=20){
		 val2 += StrUtil.toInt(val[2],-1);
	 }else if(StrUtil.toInt(val[1],-1)>20&&StrUtil.toInt(val[1],-1)<=30){
		 val3 += StrUtil.toInt(val[2],-1);
	 }else if(StrUtil.toInt(val[1],-1)>30&&StrUtil.toInt(val[1],-1)<=40){
		 val4 += StrUtil.toInt(val[2],-1);
	 }else if(StrUtil.toInt(val[1],-1)>40&&StrUtil.toInt(val[1],-1)<=50){
		 val5 += StrUtil.toInt(val[2],-1);
	 }else if(StrUtil.toInt(val[2],-1)>50&&StrUtil.toInt(val[1],-1)<=60){
		 val6 += StrUtil.toInt(val[2],-1);
	 }else if(StrUtil.toInt(val[1],-1)>60&&StrUtil.toInt(val[1],-1)<=70){
		 val7 += StrUtil.toInt(val[2],-1);
	 }else if(StrUtil.toInt(val[1],-1)>70){
		 val8 += StrUtil.toInt(val[2],-1);
	 }
 }
}
if(term.equals("age")){
 %>  
 				['年龄:0-10',  <%=val1%>],
	 			['年龄:10-20',  <%=val2%>],
	 			['年龄:20-30',  <%=val3%>],
	 			{
                    name: '年龄:30-40',
                    y: <%=val4%>,
                    sliced: true,
                    selected: true
                },
	 			['年龄:40-50', <%=val5%>],
	 			['年龄:50-60', <%=val6%>],
	 			['年龄:60-70', <%=val7%>],
	 			['年龄:70以上', <%=val8%>],    
<%
}%>             
                ]
            }]
        });
    });
<%
}else if(type.equals("zzt")){
%>
  var array = new Array();
  $(document).ready(function() {
        var chart = new Highcharts.Chart({
	        chart: {
	            renderTo: 'container1',
	            type: 'column',
	            margin: 75,
	            options3d: {
	                enabled: true,
	                alpha: 10,
	                beta: 10,
	                depth: 50,
	                viewDistance: 25
	            }
	        },
	        title: {
	            text: ''
	        },
	        subtitle: {
	            text: ''
	        },
	        plotOptions: {
	            column: {
	                depth: 35
	            }
	        },
            xAxis: {
                categories: [
<%      
int valz1 = 0;
int valz2 = 0;
int valz3 = 0;
int valz4 = 0;
int valz5 = 0;
int valz6 = 0;
int valz7 = 0;
int valz8 = 0;
ArrayList<String> list = new ArrayList<String>();
while(itr.hasNext()){
	String values = itr.next().toString();
	String vals[] = values.split("\\|");
	String termss = "";
	 if(vals[0].equals("age")){
		 termss = "年龄";
     }else if(vals[0].equals("people")){
    	 termss = "民族";
     }else if(vals[0].equals("zzmm")){
    	 termss = "政治面貌";
     }else if(vals[0].equals("zgxl")){
    	 termss = "学历";
     }
	 vecs.addElement(vals[1]+"|"+vals[2]);
	 if(!term.equals("age")){
		 list.add(termss+":"+vals[1]);
%>
				'<%=termss+":"+vals[1]%>',
<%}else{
	 if(StrUtil.toInt(vals[1],-1)>0&&StrUtil.toInt(vals[1],-1)<=10){
		 valz1 += StrUtil.toInt(vals[2],-1);
	 }else if(StrUtil.toInt(vals[1],-1)>10&&StrUtil.toInt(vals[1],-1)<=20){
		 valz2 += StrUtil.toInt(vals[2],-1);
	 }else if(StrUtil.toInt(vals[1],-1)>20&&StrUtil.toInt(vals[1],-1)<=30){
		 valz3 += StrUtil.toInt(vals[2],-1);
	 }else if(StrUtil.toInt(vals[1],-1)>30&&StrUtil.toInt(vals[1],-1)<=40){
		 valz4 += StrUtil.toInt(vals[2],-1);
	 }else if(StrUtil.toInt(vals[1],-1)>40&&StrUtil.toInt(vals[1],-1)<=50){
		 valz5 += StrUtil.toInt(vals[2],-1);
	 }else if(StrUtil.toInt(vals[2],-1)>50&&StrUtil.toInt(vals[1],-1)<=60){
		 valz6 += StrUtil.toInt(vals[2],-1);
	 }else if(StrUtil.toInt(vals[1],-1)>60&&StrUtil.toInt(vals[1],-1)<=70){
		 valz7 += StrUtil.toInt(vals[2],-1);
	 }else if(StrUtil.toInt(vals[1],-1)>70){
		 valz8 += StrUtil.toInt(vals[2],-1);
	 }
 }
}
	 if(term.equals("age")){
		 list.add("年龄:0-10");
		 list.add("年龄:10-20");
		 list.add("年龄:20-30");
		 list.add("年龄:30-40");
		 list.add("年龄:40-50");
		 list.add("年龄:50-60");
		 list.add("年龄:70以上");
%>
				'年龄:0-10',
	 			'年龄:10-20',
	 			'年龄:20-30',
	 			'年龄:30-40',
	 			'年龄:40-50',
	 			'年龄:50-60',
	 			'年龄:60-70',
	 			'年龄:70以上'
<%		 
	 }
if(vecs !=null){
	itrs = vecs.iterator();	
}%>
                ]
            },
            yAxis: {
				 min: 0,
                 max: 1,
                 labels: {
                             enabled: true
                         },
                title: {
					//enabled: false,
                    text: ''
                }
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
            tooltip: {
                formatter: function() {
                    return "<a href=\"javascript:addTab('" + this.x + "', '<%=request.getContextPath()%>/hr/picture_data_list.jsp?op=search&name=" + this.x + "')\">"+this.x +": "+ (this.y*100).toFixed(2) +"%"+";共"+this.y*<%=countAll%>+"个</a>";
                }
            },
            plotOptions: {
            	
            
                column: {
                point:{
							events : {
								click:function(){
									addTab(array[this.x], '<%=request.getContextPath()%>/hr/picture_data_list.jsp?op=search&name=' + array[this.x] );
								}
							}
						},
                    pointPadding: 0.2,
                    borderWidth: 0
                }
            },
            series: [
            {
                data: [
<% 
if(term.equals("age")){
	%>
	<%=valz1%>/<%=countAll%>,
	<%=valz2%>/<%=countAll%>,
	<%=valz3%>/<%=countAll%>,
	<%=valz4%>/<%=countAll%>,
	<%=valz5%>/<%=countAll%>,
	<%=valz6%>/<%=countAll%>,
	<%=valz7%>/<%=countAll%>,
	<%=valz8%>/<%=countAll%>,
	<%
}else
while(itrs.hasNext()){
	String valuess = itrs.next().toString();
	String valss[] = valuess.split("\\|");
%>
			<%=valss[1]%>/<%=countAll%>,
            
<%
}%>
			]
			}]
        });
        // Activate the sliders
	    $('#R0').on('change', function(){
	        chart.options.chart.options3d.alpha = this.value;
	        showValues();
	        chart.redraw(false);
	    });
	    $('#R1').on('change', function(){
	        chart.options.chart.options3d.beta = this.value;
	        showValues();
	        chart.redraw(false);
	    });
	
	    function showValues() {
	        $('#R0-value').html(chart.options.chart.options3d.alpha);
	        $('#R1-value').html(chart.options.chart.options3d.beta);
	    }
	    showValues();
});
<%
for (String temp : list) { %>
	array.push('<%=temp%>');
<%}
}
%>
</script>
<div id="container1" style="width: 800px;height: 400px"></div>
<table>
	<tr><td>Alpha Angle</td><td><input id="R0" type="range" min="0" max="45" value="10"/> </td></tr>
	<tr><td>Beta Angle</td><td><input id="R1" type="range" min="0" max="45" value="10"/> </td></tr>
</table>
</body>
</html>