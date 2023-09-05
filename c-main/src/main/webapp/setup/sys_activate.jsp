<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.account.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.BasicDataMgr" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.redmoon.oa.Config" %>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<%@ page import="com.cloudwebsoft.framework.util.IPUtil" %>
<%@ page import="com.redmoon.oa.kernel.License" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>激活系统</title>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script type="text/javascript" src="../js/activebar2.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
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

    Config cfg = Config.getInstance();
    boolean isCloud = cfg.getBooleanProperty("isCloud");
    String url;
    if (isCloud) {
        url = "https://www.xiaocaicloud.com/public/license/onlineActivateV2.do";
    } else {
        url = "http://localhost:8899/oa_ide/public/license/onlineActivateV2.do";
    }
%>
<TABLE align="center" class="tabStyle_1 percent60" style="margin-top: 20px">
    <TBODY>
    <TR>
        <TD align="left" class="tabStyle_1_title">激活系统</TD>
    </TR>
    <TR>
        <td colspan="2" style="line-height: 2">
            <p>
                授权单位：<%=license.getCompany()%> <br>
                用户数：<%=license.getUserCount()%> <br>
                类型：<%=license.getType()%> <br>
                到期时间：<%=DateUtil.format(license.getExpiresDate(), "yyyy-MM-dd")%> <br>
                域名：<%=license.getDomain()%> <br>
                注：如果在设计流程时提示”激活码错误“，请删除activex目录下的ac.dat并重新激活
            </p>
        </td>
    </tr>
    <tr>
        <TD height="30" colspan="2" align="center">
            <input type="button" class="btn" value="在线激活" onclick="activate()">
            &nbsp;&nbsp;&nbsp;&nbsp;
            <input id="btnOfflineActivate" type="button" class="btn" value="离线激活">
            &nbsp;&nbsp;&nbsp;&nbsp;
            <input type="button" class="btn" value="返回" onclick="window.history.back();">
        </TD>
    </TR>
    </TBODY>
</TABLE>
<form id="myForm"></form>
<script>
    function activate() {
        // 下载license文件
        var params = null;
        var data = {};
        if (data) {
            params = JSON.stringify(data);
        }

        var xhr = new XMLHttpRequest();
        xhr.open('POST', '../public/lic/download', true);
        xhr.responseType = 'blob';
        xhr.onload = function () {
            if (this.status === 200) {
                var blob = this.response;
                var filename = "";
                var disposition = xhr.getResponseHeader('Content-Disposition');
                if (disposition && disposition.indexOf('attachment') !== -1) {
                    var filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
                    var matches = filenameRegex.exec(disposition);
                    if (matches != null && matches[1]) filename = matches[1].replace(/['"]/g, '');
                }

                var formData = new FormData($('#myForm')[0]);
                <%
                String server = IPUtil.getRemoteAddr(request);
                %>
                // var activateRequestCode = "<%=server%>|" + (new Date()).Format("yyyy-MM-dd hh:mm:ss");
                var activateRequestCode = window.location.host + "|" + (new Date()).Format("yyyy-MM-dd hh:mm:ss");
                formData.append("activateRequestCode", activateRequestCode);
                formData.append("filename0", blob, "license.dat");
                $.ajax({
                    async: false,
                    type: "post",
                    url: "<%=url%>",
                    data: formData,
                    cache: false,
                    processData: false,
                    contentType: false,
                    dataType: "json",
                    beforeSend: function (XMLHttpRequest) {
                        $('body').showLoading();
                    },
                    success: function (data, status) {
                        if (data.ret == 1) {
                            // 上传激活码
                            $.ajax({
                                type: "post",
                                url: "../public/lic/setActivationCodeV2.do?ac=" + data.activationCode,
                                data: {},
                                dataType: "html",
                                beforeSend: function (XMLHttpRequest) {
                                    $("body").showLoading();
                                },
                                success: function (data, status) {
                                    data = $.parseJSON(data);
                                    if (data.ret == 1) {
                                        jAlert(data.msg, '提示');
                                    }
                                },
                                complete: function (XMLHttpRequest, status) {
                                    $("body").hideLoading();
                                },
                                error: function (XMLHttpRequest, textStatus) {
                                    // 请求出错处理
                                    alert(XMLHttpRequest.responseText);
                                }
                            });
                        } else {
                            jAlert(data.msg, "提示");
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        $('body').hideLoading();
                    },
                    error: function () {
                        //请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        };
        xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
        xhr.send(params);
    }

    $(function () {
        $('#btnCopy').click(function () {
            o('offlineCode').select();
            if (document.execCommand('copy')) {
                document.execCommand('copy');
            }
            jAlert("复制成功！", "提示");
        })

        $('#btnOfflineActivate').click(function () {
            window.location.href = 'sys_activate_offline.jsp';
        })
    })
</script>
</body>
</html>
