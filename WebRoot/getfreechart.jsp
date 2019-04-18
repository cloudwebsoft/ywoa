<%@ page contentType="text/html;charset=GBK"%>
<%@ page import="org.jfree.data.general.DefaultPieDataset"%>
<%@ page import="org.jfree.chart.JFreeChart"%>
<%@ page import="org.jfree.chart.plot.PiePlot"%>
<%@ page import="org.jfree.chart.ChartRenderingInfo"%>
<%@ page import="org.jfree.chart.servlet.ServletUtilities"%>
<%@ page import="org.jfree.chart.urls.StandardPieURLGenerator"%>
<%@ page import="org.jfree.chart.entity.StandardEntityCollection"%>
<%@ page import="org.jfree.chart.encoders.SunPNGEncoderAdapter"%>
<%
/*
DefaultPieDataset dataset = new DefaultPieDataset();
dataset.setValue(StrUtil.GBToUnicode("苹果GOOD"),100);
dataset.setValue("梨子",200);
dataset.setValue("葡萄",300);
dataset.setValue("香蕉",400);
dataset.setValue("荔枝",500);
response.setContentType("image/jpeg");

JFreeChart chart = ChartFactory.createPieChart3D("水果产量图", dataset, true, false, false);

Font font = new Font("宋体",Font.TRUETYPE_FONT, 12);
// StandardLegend legend = (StandardLegend) chart.getLegend();
// legend.setItemFont(font);

String title = "空调2002年市场占有率";
//设定图片标题
chart.setTitle(new TextTitle(title, new Font("宋体", Font.ITALIC, 15)));
// chart.addSubtitle(new TextTitle("2002财年分析", new Font("隶书", Font.ITALIC, 12)));
//设定背景
// chart.setBackgroundPaint(Color.white);
//chart.s
//饼图使用一个PiePlot 
PiePlot pie = (PiePlot)chart.getPlot();
//pie.setSectionLabelType(PiePlot.NAME_AND_PERCENT_LABELS);
//设定显示格式(名称加百分比或数值)
// pie.setPercentFormatString("#,###0.0#%");
//设定百分比显示格式
// pie.setBackgroundPaint(Color.white);
pie.setLabelFont(new Font("宋体", Font.TRUETYPE_FONT, 12));
//设定背景透明度（0-1.0之间）
pie.setBackgroundAlpha(0.6f);
//设定前景透明度（0-1.0之间）
pie.setForegroundAlpha(0.90f);
*/


/*
//图片标题
String title = "空调2002年市场占有率";

DefaultPieDataset piedata = new DefaultPieDataset();
//第一个参数为名称，第二个参数是double数
piedata.setValue("联想", 27.3);
piedata.setValue("长城", 12.2);
piedata.setValue("海尔", 5.5);
piedata.setValue("美的", 17.1);
piedata.setValue("松下", 9.0);
piedata.setValue("科龙", 19.0);
//创建JFreeChart，都使用ChartFactory来创建JFreeChart,很标准的工厂设计模式
JFreeChart chart = ChartFactory.createPieChart(title, piedata, true, true, true);
//设定图片标题
chart.setTitle(new TextTitle(title, new Font("隶书", Font.ITALIC, 15)));
//chart.addSubtitle(new TextTitle("2002财年分析", new Font("隶书", Font.ITALIC, 12)));
//设定背景
chart.setBackgroundPaint(Color.white);
//chart.s
//饼图使用一个PiePlot 
PiePlot pie = (PiePlot)chart.getPlot();
// pie.setSectionLabelType(PiePlot.NAME_AND_VALUE_LABELS);
//设定显示格式(名称加百分比或数值)
// pie.setPercentFormatString("#,###0.0#%");
//设定百分比显示格式
pie.setBackgroundPaint(Color.white);
// pie.setSectionLabelFont(new Font("黑体", Font.TRUETYPE_FONT, 12));
//设定背景透明度（0-1.0之间）
pie.setBackgroundAlpha(0.6f);
//设定前景透明度（0-1.0之间）
pie.setForegroundAlpha(0.90f);


ChartUtilities.writeChartAsJPEG(response.getOutputStream(), 100, chart, 400, 300, null);

*/
DefaultPieDataset data = new DefaultPieDataset();
data.setValue("六月", 500);
data.setValue("七月", 580);
data.setValue("八月", 828); 
data.setValue("梨子",200);
data.setValue("葡萄",300);
data.setValue("香蕉",400);
data.setValue("荔枝",500);


PiePlot plot = new PiePlot(data);
JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
chart.setBackgroundPaint(java.awt.Color.white);  //可选，设置图片背景色
chart.setTitle("档案分析"); //可选，设置图片标题

plot.setToolTipGenerator(new org.jfree.chart.labels.StandardPieToolTipGenerator()); 

ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

//500是图片长度，300是图片高度
String filename = ServletUtilities.saveChartAsPNG(chart, 500, 300, info, session);
String graphURL = request.getContextPath() + "/servlet/DisplayChart?filename=" + filename; 
%>
<HTML>
<HEAD>
       <TITLE>Welcome to Jfreechart !</TITLE>
</HEAD>
<BODY>
<P ALIGN="CENTER">
<img src="<%=graphURL %>" width=500 height=300 border=0 usemap="#<%= filename %>">
</P>
</BODY>

</HTML>
