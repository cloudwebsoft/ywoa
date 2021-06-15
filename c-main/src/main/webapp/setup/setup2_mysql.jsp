<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.StrUtil,
                 cn.js.fan.util.XMLConfig,
                 com.redmoon.oa.kernel.License,
                 org.apache.commons.configuration.PropertiesConfiguration,
                 java.io.File,
                 java.io.FileInputStream,
                 java.net.URLDecoder,
                 java.net.URL"
%>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<%@ page import="cn.js.fan.web.Global" %>
<%
    License lic = License.getInstance();
%>
<!DOCTYPE>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title><%=lic.getCompany()%>系统安装 - 配置数据库连接</title>
    <link rel="stylesheet" type="text/css" href="../common.css">
    <!-- 注意不能用SkinMgr.getSkin(request)，因为如果数据库连接出错，其中调用到数据库的地方就会出错 -->
    <link type="text/css" rel="stylesheet" href="../skin/lte/css.css"/>
    <style type="text/css">
        td {
            height: 34px;
        }
        input:not([type="radio"]):not([type="button"]):not([type="checkbox"]) {
            width: 200px;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script type="text/javascript" src="../js/jquery.toaster.js"></script>
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

    XMLConfig cfg = new XMLConfig("config.xml", false, "gb2312");

    URL confURL = getClass().getResource("/application.properties");
    String xmlpath = confURL.getFile();
    xmlpath = URLDecoder.decode(xmlpath);
    FileInputStream fis = new FileInputStream(xmlpath);

    // 须用load及save加utf-8参数的形式，否则会乱码
    PropertiesConfiguration conf = new PropertiesConfiguration();
    conf.load(fis, "utf-8");
    fis.close();

    String url = conf.getString("spring.datasource.url");
    String user = conf.getString("spring.datasource.username");
    String pwd = conf.getString("spring.datasource.password");
    int maximumConnectionCount = StrUtil.toInt(conf.getString("spring.datasource.maxActive"), 200);

    int beginIndex = url.indexOf("//");
    String ip = url.substring(beginIndex + 2, url.indexOf(":", beginIndex));
    int secondIndex = url.indexOf("/", beginIndex + 2);
    String database = url.substring(secondIndex + 1, url.lastIndexOf("?"));
    int port = StrUtil.toInt(url.substring(url.lastIndexOf(":") + 1, secondIndex), 3306);
%>
<table cellpadding="6" cellspacing="0" border="0" width="100%">
    <tr>
        <td width="1%" valign="top"></td>
        <td width="99%" align="center" valign="top">
            <div align="left"><b>欢迎您使用<%=lic.getCompany()%>系统<%=cfg.get("oa.version")%> MySQL版本</b></div>
            <hr size="0">
            <form id="form1" action="?op=setup" method=post>
                <table width="100%" border="0" cellpadding="0" cellspacing="0">
                    <tr>
                        <td height="24" colspan="2" align="left">配置数据库连接：</td>
                    </tr>
                    <tr>
                        <td height="24" align="right">&nbsp;</td>
                        <td>
                            <span id="info"></span>
                        </td>
                    </tr>
                    <tr>
                        <td height="24" align="right">配置文件路径：</td>
                        <td><%=application.getRealPath("/") + "WEB-INF" + java.io.File.separator + "classes" + File.separator + "application.properties"%>
                            <br/>(在初始化前，请先将redmoonoa.sql导入mysql数据库)</td>
                    </tr>
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
                        <td><input type="text" name="maximum_connection_count" value="<%=maximumConnectionCount%>"/></td>
                    </tr>
                </table>
            </form>
            <hr size="0">
            <div align="center">
                <input class="btn" type="button" onclick="window.location.href='setup.jsp'" value="上一步"/>
                &nbsp;&nbsp;&nbsp;&nbsp;
                <input id="btnConn" class="btn" type="button" value="连接测试">
                &nbsp;&nbsp;&nbsp;&nbsp;
                <input class="btn" type="button" onclick="window.location.href='setup3.jsp?db_type=mysql'" value="下一步"/>
            </div>
        </td>
    </tr>
</table>
</body>
<script>
    $(function() {
        $('#btnConn').click(function() {
            setupDbProp();
        });
    });

    function setupDbProp() {
        $.ajax({
            url: "../setup/setupDbProp",
            type: "post",
            dataType: "json",
            data: $('#form1').serialize(),
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                $('body').hideLoading();
                if (data.ret == 1) {
                    refreshDruid();
                }
            },
            error: function (XMLHttpRequest, textStatus) {
                $('body').hideLoading();
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    function refreshDruid() {
        $.ajax({
            async: false,
            type: "post",
            url: "management/refresh",
            data: {},
            dataType: "json",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            complete: function (XMLHttpRequest, status) {
            },
            success: function (data, status) {
                $('body').hideLoading();
                // 返回为[]或["spring.datasource.url"]或["spring.datasource.maxActive","spring.datasource.url"]
                // consoleLog(data);
                checkPool();
            },
            error: function (XMLHttpRequest, textStatus) {
                $('body').hideLoading();
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    function checkPool() {
        $.toaster({priority: 'info', message: '参数设置成功，开始连接测试'});

        $.ajax({
            async: false,
            type: "post",
            url: "../setup/checkPool",
            data: {},
            dataType: "json",
            beforeSend: function (XMLHttpRequest) {
            },
            complete: function (XMLHttpRequest, status) {
            },
            success: function (data, status) {
                if (data.ret == 1) {
                    $('#info').html('<span style="color:green"><b>测试连接成功，缓存已清除！</b></span>');
                } else {
                    $('#info').html('<span style="color:red"><b>测试连接失败！请检查连接字符串、用户名和密码是否正确！</b></span>');
                }
            },
            error: function (XMLHttpRequest, textStatus) {
                alert(XMLHttpRequest.responseText);
            }
        });
    }
</script>
</html>