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
<%@ page import = "org.jfree.chart.ChartFactory,
org.jfree.chart.title.TextTitle,
org.jfree.data.time.TimeSeries,
org.jfree.data.time.Year,
org.jfree.data.time.TimeSeriesCollection,
org.jfree.chart.plot.XYPlot,
org.jfree.chart.renderer.xy.XYLineAndShapeRenderer,
java.awt.Color,
org.jfree.ui.RectangleInsets,
java.awt.Font,
org.jfree.chart.renderer.xy.XYItemRenderer,
org.jfree.chart.JFreeChart,
org.jfree.chart.servlet.ServletUtilities,
org.jfree.chart.labels.*,
org.jfree.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>折线图</title>
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

String fieldDesc = aqd.getChartLine();
boolean isSeted = !fieldDesc.equals("");
if (!isSeted) {
	out.print(StrUtil.Alert_Back("请先设置参数！"));
	return;
}

int beginYear,endYear;

String[] ary = StrUtil.split(fieldDesc, ";");

String chartFieldCode = ary[0];
String fieldOptDb = ary[1];
String calcFieldCode = "";
String calcFunc = "0";

if (ary.length>2) {
	calcFieldCode = ary[2];
	calcFunc = ary[3];
}

String[] ary2 = fieldOptDb.split("-");
beginYear = StrUtil.toInt(ary2[0]);
endYear = StrUtil.toInt(ary2[1]);
%>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td height="23" valign="middle" class="tdStyle_1">预览</td>
  </tr>
  <tr>
    <td valign="top" background="images/tab-b-back.gif">
	<form name="form1" action="?op=set" method="post">
	<table class="" width="97%" border="0" align="center" cellpadding="2" cellspacing="0" >
          <tr>
            <td><table width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td align="center"><br /><%
String[] sqls = null;
try {
	FormSQLBuilder fsb = new FormSQLBuilder();
	sqls = fsb.getSmartQueryChartLine(request, id);
}
catch (ErrMsgException e) {
	out.print(StrUtil.Alert_Back(e.getMessage()));
	return;
}
//访问量统计时间线
TimeSeries timeSeries = new TimeSeries("统计图", Year.class);
//时间曲线数据集合
TimeSeriesCollection lineDataset = new TimeSeriesCollection();
// 构造数据集合
int k = 0;
for (int i=beginYear; i<=endYear; i++) {
	System.out.println(getClass() + " i=" + i + " sql=" + sqls[k]);
	System.out.println("--------------");
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = jt.executeQuery(sqls[k]);
	// System.out.println(getClass() + " " + ri.size());
	
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
	
	timeSeries.add(new Year(i), val);
	
	k++;
}

lineDataset.addSeries(timeSeries);
JFreeChart chart = ChartFactory.createTimeSeriesChart("时间线", "年份", "数量", lineDataset, true, true, true);


XYPlot plot = (XYPlot) chart.getPlot();
XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer)plot.getRenderer();
//设置网格背景颜色
plot.setBackgroundPaint(Color.white);
//设置网格竖线颜色
plot.setDomainGridlinePaint(Color.pink);
//设置网格横线颜色
plot.setRangeGridlinePaint(Color.pink);
//设置曲线图与xy轴的距离
plot.setAxisOffset(new RectangleInsets(0D, 0D, 0D, 10D));
//设置曲线是否显示数据点
xylineandshaperenderer.setBaseShapesVisible(true);
//设置曲线显示各数据点的值
XYItemRenderer xyitem = plot.getRenderer();   
xyitem.setBaseItemLabelsVisible(true);
xyitem.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
xyitem.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
xyitem.setBaseItemLabelFont(new Font("Dialog", 1, 14));
plot.setRenderer(xyitem);


//设置子标题
TextTitle subtitle = new TextTitle(beginYear + " - " + endYear + "年度", new Font("黑体", Font.BOLD, 12));
chart.addSubtitle(subtitle);
//设置主标题
chart.setTitle(new TextTitle(aqd.getQueryName(), new Font("宋体", Font.BOLD, 15)));
chart.setAntiAlias(true);
String filename = ServletUtilities.saveChartAsPNG(chart, 600, 400, null, session);
String graphURL = request.getContextPath() + "/servlet/DisplayChart?filename=" + filename;
%>
<img src="<%=graphURL%>" width=600 height=400 border=0 usemap="#<%=filename%>">

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
