<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*,
                 cn.js.fan.web.*,
                 com.redmoon.oa.dept.*,
                 com.redmoon.oa.flow.*,
                 java.util.*"
%>
<%@page import="com.redmoon.oa.util.TwoDimensionCode" %>
<%@page import="com.redmoon.oa.kernel.License" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%
    XMLConfig cfg = new XMLConfig("config_cws.xml", false, "utf-8");
    XMLConfig cfg_oa = new XMLConfig("config_oa.xml", false, "utf-8");
    int isHideStep = ParamUtil.getInt(request, "isHideStep", 0);
    License lic = License.getInstance();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title><%=lic.getCompany()%>系统安装 - 配置环境变量</title>
    <link rel="stylesheet" type="text/css" href="../common.css">
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery.js"></script>
    <jsp:useBean id="myconfig" scope="page" class="com.redmoon.oa.Config"/>
    <style type="text/css">
        .tip {
            color: #FF0000
        }
    </style>
    <script type="text/javascript" src="../inc/common.js"></script>
</head>
<body>
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
                String op = ParamUtil.get(request, "op");
                String publicIp = ParamUtil.get(request, "publicIp");
                if (op.equals("setup")) {
                    try {
                        Enumeration e = request.getParameterNames();
                        while (e.hasMoreElements()) {
                            String fieldName = (String) e.nextElement();
                            if (fieldName.startsWith("Application") || fieldName.startsWith("i18n")) {
                                String value = ParamUtil.get(request, fieldName);
                                cfg.set(fieldName, value);

                                System.out.println(fieldName + "=" + value);
                            }
                        }
                        if (!publicIp.trim().equals("")) {
                            if (IpUtil.isDomain(publicIp)) {
                                cfg.set("Application.server", publicIp);
                            } else {
                                if (!IpUtil.isInnerIP(publicIp)) {
                                    cfg.set("Application.server", publicIp);
                                }
                            }

                        }
                        cfg.writemodify();
                        Global.init();

                        String deptName = ParamUtil.get(request, "deptName");
                        if (!deptName.equals("")) {
                            dd.setName(deptName);
                            dd.save();
                        }

                        String value = ParamUtil.get(request, "Application.name");
                        myconfig.put("enterprise", value);
                        out.print(StrUtil.Alert("操作成功！"));
                        TwoDimensionCode.generate2DCodeByMobileClient();//生成手机端二维码
                    } catch (Exception e) {
                        out.print(StrUtil.Alert_Back(e.getMessage()));
                        e.printStackTrace();
                    }

                    // 过滤表单中的图片路径
                    String sql = "select code from form";
                    FormDb fd = new FormDb();
                    Iterator ir = fd.list(sql).iterator();
                    while (ir.hasNext()) {
                        fd = (FormDb) ir.next();
                        String content = fd.getContent();
                        content = FormForm.initImgLink(content);
                        fd.setContent(content);
                        fd.saveContent();
                    }
                }
            %>
            <table width="100%" border="0" cellpadding="0" cellspacing="0">
                <form name=form1 action="?op=setup&isHideStep=<%=isHideStep %>" method=post>
                    <tr>
                        <td height="25" colspan="2" align="left">配置环境变量：</td>
                    </tr>
                    <tr>
                        <td height="24" align="right">系统名称：</td>
                        <td>
                            <input type="text" name="Application.name" value="<%=Global.AppName%>"/>&nbsp;
                            <input name="Application.server" value="<%=request.getServerName()%>" type="hidden"/>
                            <input type="hidden" name="Application.port" value="<%=request.getServerPort()%>"/>
                            <input type="hidden" name="Application.title" value=""/>
                            <input type="hidden" name="Application.desc" value=""/>
                            <%
                                String vPath = request.getContextPath();
                                if (!vPath.equals("")) {
                                    vPath = vPath.substring(1);
                                }
                                String realPathRecommand = application.getRealPath("/").replaceAll("\\\\", "/");
                                boolean isCluster = Global.isCluster();
                                String realPath = realPathRecommand;
                                if (isCluster) {
                                    realPath = Global.getRealPath();
                                }
                                // String realPath = Global.getRealPath().replaceAll("\\\\", "/");
                            %>
                            <input type="hidden" name="Application.virtualPath" value="<%=vPath%>"/>
                        </td>
                    </tr>
                    <tr>
                        <td height="24" align="right">单位名称：</td>
                        <td><input id="deptName" name="deptName" value="<%=dd.getName()%>"/></td>
                    </tr>
                    <tr>
                        <td height="24" align="right">集群：</td>
                        <td>
                            <select id="isCluster" name="Application.isCluster">
                                <option value="false" selected>否</option>
                                <option value="true">是</option>
                            </select>
                            <script>
                                $(function () {
                                    o("isCluster").value = "<%=isCluster%>";

                                    $('#isCluster').change(function () {
                                        if ($(this).val() == "true") {
                                            $('#trRealPath').show();
                                        } else {
                                            $('#trRealPath').hide();
                                            o('Application.realPath').value = '<%=realPathRecommand%>';
                                        }
                                    })
                                });
                            </script>
                        </td>
                    </tr>
                    <tr id="trRealPath" style="display:<%=isCluster?"":"none"%>">
                        <td height="24" align="right">文件上传路径：</td>
                        <td>
                            <%
                                String cloudHome = application.getRealPath("/");
                                cloudHome = cloudHome.replaceAll("\\\\", "/");
                                if (cloudHome.lastIndexOf("/") != cloudHome.length() - 1)
                                    cloudHome += "/";
                                if (isCluster) {
                                    cloudHome = Global.getRealPath();
                                }
                            %>
                            <input id="Application.realPath" name="Application.realPath" style="width:400px" value="<%=cloudHome%>"/>
                            建议：<a href="javascript:;" title="点击设置文件上传路径" onclick="o('Application.realPath').value='<%=realPath%>'"><%=realPath%>
                        </a>
                        </td>
                    </tr>
                    <tr>
                        <td height="24" align="right">服务器request是否直接支持中文：</td>
                        <td><select name="Application.isRequestSupportCN">
                            <option value="true">是</option>
                            <option value="false" selected="selected">否</option>
                        </select>
                            <script>
                                var supobj = o("Application.isRequestSupportCN");
                                supobj.value = "<%=Global.requestSupportCN%>";
                            </script>
                            ( Tomcat 选否，Resin选是，<span class="tip">注意慎重选用，否则在提交后可能会出现乱码</span> )
                        </td>
                    </tr>
                    <tr>
                        <td height="24" align="right">SSL安全套接字连接：</td>
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
                        <td height="24" align="right">默认时区：</td>
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
                        <td height="24" align="right">外网访问OA服务器的域名或IP地址：</td>
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
                            例如：www.sina.com.cn或183.232.231.1 <span class="tip">(请填写外网访问OA服务器的域名或IP地址，否则手机端只能内网使用)</span>
                            <%
                                    }
                                }
                            %>
                        </td>
                    </tr>
                </form>
            </table>
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
                <input type="button" onclick="window.location.href='<%=url%>'" value="上一步"/>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <%
                    }
                %>
                <input type="button" value="设 置" onClick="form1.submit()">
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <%if (true || op.equals("setup")) {%>
                <input name="button2" type="button" onclick="window.location.href='<%=request.getContextPath()%>/index.jsp'" value="进入系统"/>
                <%}%>
            </div>
        </td>
    </tr>
</table>
</body>
</html>