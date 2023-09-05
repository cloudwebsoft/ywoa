<%@page language="java" contentType="text/html;charset=utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.android.system.MobileAppIconConfigMgr" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="com.redmoon.oa.android.system.MobileAppIconConfigDb" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="cn.js.fan.web.Global" %>
<%
    Privilege pvg = new Privilege();
    boolean re = pvg.auth(request);
    if (!re) {
        out.print(pvg.getErrMsg());
        return;
    }
    String skey = pvg.getSkey();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title><%=Global.AppName%></title>
    <meta name="viewport"
          content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <link rel="stylesheet" href="css/mui.css">
    <script src="../fonts/mobile/iconfont.js"></script>
    <style>
        .icon {
            width: 1em;
            height: 1em;
            vertical-align: -0.15em;
            fill: currentColor;
            overflow: hidden;
            font-size: 42px;
        }

        .mui-plus.mui-android header.mui-bar {
            display: none;
        }

        .mui-plus.mui-android .mui-bar-nav ~ .mui-content {
            padding: 0;
        }

        .mui-slider-indicator {
            bottom: 0;
        }

        .mui-slider {
            background-color: #f2f2f2;
        }

        .app-icon img {
            width:60px;
            height:60px;
        }
    </style>
    <script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="js/mui.min.js"></script>
</head>
<body>
<div class="mui-content">
    <div id="Gallery" style="margin-top: 0px;">
        <ul class="mui-table-view mui-grid-view mui-grid-9">
            <%
                MobileAppIconConfigMgr appMgr = new MobileAppIconConfigMgr();
                JSONArray arr = appMgr.getAppIcons(request);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject json = arr.getJSONObject(i);
                    String mName = json.getString("mName");
                    String imgUrl = json.getString("imgUrl");
                    int mId = json.getInt("mId");
                    String icon = "";
                    if (json.has("icon")) {
                        icon = json.getString("icon");
                    }
                    String code = json.getString("code");
                    int type = json.getInt("type");
                    String url = "";
                    if (type== MobileAppIconConfigDb.TYPE_MODULE) {
                        url = request.getContextPath() + "/weixin/visual/module_list.jsp?moduleCode=" + code + "&skey=" + skey;;
                    }
                    else if (type==MobileAppIconConfigDb.TYPE_FLOW) {
                        url = request.getContextPath() + "/weixin/flow/flow_dispose.jsp?type=2&flowTypeCode=" + code + "&skey=" + skey;
                    }
                    else if (type==MobileAppIconConfigDb.TYPE_LINK) {
                        String link = code;
                        if (link.indexOf("http")!=0) {
                            if (link.contains("?")) {
                                url = request.getContextPath() + "/" + link + "&skey=" + skey;
                            } else {
                                url = request.getContextPath() + "/" + link + "?skey=" + skey;
                            }
                        }
                        else {
                            if (link.contains("?")) {
                                url = link + "&skey=" + skey;
                            } else {
                                url = link + "?skey=" + skey;
                            }
                        }
                    }
                    else if (type==MobileAppIconConfigDb.TYPE_MENU) {
                        if ("flow".equals(code)) {
                            url = request.getContextPath() + "/weixin/flow/flow_main.jsp?skey=" + skey;
                        }
                        else if ("flow_wait".equals(code)) {
                            url = request.getContextPath() + "/weixin/flow/flow_doing_or_return.jsp?skey=" + skey;
                        }
                        else if ("myflow".equals(code)) {
                            url = request.getContextPath() + "/weixin/flow/flow_attend.jsp?skey=" + skey;
                        }
                        else if ("flow_launch".equals(code)) {
                            url = request.getContextPath() + "/weixin/flow/flow_initiate.jsp?skey=" + skey;
                        }
                        else if (MobileAppIconConfigMgr.OA_TEAM == mId) {
                            url = request.getContextPath() + "/weixin/address/address_list.jsp?skey=" + skey;
                        }
                        else if (MobileAppIconConfigMgr.OA_FILECASE == mId) {
                            url = request.getContextPath() + "/weixin/fileark/dir.jsp?skey=" + skey;
                        }
                        else if (MobileAppIconConfigMgr.OA_FLOW == mId) {
                            url = request.getContextPath() + "/weixin/flow/flow_main.jsp?skey=" + skey;
                        }
                        else {
                            continue;
                        }
                    }
                    else {
                        continue;
                    }

                    boolean isBadge = false;
                    String liId = "";
                    if (url.contains("flow_doing_or_return")) {
                        liId = "liWaitCount";
                        isBadge = true;
                    }
                    else if (url.contains("msg_new_list")) {
                        liId = "liUnreadCount";
                        isBadge = true;
                    }
            %>
            <li class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                <a href="<%=url%>">
                    <span class="mui-icon app-icon">
                        <span id="<%=liId%>" class="mui-badge mui-badge-danger" style="display: <%=isBadge?"":"none"%>"></span>
                        <%--<svg class="icon svg-icon" aria-hidden="true">
                            <use id="useFontIcon" xlink:href="<%=icon%>"></use>
                        </svg>--%>
                        <img src="../<%=imgUrl%>">
                    </span>
                    <div class="mui-media-body"><%=mName%></div>
                </a>
            </li>
            <%
                }
            %>
        </ul>
    </div>
</div>
<jsp:include page="inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>" />
    <jsp:param name="tabId" value="app" />
</jsp:include>
<script>
    $.ajax({
        type: "post",
        url: "../public/android/getCounts.do",
        data: {
            skey: "<%=skey%>"
        },
        dataType: "html",
        beforeSend: function (XMLHttpRequest) {
        },
        success: function (data, status) {
            data = $.parseJSON(data);
            var waitCount = data.waitCount;
            var unReadCount = data.unReadCount;
            if (waitCount > 0) {
                $('#liWaitCount').html(waitCount);
            }
            else {
                $('#liWaitCount').hide();
            }
            if (unReadCount > 0) {
                $('#liUnreadCount').html(unReadCount);
            }
            else {
                $('#liUnreadCount').hide();
            }
        },
        complete: function (XMLHttpRequest, status) {
        },
        error: function (XMLHttpRequest, textStatus) {
            alert(XMLHttpRequest.responseText);
        }
    });
</script>
</body>
</html>
