<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.IpUtil,
                 cn.js.fan.util.ParamUtil,
                 cn.js.fan.util.XMLConfig,
                 cn.js.fan.web.Global,
                 com.redmoon.oa.kernel.License,
                 com.redmoon.oa.dept.DeptDb"
%>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<%@ page import="com.cloudweb.oa.utils.ConfigUtil" %>
<%@ page import="com.cloudweb.oa.utils.SysUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.base.IConfigUtil" %>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<%
    License lic = License.getInstance();
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title><%=lic.getCompany()%>系统安装 - 配置环境变量</title>
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
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script type="text/javascript" src="../js/jquery.toaster.js"></script>
    <style type="text/css">
        .tip {
            color: #FF0000
        }
    </style>
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

    XMLConfig cfg = new XMLConfig("config_sys.xml", false, "utf-8");
    XMLConfig cfg_oa = new XMLConfig("config.xml", false, "utf-8");
    int isHideStep = ParamUtil.getInt(request, "isHideStep", 0);
%>
<table cellpadding="6" cellspacing="0" border="0" width="100%">
    <tr>
        <td width="1%" valign="top"></td>
        <td width="99%" align="center" valign="top">
            <div align="left"><b>欢迎您使用<%=lic.getCompany()%>系统 版本<%=cfg_oa.get("oa.version")%>
            </b></div>
            <hr size="0">
            <%
                DeptDb dd = new DeptDb();
                dd = dd.getDeptDb(DeptDb.ROOTCODE);
            %>
            <form id="form1" action="?op=setup&isHideStep=<%=isHideStep %>" method="post">
                <table width="100%" border="0" cellpadding="0" cellspacing="0">
                    <tr>
                        <td height="25" colspan="2" align="left">配置环境变量：</td>
                    </tr>
                    <tr>
                        <td align="right">系统名称：</td>
                        <td>
                            <input type="text" name="Application.name" value="<%=Global.AppName%>"/>&nbsp;
                            <input name="Application.server" value="<%=request.getServerName()%>" type="hidden"/>
                            <input type="hidden" name="Application.port" value="<%=request.getServerPort()%>"/>
                            <input type="hidden" name="Application.title" value=""/>
                            <input type="hidden" name="Application.desc" value=""/>
                        </td>
                    </tr>
                    <tr style="display: none">
                        <td align="right">虚拟路径：</td>
                        <td align="left">
                            <%
                                boolean isCluster = Global.isCluster();
                                String realPathRecommand;
                                String realPath = "";
                                IConfigUtil configUtil = SpringUtil.getBean(IConfigUtil.class);
                                if (configUtil.isRunJar()) {
                                    realPathRecommand = Global.getRealPath();
                                    realPath = Global.getRealPath();
                                }
                                else {
                                    // 当以内置tomcat运行时，取得的为：WEB-INF/classes/META-INF/resources/
                                    // realPathRecommand = application.getRealPath("/").replaceAll("\\\\", "/");
                                    realPathRecommand = Global.getAppPath();
                                    LogUtil.getLog(getClass()).info("realPathRecommand=" + realPathRecommand);
                                    if (isCluster) {
                                        realPath = Global.getRealPath();
                                    }
                                    else {
                                        if ("".equals(Global.getRealPath())) {
                                            realPath = realPathRecommand;
                                        }
                                        else {
                                            realPath = Global.getRealPath();
                                        }
                                    }
                                }

                                String vPath = Global.virtualPath;
                                if ("".equals(vPath)) {
                                    vPath = request.getContextPath();
                                    if (vPath.startsWith("/")) {
                                        vPath = vPath.substring(1);
                                    }
                                }
                            %>
                            <input name="Application.virtualPath" value="<%=vPath%>"/>
                        </td>
                    </tr>
                    <tr>
                        <td align="right">调试状态：</td>
                        <td align="left">
                            <select id="isDebug" name="Application.isDebug">
                                <option value="true">是</option>
                                <option value="false">否</option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td align="right">单位名称：</td>
                        <td><input id="deptName" name="deptName" value="<%=dd.getName()%>"/></td>
                    </tr>
                    <tr>
                        <td align="right">集群：</td>
                        <td>
                            <select id="isCluster" name="Application.isCluster">
                                <option value="false" selected>否</option>
                                <option value="true">是</option>
                            </select>
                        </td>
                    </tr>
                    <tr id="trClusterNo" style="display:<%=isCluster?"":"none"%>">
                        <td align="right">显示集群编号：</td>
                        <td>
                            <select id="isClusterNoDisplay" name="Application.isClusterNoDisplay">
                                <option value="true" selected>是</option>
                                <option value="false">否</option>
                            </select>
                        </td>
                    </tr>
                    <tr id="trRealPath">
                        <td align="right">文件上传路径：</td>
                        <td>
                            <input id="realPath" name="realPath" style="width:400px" value="<%=realPath%>"/>
                            建议：<a href="javascript:;" title="点击设置文件上传路径" onclick="o('realPath').value='<%=realPathRecommand%>'"><%=realPathRecommand%>
                        </a>
                        </td>
                    </tr>
                    <tr style="display: none;">
                        <td align="right">服务器request是否直接支持中文：</td>
                        <td><select name="Application.isRequestSupportCN">
                            <option value="true">是</option>
                            <option value="false" selected="selected">否</option>
                        </select>
                            <script>
                                var supobj = o("Application.isRequestSupportCN");
                                supobj.value = "<%=Global.requestSupportCN%>";
                            </script>
                            ( Tomcat 默认选是，如果使用了防乱码过滤器也选是，<span class="tip">注意慎重选用，否则在提交后可能会出现乱码</span> )
                        </td>
                    </tr>
                    <tr>
                        <td align="right">SSL安全套接字连接：</td>
                        <td><select name="Application.internetFlag">
                            <option value="secure">是</option>
                            <option value="no">否</option>
                        </select>
                            <script>
                                var obj = o("Application.internetFlag");
                                obj.value = "<%=Global.internetFlag%>";
                            </script>
                        </td>
                    </tr>
                    <tr>
                        <td align="right">系统正式启用：</td>
                        <td>
                            <select name="Application.isFormalOpen">
                            <option value="true">是</option>
                            <option value="false">否</option>
                            </select>
                            <script>
                                o("Application.isFormalOpen").value = "<%=Global.getInstance().isFormalOpen()%>";
                            </script>
                            (系统如正式启用，则需以管理员身份登录才能setup)
                        </td>
                    </tr>
                    <tr>
                        <td align="right">默认时区：</td>
                        <td>
                            <select name="i18n.timeZone">
                                <option value="GMT-11:00">(GMT-11.00)中途岛，萨摩亚群岛</option>
                                <option value="GMT-10:00">(GMT-10.00)夏威夷</option>
                                <option value="GMT-09:00">(GMT-9.00)阿拉斯加</option>
                                <option value="GMT-08:00">(GMT-8.00)太平洋时间（美国和加拿大）；蒂华纳</option>
                                <option value="GMT-07:00">(GMT-7.00)山地时间（美国和加拿大）</option>
                                <option value="GMT-06:00">(GMT-6.00)中美洲</option>
                                <option value="GMT-05:00">(GMT-5.00)波哥大，利马，基多</option>
                                <option value="GMT-04:00">(GMT-4.00)加拉加斯，拉巴斯</option>
                                <option value="GMT-03:00">(GMT-3.00)格陵兰</option>
                                <option value="GMT-02:00">(GMT-2.00)中大西洋</option>
                                <option value="GMT-01:00">(GMT-1.00)佛得角群岛</option>
                                <option value="GMT">(GMT)格林威治标准时间，都柏林，爱丁堡，伦敦，里斯本</option>
                                <option value="GMT+01:00">(GMT+1.00)阿姆斯特丹，柏林，伯尔尼，罗马，斯德哥尔摩，维也纳</option>
                                <option value="GMT+02:00">(GMT+2.00)雅典，贝鲁特，伊斯坦布尔，明斯克</option>
                                <option value="GMT+03:00">(GMT+3.00)莫斯科，圣彼得堡，伏尔加格勒</option>
                                <option value="GMT+04:00">(GMT+4.00)阿布扎比，马斯喀特</option>
                                <option value="GMT+04:30">(GMT+4.30)喀布尔</option>
                                <option value="GMT+05:00">(GMT+5.00)叶卡捷琳堡</option>
                                <option value="GMT+05:30">(GMT+5.30)马德拉斯，加尔各答，孟买，新德里</option>
                                <option value="GMT+05:45">(GMT+5.45)加德满都</option>
                                <option value="GMT+06:00">(GMT+6.00)阿拉木图，新西伯利亚</option>
                                <option value="GMT+06:30">(GMT+6.30)仰光</option>
                                <option value="GMT+07:00">(GMT+7.00)曼谷，河内，雅加达</option>
                                <option value="GMT+08:00" selected="selected">(GMT+8.00)北京，台北，重庆，香港特别行政区，乌鲁木齐</option>
                                <option value="GMT+09:00">(GMT+9.00)汉城，大坂，东京，札幌</option>
                                <option value="GMT+09:30">(GMT+9.30)达尔文</option>
                                <option value="GMT+10:00">(GMT+10.00)关岛，莫尔兹比港</option>
                                <option value="GMT+11:00">(GMT+11.00)马加丹，索罗门群岛，新喀里多尼亚</option>
                                <option value="GMT+12:00">(GMT+12.00)斐济，堪察加半岛，马绍尔群岛</option>
                                <option value="GMT+13:00">(GMT+13.00)努库阿洛法</option>
                            </select>
                            <script>
                                o("i18n.timeZone").value = "<%=Global.timeZone.getID()%>";
                            </script>
                        </td>
                    </tr>
                    <tr>
                        <%
                            String urlIp = request.getServerName();
                            if (!IpUtil.isDomain(urlIp)) {
                                if (IpUtil.isInnerIP(urlIp)) {%>
                        <td align="right">外网访问OA服务器的域名或IP地址：</td>
                        <td>
                            <%
                                String server = cfg.get("Application.server");
                                if (IpUtil.isInnerIP(server)) {%>
                            <input id="publicIp" name="publicIp" value=""/>
                            <%
                            } else {
                            %>
                            <input id="publicIp" name="publicIp" value="<%=server%>"/>
                            <%
                                }
                            %>
                            例如：www.sina.com.cn或183.232.231.1 <br/>
                            <span class="tip">(默认无需填写，但如果存在多个访问地址，请填写外网访问OA服务器的域名或IP地址，否则会影响手机端使用)</span>
                            <%
                                    }
                                }
                            %>
                        </td>
                    </tr>
                </table>
            </form>
            <hr size="0">
            <div align="center">
                <%
                    if (isHideStep == 0) {
                        String oadb = ParamUtil.get(request, "oadb");
                        String url = "setup2_mysql.jsp";
                        if (Global.db.equals(Global.DB_MYSQL)) {
                            url = "setup2_mysql.jsp";
                        } else if (oadb.equals("oracle")) {
                            url = "setup2_oracle.jsp";
                        } else if (oadb.equals("mssql")) {
                            url = "setup2_mssql.jsp";
                        } else if (oadb.equals("PostGreSql")) {
                            url = "setup2_postGreSql.jsp";
                        }
                %>
                <input class="btn" type="button" onclick="window.location.href='<%=url%>'" value="上一步"/>
                &nbsp;&nbsp;&nbsp;&nbsp;
                <%
                    }
                %>
                <input id="btnOk" class="btn" type="button" value="设置">
                &nbsp;&nbsp;&nbsp;&nbsp;
                <input class="btn" type="button" onclick="window.location.href='<%=request.getContextPath()%>/index.do'" value="进入系统"/>
            </div>
        </td>
    </tr>
</table>
</body>
<script>
    $(function () {
        $('#isDebug').val('<%=Global.getInstance().isDebug()%>');

        o("isCluster").value = "<%=isCluster%>";
        o("isClusterNoDisplay").value = "<%=Global.getInstance().isClusterNoDisplay()%>";

        $('#isCluster').change(function () {
            if ($(this).val() == "true") {
                // $('#trRealPath').show();
                $('#trClusterNo').show();
            } else {
                // $('#trRealPath').hide();
                $('#trClusterNo').hide();
                $('#isClusterNoDisplay').val('false');
                // o('realPath').value = '<%=realPathRecommand%>';
            }
        });

        $('#btnOk').click(function() {
            $.ajax({
                async: false,
                type: "post",
                url: "../setup/setupConfig",
                data: $('#form1').serialize(),
                dataType: "json",
                beforeSend: function (XMLHttpRequest) {
                    $('body').showLoading();
                },
                complete: function (XMLHttpRequest, status) {
                },
                success: function (data, status) {
                    $('body').hideLoading();
                    jAlert(data.msg, '提示');
                },
                error: function (XMLHttpRequest, textStatus) {
                    $('body').hideLoading();
                    alert(XMLHttpRequest.responseText);
                }
            });
        })
    });
</script>
</html>