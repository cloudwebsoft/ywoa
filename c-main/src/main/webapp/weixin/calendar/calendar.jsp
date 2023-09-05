<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();

    boolean isShared = ParamUtil.getBoolean(request, "isShared", false);
    String docTitle = "日程安排";
    if (isShared) {
        docTitle = "共享日程";
    }
    boolean isUniWebview = ParamUtil.getBoolean(request, "isUniWebview", false);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title><%=docTitle %></title>
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no"/>
    <meta name="format-detection" content="telephone=no,email=no,adress=no">
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="css/reset.css"/>
    <link rel="stylesheet" type="text/css" href="css/simple-calendar.css">
    <link rel="stylesheet" href="css/calendar.css"/>
</head>
<body>
<header class="mui-bar mui-bar-nav">
	<a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <a class="mui-icon mui-icon-plusempty mui-pull-right mui-a-color"></a>
    <h1 class="mui-title"><%=docTitle%></h1>
</header>
<div class="mui-content inner">
    <div id='calendar' class="sc-calendar">
        <div class="sc-header">
            <div class="sc-title">
                <div class="year">&nbsp;<span class="sc-select-year" name=""></span>年</div>
                <div class="month">
                    <div class="arrow sc-mleft"></div>
                    <div class="monthdiv">
                        <span class="sc-select-month" name=""></span>
                    </div>
                    <div class="arrow sc-mright"></div>
                </div>
            </div>
            <div class="sc-week"></div>
        </div>
        <div class="sc-body">
            <div class="sc-days"></div>
        </div>
    </div>
    <div class="announcement">
        <ul class="matter">
        </ul>
    </div>
</div>
<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="../js/mui.min.js"></script>
<script type="text/javascript" src="js/simple-calendar.js"></script>
<script type="text/javascript" src="js/hammer-2.0.8-min.js"></script>
<script type="text/javascript">
    var isUniWebview = <%=isUniWebview%>;

    if(!mui.os.plus || isUniWebview) {
		// 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
		$('.mui-bar').remove();
	}

    mui.init({
        keyEventBind: {
            backbutton: !isUniWebview //关闭back按键监听
        }
    });

    var myCalendar = new SimpleCalendar('#calendar');
    $(function () {
        var year = $('.sc-select-year').text();
        var monthCH = $('.sc-select-month').text();
        var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH) + 1;
        loadMark(year, month);

        $(".sc-mleft").click(function () {
            myCalendar.subMonth();
            var year = $('.sc-select-year').text();
            var monthCH = $('.sc-select-month').text();
            var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH) + 1;

            loadMark(year, month);
        })
        $(".sc-mright").click(function () {
            myCalendar.addMonth();
            var year = $('.sc-select-year').text();
            var monthCH = $('.sc-select-month').text();
            var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH) + 1;

            loadMark(year, month);
        })
    });

    // 滑动切换
    var myElement = document.getElementById('calendar');
    var hammer = new Hammer(myElement);
    hammer.on("swipeleft", function (ev) {
        myCalendar.addMonth();

        var year = $('.sc-select-year').text();
        var monthCH = $('.sc-select-month').text();
        var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH) + 1;
        loadMark(year, month);
        console.log("month=" + month);
    });
    hammer.on("swiperight", function (ev) {
        myCalendar.subMonth();
        var year = $('.sc-select-year').text();
        var monthCH = $('.sc-select-month').text();
        var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH) + 1;
        loadMark(year, month);
        console.log("month=" + month);
    });

    var mark;

    function loadMark(y, m) {
        $.ajax({
            url: "../../public/plan/getPlans.do",
            type: "post",
            data: {
                skey: "<%=skey%>",
                year: y,
                month: m,
                isShared: <%=isShared%>
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                mark = data.result;

                myCalendar._defaultOptions.mark = mark;
                // console.log(mark);

                myCalendar.update(m, y);

                // 显示当天的活动在初始化mark之后
                // 初始化今天的活动
                announceList($('.sc-today'));
            },
            complete: function (XMLHttpRequest, status) {
            },
            error: function (XMLHttpRequest, textStatus) {
            }
        });

    }

    // 有标记的日期点击事件
    $('#calendar').on("click", '.sc-selected', function () {
        announceList($(this));
    });

    // 显示选择日期当天的活动
    function announceList(v) {
        if (v.children().hasClass('sc-mark-show')) {
            var year = $('.sc-select-year').text();
            var monthCH = $('.sc-select-month').text();
            var day = v.children()[1].innerText;
            var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH) + 1;
            var date = year + '-' + month + '-' + day;
            var content = mark[date];
            if (content == null) {
                content = "";
            }
            var matterHtml = '';
            for (var i = 0; i < content.length; i++) {
                var id = content[i].id;
                var isClosed = "true" == content[i].isClosed;
                var imgPath = "";
                if (isClosed) {
                    imgPath = "../../images/task_complete.png";
                } else {
                    imgPath = "../../images/task_ongoing.png";
                }
                matterHtml += '<li class="announceItem" onclick="show(' + id + ')"><div><div class="fl announceImg">'
                    + '<img src="' + imgPath + '"></div>'
                    + '<p class="announceContent">' + content[i].title + '</p>'
                    + '</div><div class="announceTime">' + content[i].startTime + ' - ' + content[i].endTime + '</div></li>';
            }
            $('.matter').html(matterHtml);
        } else {
            var matterHtml = '';
            matterHtml += '<li class="announceItem"><div><p class="announceContent">当前日期暂无活动</p></div></li>';
            $('.matter').html(matterHtml);
        }
    }

    function show(id) {
        window.location.href = "calendar_show.jsp?id=" + id;
    }

    var btnAddShow = 1;
    <%if (isShared) {%>
    btnAddShow = 0;
    var iosCallJS = '{ "btnAddShow":0, "btnAddUrl":"weixin/calendar/calendar_add.jsp" }';
    <%}else{%>
    var iosCallJS = '{ "btnAddShow":1, "btnAddUrl":"weixin/calendar/calendar_add.jsp" }';
    <%}%>

    function callJS() {
        return {"btnAddShow": btnAddShow, "btnAddUrl": "weixin/calendar/calendar_add.jsp"};
    }

    if(mui.os.plus) {
        mui('.mui-bar').on("tap", '.mui-icon-plusempty', function (e) {
            mui.openWindow({
                "url": "calendar_add.jsp?skey=<%=skey%>",
                "id": "noticeAddWin"
            });
        });
    }
</script>

<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>"/>
    <jsp:param name="isBarBtnAddShow" value="<%=!isShared%>"/>
    <jsp:param name="barBtnAddUrl" value="calendar_add.jsp"/>
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>