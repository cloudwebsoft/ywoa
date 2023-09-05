<%@ page language="java" contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="com.redmoon.oa.android.Privilege" %>
<%@ page import="com.redmoon.oa.message.MessageDb" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%
    String skey = ParamUtil.get(request, "skey");
    Privilege pvgNav = new Privilege();
    if (!pvgNav.auth(request)) {
        out.print(StrUtil.p_center("请登录"));
        return;
    } else {
        skey = pvgNav.getSkey();
    }
    String userNameNav = pvgNav.getUserName(skey);
    MessageDb md = new MessageDb();
    int newMsgCount = md.getNewMsgCount(userNameNav);
    String tabId = ParamUtil.get(request, "tabId");
    boolean isBarBtnAddShow = ParamUtil.getBoolean(request, "isBarBtnAddShow", false);
    String barBtnAddUrl = ParamUtil.get(request, "barBtnAddUrl");
    boolean isBarBottomShow = ParamUtil.getBoolean(request, "isBarBottomShow", true);
    boolean isUniWebview = ParamUtil.getBoolean(request, "isUniWebview", false);
%>
<nav id="barBottom" class="mui-bar mui-bar-tab" style="display: <%=isBarBottomShow?"block":"none"%>;">
    <a id="tabmsg" class="mui-tab-item" href="weixin/message/msg_new_list.jsp?skey=<%=skey%>">
        <span class="mui-icon mui-icon-chatbubble">
            <%if (newMsgCount > 0) {%>
            <span class="mui-badge"><%=newMsgCount%></span>
            <%}%>
        </span>
        <span class="mui-tab-label">消息</span>
    </a>
    <a id="tabapp" class="mui-tab-item" href="weixin/app.jsp">
        <span class="mui-icon mui-icon-home"></span>
        <span class="mui-tab-label">应用</span>
    </a>
    <a id="tabme" class="mui-tab-item" href="weixin/me.jsp">
        <span class="mui-icon mui-icon-contact"></span>
        <span class="mui-tab-label">我</span>
    </a>
</nav>
<script>
    // 是否在原生app程序的webview中打开
    function isInWebview() {
        var ua = navigator.userAgent.toLowerCase();
        // console.log("ua=" + ua);
        if (ua.match(/MicroMessenger/i) == 'micromessenger') { // 微信浏览器判断
            return -1;
        } else if (ua.match(/QQ/i) == 'qq') { // QQ内嵌浏览器判断
            return 0;
        } else if (ua.match(/WeiBo/i) == "weibo") {
            return 0;
        } else if (ua.match(/ucbrowser/i) == "ucbrowser") {
            return 0; // UC浏览器
        } else if (ua.match(/dingtalk/i)) {
            return -1;
        } else {
            if (ua.match(/Android/i) != null) {
                if (ua.match(/Version/i)) { // app
                    return 1;
                } else {
                    return 0;
                }
            } else if (ua.match(/iPhone/i) != null) {
                return ua.match(/Version/i) == null ? 1 : 0;
            } else {
                return (ua.match(/macintosh/i) == null && ua.match(/windows/i) == null) ? 1 : 0;
            }
        }
    }

    $(function () {
        var r = isInWebview();
        // 如果是在app应用中，则不显示底部及头部
        if (r == 1) {
            $('#barBottom').hide();
            return;
        }
        <%
        if (isUniWebview) {
        %>
            $('#barBottom').hide();
            return;
        <%
        }
        %>

        // ------------------处理底部-------------------------
        <%--<%if (!isBarBottomShow) {%>
        $('#barBottom').hide();
        <%}%>--%>

        <%if (!"".equals(tabId)) {%>
        $('#tab<%=tabId%>').addClass('mui-active');
        <%}%>

        // 增大高度，使九宫格的下方靠底部的图标，当上滑可见时，不致于被底部选项卡遮挡
        $('.mui-content').height($('.mui-content').height() + 51);
        //底部选项卡点击事件
        mui('.mui-bar-tab').on('tap', 'a', function (e) {
            //显示目标选择卡
            window.location.href = "<%=request.getContextPath()%>/" + $(this).attr('href');
        });

        // -------------------处理顶部--------------------
        var isBarTopShow = true;
        // 如果是在微信中，显示底部，但不显示顶部
        if (r == -1) {
            // isBarTopShow = false;
        }
        var url = '<%=request.getRequestURL()%>';
        if (url.indexOf('app.jsp') != -1 || url.indexOf('msg_new_list.jsp') !=-1) {
            isBarTopShow = false;
        }
        if (isBarTopShow) {
            // 加载头部
            var barHeader = "";
            barHeader += '<header id="barTop" class="mui-bar mui-bar-nav">';
            barHeader += '  <a id="barBtnBack" class="mui-icon mui-icon-arrowleft mui-pull-left" style="color: #999;"></a>';
            barHeader += '  <a id="barBtnAdd" style="display:none; color: #999" class="mui-icon mui-icon-plusempty mui-pull-right"></a>';
            barHeader += '    <h1 class="mui-title">' + document.title + '</h1>';
            barHeader += '</header>';
            $('body').prepend(barHeader);

            // 如果有下拉区域则margin-top再 + 45
            var $pullrefresh = $('#pullrefresh');
            if ($pullrefresh) {
                $pullrefresh.css("margin-top", 60 + 45);
            }

            // 调整显示文件柜的目录
            $('.jqm-tree-dir').css("margin-top", 45);

            <%if (isBarBtnAddShow) {%>
            $('#barBtnAdd').show();
            <%}%>

            mui('#barTop').on('tap', 'a', function () {
                var btnId = this.getAttribute('id');
                if (btnId == "barBtnBack") {
                    window.history.back();
                } else if (btnId == "barBtnAdd") {
                    var url = "<%=barBtnAddUrl%>";
                    if (url.indexOf("?") != -1) {
                        url += "&skey=<%=skey%>";
                    } else {
                        url += "?skey=<%=skey%>";
                    }
                    window.location.href = url;
                }
            })
        }
    })
</script>