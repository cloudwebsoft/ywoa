<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="com.redmoon.oa.ui.SkinMgr"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="com.redmoon.oa.address.FileUpMgr"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<%@page import="jxl.Workbook"%>
<%@page import="jxl.Sheet"%>
<%@page import="jxl.Cell"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>导入手机号码</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
  </head>
  <%
  	String op = ParamUtil.get(request,"op");
  	String mobile = "";
  	if(op.equals("import")){
  		FileUpMgr fum = new FileUpMgr();
		String excelFile = fum.uploadExcel(application, request);
		if (excelFile.equals("")) {
			out.print(StrUtil.Alert_Back("请上传excel文件"));
			return;
		}
		Workbook wb = Workbook.getWorkbook(new java.io.File(excelFile));
		Sheet sheet = wb.getSheet(0);
		
		int maxRow = sheet.getRows(); //excel数据行数
		
		// System.out.println(getClass()+" maxRow="+maxRow);
		
		for(int i = 0; i < maxRow; i ++){
			Cell cell = sheet.getCell(0,i);
			if(mobile.equals("")){
				mobile += cell.getContents().trim();
			}else{
				mobile += ","+cell.getContents().trim();
			}
		}
	%>
	  <script type="text/javascript">
        var mobiles = "<%=mobile%>";
        if(mobiles!=""){
            window.opener.setMobiles(mobiles);
            window.close();
        }
      </script>
	<%		
  	}
   %>
  <body>
    <table cellpadding="0" cellspacing="0" width="100%">
    	<tr>
    		<td class="tdStyle_1">导入手机号码</td>
    	</tr>
    </table>
    <form  action="sms_import_excel.jsp?op=import" method="post" enctype="multipart/form-data" name="form1" id="form1">
    <table cellpadding="0" cellspacing="0" width="98%" class="tabStyle_1 percent98">
    	<tr>
    		<td class="tabStyle_1_title">
    			请选择Excel文件
    		</td>
    		<td>
    			<input type="file" name="mobiles" id="mobiles" value="" />
    			<input type="submit" name="submit" id="submit" value="提交" class="btn">
    		</td>
   		</tr>
    	<tr>
    	  <td colspan="2">表格式：第一列为电话号码，不需要表头</td>
   	  </tr>
    </table>
    </form>
  </body>
</html>
