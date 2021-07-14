<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.DateUtil,
                 cn.js.fan.util.ParamUtil,
                 cn.js.fan.util.StrUtil,
                 cn.js.fan.util.file.FileUtil,
                 cn.js.fan.web.Global,
                 com.redmoon.kit.util.FileInfo,
                 com.redmoon.kit.util.FileUpload,
                 com.redmoon.oa.Config,
                 com.redmoon.oa.SpConfig,
                 com.redmoon.oa.kernel.License,
                 com.redmoon.oa.kernel.LicenseUtil,
                 com.redmoon.oa.pvg.Privilege,
                 com.redmoon.oa.ui.SkinMgr,
                 java.util.Vector,
                 java.io.IOException"
%>
<%@ page import="com.cloudweb.oa.api.ILicense" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<jsp:useBean id="backup" scope="page" class="cn.js.fan.util.Backup"/>
<jsp:useBean id="cfg" scope="page" class="cn.js.fan.web.Config"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>许可证</title>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
</head>
<body>
<%
    String op = ParamUtil.get(request, "op");
    String licFileName = "";
    if ("upload".equals(op)) {
        FileUpload fileUpload = new FileUpload();
        fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        String[] extnames = {"dat"};
        fileUpload.setValidExtname(extnames); //设置可上传的文件类型
        int ret = 0;
        try {
            ret = fileUpload.doUpload(application, request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (fileUpload.getRet() == FileUpload.RET_SUCCESS) {
            Vector v = fileUpload.getFiles();
            FileInfo fi = null;
            if (v.size() > 0) {
                fi = (FileInfo) v.get(0);
            }
            String vpath = "";
            if (fi != null) {
                // 置保存路径
                String filepath = Global.getRealPath() + FileUpload.TEMP_PATH + "/";
                fileUpload.setSavePath(filepath);
                fileUpload.writeFile(true);
                // 使用随机名称写入磁盘
                // fi.writeToPath(filepath);
                licFileName = filepath + fi.getDiskName();
            }
        }
    } else if ("change".equals(op)) {
        licFileName = ParamUtil.get(request, "licFileName");
        FileUtil.CopyFile(licFileName, Global.getAppPath() + "WEB-INF/license.dat");
        License.getInstance().init();
        ILicense iLicense = SpringUtil.getBean(ILicense.class);
        iLicense.init();
        out.print(StrUtil.Alert("操作成功！"));
    }

    License.getInstance().init();

    Privilege pvg = new Privilege();
    if (Global.getInstance().isFormalOpen()) {
        if (!pvg.isUserPrivValid(request, Privilege.ADMIN)) {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "请以管理员身份登录以后再操作"));
            return;
        }
    }
%>
<form name="addform" action="license.jsp?op=upload" method="post" enctype="MULTIPART/FORM-DATA">
    <table border="0" width="100%">
        <tr>
            <td align="center">许可证文件
                <input type="file" name="attachment0"/>
                <input class="btn" type="submit" value="上传"/>
            </td>
        </tr>
    </table>
</form>
<%if ("".equals(licFileName)) {%>
<table width="53%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent80">
    <tbody>
    <tr>
        <td colspan="2" align="left" class="tabStyle_1_title">&nbsp;许可证信息</td>
    </tr>
    <tr>
        <td width="17%" align="left">授权单位</td>
        <td width="83%" align="left">
            <%
                Config oaCfg = new Config();
                SpConfig spCfg = new SpConfig();
                String version = StrUtil.getNullStr(oaCfg.get("version"));
                String spVersion = StrUtil.getNullStr(spCfg.get("version"));
                License license = License.getInstance();
                out.print(license.getCompany());
            %>
        </td>
    </tr>
    <tr>
        <td width="17%" align="left">名称</td>
        <td width="83%" align="left">
            <%=license.getName()%>
        </td>
    </tr>
    <tr>
        <td align="left">企业号</td>
        <td align="left"><%=license.getEnterpriseNum() != null ? license.getEnterpriseNum() : license.getName()%>
        </td>
    </tr>
    <tr>
        <td align="left">用户数</td>
        <td align="left"><%=license.getUserCount()%>
        </td>
    </tr>
    <tr>
        <td align="left">类型</td>
        <td align="left"><%=license.getType().equals(License.TYPE_COMMERICAL) ? "免费版" : license.getType()%>
        </td>
    </tr>
    <tr>
        <td align="left">到期时间</td>
        <td align="left"><%=DateUtil.format(license.getExpiresDate(), "yyyy-MM-dd")%>
        </td>
    </tr>
    <tr>
        <td align="left">域名</td>
        <td align="left"><%=license.getDomain()%>
        </td>
    </tr>
    <tr>
        <td align="left">流程最大节点数</td>
        <td align="left"><%=license.getActionCount()%>
        </td>
    </tr>
    <tr>
        <td align="left">试用版</td>
        <td align="left"><%=license.isTrial()?"是":"否"%></td>
    </tr>
    <tr>
        <td align="left">流程设计器</td>
        <td align="left"><%=license.getFlowDesigner().equals(License.FLOW_DESIGNER_A) ? "A版（仅可在IE下设计）":"X版"%>
        </td>
    </tr>
    <tr>
        <td align="left">Office控件序列号</td>
        <td align="left"><%=license.getOfficeControlKey()%>
        </td>
    </tr>
    <tr>
        <td align="left">系统版本</td>
        <td align="left"><%=version%>
        </td>
    </tr>
    <tr>
        <td align="left">系统补丁版本</td>
        <td align="left"><%=spVersion%>
        </td>
    </tr>
    <tr>
        <td colspan="2" align="center" style="padding: 5px">
            <input class="btn" type="button" value="激活" onclick="window.location.href='sys_activate.jsp'"/>
            &nbsp;&nbsp;
            <input class="btn" type="button" onclick="window.location.href='setup.jsp'" value="返回"/>
        </td>
    </tr>
    </tbody>
</table>
<%} else {%>
<table width="53%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent80">
    <tbody>
    <tr>
        <td colspan="2" align="left" class="tabStyle_1_title">&nbsp;上传的许可证信息</td>
    </tr>
    <tr>
        <td width="17%" align="left">授权单位</td>
        <td width="83%" align="left"><%
            LicenseUtil.setLicenseFilePath(licFileName);
            LicenseUtil lu = LicenseUtil.getInstance();
            lu.init();
        %>
            <%=lu.getCompany()%>
        </td>
    </tr>
    <tr>
        <td width="17%" align="left">使用单位</td>
        <td width="83%" align="left">
            <%=lu.getName()%>
        </td>
    </tr>
    <tr>
        <td align="left">企业号</td>
        <td align="left"><%=lu.getEnterpriseNum()%>
        </td>
    </tr>
    <tr>
        <td align="left">用户数</td>
        <td align="left"><%=lu.getUserCount()%>
        </td>
    </tr>
    <tr>
        <td align="left">流程最大节点数</td>
        <td align="left"><%=lu.getActionCount()%>
        </td>
    </tr>
    <tr>
        <td align="left">试用版</td>
        <td align="left"><%=lu.isTrial()?"是":"否"%></td>
    </tr>
    <tr>
        <td align="left">流程设计器</td>
        <td align="left"><%=lu.getFlowDesigner().equals(License.FLOW_DESIGNER_A) ? "A版（仅可在IE下设计）":"X版"%>
        </td>
    </tr>
    <tr>
        <td align="left">类型</td>
        <td align="left"><%=lu.getType().equals(License.TYPE_COMMERICAL) ? "免费版" : lu.getType()%>
        </td>
    </tr>
    <tr>
        <td align="left">到期时间</td>
        <td align="left"><%=DateUtil.format(lu.getExpiresDate(), "yyyy-MM-dd")%>
        </td>
    </tr>
    <tr>
        <td align="left">域名</td>
        <td align="left"><%=lu.getDomain()%>
        </td>
    </tr>
    <tr>
        <td colspan="2" align="center" style="padding: 5px">
            <input class="btn" id="btnChange" type="button" value="替换"/>
            <script>
                $('#btnChange').click(function () {
                    jConfirm('您确定要替换么？', '提示', function (r) {
                        if (r) {
                            window.location.href = "license.jsp?op=change&licFileName=<%=licFileName%>";
                        }
                    })
                });
            </script>
            &nbsp;&nbsp;
            <input class="btn" type="button" onclick="window.location.href='license.jsp'" value="返回"/>
        </td>
    </tr>
    </tbody>
</table>
<%
    }
%>
</body>
</html>