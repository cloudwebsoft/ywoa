<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="jofc2.model.axis.*"%>
<%@ page import="jofc2.model.elements.*"%>
<%@ page import="jofc2.model.*"%>
<%@page import="jofc2.OFCException"%>
<%@ include file="../inc/nocache.jsp"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege" />
<%
if (!privilege.isUserPrivValid(request, "admin")) {
	return;
}

PieChart c2 = new PieChart(); // 饼图

String realPath = Global.getRealPath();
if (realPath.indexOf(":")==1) {
	String disk = realPath.substring(0, 2);
	File file = new File(disk);
	
	double freeSpace = (double)file.getFreeSpace()/1024000000;
	
	double totalSpace = (double)file.getTotalSpace()/1024000000; // G
	boolean isCloud = false;
	com.redmoon.oa.android.CloudConfig cfg = com.redmoon.oa.android.CloudConfig.getInstance();
	if (cfg.getIntProperty("diskSpace")!=-1) {
		isCloud = true;
		totalSpace = cfg.getIntProperty("diskSpace") / 1024; // G
	}

	String sql = "select sum(file_size) from oa_message_attach";
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = jt.executeQuery(sql);
	int msgSpace = 0;
	if (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		msgSpace = rr.getInt(1);
		c2.addSlice((int)msgSpace/1024000000, "内部邮箱" + NumberUtil.round(msgSpace/1024000000, 2) + "G"); // 增加一块
	}
	
	sql = "select sum(file_size) from flow_document_attach";
	ri = jt.executeQuery(sql);
	int flowFileSize = 0;
	if (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		flowFileSize = rr.getInt(1);
		c2.addSlice((int)flowFileSize/1024000000, "流程文件" + NumberUtil.round(flowFileSize/1024000000, 2) + "G"); // 增加一块
	}
	
	sql = "select sum(file_size) from document_attach";
	ri = jt.executeQuery(sql);
	int docFileSize = 0;
	if (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		docFileSize = rr.getInt(1);
		c2.addSlice((int)docFileSize/1024000000, "文件柜" + NumberUtil.round(docFileSize/1024000000, 2) + "G"); // 增加一块
	}	
	
	if (!isCloud) {
		double usedSpace = (double)(totalSpace*1024000000-file.getFreeSpace()-msgSpace - flowFileSize - docFileSize)/1024000000;
		c2.addSlice((int)usedSpace, "其它" + NumberUtil.round(usedSpace, 2) + "G"); // 增加一块
	}
	
	if (!isCloud) {
		c2.addSlice((int)freeSpace, "可用" + NumberUtil.round(freeSpace, 2) + "G"); // 增加一块 
	}
	else {
		double remainSpace = (double)(totalSpace*1024000000 - msgSpace - flowFileSize - docFileSize)/1024000000;
		c2.addSlice((int)freeSpace, "可用" + NumberUtil.round(remainSpace, 2) + "G"); // 增加一块 
	}
	
	c2.setStartAngle(-90); // 开始的角度
	c2.setColours(new String[] { "0x336699", "0x88AACC", "0x999933", "0x666699", "0xCC9933", "0x006666", "0x3399FF", "0x993300", "0xAAAA77", "0x666666", "0xFFCC66", "0x6699CC", "0x663366", "0x9999CC", "0xAAAAAA", "0x669999", "0xBBBB55", "0xCC6600", "0x9999FF", "0x0066CC", "0x99CCCC", "0x999999", "0xFFCC00", "0x009999", "0x99CC33", "0xFF9900", "0x999966", "0x66CCCC", "0x339966", "0xCCCC33" });//饼图每块的颜色
	c2.setTooltip("#val#  /  #total#<br>占百分之 #percent#\n 角度 = #radius#"); //鼠标移动上去后提示内容
	
	Chart flashChart = new Chart();
	flashChart = new Chart("共计：" + NumberUtil.round(totalSpace, 2) + "G"); // 整个图的标题
	flashChart.addElements(c2); // 把饼图加入到图表
	String printString = "";
	try {
		printString = flashChart.toString();
	} catch (OFCException e) {
		printString = flashChart.toDebugString();
	}
	
	out.print(printString);
}
%>
