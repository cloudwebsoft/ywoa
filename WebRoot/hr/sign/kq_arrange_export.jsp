<%@ page contentType="text/html;charset=utf-8" language="java" %>
<%@ page import="com.redmoon.oa.notice.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.hr.*"%>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@ page import="cn.js.fan.db.ResultRecord"%>
<%@ page import="java.io.*"%>
<%@ page import="jxl.*"%>
<%@ page import="jxl.write.*"%>
<%@ page import="com.cloudwebsoft.framework.util.*"%>
<%@ page import="org.apache.poi.hssf.usermodel.*"%>
<%@ page import="org.apache.poi2.poifs.filesystem.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege" />
<%
	String beginDate = ParamUtil.get(request, "begin_date");
	String endDate = ParamUtil.get(request, "end_date");
	String departCode = ParamUtil.get(request,"deptCode");
	String name = ParamUtil.get(request,"name");
	String op = ParamUtil.get(request,"op");
	String sql = "select d.name,kq.number,kq.name,sum(kq.txsc),sum(kq.earlycount),sum(kq.latecount),sum(kq.abscount),sum(kq.nocount),sum(kq.days),sum(kq.tripday),sum(kq.sickday),sum(kq.thingday),sum(kq.yearday),sum(kq.marryday),sum(kq.maternityday),sum(kq.otherday),sum(kq.workday),sum(kq.wcday) from kaoqin_arrange kq,users u,dept_user du,department d where kq.number = u.person_no and u.name = du.USER_NAME and du.DEPT_CODE = d.`code` and date >= "
		+ SQLFilter.getDateStr(beginDate,"yyyy-MM-dd")
		+ "and date <= "
		+ SQLFilter.getDateStr(endDate,"yyyy-MM-dd")+" group by d.name,kq.number,kq.name";
	if ("search".equals(op)){
			SignMgr kq = new SignMgr(); 
			String[] preDate = kq.getPreMonDate(); 
			String begindate = preDate[0];
			String enddate = preDate[1];
			String condition = " where kq.number = u.person_no and u.name = du.USER_NAME and du.DEPT_CODE = d.`code`";
			if (!"0".equals(departCode)){
				condition += " and d.code = "+SQLFilter.sqlstr(departCode);
			}
			if (!"".equals(beginDate)&&!"".equals(endDate)){
					condition += " and kq.date >= "+SQLFilter.getDateStr(beginDate,"yyyy-MM-dd") +" and kq.date <= "+SQLFilter.getDateStr(endDate,"yyyy-MM-dd");
			} else {
					condition += " and kq.date >= "+SQLFilter.getDateStr(begindate,"yyyy-MM-dd") +" and kq.date <= "+SQLFilter.getDateStr(enddate,"yyyy-MM-dd");
			}
			if (!"".equals(name)){
					condition += " and kq.name = "+SQLFilter.sqlstr(name);
			}
			sql = "select d.name,kq.number,kq.name,sum(kq.txsc),sum(kq.earlycount),sum(kq.latecount),sum(kq.abscount),sum(kq.nocount),sum(kq.days),sum(kq.tripday),sum(kq.sickday),sum(kq.thingday),sum(kq.yearday),sum(kq.marryday),sum(kq.maternityday),sum(kq.otherday),sum(kq.workday),sum(kq.wcday) from kaoqin_arrange kq,users u,dept_user du,department d"+condition+" group by d.name,kq.number,kq.name";
	   }
	   JdbcTemplate jt = new JdbcTemplate();
	   ResultIterator ri = jt.executeQuery(sql);
	   response.setContentType("application/vnd.ms-excel");
	   response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode("员工考勤记录.xls"));  

	   OutputStream os = response.getOutputStream();

	   try {
	   	File file = new File(Global.realPath + "hr/sign/leave_list_excel1.xls");
	   	
	   	HSSFWorkbook wb = new HSSFWorkbook();  
	   	HSSFSheet sheet = wb.createSheet("考勤记录汇总");  
	   	HSSFRow row = sheet.createRow(0);  
	   	HSSFCellStyle style = wb.createCellStyle();  
	   	style.setAlignment(HSSFCellStyle.ALIGN_CENTER); 
	   	HSSFCell cell = row.createCell(0);  
	    cell.setCellValue("部门");  
	    cell.setCellStyle(style);  
	    cell = row.createCell(1);  
	    cell.setCellValue("编号");  
	    cell.setCellStyle(style);  
	    cell = row.createCell(2);  
	    cell.setCellValue("姓名");  
	    cell.setCellStyle(style);  
	    cell = row.createCell(3);  
	    cell.setCellValue("实际上班天数");  
	    cell.setCellStyle(style);
	    cell = row.createCell(4);  
	    cell.setCellValue("早退次数");  
	    cell.setCellStyle(style);
	    cell = row.createCell(5);  
	    cell.setCellValue("迟到次数");  
	    cell.setCellStyle(style);
	    cell = row.createCell(6);  
	    cell.setCellValue("旷工天数");  
	    cell.setCellStyle(style);
	    cell = row.createCell(7);  
	    cell.setCellValue("缺勤天数");  
	    cell.setCellStyle(style);
	    cell = row.createCell(8);  
	    cell.setCellValue("出差天数");  
	    cell.setCellStyle(style);
	    cell = row.createCell(9);  
	    cell.setCellValue("病假天数");  
	    cell.setCellStyle(style);
	    cell = row.createCell(10);  
	    cell.setCellValue("事假天数");  
	    cell.setCellStyle(style);
	    cell = row.createCell(11);  
	    cell.setCellValue("年假天数");  
	    cell.setCellStyle(style);
	    cell = row.createCell(12);  
	    cell.setCellValue("婚假天数");  
	    cell.setCellStyle(style);
	    cell = row.createCell(13);  
	    cell.setCellValue("产假天数");  
	    cell.setCellStyle(style);
	    cell = row.createCell(14);  
	    cell.setCellValue("其它天数");  
	    cell.setCellStyle(style);
	    cell = row.createCell(15);  
	    cell.setCellValue("外出天数");  
	    cell.setCellStyle(style);
	    cell = row.createCell(16);  
	    cell.setCellValue("加班(小时)");  
	    cell.setCellStyle(style);
	    cell = row.createCell(17);  
	    cell.setCellValue("调休(小时)");  
	    cell.setCellStyle(style);
	   	ResultRecord rd = null;
	   	int j = 1;
	   	while (ri.hasNext()) {
	   		rd = (ResultRecord) ri.next();
	   		String deptName = rd.getString(1);
			String number = rd.getString(2);
			String realName = rd.getString(3);
			double txsc = rd.getDouble(4);
			int earlycount = rd.getInt(5);
			int latecount = rd.getInt(6);
			int abscount = rd.getInt(7);
			int nocount = rd.getInt(8);
			int days = rd.getInt(9);                 //实际工作天数
			int tripDay = rd.getInt(10);
			int sickday = rd.getInt(11);
			int thingDay = rd.getInt(12);
			int yearDay = rd.getInt(13);
			int marryDay = rd.getInt(14);
			int maternityDay = rd.getInt(15);
			int otherDay = rd.getInt(16);
			double workDay = rd.getDouble(17);
			int wcDay = rd.getInt(18);
	   		
	   		row = sheet.createRow(j);
	   	    cell = row.createCell(0);  
		    cell.setCellValue(deptName);  
		    cell = row.createCell(1);  
		    cell.setCellValue(number);  
		    cell = row.createCell(2);  
		    cell.setCellValue(realName);  
		    cell = row.createCell(3);  
		    cell.setCellValue(days);  
		    cell = row.createCell(4);  
		    cell.setCellValue(earlycount);  
		    cell = row.createCell(5);  
		    cell.setCellValue(latecount);  
		    cell = row.createCell(6);  
		    cell.setCellValue(nocount);  
		    cell = row.createCell(7);  
		    cell.setCellValue(abscount);  
		    cell = row.createCell(8);  
		    cell.setCellValue(tripDay);  
		    cell = row.createCell(9);  
		    cell.setCellValue(sickday);  
		    cell = row.createCell(10);  
		    cell.setCellValue(thingDay);  
		    cell = row.createCell(11);  
		    cell.setCellValue(yearDay);  
		    cell = row.createCell(12);  
		    cell.setCellValue(marryDay);  
		    cell = row.createCell(13);  
		    cell.setCellValue(maternityDay);  
		    cell = row.createCell(14);  
		    cell.setCellValue(otherDay);  
		    cell = row.createCell(15);  
		    cell.setCellValue(wcDay);  
		    cell = row.createCell(16);  
		    cell.setCellValue(NumberUtil.round(workDay, 1));
		    cell = row.createCell(17);  
		    cell.setCellValue(txsc); 
	   		j++;
	   	}
	   	wb.write(os);
	   }
	   catch (Exception e) {
		   LogUtil.getLog(getClass()).error("考勤数据导出异常："+StrUtil.trace(e));
	   }
	   finally {
	   	os.close();
	   }
	   out.clear();
	   out = pageContext.pushBody();
%>