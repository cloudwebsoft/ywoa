<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.visual.*" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="org.json.JSONObject" %>
<%@page import="cn.js.fan.util.StrUtil" %>
<%
    String moduleCode = ParamUtil.get(request, "moduleCode");
    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDb(moduleCode);
    if (msd == null) {
        out.print("模块不存在！");
        return;
    }
    String formCode = msd.getString("form_code");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title><%=msd.getString("name")%>
    </title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport"
          content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../css/my_dialog.css"/>
    <link rel="stylesheet" href="../css/iconfont.css"/>
    <link rel="stylesheet" type="text/css"
          href="../css/mui.picker.min.css"/>
</head>
<style>
    html,
    body {
        background-color: #efeff4;
    }

    .mui-bar ~ .mui-content .mui-fullscreen {
        top: 44px;
        height: auto;
    }

    .mui-pull-top-tips {
        position: absolute;
        top: -20px;
        left: 50%;
        margin-left: -25px;
        width: 40px;
        height: 40px;
        border-radius: 100%;
        z-index: 1;
    }

    .mui-bar ~ .mui-pull-top-tips {
        top: 24px;
    }

    .mui-pull-top-wrapper {
        width: 42px;
        height: 42px;
        display: block;
        text-align: center;
        background-color: #efeff4;
        border: 1px solid #ddd;
        border-radius: 25px;
        background-clip: padding-box;
        box-shadow: 0 4px 10px #bbb;
        overflow: hidden;
    }

    .mui-pull-top-tips.mui-transitioning {
        -webkit-transition-duration: 200ms;
        transition-duration: 200ms;
    }

    .mui-pull-top-tips .mui-pull-loading {
        /*-webkit-backface-visibility: hidden;
        -webkit-transition-duration: 400ms;
        transition-duration: 400ms;*/

        margin: 0;
    }

    .mui-pull-top-wrapper .mui-icon,
    .mui-pull-top-wrapper .mui-spinner {
        margin-top: 7px;
    }

    .mui-pull-top-wrapper .mui-icon.mui-reverse {
        /*-webkit-transform: rotate(180deg) translateZ(0);*/
    }

    .mui-pull-bottom-tips {
        text-align: center;
        background-color: #efeff4;
        font-size: 15px;
        line-height: 40px;
        color: #777;
    }

    .mui-pull-top-canvas {
        overflow: hidden;
        background-color: #fafafa;
        border-radius: 40px;
        box-shadow: 0 4px 10px #bbb;
        width: 40px;
        height: 40px;
        margin: 0 auto;
    }

    .mui-pull-top-canvas canvas {
        width: 40px;
    }

    .mui-slider-indicator.mui-segmented-control {
        background-color: #efeff4;
    }
</style>
<%
    String skey = ParamUtil.get(request, "skey");
    int id = ParamUtil.getInt(request, "id", 0);
%>
<body>
<div class="mui-content">
    <div id="slider" class="mui-slider mui-fullscreen">
        <div id="sliderSegmentedControl"
             class="mui-scroll-wrapper mui-slider-indicator mui-segmented-control mui-segmented-control-inverted">
            <div class="mui-scroll" id="tabTitle">
                <a class="mui-control-item mui-active" href="#item1mobile">
                    查看信息
                </a>

            </div>
        </div>
        <div class="mui-slider-group" id="tabContent">
            <div id="item1mobile" class="mui-slider-item mui-control-content mui-active">
                <div id="scroll1" class="mui-scroll-wrapper">
                    <div class="mui-scroll" id="formDetailScroll">
                        <ul class="mui-table-view" id="formDetail">
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<link rel="stylesheet" href="../css/photoswipe.css">
<link rel="stylesheet" href="../css/photoswipe-default-skin/default-skin.css">
<script type="text/javascript" src="../js/photoswipe.js"></script>
<script type="text/javascript" src="../js/photoswipe-ui-default.js"></script>
<script type="text/javascript" src="../js/photoswipe-init-manual.js"></script>

<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="../js/newPopup.js"></script>
<script src="../js/macro/open_window_macro.js"></script>
<script src="../js/macro/macro.js"></script>
<script type="text/javascript" src="../js/mui.min.js"></script>
<script src="../js/mui.picker.min.js"></script>
<script type="text/javascript" src="../js/config.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.js"></script>
<script type="text/javascript"
        src="../js/mui.pullToRefresh.material.js"></script>

<script src="../js/visual/module_list.js"></script>
<script type="text/javascript" src="../js/base/mui.form.js"></script>
<script type="text/javascript" src="../js/visual/mui_module.js"></script>

<script src="../js/jq_mydialog.js"></script>
<script>
    function callJS() {
        return "";
    }

    mui.init();
    //阻尼系数
    var deceleration = mui.os.ios ? 0.003 : 0.0009;
    mui('.mui-scroll-wrapper').scroll({
        bounce: false,
        indicators: true, //是否显示滚动条
        deceleration: deceleration
    });
    mui.ready(function () {
        var skey = '<%=skey%>';
        var moduleCode = '<%=moduleCode%>';
        var id = <%=id%>;
        var options = {
            "skey": skey,
            "moduleCode": moduleCode,
            "id": id,
            "ulSelector": "#formDetail"
        };
        var content = document.querySelector('.mui-content');
        window.ModuleForm = new mui.ModuleForm(content, options);
        window.ModuleForm.moduleDetail();
    });

</script>

<script src="../flow/form_js/<%=formCode%>.jsp?pageType=showModule&id=<%=id%>"></script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>" />
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
