<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "java.io.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.BasicDataMgr"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import="org.jfree.chart.entity.StandardEntityCollection"%>
<%@ page import="org.jfree.chart.*,
org.jfree.chart.labels.*,
org.jfree.chart.plot.*,
org.jfree.data.general.*,
org.jfree.chart.servlet.*,
java.text.*,
java.util.*,
java.awt.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>查询报表 - 饼图</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<%
if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int id = ParamUtil.getInt(request, "id");
FormQueryDb aqd = new FormQueryDb();
aqd = aqd.getFormQueryDb(id);

FormDb fd = new FormDb();
fd = fd.getFormDb(aqd.getTableCode());

String fieldDesc = aqd.getChartPie();
boolean isSeted = !fieldDesc.equals("");
if (!isSeted) {
	out.print(StrUtil.Alert_Back("请先设置参数！"));
	return;
}
String fieldCodeDb = "";

String fieldOptDb = "";
String[] ary = StrUtil.split(fieldDesc, ";");
fieldCodeDb = ary[0];
fieldOptDb = ary[1];
String[] opts = StrUtil.split(fieldOptDb, ",");

String calcFieldCode = "";
String calcFunc = "0";
if (ary.length > 2) {
   calcFieldCode = ary[2];
   calcFunc = ary[3];
} 

FormField ff = fd.getFormField(fieldCodeDb);

FormQueryConditionDb aqcd = new FormQueryConditionDb();
String sql = FormSQLBuilder.getFormQueryCondition(id);

String op = ParamUtil.get(request, "op");
%>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td height="23" valign="middle" class="tdStyle_1">查看图表</td>
  </tr>
  <tr>
    <td valign="top" background="images/tab-b-back.gif">
	<form name="form1" action="?op=set" method="post">
	<table class="" width="97%" border="0" align="center" cellpadding="2" cellspacing="0" >
          <tr>
            <td><table width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td align="center"><br />
<%
MacroCtlMgr mm = new MacroCtlMgr();
String[][] optAry = FormParser.getOptionsArrayOfSelect(fd, ff);
FormSQLBuilder fsb  = new FormSQLBuilder();
String[] sqls = fsb.getSmartQueryChartPie(request, id);
int len = opts.length;
JdbcTemplate jt = new JdbcTemplate();
DefaultPieDataset dataset = new DefaultPieDataset();
for (int i=0; i<len; i++) {
	ResultIterator ri = jt.executeQuery(sqls[i]);
	
	String optText = "";
	if(ff.getType().equals(FormField.TYPE_SELECT)) {
		optText = FormParser.getOptionText(optAry, opts[i]);
	}
	else {
		MacroCtlUnit mu = null;
		String macroCode = "";
		if(ff.getType().equals(FormField.TYPE_MACRO)) {
			 mu = mm.getMacroCtlUnit(ff.getMacroType());
			 if (mu.getCode().equals("macro_flow_select")) {
				SelectOptionDb sod = new SelectOptionDb();
				optText = sod.getOptionName(ff.getDefaultValue(), opts[i]);
			 }
		}
	}
	
	double val = ri.size();
	if (!calcFieldCode.equals("")) {
		if (calcFunc.equals("0")) {
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				val = rr.getDouble(1);
				if (val==-1)
					val = 0;				
			}
		}
		else {
			// 求平均值
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				val = rr.getDouble(1);
				val = val / ri.size();
			}
		}
	}
	
	dataset.setValue(optText, val);
}
		
// 通过工厂类生成JFreeChart对象
JFreeChart chart = ChartFactory.createPieChart("分布图", dataset, true, false, false);
PiePlot pieplot = (PiePlot) chart.getPlot();
pieplot.setLabelFont(new Font("宋体", 0, 12));
// 没有数据的时候显示的内容
pieplot.setNoDataMessage("无数据显示");
pieplot.setCircular(false);
pieplot.setLabelGap(0.02D);

// {0}表示section名，{1}表示section的值，{2}表示百分比。可以自定义。而new DecimalFormat("0.00%")表示小数点后保留两位
pieplot.setLabelGenerator(new StandardPieSectionLabelGenerator(("{0}: {1}({2})"), NumberFormat.getNumberInstance(),new DecimalFormat("0.00%")));

// pieplot.setLegendLabelGenerator(new StandardPieItemLabelGenerator("{0} {2}"));  

StandardEntityCollection sec = new StandardEntityCollection(); 
ChartRenderingInfo info = new ChartRenderingInfo(sec);
PrintWriter w = new PrintWriter(out);//输出MAP信息 

String filename = ServletUtilities.saveChartAsPNG(chart, 800, 600, null, session);
String graphURL = request.getContextPath() + "/servlet/DisplayChart?filename=" + filename;

ChartUtilities.writeImageMap(w, "map0", info, false);
%>
                    <img src="<%=graphURL%>" width="800" height="600" border="0" usemap="#map0" />
                  </td>
              </tr>
            </table></td>
          </tr>
      </table>
	  </form>
    </td>
  </tr>
</table>
</body>
</html>
