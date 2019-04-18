<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "java.io.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.address.*"%>
<%@ page import = "com.redmoon.oa.pvg.Privilege"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "org.json.*"%>
<%@page import="com.redmoon.oa.flow.FormDb"%>
<%@page import="com.redmoon.oa.visual.ModuleSetupDb"%>
<%@page import="com.redmoon.oa.visual.ModuleImportTemplateDb"%>
<%@page import="org.apache.poi.hssf.usermodel.HSSFWorkbook"%>
<%@page import="org.apache.poi.hssf.usermodel.HSSFSheet"%>
<%@page import="org.apache.poi.ss.usermodel.WorkbookFactory"%>
<%@page import="org.apache.poi.hssf.usermodel.HSSFCell"%>
<%@page import="com.redmoon.oa.flow.FormField"%>
<%@page import="org.apache.poi.hssf.usermodel.HSSFRow"%>
<%@page import="com.redmoon.oa.visual.FormDAO"%> 
<%@page import="org.apache.poi.xssf.usermodel.XSSFWorkbook"%>
<%@page import="org.apache.poi.xssf.usermodel.XSSFSheet"%>
<%@page import="org.apache.poi.xssf.usermodel.XSSFCell"%>
<%@page import="org.apache.poi.xssf.usermodel.XSSFRow"%>
<%@page import="com.cloudwebsoft.framework.util.LogUtil"%>
<%@page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@page import="org.apache.poi.hssf.usermodel.*" %>
<%@page import="com.redmoon.oa.person.UserDb"%>
<%@page import="com.redmoon.oa.dept.DeptUserDb"%>
<%@page import="com.redmoon.oa.dept.DeptDb"%>
<%@page import="com.redmoon.oa.person.UserCache"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String op = ParamUtil.get(request, "op");
	String code = ParamUtil.get(request, "code");
	String formCode = ParamUtil.get(request,"formCode");
	String cws_id = ParamUtil.get(request,"parentId");
	String menuItem = ParamUtil.get(request, "menuItem");
	FormDb fd = new FormDb();
	fd = fd.getFormDb(formCode);
	
	String userName = privilege.getUser(request);
	
	boolean isAll = ParamUtil.getBoolean(request, "isAll", false);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=fd.getName()%>导入</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<%	if (!fd.isLoaded()) {
		out.print(StrUtil.jAlert_Back("表单不存在！","提示"));
		return;
	}
	String username = privilege.getUser(request);
	FileUpMgr fum = new FileUpMgr();
	boolean re = false;

	String excelFile="";
	if (op.equals("import")) {
		try {
			excelFile = fum.uploadExcel(application, request);
			if (!excelFile.equals("")) {
				int templateId = StrUtil.toInt(fum.getFileUpload().getFieldValue("templateId"), -1);
				JSONArray rowAry = importData(userName, formCode,privilege.getUserUnitCode(request), excelFile, isAll,cws_id, templateId);
				File file = new File(excelFile);
				file.delete();
				request.setAttribute("importRecords", rowAry);
				request.setAttribute("code", code);
				request.setAttribute("formCode", formCode);
				request.setAttribute("templateId", new Integer(templateId));
				request.setAttribute("parentId", cws_id);
				request.setAttribute("menuItem", menuItem);
				request.getRequestDispatcher("module_import_preview.jsp").forward(request, response);
				// out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "module_list.jsp?code=" + code + "&formCode=" + formCode));
			}
			else
				out.print(StrUtil.jAlert_Back("文件不能为空！","提示"));
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		}
		return;
	}
%>
</head>
<body>
<%@ include file="module_inc_menu_top.jsp"%>
<div class="spacerH"></div>
<%
if ("".equals(menuItem)) {
%>
<script>
o("menu1").className="current";
</script>
<%
}
else {
%>
<script>
o("menu<%=menuItem%>").className="current";
</script>
<%
}
%>
<form name="form1" action="?op=import&menuItem=<%=menuItem%>&code=<%=code%>&formCode=<%=formCode%>&isAll=<%=isAll%>&parentId=<%=cws_id%>" method="post" enctype="multipart/form-data">
<table width="525" border="0" align="center" cellspacing="0" class="tabStyle_1 percent60">
	<thead>
    <tr>
      <td class="tabStyle_1_title">请选择需导入的文件</td>
    </tr>
    </thead>
    <tr>
      <td width="319" align="center">
        <%
		ModuleImportTemplateDb mid = new ModuleImportTemplateDb();
		String sql = mid.getTable().getSql("listForForm");
		Vector v = mid.list(sql, new Object[]{formCode});
		if (v.size()>0) {
		%>
      	模板
        <select id="templateId" name="templateId" title="默认按显示的列">
        <option value="">默认</option>
        <%
		Iterator ir = v.iterator();
		while (ir.hasNext()) {
			mid = (ModuleImportTemplateDb)ir.next();
			%>
			<option value="<%=mid.getLong("id")%>"><%=mid.getString("name")%></option>
			<%
		}
		%>
        </select>
        <%}%>
        <input title="选择附件文件" type="file" size="20" name="excel" />
		<input class="btn" name="submit" type="submit" value="确  定" /></td>
    </tr>
</table>
</form>
</body>
</html>
<%!
	public JSONArray importData(String userName, String formCode,String unitCode,String path, boolean isAll,String cws_id, int templateId) throws ErrMsgException, IOException{
		JSONArray rowAry = new JSONArray();
		// System.out.println("userName = " + userName + "formCode = "+ formCode + "unitCode = "+unitCode + "path = "+ path + "isall = "+ isAll);
		InputStream in = null;
		try {
			// System.out.println(getClass()+"::::"+formCode);
			ModuleSetupDb msd = new ModuleSetupDb();
			msd = msd.getModuleSetupDbOrInit(formCode);
	
			String listField = StrUtil.getNullStr(msd.getString("list_field"));
			String[] fields = StrUtil.split(listField, ",");

			JSONArray arr = null;
			if (templateId!=-1) {
				ModuleImportTemplateDb mid = new ModuleImportTemplateDb();
				mid = mid.getModuleImportTemplateDb(templateId);

				String rules = mid.getString("rules");
				try {
					arr = new JSONArray(rules);
					if (arr.length()>0) {
						fields = new String[arr.length()];
						for (int i = 0; i < arr.length(); i++) {
							JSONObject json = (JSONObject) arr.get(i);
							fields[i] = json.getString("name");
						}
					}
				}
				catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			/*
			 for(int i = 0 ; i < fields.length; i ++){
			 System.out.println(getClass()+"::"+i+","+fields[i]); }
			 */
			// System.out.println(getClass()+"::path="+path);
			FormDb fd = new FormDb(formCode);
			// System.out.println(getClass() + " isAll2=" + isAll);
			if (isAll) {
				Vector vt = fd.getFields();
				fields = new String[vt.size()];
				Iterator ir = vt.iterator();
				int i=0;
				while (ir.hasNext()) {
					FormField ff = (FormField)ir.next();
					fields[i] = ff.getName();
					i++;
				}
			}			
			
			MacroCtlMgr mm = new MacroCtlMgr();
						
			in = new FileInputStream(path);
			String pa = StrUtil.getFileExt(path);
			if (pa.equals("xls")) {  
					// 读取xls格式的excel文档
					HSSFWorkbook w = (HSSFWorkbook) WorkbookFactory.create(in);
					// 获取sheet
					for (int i = 0; i < w.getNumberOfSheets(); i++) {
						HSSFSheet sheet = w.getSheetAt(i);
						if (sheet != null) {
							// 获取行数
							int rowcount = sheet.getLastRowNum();
							HSSFCell cell = null;
							
							// 取得第0行，检查表头是否相符
							HSSFRow rowHeader = sheet.getRow(0);
							if (rowHeader != null) {
								int colcount = rowHeader.getLastCellNum();
								// 获取每一单元格
								for (int m = 0; m < colcount; m++) {
									cell = rowHeader.getCell(m);
									if (cell==null) {
										continue;
									}
									cell.setCellType(HSSFCell.CELL_TYPE_STRING);
									String colTitle = cell.getStringCellValue();
									if (templateId!=-1) {
										JSONObject json = (JSONObject) arr.get(m);
									 	String title = json.getString("title");
										if (!title.equals(colTitle)) {
											throw new ErrMsgException("表头“" + colTitle + "”与模板文件中的“" + title + "”不相符");
										}			
									}
								}
							}
	
							// 获取每一行
							for (int k = 1; k <= rowcount; k++) {
								HSSFRow row = sheet.getRow(k);
								if (row != null) {
									int colcount = row.getLastCellNum();
									if (colcount > fields.length)
										colcount = fields.length;
									JSONObject jo = new JSONObject();
										
									// 获取每一单元格
									for (int m = 0; m < colcount; m++) {
										cell = row.getCell(m);
										
										String colName = fields[m];

										if (cell==null) {
											jo.put(colName, "");
											continue;
										}
										
										// 为空表示不需要导入
										if ("".equals(fields[m])) {
											jo.put(colName, "");
											continue;
										}										
										// System.out.println(getClass() + " m=" + m + " fields[m]=" + fields[m]);
										if (fields[m].equals("cws_creator")) {
											jo.put(colName, userName);
										}
										else {
											if (HSSFCell.CELL_TYPE_NUMERIC == cell.getCellType() && HSSFDateUtil.isCellDateFormatted(cell)) {
												Date date = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());
												jo.put(colName, DateUtil.format(date, "yyyy-MM-dd"));
											}
											else {
												cell.setCellType(HSSFCell.CELL_TYPE_STRING);
												String val = cell.getStringCellValue().trim();								
												jo.put(colName, val);
											}
										}
									}
									rowAry.put(jo);
								}
							}
						}
					}
			} else if (pa.equals("xlsx")) {
				XSSFWorkbook w = (XSSFWorkbook) WorkbookFactory.create(in);
				for (int i = 0; i < w.getNumberOfSheets(); i++) {
					XSSFSheet sheet = w.getSheetAt(i);
					if (sheet != null) {
						int rowcount = sheet.getLastRowNum();
						XSSFCell cell = null;
						// 取得第0行，检查表头是否相符
						XSSFRow rowHeader = sheet.getRow(0);
						if (rowHeader != null) {
							int colcount = rowHeader.getLastCellNum();
							// 获取每一单元格
							for (int m = 0; m < colcount; m++) {
								cell = rowHeader.getCell(m);
								if (cell==null) {
									continue;
								}
								cell.setCellType(XSSFCell.CELL_TYPE_STRING);
								String colTitle = cell.getStringCellValue();
								if (templateId!=-1) {
									JSONObject json = (JSONObject) arr.get(m);
								 	String title = json.getString("title");
									if (!title.equals(colTitle)) {
										throw new ErrMsgException("表头“" + colTitle + "”与模板文件中的“" + title + "”不相符");
									}			
								}
							}
						}	
						// FormDAO fdao = new FormDAO();
						for (int k = 1; k <= rowcount; k++) {
							XSSFRow row = sheet.getRow(k);
							if (row != null) {
								int colcount = row.getLastCellNum();
								if (colcount > fields.length)
									colcount = fields.length;
												
								JSONObject jo = new JSONObject();
								for (int m = 0; m < colcount; m++) {
									cell = row.getCell(m);

									String colName = fields[m];
									
									if (cell==null) {
										jo.put(colName, "");
										continue;
									}								
									// 为空表示不需要导入
									if ("".equals(fields[m])) {
										jo.put(colName, "");
										continue;
									}										
									
									if (fields[m].equals("cws_creator")) {
										jo.put(colName, userName);
									}
									else {
										if (XSSFCell.CELL_TYPE_NUMERIC == cell.getCellType() && HSSFDateUtil.isCellDateFormatted(cell)) {
											Date date = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());
											jo.put(colName, DateUtil.format(date, "yyyy-MM-dd"));
										}
										else {
											cell.setCellType(XSSFCell.CELL_TYPE_STRING);
											String val = cell.getStringCellValue().trim();
											jo.put(colName, val);
										}
									}
								}
								rowAry.put(jo);										
							}
						}
					}
				}
			}
		}
		catch (ErrMsgException e) {
			throw e;
		} 
		catch (Exception e) {
			//LogUtil.getLog(SignMgr.class).error(e.getMessage());
			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return rowAry;
	}
%>
