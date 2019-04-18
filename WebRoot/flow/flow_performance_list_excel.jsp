<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %><%@ page import = "com.redmoon.oa.BasicDataMgr"%><%@ page import = "cn.js.fan.web.*"%><%@ page import="cn.js.fan.util.*"%><%@ page import="java.util.*"%><%@ page import = "com.redmoon.oa.archive.*"%><%@ page import="jxl.*"%><%@ page import="jxl.write.*"%><%@ page import="java.io.*"%><%
		String path = request.getContextPath();
		String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
		response.setContentType("application/vnd.ms-excel");
		
		response.setHeader("Content-disposition", "attachment; filename="+StrUtil.GBToUnicode("绩效统计.xls"));  

    	String param = ParamUtil.get(request, "param");
    		
  		OutputStream os = response.getOutputStream();
  		try {
			String fileName = "performance_stat.xls";
			File file = new File(Global.realPath +"/flow/doc_templ/"+ fileName);				
			Workbook wb = Workbook.getWorkbook(file);
			WritableWorkbook workbook = Workbook.createWorkbook(os, wb);
			WritableSheet sheet = workbook.getSheet(0);
			WritableCell wc = sheet.getWritableCell(0, 4);
			Label label = null;
			jxl.write.Number number = null;
			String sTimePoint = ParamUtil.get(request, "sTimePoint");
			java.util.Date timePoint = null;
			if(sTimePoint.equals("")) {
				timePoint = DateUtil.parse(DateUtil.format(Calendar.getInstance().getTime(), "yyyy-MM-dd"), "yyyy-MM-dd");
			} else {
				timePoint = DateUtil.parse(sTimePoint, "yyyy-MM-dd");
			}
			int col = 0;
			String[] cont = param.split("\\|");
			int row = cont.length;
			for(int i = 0; i < row; i++){
				String[] col_cont = cont[i].split(",");
				col = col_cont.length;
				for(int j=0; j<col; j++ ){
					label = new Label(j, i+1, col_cont[j]);
					sheet.addCell(label);
				}
			} 
			workbook.write();
      		workbook.close();
			wb.close();
			out.clear();
			out = pageContext.pushBody();
			
		}catch (Exception e) {
		e.printStackTrace();
    } finally {
		os.close();
	}
%>