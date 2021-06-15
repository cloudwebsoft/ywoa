<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.net.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
String user = StrUtil.UnicodeToGB(request.getParameter("user"));
String pwd = StrUtil.UnicodeToGB(request.getParameter("pwd"));
if (user==null)
{
	out.println("警告非法用户，你无访问此页的权限！");
}
else
{
	if (user.equals("redmoon")) // && pwd.equals("redmoon"))
		;
	else
	{
		out.println("警告非法用户，你无访问此页的权限！");
		return;
	}
}

String filename = user+".bmp";

response.setContentType("application/bmp");
response.setHeader("Content-disposition","attachment; filename="+filename);

BufferedInputStream bis = null;
BufferedOutputStream bos = null;

try {
bis = new BufferedInputStream(new FileInputStream(application.getRealPath("/") + "images/stamp/"+filename));
bos = new BufferedOutputStream(response.getOutputStream());

byte[] buff = new byte[2048];
int bytesRead;

while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
bos.write(buff,0,bytesRead);
}

} catch(final IOException e) {
System.out.println ( "出现IOException." + e );
} finally {
if (bis != null)
bis.close();
if (bos != null)
bos.close();
}
%>



