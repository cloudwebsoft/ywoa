<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "java.io.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.BasicDataMgr"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import="org.jfree.chart.*" %>
<%@ page import="org.jfree.chart.axis.*" %>
<%@ page import="org.jfree.chart.labels.*" %>
<%@ page import="org.jfree.chart.plot.*" %>
<%@ page import="org.jfree.chart.renderer.*" %>
<%@ page import="org.jfree.chart.renderer.category.*" %>
<%@ page import="org.jfree.data.category.*" %>
<%@ page import="org.jfree.chart.servlet.ServletUtilities" %>
<%@ page import="org.jfree.ui.TextAnchor" %>
<%@ page import="org.jfree.chart.urls.StandardCategoryURLGenerator" %>
<%@ page import="java.awt.*"%>
<%@ page import="org.jfree.chart.entity.StandardEntityCollection"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>柱状图</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<%
if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

/*
String item = request.getParameter("item");
if (item!=null) {
	item = new String(item.getBytes("ISO8859_1"), "GBK");
}
if (item!=null) {
out.print(item);
return;
}
*/

int id = ParamUtil.getInt(request, "id");
FormQueryDb aqd = new FormQueryDb();
aqd = aqd.getFormQueryDb(id);

FormDb fd = new FormDb();
fd = fd.getFormDb(aqd.getTableCode());

String fieldDesc = aqd.getChartHistogram();
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

String fieldCode = fieldCodeDb;

FormField ff = fd.getFormField(fieldCode);

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
                <td align="center"><br /><%
MacroCtlMgr mm = new MacroCtlMgr();
String[][] optAry = FormParser.getOptionsArrayOfSelect(fd, ff);
FormSQLBuilder fsb  = new FormSQLBuilder();		
String[] sqls = fsb.getSmartQueryChartHistogram(request, id);
int len = sqls.length;
JdbcTemplate jt = new JdbcTemplate();
DefaultCategoryDataset dataset = new DefaultCategoryDataset();
for (int i=0; i<len; i++) {
	System.out.println(getClass() + " " + sqls[i]);
	System.out.println("###--------------");
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
	
	dataset.addValue(val, "数量", optText);		
}

JFreeChart chart = ChartFactory.createBarChart3D(aqd.getQueryName(), 
                  ff.getTitle(),
                  "数量",
                  dataset,
                  PlotOrientation.VERTICAL,
                  false,
                  false,
                  false);
BarRenderer3D renderer = new BarRenderer3D();
// 设置柱的颜色
CategoryPlot plot = chart.getCategoryPlot();
renderer.setSeriesPaint(0, new Color(0xff00));

renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
renderer.setBaseItemLabelsVisible(true);
renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
renderer.setItemLabelAnchorOffset(10D);
renderer.setItemMargin(0.1);

renderer.setBaseItemURLGenerator(new StandardCategoryURLGenerator("archive_query_chart_histogram_show.jsp", "item", ""));

plot.setRenderer(renderer);

//设置柱的透明度
plot.setForegroundAlpha(0.5f);

java.io.PrintWriter pw=new java.io.PrintWriter(out);

ChartRenderingInfo info = new ChartRenderingInfo(new
                    StandardEntityCollection());

String filename = ServletUtilities.saveChartAsPNG(chart, 700, 400, info, session);
String graphURL = request.getContextPath() + "/servlet/DisplayChart?filename=" + filename;

// ChartUtilities.writeImageMap(pw, "map0", info, false); 
pw.flush();
%>
      <img src="<%=graphURL%>" height=400 border=0 usemap="#map0">
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
