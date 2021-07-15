<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.io.*,
                 cn.js.fan.db.*,
                 cn.js.fan.util.*,
                 cn.js.fan.web.*,
                 com.redmoon.forum.*,
                 com.redmoon.oa.flow.macroctl.*,
                 com.redmoon.oa.db.*,
                 org.jdom.*,
                 com.cloudwebsoft.framework.base.*,
                 com.cloudwebsoft.framework.security.*,
                 java.util.*,
                 com.redmoon.oa.kernel.*,
                 cn.js.fan.cache.jcs.*,
                 com.redmoon.oa.ui.*,
                 java.lang.reflect.*"
%>
<%@ page import="com.redmoon.oa.fileark.plugin.PluginMgr" %>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.api.ILicense" %>
<jsp:useBean id="myconfig" scope="page" class="com.redmoon.oa.Config"/>
<%
    // 重新载入许可证
    License lic = License.getInstance();
    lic.init();

    ILicense iLicense = SpringUtil.getBean(ILicense.class);
    iLicense.init();

    Global.getInstance().init();
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title><%=lic.getCompany()%>系统安装</title>
    <link rel="stylesheet" type="text/css" href="../common.css">
    <link type="text/css" rel="stylesheet" href="../skin/lte/css.css"/>
    <style>
        img {
            vertical-align: center;
            margin-top: 9px;
        }

        td {
            height: 26px;
        }
    </style>
    <script src="../inc/common.js"></script>
</head>
<body>
<%
    Privilege pvg = new Privilege();
    if (Global.getInstance().isFormalOpen()) {
        if (!pvg.isUserPrivValid(request, Privilege.ADMIN)) {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "请以管理员身份登录以后再操作"));
            return;
        }
    }

    // 清除缓存
    RMCache.refresh();
    RMCache rmcache = RMCache.getInstance();
    rmcache.clear();
    System.out.println("setup:清除缓存！");

    // 重新载入config_db.xml
    QDBConfig qdbcfg = new QDBConfig();
    qdbcfg.reload();
    System.out.println("setup:重新载入数据库配置文件！");

    ProtectConfig pc = new ProtectConfig();
    pc.reload();

    SequenceManager.init();

    MacroCtlMgr.reload();

    DesktopMgr dm = new DesktopMgr();
    dm.reload();

    com.redmoon.weixin.Config.reload();
    com.redmoon.oa.android.CloudConfig.reload();
    // com.redmoon.oa.robot.Config.reload();

    com.redmoon.forum.Config forumCfg = com.redmoon.forum.Config.getInstance();
    forumCfg.refresh();
    com.redmoon.forum.MsgDb.initParam();

    cn.js.fan.module.cms.plugin.wiki.Config.getInstance().refresh();

    com.redmoon.oa.security.Config.getInstance().refresh();

    SkinMgr.reload();

    com.redmoon.oa.visual.func.FuncMgr fm = new com.redmoon.oa.visual.func.FuncMgr();
    fm.reload();

    com.redmoon.dingding.Config.reload();

    com.redmoon.oa.flow.strategy.StrategyMgr.reload();

    com.redmoon.oa.flow.WorkflowConfig.reload();

    com.redmoon.oa.ui.menu.Config.getInstance().refresh();

    PluginMgr.reload();

    com.redmoon.oa.Config.reload();

    String op = ParamUtil.get(request, "op");
    if (op.equals("select")) {
        String url = "";
        String db = "";
        String oadb = ParamUtil.get(request, "oadb");
        if (oadb.equals("mysql")) {
            db = "MySQL";
            url = "setup2_mysql.jsp";
        } else if (oadb.equals("oracle")) {
            db = "Oracle";
            url = "setup2_oracle.jsp";
        } else if (oadb.equals("mssql")) {
            db = "SQLServer";
            url = "setup2_mssql.jsp";
        } else if (oadb.equals("PostGreSql")) {
            db = "PostGreSql";
            url = "setup2_postGreSql.jsp";
        } else {
            out.print(StrUtil.Alert_Redirect("请选择数据库类型!", "setup.jsp"));
            return;
        }

        XMLConfig cfg = new XMLConfig("config_sys.xml", false, "utf-8");
        cfg.set("Application.db", db);
        cfg.writemodify();
        Global.getInstance().init();
        response.sendRedirect(url);

        return;
    }

    XMLConfig cfg = new XMLConfig("config.xml", false, "gb2312");
%>
<table cellpadding="6" cellspacing="0" border="0" width="100%">
    <tr>
        <td width="1%" valign="top"></td>
        <td width="99%" valign="top">
            <b><%=lic.getCompany()%>，欢迎您使用，版本<%=cfg.get("oa.version")%>
            </b>
            <hr size="0">
            在安装继续进行前，您的服务器环境必须通过以下所有检查:
            <ul>
                <table border="0">
                    <tr>
                        <td valign=top><img src="images/check.gif" width="13" height="13"></td>
                        <td>
                            安装工具检测到你正运行在
                            <%= application.getServerInfo() %>
                        </td>
                    </tr>

                    <% // JDK check. See if they have Java2 or later installed by trying to
                        // load java.util.HashMap.
                        boolean isJDK1_8 = true;
                        String javaVer = System.getProperty("java.version");
                        if (!javaVer.startsWith("1.8")) {
                            isJDK1_8 = false;
                        }
                        if (isJDK1_8) {
                    %>
                    <tr>
                        <td valign=top><img src="images/check.gif" width="13" height="13"></td>
                        <td>
                                你的JDK版本为1.8或者更新。
                        </td>
                    </tr>
                    <% } else {
                    %>
                    <tr>
                        <td valign=top><img src="images/x.gif" width="13" height="13"></td>
                        <td>
                                您的JDK版本好像低于JDK 1.6。因此安装不能继续。如果可能，请更新JDK版本并重新开始这个过程。
                        </td>
                    </tr>
                    <% }

                        // Servlet version check. The appserver must support at least support
                        // the Servlet API 2.2.
                        boolean servlet2_2 = true;
                        try {
                            Class sessionClass = session.getClass();
                            Class[] setAttributeParams = new Class[1];
                            setAttributeParams[0] = Class.forName("java.lang.String");
                            Method getAttributeMethod = sessionClass.getMethod("getAttribute", setAttributeParams);
                        } catch (SecurityException se) {
                            // some class loaders might not let us do the reflection above, so use
                            // the old method of finding the appserver version:
                            servlet2_2 = application.getMajorVersion() >= 2
                                    && application.getMinorVersion() >= 2;
                        } catch (Exception e) {
                            // ClassNotFoundException & MethodNotFoundException end up here.
                            servlet2_2 = false;
                        }
                        if (servlet2_2) {
                    %>
                    <tr>
                        <td valign=top><img src="images/check.gif" width="13" height="13"></td>
                        <td>您的应用服务器支持servlet 2.2或者更新。</td>
                    </tr>
                    <% } else {
                    %>
                    <tr>
                        <td valign=top><img src="images/x.gif" width="13" height="13"></td>
                        <td>
                            您的应用服务器不支持servlet 2.2或者更新。
                        </td>
                    </tr>
                    <%
                        }

                        // cloudwebsoft
                        boolean cloudInstalled = true;
                        try {
                            Class.forName("com.redmoon.forum.MsgDb");
                        } catch (ClassNotFoundException cnfe) {
                            cloudInstalled = false;
                        }

                        // workplan
                        boolean workplanInstalled = true;
                        try {
                            Class.forName("com.redmoon.oa.workplan.WorkPlanDb");
                        } catch (ClassNotFoundException cnfe) {
                            workplanInstalled = false;
                        }

                        // address
                        boolean addressInstalled = true;
                        try {
                            Class.forName("com.redmoon.oa.address.AddressDb");
                        } catch (ClassNotFoundException cnfe) {
                            addressInstalled = false;
                        }

                        // message
                        boolean messageInstalled = true;
                        try {
                            Class.forName("com.redmoon.oa.message.MessageDb");
                        } catch (ClassNotFoundException cnfe) {
                            messageInstalled = false;
                        }

                        // task
                        boolean taskInstalled = true;
                        try {
                            Class.forName("com.redmoon.oa.task.TaskDb");
                        } catch (ClassNotFoundException cnfe) {
                            taskInstalled = false;
                        }

                        // kaoqin
                        boolean kaoqinInstalled = true;
                        try {
                            Class.forName("com.redmoon.oa.kaoqin.KaoqinDb");
                        } catch (ClassNotFoundException cnfe) {
                            kaoqinInstalled = false;
                        }

                        // worklog
                        boolean worklogInstalled = true;
                        try {
                            Class.forName("com.redmoon.oa.worklog.WorkLogDb");
                        } catch (ClassNotFoundException cnfe) {
                            worklogInstalled = false;
                        }

                        // netdisk
                        boolean netdiskInstalled = true;
                        try {
                            Class.forName("com.redmoon.oa.netdisk.Leaf");
                        } catch (ClassNotFoundException cnfe) {
                            netdiskInstalled = false;
                        }

                        // book
                        boolean bookInstalled = true;
                        try {
                            Class.forName("com.redmoon.oa.book.BookDb");
                        } catch (ClassNotFoundException cnfe) {
                            bookInstalled = false;
                        }

                        // officeequip
                        boolean officeequipInstalled = true;
                        try {
                            Class.forName("com.redmoon.oa.officeequip.OfficeDb");
                        } catch (ClassNotFoundException cnfe) {
                            officeequipInstalled = false;
                        }

                        // asset
                        boolean assetInstalled = true;
                        try {
                            Class.forName("com.redmoon.oa.asset.AssetDb");
                        } catch (ClassNotFoundException cnfe) {
                            assetInstalled = false;
                        }

                        // vehicle
                        boolean vehicleInstalled = true;
                        try {
                            Class.forName("com.redmoon.oa.vehicle.VehicleDb");
                        } catch (ClassNotFoundException cnfe) {
                            vehicleInstalled = false;
                        }

                        // meeting
                        boolean meetingInstalled = true;
                        try {
                            Class.forName("com.redmoon.oa.meeting.BoardroomDb");
                        } catch (ClassNotFoundException cnfe) {
                            meetingInstalled = false;
                        }

                        // Lucene
                        boolean luceneInstalled = true;
                        try {
                            Class.forName("org.apache.lucene.document.Document");
                        } catch (ClassNotFoundException cnfe) {
                            luceneInstalled = false;
                        }

                        // Lucene Chinese support
                        boolean luceneChineseInstalled = true;
                        try {
                            Class.forName("org.apache.lucene.analysis.cn.ChineseAnalyzer");
                        } catch (ClassNotFoundException cnfe) {
                            luceneChineseInstalled = false;
                        }

                        // JavaMail
                        boolean javaMailInstalled = true;
                        try {
                            Class.forName("javax.mail.Address");  // mail.jar
                            Class.forName("javax.activation.DataHandler"); // activation.jar
                            // Class.forName("dog.mail.nntp.Newsgroup"); // nntp.jar
                        } catch (ClassNotFoundException cnfe) {
                            javaMailInstalled = false;
                        }

                        // JDBC std ext
                        boolean jdbcExtInstalled = true;
                        try {
                            Class.forName("javax.sql.DataSource");
                        } catch (ClassNotFoundException cnfe) {
                            jdbcExtInstalled = false;
                        }

                        boolean filesOK = cloudInstalled && workplanInstalled && addressInstalled && taskInstalled && kaoqinInstalled && worklogInstalled && messageInstalled && netdiskInstalled && bookInstalled && assetInstalled && officeequipInstalled && vehicleInstalled && meetingInstalled && javaMailInstalled && jdbcExtInstalled;
                        if (filesOK) {
                    %>
                    <tr>
                        <td valign=top><img src="images/check.gif" width="13" height="13"></td>
                        <td>
                            所有的应用程序包都安装正确。
                                <%  }
		else {
	%>
                    <tr>
                        <td valign=top><img src="images/x.gif" width="13" height="13"></td>
                        <td>
                            一个或者多个应用程序包没有被安装。
                                <%  }  %>
                    <tr>
                        <td colspan="2" valign=top>
                            <ul>
                                <img src="images/<%= workplanInstalled?"check.gif":"x.gif" %>" width="13" height="13">
                                工作流内核
                                <br> <img src="images/<%= javaMailInstalled?"check.gif":"x.gif" %>" width="13" height="13">
                                JavaMail支持 (mail.jar, activation.jar,)
                                <br> <img src="images/<%= jdbcExtInstalled?"check.gif":"x.gif" %>" width="13" height="13">
                                JDBC 2.0 扩展 (jdbc2_0-stdext.jar)
                            </ul>
                        </td>
                    </tr>
                    <%
                        // 改变JCS 目录
                        String cloudHome = application.getRealPath("/");
                        cloudHome = cloudHome.replaceAll("\\\\", "/");
                        if (cloudHome.lastIndexOf("/") != cloudHome.length() - 1) {
                            cloudHome += "/";
                        }

                        System.out.println("cloudHome=" + cloudHome);
                        License.getInstance().init();

                        PropertiesUtil pu = new PropertiesUtil(cloudHome + "WEB-INF/log4j.properties");
                        pu.setValue("log4j.appender.R.File", cloudHome + "logs/oa.log");
                        pu.saveFile(cloudHome + "WEB-INF/log4j.properties");
                        java.net.URL cfgURL = getClass().getResource("/cache.ccf");
                        PropertiesUtil pucache = new PropertiesUtil(java.net.URLDecoder.decode(cfgURL.getFile()));
                        pucache.setValue("jcs.auxiliary.DC.attributes.DiskPath", cloudHome + "CacheTemp");
                        pucache.saveFile(java.net.URLDecoder.decode(cfgURL.getFile()));

                        XMLConfig reportCfg = new XMLConfig(cloudHome + "WEB-INF" + java.io.File.separator + "reportConfig.xml", true, "utf-8");
                        try {
                            Element root = reportCfg.getRootElement();
                            Iterator ir = root.getChildren().iterator();
                            while (ir.hasNext()) {
                                Element e = (Element) ir.next();
                                if ("cachedReportDir".equals(e.getChild("name").getValue())) {
                                    e.getChild("value").setText(cloudHome + "report/cached");
                                    File rptCacheDir = new File(cloudHome + "report/cached");
                                    if (!rptCacheDir.exists()) {
                                        rptCacheDir.mkdir();
                                    }

                                    break;
                                }
                            }
                            reportCfg.writemodify();
                        } catch (Exception e) {
                            // out.print("<font style='font-size:14px' color='#FF0000'>请检查WEB-INF/proxool.xml文件中的driver-url是否设置正确！<br>参照设置为：jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=cwbbs</font><br>");
                            e.printStackTrace();
                        }

                        boolean propError = false;
                        String errorMessage = null;
                        try {
                            if (cloudHome != null) {
                                try {
                                    File file = new File(cloudHome);
                                    if (!file.exists()) {
                                        propError = true;
                                        errorMessage = "目录 <tt>" + cloudHome + "</tt> " +
                                                "不存在。请编辑 <tt>jive_init.properties</tt> 文件" +
                                                "指定正确的jiveHome目录。";
                                    }
                                } catch (Exception e) {
                                }
                                if (!propError) {
                                    // See if cloudHome is readable and writable.
                                    // Method jiveHomeReadable = jiveGlobals.getMethod("isJiveHomeReadable", null);
                                    // boolean readable = ((Boolean)jiveHomeReadable.invoke(null, null)).booleanValue();
                                    boolean readable = (new File(cloudHome)).canRead();
                                    if (!readable) {
                                        propError = true;
                                        errorMessage = "<tt>cloudHome</tt> 存在于<tt>" + cloudHome +
                                                "</tt>, 但是您的应用服务器没有对它的读权限。请设置目录的权限修正此问题。";
                                    }
                                    // Method jiveHomeWritable = jiveGlobals.getMethod("isJiveHomeWritable", null);
                                    // boolean writable = ((Boolean)jiveHomeWritable.invoke(null, null)).booleanValue();
                                    boolean writable = (new File(cloudHome)).canWrite();
                                    if (!writable) {
                                        propError = true;
                                        errorMessage = "<tt>cloudHome</tt> 存在于<tt>" + cloudHome +
                                                "</tt>, 但是您的应用服务器没有对它的写权限。请设置目录的权限修正此问题。";
                                    }
                                    // Jive Home appears to exist and to be setup correctly. Make sure that all of the proper sub-dirs exist
                                    // or create them as necessary.
                                    File homeFile = new File(cloudHome);
                                    String[] subDirs = new String[]{"log", "upfile"};
                                    for (int i = 0; i < subDirs.length; i++) {
                                        File subDir = new File(cloudHome, subDirs[i]);
                                        if (!subDir.exists()) {
                                            subDir.mkdir();
                                        }
                                    }
                                }
                            } else {
                                propError = true;
                                errorMessage = "<tt>cloudHome</tt> 目录设置不正确。请参考安装文档正确设置 <tt>jive_init.properties</tt> 文件中的值。";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            propError = true;
                            errorMessage = "检查<tt>cloudHome</tt>目录时发生异常。" +
                                    "请确认您安装的系统程序是否完整！";
                        }
                        if (!propError) {
                    %>
                    <tr>
                        <td valign=top><img src="images/check.gif" width="13" height="13"></td>
                        <td>
                            系统目录正确配置于: <tt><%= cloudHome %>
                        </td>
                    </tr>
                    <%
                    } else {
                    %>
                    <tr>
                        <td valign=top><img src="images/x.gif" width="13" height="13"></td>
                        <td>
                            <%= errorMessage %>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                    <tr>
                        <td valign=top><img src="images/check.gif" width="13" height="13"></td>
                        <td>您的应用服务器支持servlet 2.2或者更新。</td>
                    </tr>
                    <tr>
                        <td valign=top><img src="images/check.gif" width="13" height="13"></td>
                        <td><b>您的缓存已清除，配置信息已更新。</b></td>
                    </tr>
                </table>
            </ul>
            <%
                if (propError || !isJDK1_8 || !servlet2_2) {
            %>
            <b>安装初始化检查过程中发现错误，请更正，然后重新启动服务器重新开始安装过程。</b>
            <%
            } else {
            %>
            <form method="post" action="setup.jsp?op=select">
                请选择数据库类型：&nbsp;&nbsp;&nbsp;
                <input name="oadb" value="MySQL" type="hidden"/>
                <select name="oadb" disabled>
                    <option value="">请选择数据库类型</option>
                    <%
                        String dbType = StrUtil.getNullStr(Global.db);
                        // System.out.println("dbType=" + dbType);
                        if (dbType.equals("MySQL")) {
                    %>
                    <option value="mysql" selected="selected">MYSQL</option>
                    <%} else {%>
                    <option value="mysql">MYSQL</option>
                    <%} %>
                    <option value="mssql">SQLServer2000</option>
                    <option value="oracle">Oracle</option>
                    <option value="oracle">PostGreSql</option>
                </select>
                <script>
                    o("oadb").value = "mysql";
                </script>
                <hr size="0">
                <div align="center">
                    <input id="btnLogin" class="btn" type="button" value="登录" onclick="window.location.href='<%=request.getContextPath()%>/index.do'" />
                    &nbsp;&nbsp;
                    <input class="btn" type="button" value="许可证" onclick="window.location.href='license.jsp'"/>
                    &nbsp;&nbsp;
                    <input class="btn" type="submit" value="下一步"/>
                </div>
            </form>
            <%
                }
            %>
        </td>
    </tr>
</table>
</body>
</html>