<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.io.*,
                 cn.js.fan.db.*,
                 cn.js.fan.util.*,
                 cn.js.fan.web.*,
                 org.jdom.*,
                 java.util.*"
%>
<%@page import="com.redmoon.oa.kernel.License" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%
    XMLConfig cfg = new XMLConfig("config.xml", false, "gb2312");
    License lic = License.getInstance();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title><%=lic.getCompany()%>系统安装 - 配置数据库连接</title>
    <link rel="stylesheet" type="text/css" href="../common.css">
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
</head>
<body>
<table cellpadding="6" cellspacing="0" border="0" width="100%">
    <tr>
        <td width="1%" valign="top"></td>
        <td width="99%" align="center" valign="top">
            <div align="left"><b>欢迎您使用<%=lic.getCompany()%>系统 版本<%=cfg.get("oa.version")%>
            </b></div>
            <hr size="0">
            <%
                cfg = new XMLConfig("proxool.xml", true, "iso-8859-1");
                Element root = cfg.getRootElement();
                Element driverProp = root.getChild("proxool").getChild("driver-properties");
                List list = driverProp.getChildren();
                Element e_user = (Element) list.get(0);
                Element e_pwd = (Element) list.get(1);
                String user = e_user.getAttributeValue("value");
                String pwd = e_pwd.getAttributeValue("value");
                String url = cfg.get("proxool.driver-url");
                int beginIndex = url.indexOf("//");
                String ip = url.substring(beginIndex + 2, url.indexOf(":", beginIndex));
                String database = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
                String port = url.substring(url.lastIndexOf(":") + 1, url.lastIndexOf("/"));
                String maximum_connection_count = cfg.get("proxool.maximum-connection-count");
                String op = ParamUtil.get(request, "op");
                boolean isValid = false;
                if (op.equals("setup")) {
                    user = ParamUtil.get(request, "user");
                    ip = ParamUtil.get(request, "ip");
                    port = ParamUtil.get(request, "port");
                    database = ParamUtil.get(request, "database");
                    pwd = ParamUtil.get(request, "pwd");
                    url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=CONVERT_TO_NULL&useSSL=false&serverTimezone=Asia/Shanghai";
                    maximum_connection_count = ParamUtil.get(request, "maximum_connection_count");
                    cfg.set("proxool.driver-url", url);
                    e_user.setAttribute("value", user);
                    e_pwd.setAttribute("value", pwd);
                    cfg.set("proxool.maximum-connection-count", maximum_connection_count);
                    cfg.writemodify();
                    Global.getInstance().init();
                    try {
                        org.logicalcobwebs.proxool.ProxoolFacade.removeAllConnectionPools(5000);
                        org.logicalcobwebs.proxool.configuration.JAXPConfigurator.configure(application.getRealPath("/") + "WEB-INF/proxool.xml", false);
                    } catch (Exception e) {
                        out.print("Problem configuring: " + e.getMessage());
                        e.printStackTrace();
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
                                String sql = "select * from redmoonid";
                                Conn conn = new Conn(Global.getDefaultDB());
                                if (conn.getCon() != null) {
                                    try {
                                        conn.executeQuery(sql);
                                        isValid = true;
                                    } catch (Exception e) {
                                        out.print(e.getMessage());
                                    } finally {
                                        if (conn != null) {
                                            conn.close();
                                            conn = null;
                                        }
                                    }
                                }
                                if (!isValid) {
                                    out.print("<font color=red>测试连接失败！请检查连接字符串、用户名和密码是否正确！</font>");
                                } else
                                    out.print("<font color=green><b>测试连接成功！</b></font>");
                            }
                        %></td>
                    </tr>
                    <tr>
                        <td height="24" align="right">配置文件路径：</td>
                        <td><%=application.getRealPath("/") + "WEB-INF" + java.io.File.separator + "proxool.xml"%>&nbsp;&nbsp;&nbsp;(在初始化前，请先将redmoonoa.sql导入mysql数据库)</td>
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
                        <td><input type="password" name="pwd" value="<%=pwd%>" autocomplete="off"/></td>
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
                <input name="button22" type="button" onclick="window.location.href='setup.jsp'" value="上一步"/>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <input type="button" value="连接测试" onClick="form1.submit()">
                <%if (true || isValid) {%>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <input name="button2" type="button" onclick="window.location.href='setup3.jsp'" value="下一步"/>
            </div>
        </td>
        <%}%>
    </tr>
</table>
</body>
</html>