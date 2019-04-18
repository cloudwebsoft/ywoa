<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>发起流程</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../css/mui.css">
    <link href="../css/mui.indexedlist.css" rel="stylesheet"/>
    <link rel="stylesheet" href="../css/my_dialog.css"/>

</head>
<body>
<%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();
%>
<div class="mui-content">
    <div id='list' class="mui-indexed-list">
        <div class="mui-indexed-list-search mui-input-row mui-search">
            <input type="search" class="mui-input-clear mui-indexed-list-search-input" placeholder="搜索流程名称">
        </div>
        <div class="mui-indexed-list-bar">
            <a>A</a>
            <a>B</a>
            <a>C</a>
            <a>D</a>
            <a>E</a>
            <a>F</a>
            <a>G</a>
            <a>H</a>
            <a>I</a>
            <a>J</a>
            <a>K</a>
            <a>L</a>
            <a>M</a>
            <a>N</a>
            <a>O</a>
            <a>P</a>
            <a>Q</a>
            <a>R</a>
            <a>S</a>
            <a>T</a>
            <a>U</a>
            <a>V</a>
            <a>W</a>
            <a>X</a>
            <a>Y</a>
            <a>Z</a>
        </div>

        <div class="mui-indexed-list-alert"></div>
        <div class="mui-indexed-list-inner">
            <div class="mui-indexed-list-empty-alert">没有数据</div>
            <ul class="mui-table-view">
            </ul>
        </div>
    </div>
</div>
<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="../js/mui.min.js"></script>
<script type="text/javascript" src="../js/mui.indexedlist.js"></script>
<script src="../js/jq_mydialog.js"></script>
<script type="text/javascript" charset="utf-8">
    var skey = '<%=skey%>';
    mui.init();
    mui.ajaxSettings.beforeSend = function (xhr, setting) {
        jQuery.myloading();
        //beforeSend演示,也可在$.ajax({beforeSend:function(){}})中设置单个Ajax的beforeSend
        //console.log('beforeSend:::' + JSON.stringify(setting));
    };
    //设置全局complete
    mui.ajaxSettings.complete = function (xhr, status) {
        //console.log('complete:::' + status);
        jQuery.myloading("hide");
    }
    var ajax_get = function (datas) {
        $.ajax({
            type: "post",
            url: "../do/flow_do.jsp",
            contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
            data: datas,
            dataType: "json",
            beforeSend: function(XMLHttpRequest){
            },
            success: function(data, status){
                var datas = data.datas;
                mui.each(datas, function (index, item) {
                    var isGroup = item.isGroup;
                    var type = item.type;
                    var pyName = item.pyName;
                    var name = item.name;
                    var code = item.code;
                    var params = item.params; // 带入的链接参数
                    if (isGroup) {
                        var $li_group = '<li data-group="' + pyName + '" class="mui-table-view-divider mui-indexed-list-group">' + name + '</li>';
                        jQuery(".mui-table-view").append($li_group);
                    } else {
                        var $li = '<li id="' + code + '" type="' + type + '"  data-tags="' + pyName + '" class="mui-table-view-cell mui-indexed-list-item">';
                        $li += '<a href ="flow_dispose.jsp?type=' + type + '&code=' + code + '&' + encodeURI(params) + '&skey=' + skey + '">' + name + '</a></li>';
                        jQuery(".mui-table-view").append($li);
                    }
                    mui.ready(function () {
                        var list = document.getElementById('list');
                        list.style.height = (document.body.offsetHeight) + 'px';
                        window.indexedList = new mui.IndexedList(list);
                    });
                });
            },
            complete: function(XMLHttpRequest, status){
            },
            error: function(XMLHttpRequest, textStatus){
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }
    ajax_get({"op": "flow_init_list", "userName": "<%=pvg.getUserName()%>"});

</script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=pvg.getSkey()%>" />
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
