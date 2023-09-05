<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.sys.SysUtil" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<%
    Privilege privilege = new Privilege();
    if (!privilege.isUserPrivValid(request, "admin")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = ParamUtil.get(request, "op");
    if ("init".equals(op)) {
        JSONObject json = new JSONObject();
        try {
            boolean isClearUser = ParamUtil.getBoolean(request, "isClearUser", false);
            SysUtil.initSystem(isClearUser);
        }
        catch (ErrMsgException e) {
            json.put("res", 1);
            json.put("msg", e.getMessage());
            out.print(json.toString());
            return;
        }
        json.put("res", 1);
        json.put("msg", "操作成功");
        out.print(json.toString());
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>系统初始化</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
    <script type="text/javascript" src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
    <script src="../js/layui/layui.js" charset="utf-8"></script>
</head>
<body>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
    <TBODY>
    <TR>
        <TD class="tdStyle_1">清空系统</TD>
    </TR>
    </TBODY>
</TABLE>
<br>
<div style="text-align: center; margin: 30px 0 10px 0">
    <input type="button" onclick="initSys(true, this)" class="btn btn-default" value="清空系统（含用户及权限）"/>
    &nbsp;&nbsp;
    <input type="button" onclick="initSys(false, this)" class="btn btn-default" value="清空系统（不含用户及权限）"/>
</div>
<div class="text-center">
    (<b>注：清除时将不会清除已设计的流程</b>)
</div>
</body>
<script>
    function initSys(isClearUser, btn) {
        var btnName = btn.value;
        layer.confirm('请再次确认要' + btnName + '么？清空前如果未做好备份，可能会丢失数据！', {icon: 3, title: '提示'}, function (index) {
            layer.confirm('请再次确认要' + btnName + '么？清空前如果未做好备份，可能会丢失数据！', {icon: 3, title: '提示'}, function (index) {
                $.ajax({
                    type: "post",
                    url: "sys_init.jsp",
                    data: {
                        op: 'init',
                        isClearUser: isClearUser
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        $("body").showLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        layer.alert(data.msg, {icon: 3});
                    },
                    complete: function (XMLHttpRequest, status) {
                        $("body").hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });
                layer.close(index);
            });
        });
    }
</script>
</html>
