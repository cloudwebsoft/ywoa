<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.account.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.BasicDataMgr" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="org.json.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>离线激活系统</title>
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
<table align="center" class="tabStyle_1 percent60" style="margin-top: 20px">
    <tbody>
    <tr>
        <TD align="left" class="tabStyle_1_title">离线激活</TD>
    </tr>
    <tr id="trOffline">
        <td height="30" colspan="2" align="center">
            <textarea id="offlineCode" style="width: 450px;height: 120px;"></textarea>
            <br/>
            请粘贴激活码
            &nbsp;&nbsp;&nbsp;&nbsp;
        </td>
    </tr>
    <tr>
        <td height="30" colspan="2" align="center">
            <input id="btnBefore" type="button" class="btn btn-default" value="上一步"
                   onclick="window.location.href='sys_activate.jsp'"/>
            &nbsp;&nbsp;&nbsp;&nbsp;
            <input id="btnCloud" type="button" class="btn btn-default" value="获取激活码" title="获取激活码"/>
            &nbsp;&nbsp;&nbsp;&nbsp;
            <input id="btnOfflineActivate" type="button" class="btn btn-default" value="激活"/>
        </td>
    </tr>
    </tbody>
</table>
<script>
    $(function () {
        $('#btnOfflineActivate').click(function (e) {
            e.preventDefault();
            if ($('#offlineCode').val() == '') {
                jAlert('请输入激活码', '提示');
                return;
            }
            $.ajax({
                type: "post",
                url: "../public/lic/setActivationCodeV2.do?ac=" + $('#offlineCode').val(),
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
        })

        $('#btnCloud').click(function(e) {
            e.preventDefault();
            window.open('https://www.xiaocaicloud.com/public/offline_activate.jsp');
        });
    })
</script>
</body>
</html>
