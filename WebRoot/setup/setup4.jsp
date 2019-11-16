<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.io.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@page import="com.redmoon.oa.kernel.License"%>
<html>
<head>
<%
License lic = License.getInstance();
%>
<title><%=com.redmoon.oa.Config.getInstance().get("enterprise")%>安装 - 配置Log4j</title>
<link href="../common.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
.style1 {
	color: #FFFFFF;
	font-weight: bold;
}
.style2 {color: #FFFFFF}
-->
</style>
</head>
<body bgcolor="#FFFFFF" text="#000000">
<jsp:useBean id="backup" scope="page" class="cn.js.fan.util.Backup"/>
<%
XMLConfig cfg = new XMLConfig("config_oa.xml", true, "iso-8859-1");

// java.net.URL cfgURL = getClass().getClassLoader().getResource("res/common_zh_CN.properties");
// java.net.URL cfgURL = getClass().getClassLoader().getResource("res/common_zh_CN.properties");
// PropertiesUtil pu = new PropertiesUtil(cfgURL.getFile());
PropertiesUtil pu = new PropertiesUtil(Global.getRealPath() + "WEB-INF/log4j.properties");

String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	String name = ParamUtil.get(request, "name");
	String value = ParamUtil.get(request, "value");
	pu.setValue(name, value);
	pu.saveFile(Global.getRealPath() + "WEB-INF/log4j.properties");	
	out.print(StrUtil.Alert("修改成功！"));
}
%><br>
<TABLE cellSpacing=0 cellPadding=3 width="95%" align=center>
  <!-- Table Head Start-->
  <TBODY>
    <TR>
      <TD class=thead style="PADDING-LEFT: 10px" noWrap width="70%"><div align="left"><b>欢迎您使用 <%=%>com.redmoon.oa.Config.getInstance().get("enterprise")%> 版本<%=cfg.get("oa.version")%></b>
        <hr size="0">
      </div></TD>
    </TR>
    <TR>
      <TD height="175" align="center" style="PADDING-LEFT: 10px">
	  <table width="100%" border="0" align="center">
	  <%
		java.util.Iterator ir = pu.getKeys().iterator();
		while (ir.hasNext()) {
			String name = (String)ir.next();
			String value = pu.getValue(name);
	  %>
	  <form action="?op=modify" method="post">
        <tr>
          <td width="2%">&nbsp;</td>
          <td width="24%"><%=name%><input name="name" type=hidden value="<%=name%>">
            <%
		  if (name.equals("log4j.appender.R.File")) {
		  %>
(日志存放的物理路径)
<%}%></td>
          <td width="42%"><textarea name="value" cols="60" rows="2"><%=value%></textarea></td>
          <td width="32%"><input type="submit" value="修 改"></td>
        </tr>
	  </form>
	  <%
	  	}
	  %>
      </table></TD>
    </TR>
    <!-- Table Body End -->
    <!-- Table Foot -->
    <TR>
      <TD class=tfoot align=right>
        <div align="center">
          <hr size="0">
        <input name="button22" type="button" onClick="window.location.href='setup3.jsp'" value="上一步" />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input name="button2" type="button" onClick="window.location.href='setup5.jsp'" value="下一步" />
        </DIV></TD>
    </TR>
    <!-- Table Foot -->
  </TBODY>
</TABLE>
</body>
<script language="javascript">
<!--

//-->
</script>
</html>