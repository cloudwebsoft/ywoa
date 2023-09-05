<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="cn.js.fan.util.StrUtil" %>
<%@page import="com.redmoon.oa.flow.WorkflowDb" %>
<%@page import="com.redmoon.oa.flow.Leaf" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.stamp.StampMgr" %>
<%@ page import="com.redmoon.oa.stamp.StampDb" %>
<%@ page import="com.redmoon.oa.stamp.StampPriv" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>图片签名框</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../css/iconfont.css"/>
    <link rel="stylesheet" type="text/css" href="../css/mui.picker.min.css"/>
    <link rel="stylesheet" href="../css/my_dialog.css"/>
</head>
<%
    String skey = ParamUtil.get(request, "skey");
    String code = ParamUtil.get(request, "code");
    Privilege _priv = new Privilege();
    String userName = _priv.getUserName(skey);
    UserDb _userDb = new UserDb(userName);
    String realName = _userDb.getRealName();
    String pwd = _userDb.getPwdRaw();
%>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">
        签章选择
    </h1>
</header>
<div class="mui-content">
    <div class="mui-input-row mui-select">
        <label style="color: #000;">
            印章
        </label>
        <%
            StampPriv sp = new StampPriv();
            Vector v = sp.getStampsOfUser(userName);
            Iterator ir = v.iterator();
        %>
        <select id="stampId" name="stampId">
            <option value="-1">无</option>
            <%
                Map map = new HashMap();
                while (ir.hasNext()) {
                    StampDb ld = (StampDb) ir.next();
                    if (ld != null && ld.isLoaded()) {
                        if (map.containsKey(String.valueOf(ld.getId()))) {
                            continue;
                        }
                        map.put(String.valueOf(ld.getId()), "");
            %>
            <option value="<%=ld.getId()%>" link="<%=ld.getImageUrl()%>"><%=ld.getTitle()%></option>
            <%
                    }
                }
            %>
        </select>
    </div>
    <div class="mui-input-row">
        <label style="color: #000;">
            密码
        </label>
        <input type="password" class="mui-input-clear" id="pwd" autocomplete="off" placeholder="请输入密码">
    </div>
    <div class="mui-button-row">
        <button class="mui-btn mui-btn-primary" type="button" onclick="return false;">确定</button>
    </div>
</div>
<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/macro/open_window_macro.js"></script>
<script src="../js/mui.min.js"></script>
<script src="../js/jq_mydialog.js"></script>
<script type="text/javascript" src="../js/config.js"></script>
</body>
<script type="text/javascript">
    $(function () {
        $("#pwd").keydown(function (e) {
            var e = e || event,
                keycode = e.which || e.keyCode;
            if (keycode == 13) {
                if (comparePwd()) {
                    // window.parent.closeSignInImg('<%=code%>', '<%=realName%>');
                    // return false;
                }
            }
        });
    });

    function comparePwd() {
        var realName = '<%=realName%>';
        var pwd = '<%=pwd%>';
        var code = '<%=code%>';
        var _curPwd = jQuery("#pwd").val();
        if (pwd != _curPwd) {
            // $.toast("密码不一致!");
            mui.alert('密码错误！', '提示', function () {
            });
            return false;
        }
        return true;
    }

    (function ($) {
        $.init({
            swipeBack: true //启用右滑关闭功能
        });
        $(".mui-content").on("tap", ".mui-btn", function () {
            if (comparePwd()) {
                var id = jQuery('#stampId').val();
                if (id!="") {
                    var link = jQuery("#stampId option:selected").attr("link");
                    window.parent.closeSignInImg('<%=code%>', id, link);
                }
            }
        });
    })(mui);
</script>
</html>
