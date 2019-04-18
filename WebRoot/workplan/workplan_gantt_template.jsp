<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="java.io.OutputStream"%> 
<%@page import="java.io.PrintWriter"%> 
<%@page import="java.io.FileNotFoundException"%> 
<%@page import="java.io.File"%> 
<%@page import="java.io.FileInputStream"%>  
<%
//打开指定文件的流信息 
String fileName = "project_template.mpp"; 
String filepath = request.getRealPath("workplan/template/" + fileName); 
FileInputStream fs = null; 
try { 
fs = new FileInputStream(new File(filepath)); 
}catch(FileNotFoundException e) { 
e.printStackTrace(); 
return; 
} 
//设置响应头和保存文件名 
response.reset(); 
response.setContentType("application/x-msdownload"); 
response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\""); 
//写出流信息 
int b = 0; 
try { 
OutputStream ops = response.getOutputStream(); 
while((b=fs.read())!=-1) { 
ops.write(b); 
} 
out.flush(); 

}catch(Exception e) { 

} 
finally
{
	if(fs != null) 
	{ 
		fs.close(); 
		fs = null; 
	 } 
 
}
out.clear();
out = pageContext.pushBody();
%>