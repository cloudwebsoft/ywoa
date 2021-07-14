<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.address.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>通讯录-选择</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../inc/ajax_getpage.jsp"></script>
    <script>
        var mobiles = "";

        function addPerson(person, mobile) {
            if (mobile == "")
                return;
            // 检查是否有重复
            var ary = mobiles.split(",");
            var len = ary.length;
            for (var i = 0; i < ary.length; i++) {
                if (ary[i] == mobile) {
                    jAlert("号码： " + mobile + " 已被选择！", "提示");
                    return;
                }
            }
            divSel.innerHTML += "<table class='tabStyle_1_sub' id='d" + len + "' cellpadding=0 cellspacing=0 width='100%'><tr><td width=30%>" + person + "</td><td width=50%>" + mobile + "</td><td><a href=\"javascript:;\" onclick=\"del(" + len + ",'" + mobile + "')\">×</a></td></tr></table>";
            if (mobiles == "") {
                mobiles = mobile;
            } else {
                mobiles += "," + mobile;
            }
        }

        function clearAll() {
            o("divSel").innerHTML = "";
            mobiles = "";
        }

        function del(id, mobile) {
            $("#d" + id).remove();
            // 删除mobiles中已有的号码
            var m = "";
            var ary = mobiles.split(",");
            var len = ary.length;
            for (var i = 0; i < ary.length; i++) {
                if (ary[i] == mobile)
                    continue;
                if (m == "")
                    m = ary[i];
                else
                    m += "," + ary[i];
            }
            mobiles = m;
        }

        function doSel() {
            window.top.opener.setMobiles(mobiles);
            window.top.close();
        }
    </script>
</head>
<body topmargin="5" style="overflow:auto">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<table class="tabStyle_1" width="100%" border="0" align="center" cellpadding="2" cellspacing="0">
    <thead>
    <tr>
        <td height="21" align="left">已选名单</td>
    </tr>
    </thead>
    <tr>
        <td height="17" align="center">
            <input class="btn" type="button" onclick="doSel()" value="确定"/>
            &nbsp;
            <input type="button" onclick="clearAll()" class="btn" value="清空"/>
        </td>
    </tr>
    <tr>
        <td height="17" align="left">
            <div id="divSel"></div>
        </td>
    </tr>

</table>
</body>
</html>