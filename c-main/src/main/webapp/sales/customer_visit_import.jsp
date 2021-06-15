<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.address.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.FormDb"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="java.util.regex.*"%>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>    
<%@ page import="cn.js.fan.util.DateUtil" %>  
<%@page import="org.apache.poi.hssf.usermodel.*"%>   
<%@page import="org.apache.poi.ss.usermodel.*"%>
<%@page import="org.apache.poi.xssf.usermodel.*"%>
<%@page import="org.apache.poi.openxml4j.exceptions.*"%>
<%@ page import="com.redmoon.oa.util.ExcelUploadUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>客户导入</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="<%=request.getContextPath() %>/inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/hopscotch/css/hopscotch.css" />
<script type="text/javascript" src="<%=request.getContextPath() %>/js/hopscotch/hopscotch.js"></script>
<script src="<%=request.getContextPath() %>/inc/upload.js"></script>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/skin/common/organize.css" />
</head>
<%
String priv = "admin.user";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String curUser = privilege.getUser(request);
String flag = ParamUtil.get(request, "flag");//判断是否从引导页面跳转过来的
String op = ParamUtil.get(request,"op");
String action = ParamUtil.get(request,"action");
String kind = ParamUtil.get(request, "kind");
if ("".equals(kind)) {
	kind = "2";
}

if (op.equals("import")) {
	ExcelUploadUtil fum = new ExcelUploadUtil();
	String excelFile = "";
	try{
		excelFile = fum.uploadExcel(application, request);
		if (excelFile.equals("")) {
			//out.print("<script type='text/javascript'>parent.hiddenLoading();</script>");
			out.print(StrUtil.jAlert_Back("请上传excel文件!","提示"));
			out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
			return;
		}
	}catch(ErrMsgException e){
		//out.print("<script type='text/javascript'>parent.hiddenLoading();</script>");
		out.print(StrUtil.jAlert_Back("请上传excel文件, " + e.getMessage(), "提示"));
		out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
		return;
	}
	
	int count = 0;
	String ret = "";
	FileInputStream in = new FileInputStream(excelFile);
	try {
		if (excelFile.endsWith("xls")){
			HSSFWorkbook w = (HSSFWorkbook) WorkbookFactory.create(in);
			HSSFSheet sheet = w.getSheetAt(0);
			if (sheet != null) {
				// 获取行数
				int rowcount = sheet.getLastRowNum();
				
				FormDb fd = new FormDb();
				fd = fd.getFormDb("sales_customer");
				
				FormDb fdVisit = new FormDb();
				fdVisit = fdVisit.getFormDb("day_lxr");
				
				FormDb fdLinkman = new FormDb();
				fdLinkman = fdLinkman.getFormDb("sales_linkman");
				
				String nextVisitTodo="", nextVisitTime="", contactResult="",visitDate="",contactType="", salesPerson = "";
				// 获取每一行
				for (int k = 1; k <= rowcount; k++) {
					HSSFRow row = sheet.getRow(k);
					if (row != null) {
						HSSFCell cell = row.getCell(0);
						String val = "";
						if (cell==null){
							val = "";
						} else {
							cell.setCellType(HSSFCell.CELL_TYPE_STRING);
							val = StrUtil.getNullStr(cell.getStringCellValue());
						}
						String customer = val;
						if ("".equals(customer)) {
							continue;
						}
						
						cell = row.getCell(1);
						if (cell==null){
							val = "";
						} else {
							cell.setCellType(HSSFCell.CELL_TYPE_STRING);
							val = StrUtil.getNullStr(cell.getStringCellValue());
						}
						String linkMan = val;						

						cell = row.getCell(2);
						if (cell==null){
							val = "";
						} else {
							cell.setCellType(HSSFCell.CELL_TYPE_STRING);
							val = StrUtil.getNullStr(cell.getStringCellValue());
						}				
						String mobile = val;		
						
						cell = row.getCell(3);
						if (cell==null){
							val = "";
						} else {
							cell.setCellType(HSSFCell.CELL_TYPE_STRING);
							val = StrUtil.getNullStr(cell.getStringCellValue());
						}				
						String desc = val;	
							
						cell = row.getCell(5); // 注册资金值
						if (cell==null){
							val = "";
						} else {
							cell.setCellType(HSSFCell.CELL_TYPE_STRING);
							val = StrUtil.getNullStr(cell.getStringCellValue());
						}				
						String zczj  = val;	
						
						cell = row.getCell(6); // 成立时间
						if (cell==null){
							val = "";
						} else {
							cell.setCellType(HSSFCell.CELL_TYPE_STRING);
							val = StrUtil.getNullStr(cell.getStringCellValue());
						}				
						String regDate = val;	
						
						cell = row.getCell(7); // 来源
						if (cell==null){
							val = "";
						} else {
							cell.setCellType(HSSFCell.CELL_TYPE_STRING);
							val = StrUtil.getNullStr(cell.getStringCellValue());
						}				
						String ly = val;	
						
						// 如果不是管理员，则可以导入行动
						if (!"manage".equals(action)) {
							cell = row.getCell(8); // 联系方式
							if (cell==null){
								val = "";
							} else {
								cell.setCellType(HSSFCell.CELL_TYPE_STRING);
								val = StrUtil.getNullStr(cell.getStringCellValue());
							}				
							contactType = val;
							
							cell = row.getCell(9); // 联系时间
							if (cell==null){
								val = "";
							} else {
								cell.setCellType(HSSFCell.CELL_TYPE_STRING);
								val = StrUtil.getNullStr(cell.getStringCellValue());
							}				
							visitDate = val;						
	
							cell = row.getCell(10); // 联系结果
							if (cell==null){
								val = "";
							} else {
								cell.setCellType(HSSFCell.CELL_TYPE_STRING);
								val = StrUtil.getNullStr(cell.getStringCellValue());
							}				
							contactResult = val;	
							
							cell = row.getCell(11); // 回访时间
							if (cell==null){
								val = "";
							} else {
								cell.setCellType(HSSFCell.CELL_TYPE_STRING);
								val = StrUtil.getNullStr(cell.getStringCellValue());
							}				
							nextVisitTime = val;							
							
							cell = row.getCell(12); // 回访事项
							if (cell==null){
								val = "";
							} else {
								cell.setCellType(HSSFCell.CELL_TYPE_STRING);
								val = StrUtil.getNullStr(cell.getStringCellValue());
							}				
							nextVisitTodo = val;	
							
							cell = row.getCell(13); // 客户经理
							if (cell==null){
								val = "";
							} else {
								cell.setCellType(HSSFCell.CELL_TYPE_STRING);
								val = StrUtil.getNullStr(cell.getStringCellValue());
							}				
							salesPerson = val;	
						}
						
						if (salesPerson.equals("")) {
							salesPerson = curUser;
						}
						String sql = "select id from form_table_sales_customer where customer = '" + customer + "'";
						FormDAO formDAO = new FormDAO();
						Vector vector = formDAO.list("sales_customer", sql);
						if (vector.iterator().hasNext()) {
							//ret ="failture";
							continue;
							//response.sendRedirect("customer_visit_import.jsp?ret=failture&action=" + action + "&count=" + count);
							//return;
						}
						DeptUserDb du = new DeptUserDb();
						Vector v = du.getDeptsOfUser(salesPerson);
						String department_code = "";
						if (v.size()>=0) {
							DeptDb dd = (DeptDb)v.get(0);
							department_code = String.valueOf(dd.getCode());
						}
						FormDAO fdao = new FormDAO(fd);
						fdao.setFieldValue("customer", customer);
						fdao.setFieldValue("enterMemo", desc);
						fdao.setFieldValue("zczj", zczj);
						fdao.setFieldValue("reg_date", regDate);
						fdao.setFieldValue("ly", ly);
						if (!"manage".equals(action)) {
							kind = "1";
							fdao.setFieldValue("sales_person", salesPerson);
							fdao.setFieldValue("dept_code", department_code);
						}
						else {
							fdao.setFieldValue("kind", kind);
						} 
						fdao.setFieldValue("find_date", cn.js.fan.util.DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"));
						fdao.setUnitCode(privilege.getUserUnitCode(request));
						fdao.setCreator(curUser);
						fdao.create();
						
						count++;
						
						if (!"".equals(linkMan)) {
							FormDAO fdaoLinkman = new FormDAO(fdLinkman);
							fdaoLinkman.setFieldValue("linkmanName", linkMan);
							fdaoLinkman.setFieldValue("mobile", mobile);
							fdaoLinkman.setCwsId(String.valueOf(fdao.getId()));
							fdaoLinkman.setFieldValue("customer", String.valueOf(fdao.getId()));
							fdaoLinkman.setCreator(curUser);
							fdaoLinkman.create();
							
							if (!"manage".equals(action) && !"".equals(visitDate)) {
							//	Pattern p = Pattern.compile("^(\\d{4})-([0-1]\\d)-([0-3]\\d) ([0-5]\\d):([0-5]\\d):([0-5]\\d)$");
							//	try{
							//	Matcher m = p.matcher(nextVisitTime);
							//	if(!m.matches()) {
								//	ret = "fail";
									//response.sendRedirect("customer_visit_import.jsp?ret=fail&action=" + action + "&count=" + count);
									//return;
							//	}
							//	if(m.matches()){
									FormDAO fdaoVisit = new FormDAO(fdVisit);
									fdaoVisit.setFieldValue("visit_date", visitDate);
									fdaoVisit.setFieldValue("contact_type", contactType);
									fdaoVisit.setFieldValue("contact_result", contactResult);
									fdaoVisit.setFieldValue("next_visit_time", nextVisitTime);
									fdaoVisit.setFieldValue("next_visit_todo", nextVisitTodo);
									fdaoVisit.setCwsId(String.valueOf(fdaoLinkman.getId()));
									fdaoVisit.setFieldValue("lxr", String.valueOf(fdaoLinkman.getId()));
									fdaoVisit.setFieldValue("customer", String.valueOf(fdao.getId()));
									fdaoVisit.setFieldValue("sales_person", salesPerson);
									fdaoVisit.setCreator(curUser);
									fdaoVisit.create();
								//}
								//} catch(Exception e){
								//	LogUtil.getLog(page.getClass()).equals("导入的字段出现异常");
								//}
							}
						}
					}
				}
			}
		}
	} catch (IOException e){
		LogUtil.getLog(page.getClass()).equals("导入出现异常");
	} catch (InvalidFormatException e){
		LogUtil.getLog(page.getClass()).equals("导入出现异常");
	} finally {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e){
				LogUtil.getLog(page.getClass()).equals("导入出现异常");
			}
		}
	}
	java.io.File file = new java.io.File(excelFile);
	file.delete();		
	//if (ret==""){
	//	ret="success";
	//}
	response.sendRedirect("customer_visit_import.jsp?ret=success&action=" + action + "&count=" + count);
	return;
}
%>	
<body>
<%@ include file="customer_inc_menu_top.jsp"%>
<script>
o("menu10").className="current";
</script>
<div class="spacerH"></div>
<form action="customer_visit_import.jsp?op=import&action=<%=action%>" method="post" enctype="multipart/form-data" name="form1" id="form1" onSubmit="return submitCheck()">
<table border="0" align="center" cellspacing="0" class="tabStyle_1 percent80">
	<thead>
    <tr>
      <td class="tabStyle_1_title">导入excel</td>
    </tr>
	</thead>
    <tr>
      <td align="left">
      <%
      String ret = ParamUtil.get(request, "ret");
      int count = ParamUtil.getInt(request, "count", -1);
      if ("success".equals(ret)) {
      %>
      	<font color="red">操作成功，共导入数据<%=count%>条！</font><br /><br />
      <%
      } 
      %>
      1.编辑Excel电子表格信息，将按照模板（<a href="javascript:;" style="color:blue;"onclick="downloadTemplate() ">下载模板</a>）进行整理</td>
    </tr>
    <tr>
      <td align="left">2.选择整理完成的Excel文件进行上传</td>
    </tr>
    <tr>
      <td align="left">3.Excel中的表格首行为字段名，不能更改或删除</td>
    </tr>
    <%if ("manage".equals(action)) {%>
     <tr>
      <td align="left">
       	客户类别
       	<select id="kind" name="kind">
       	<option value="0">未分配客户</option>
       	<option value="1">回落客户</option>
       	</select>
      </td>
    </tr>
    <%}%>
    <tr>
      <td align="left" ><script>initUpload()</script></td>
    </tr>
    <tr style="display:none">
      <td align="left">
      <select id="commFro" name="commFro">
        <%
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect("comm_fro");
        Vector vsd = sd.getOptions();
        Iterator irsd = vsd.iterator();
        while (irsd.hasNext()) {
            SelectOptionDb sod = (SelectOptionDb)irsd.next();
            %>
            <option value="<%=sod.getValue()%>"><%=sod.getName()%></option>
            <%	
        }
        %>
        </select>      
      </td>
    </tr>
    <tr>
      <td align="center"><input type="submit"  value="确定" class="org-btn"/></td>
        <%
        	if("introduction".equals(flag)){
        		%>
        		<script>
	        		jQuery(document).ready(function(){
				    	var tour = {
								id : "hopscotch",
								steps : [ {
									title : "提示",
									content : "此处可以下载模版，请根据模版编写Excel(红色*号为必填项)",
									target : "btn",
									placement : "bottom",
									showNextButton : false
								}]
							};
						hopscotch.startTour(tour);
					});
				</script>
        		<%
        	}
         %>
    </tr>
</table>
</form>
</body>
<script>
	function downloadTemplate(){
		<%if (!"manage".equals(action)) {%>
		window.location.href="<%=request.getContextPath() %>/sales/customer_visit_import_template.xls";
		<%}else{%>
		window.location.href="<%=request.getContextPath() %>/sales/customer_import_template.xls";
		<%}%>
	};
	// 表单提交校验
	function submitCheck(){
		//parent.showLoading();
	}
</script>
</html>