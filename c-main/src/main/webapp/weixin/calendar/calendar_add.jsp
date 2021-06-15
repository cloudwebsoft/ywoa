<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.*" %>
<%@page import="cn.js.fan.util.*" %>
<%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();
    String userName = pvg.getUserName();

    String refer = ParamUtil.get(request, "refer");
%>
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no"/>
    <meta charset="utf-8">
    <meta name="format-detection" content="telephone=no,email=no,adress=no">
    <title>日程安排</title>
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../css/iconfont.css"/>
    <link rel="stylesheet" type="text/css" href="../css/mui.picker.min.css"/>
    <link href="../css/mui.indexedlist.css" rel="stylesheet"/>
    <link rel="stylesheet" href="../css/my_dialog.css"/>

    <style>
        .mui-input-row .input-icon {
            width: 50%;
            float: left;
        }

        .mui-input-row a {
            margin-right: 10px;
            float: right;
            text-align: left;
            line-height: 1.5;
        }

        .div_opinion {
            text-align: left;
        }

        .opinionContent {
            margin: 10px;
            width: 65%;
            float: right;
            font-weight: normal;
        }

        .opinionContent div {
            text-align: right;
        }

        .opinionContent div span {
            padding: 10px;
        }

        .opinionContent .content_h5 {
            color: #000;
            font-size: 17px;
        }

        #captureFile {
            display: none;
        }
    </style>

    <script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jq_mydialog.js"></script>
    <script type="text/javascript" src="../js/newPopup.js"></script>
    <script src="../js/macro/macro.js"></script>
    <script src="../js/mui.js"></script>
    <script src="../js/mui.picker.min.js"></script>
    <script type="text/javascript" src="../js/config.js"></script>
    <script type="text/javascript" src="../js/base/mui.form.js"></script>

    <script type="text/javascript" src="../js/visual/module_list.js"></script>
    <style>
        .mui-input-row label {
            color: #000;
        }
    </style>
</head>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">日程</h1>
</header>
<div class="mui-content">
    <form id="formAdd" class="mui-input-group">
        <div class="mui-input-row" data-code="title" data-isnull="false">
            <label><span>标题</span><span style='color:red;'>*</span></label>
            <input type="text" id="title" name="title" class="mui-input-clear"/>
        </div>
        <div class="mui-input-row" data-code="beginDate" data-isnull="false">
            <label><span>开始时间</span><span style='color:red;'>*</span></label>
            <input type="text" id="beginDate" name="beginDate" class="input-icon" value="<%=DateUtil.format(new Date(), "yyyy-MM-dd HH:mm") %>" readonly/>
            <a class="date_btn"><span class="iconfont icon-rili"></span></a>
        </div>
        <div class="mui-input-row">
            <label><span>结束时间</span></label>
            <input type="text" id="endDate" name="endDate" class="input-icon" readonly/>
            <a class="date_btn"><span class="iconfont icon-rili"></span></a>
        </div>
        <div class="mui-input-row mui-checkbox">
            <label><span>便笺</span></label>
            <input type="checkbox" id="isNotepaper" <%="notepaper".equals(refer) ? "checked" : "" %> name="isNotepaper" value="1" class="mui-input-clear"/>
        </div>
        <%
            com.redmoon.oa.pvg.Privilege oapvg = new com.redmoon.oa.pvg.Privilege();
            if (oapvg.isUserPrivValid(request, "plan.share")) {%>
        <div class="mui-input-row mui-checkbox">
            <label><span>共享</span></label>
            <input type="checkbox" id="shared" name="shared" value="1" class="mui-input-clear"/>
        </div>
        <%} %>
        <div class="mui-input-row mui-select">
            <label><span>提前提醒</span></label>
            <select name="before">
                <option value="0">请选择</option>
                <option value="10" selected>十分钟</option>
                <option value="20">二十分钟</option>
                <option value="30">三十分钟</option>
                <option value="45">四十五分钟</option>
                <option value="60">一小时</option>
                <option value="120">二小时</option>
                <option value="180">三小时</option>
                <option value="240">四小时</option>
                <option value="300">五小时</option>
                <option value="360">六小时</option>
                <option value="720">十二小时</option>
            </select>
        </div>
        <%if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {%>
        <div class="mui-input-row mui-checkbox">
            <label><span>便笺</span></label>
            <input type="checkbox" id="isToMobile" name="isToMobile" value="true" class="mui-input-clear"/>
        </div>
        <%} %>
        <div class="mui-input-row" data-code="content" data-isnull="false">
            <label><span>内容</span><span style='color:red;'>*</span></label>
            <div style="text-align:center">
                <textarea id="content" name="content" placeholder="请输入内容" style="height:150px;"></textarea>
            </div>
        </div>
    </form>
    <div class="mui-button-row">
        <a type="button" class="mui-btn mui-btn-primary btn-ok">确定</a>
    </div>
    <script type="text/javascript">
        if(!mui.os.plus) {
            // 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
            $('.mui-bar').remove();
        }

        $(function () {
            var data = formatDateTime(new Date);
            $('#mydate').val(data);

            // 日期选择控件
            $('.mui-input-group').on("tap", ".date_btn", function () {
                // var optionsJson = this.getAttribute('data-options') || '{}';
                // var options = JSON.parse(optionsJson);
                var options = this.getAttribute('data-options') || '{}';
                var id = this.getAttribute('id');
                var par = this.parentNode;
                var time_input = par.querySelector(".input-icon");
                var picker = new mui.DtPicker({}); //
                picker.show(function (rs) {
                    jQuery(time_input).val(rs.value);
                    picker.dispose();
                });
            });
        });

        $(function () {
            $('.btn-ok').click(function () {
                var _tips = "";
                jQuery("div[data-isnull='false']").each(function (i) {
                    var _code = jQuery(this).data("code");
                    var _val = jQuery("#" + _code).val();
                    if (_val == undefined || _val == "") {
                        var _text = jQuery(this).find("span:first").text();
                        _tips += _text + " 不能为空\n"
                    }
                });
                if (_tips != null && _tips != "") {
                    mui.toast(_tips);
                    return;
                }

                $.ajax({
                    type: "post",
                    url: "../../public/plan/addPlan.do",
                    data: $('#formAdd').serialize() + "&skey=<%=skey%>",
                    dataType: "html",
                    contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                    beforeSend: function (XMLHttpRequest) {
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        var btnArray = ['确定'];
                        if (data.ret == 1) {
                            mui.alert(data.msg, '提示', btnArray, function (e) {
                                <%
                                if ("notepaper".equals(refer)) {
                                %>
                                window.location.href = "notepaper_list.jsp";
                                <%}else{%>
                                window.location.href = "calendar.jsp";
                                <%}%>
                            })
                        } else {
                            mui.alert(data.msg, '提示', btnArray);
                        }
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        alert(XMLHttpRequest.responseText);
                    }
                });
            });
        });

        function callJS() {
            return {"btnAddShow": 0, "btnBackUrl": ""};
        }

        var iosCallJS = '{ "btnAddShow":0, "btnBackUrl":"" }';
    </script>
</div>

<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>"/>
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>