<%@ page language="java" pageEncoding="utf-8" %>
<%@ page import="com.redmoon.oa.android.Privilege" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();
    boolean isUniWebview = ParamUtil.getBoolean(request, "isUniWebview", false);
%>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>发起流程</title>
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../css/mui.indexedlist.css"/>
    <link rel="stylesheet" href="../css/my_dialog.css"/>
</head>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">发起流程</h1>
</header>
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
    var isUniWebview = <%=isUniWebview%>;
    // 注意只有这个页面不是用的if(!mui.os.plus || isUniWebview)，因为如果用了!，则在企业微信中，会被setTimeout将头部删掉
    // 而底部navbar.jsp中生成的头部同样也会被删掉
    if(mui.os.plus || isUniWebview) {
        $(function() {
            // 如果不延时，则画面只能显示一半高度
            setTimeout(function () {
                // 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
                $('.mui-bar').remove();
            }, 200);
        })
    }

    var skey = '<%=skey%>';
    mui.init({
        keyEventBind: {
            backbutton: !isUniWebview //关闭back按键监听
        }
    });

    mui.ajaxSettings.beforeSend = function (xhr, setting) {
        jQuery.myloading();
        //beforeSend演示,也可在$.ajax({beforeSend:function(){}})中设置单个Ajax的beforeSend
        //console.log('beforeSend:::' + JSON.stringify(setting));
    };
    //设置全局complete
    mui.ajaxSettings.complete = function (xhr, status) {
        //console.log('complete:::' + status);
        jQuery.myloading("hide");
    };

    var ajax_get = function (datas) {
        $.ajax({
            type: "post",
            url: "../../public/flow/initFlowTypeList.do",
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
                        $li += '<a href ="flow_dispose.jsp?type=' + type + '&flowTypeCode=' + code + '&' + encodeURI(params) + '&skey=' + skey + '&isUniWebview=' + isUniWebview + '">' + name + '</a></li>';
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
    };

    ajax_get({"skey":"<%=skey%>"});
</script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=pvg.getSkey()%>" />
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
