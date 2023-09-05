<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.io.*,
				 cn.js.fan.db.*,
				 cn.js.fan.util.*,
				 cn.js.fan.web.*,
				 org.jdom.*,
                 java.util.*,
				 java.lang.*"
%>
<%@page import="com.redmoon.oa.kernel.License"%>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<%
XMLConfig cfg = new XMLConfig("config.xml", false, "gb2312");
License lic = License.getInstance();
%>
<title><%=com.redmoon.oa.Config.getInstance().get("enterprise")%>安装 - 配置数据库连接</title>
<link rel="stylesheet" type="text/css" href="../common.css">
<table cellpadding="6" cellspacing="0" border="0" width="100%">
<tr>
<td width="1%" valign="top"></td>
<td width="99%" align="center" valign="top">
    <div align="left"><b>欢迎您使用<%=com.redmoon.oa.Config.getInstance().get("enterprise")%><%=cfg.get("oa.version")%>&nbsp;<font style="font-size='14px';color='red'">MSSQL版本</font></b></div>
    <hr size="0">
<%
cfg = new XMLConfig("proxool.xml", false, "iso-8859-1");
Element root, driverProp,  e_user=null, e_pwd=null, e_driverclass=null;
String user="", pwd="", url="", ip="", database="", port="", maximum_connection_count="", dirverclass="";
List list;

try
{
	root = cfg.getRootElement();
	driverProp = root.getChild("proxool").getChild("driver-properties");
	list = driverProp.getChildren();
	e_user = (Element)list.get(0);
	e_pwd = (Element)list.get(1);
	user = e_user.getAttributeValue("value");
	pwd = e_pwd.getAttributeValue("value");
	dirverclass = cfg.get("proxool.driver-class");
	
	//jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=cwbbs
	url = cfg.get("proxool.driver-url");
	int beginIndex = url.indexOf("//");
	ip = url.substring(beginIndex + 2, url.indexOf(":", beginIndex));
	database = url.substring(url.indexOf("=") + 1);
	port = url.substring(url.lastIndexOf(":")+ 1, url.indexOf(";DatabaseName"));
	maximum_connection_count = cfg.get("proxool.maximum-connection-count");
	
	Class.forName(dirverclass);	

}
catch(ClassNotFoundException cnfe)
{
	out.print("<font style='font-size:14px' color='#FF0000'>请检查WEB-INF/proxool.xml文件中的driver-class是否设置正确！<br>参照设置为：com.microsoft.jdbc.sqlserver.SQLServerDriver</font><br>");
}
catch(Exception e)
{
	out.print("<font style='font-size:14px' color='#FF0000'>请检查WEB-INF/proxool.xml文件中的driver-url是否设置正确！<br>参照设置为：jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=cwbbs</font><br>");
}	
String op = ParamUtil.get(request, "op");
boolean isValid = false;
if (op.equals("setup")) {
	//url = ParamUtil.get(request, "url");
	user = ParamUtil.get(request, "user");
	ip = ParamUtil.get(request, "ip");
	port = ParamUtil.get(request, "port");
	database = ParamUtil.get(request, "database");
	pwd = ParamUtil.get(request, "pwd");
	url = "jdbc:microsoft:sqlserver://" + ip + ":" + port + ";DatabaseName=" + database + ";SelectMethod=Cursor";
	maximum_connection_count = ParamUtil.get(request, "maximum_connection_count");
	cfg.set("proxool.driver-url", url);
	cfg.set("proxool.driver-class", "com.microsoft.jdbc.sqlserver.SQLServerDriver");
	e_user.setAttribute("value", user);
	e_pwd.setAttribute("value", pwd);
	cfg.set("proxool.maximum-connection-count", maximum_connection_count);
	cfg.writemodify();

	Global.getInstance().init();
	
	String realPath = application.getRealPath("/");
	if (realPath.lastIndexOf("/")!=realPath.length()-1)
		realPath += "/";
	
    try {
		org.logicalcobwebs.proxool.ProxoolFacade.removeAllConnectionPools(5000); // 
		org.logicalcobwebs.proxool.configuration.JAXPConfigurator.configure(realPath + "WEB-INF/proxool.xml", false);
    } catch (Exception e) {
		LogUtil.getLog(getClass()).error(e);
    }	
}
%>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
	<form name=form1 action="?op=setup" method=post>
      <tr>
        <td height="24" colspan="2" align="left">配置数据库连接：</td>
        </tr>
      <tr>
        <td height="24" align="right">&nbsp;</td>
        <td><%
if (op.equals("setup")) {
	String sql = "select * from sq_id";
	Conn conn = new Conn(Global.getDefaultDB());
	if (conn.getCon()!=null) {
		try {
			conn.executeQuery(sql);
			isValid = true;
		}
		catch (Exception e) {
			out.print(e.getMessage());
		}
		finally {
			if (conn!=null) {
				conn.close();
				conn = null;
			}
		}
	}
	if (!isValid) {
		out.print("<font color=red>测试连接失败！请检查连接字符串、用户名和密码是否正确！</font>");
	}
	else
		out.print("<font color=green><b>测试连接成功！</b></font>");
}
%></td>
      </tr>
      <tr>
        <td height="24" align="right">配置文件路径：</td>
        <td><%=application.getRealPath("/") + "WEB-INF" + java.io.File.separator + "proxool.xml"%>&nbsp;&nbsp;&nbsp;</td>
      </tr>
<!--	  
      <tr>
        <td height="24" align="right">数据库连接字符串：</td>
        <td><input name="url" value="" size="50"/></td>
      </tr>
-->	  
      <tr>
        <td height="24" align="right">用户名：</td>
        <td><input name="user" value="<%=user%>"/></td>
      </tr>
      <tr>
        <td height="24" align="right">密码：</td>
        <td><input type="password" name="pwd" value="<%=pwd%>"/></td>
      </tr>
      <tr>
        <td height="24" align="right"><span class="thead" style="PADDING-LEFT: 10px">主机名：</span></td>
        <td><input name="ip" value="<%=ip%>"/></td>
      </tr>
      <tr>
        <td height="24" align="right"><span class="thead" style="PADDING-LEFT: 10px">端口号：</span></td>
        <td><input name="port" value="<%=port%>" size="8"/></td>
      </tr>
      <tr>
        <td height="24" align="right"><span class="thead" style="PADDING-LEFT: 10px">数据库名：</span></td>
        <td><input name="database" value="<%=database%>"/></td>
      </tr>
      <tr>
        <td height="24" align="right">最大连接数：</td>
        <td><input type="text" name="maximum_connection_count" value="<%=maximum_connection_count%>"/></td>
      </tr>
	  </form>
    </table>
    <hr size="0">
    
    <div align="center">
    <input name="button22" type="button" onclick="window.location.href='setup.jsp'" value="上一步" />
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	<input type="button" value="连接测试" onClick="form1.submit()">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input name="button2" type="button" onclick="window.location.href='setup3.jsp?db_type=mssql'" value="下一步" /></td>
</tr>
</table>
