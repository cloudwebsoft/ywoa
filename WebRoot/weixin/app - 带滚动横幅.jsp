<%@page language="java" contentType="text/html;charset=utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.android.system.MobileAppIconConfigMgr" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();
    String userName = pvg.getUserName();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>首页</title>
    <meta name="viewport"
          content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <link rel="stylesheet" href="css/mui.css">
    <style>
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

        .app-icon {
            width:60px;
            height:60px;
        }
    </style>
    <script src="js/mui.min.js"></script>
    <script src="js/index.js"></script>
</head>
<body>
<div class="mui-content">
    <div id="slider" class="mui-slider">
        <div class="mui-slider-group mui-slider-loop">
            <!-- 额外增加的一个节点(循环轮播：第一个节点是最后一张轮播) -->
            <div class="mui-slider-item mui-slider-item-duplicate">
                <a href="#"> <img src="images/common3.png">
                </a>
            </div>
            <div class="mui-slider-item">
                <a href="#"> <img src="images/user_46.png">
                </a>
            </div>
            <div class="mui-slider-item">
                <a href="#"> <img src="images/common2.png">
                </a>
            </div>
            <div class="mui-slider-item">
                <a href="#"> <img src="images/common3.png">
                </a>
            </div>
            <!-- 额外增加的一个节点(循环轮播：最后一个节点是第一张轮播) -->
            <div class="mui-slider-item mui-slider-item-duplicate">
                <a href="#"> <img src="images/common1.png">
                </a>
            </div>
        </div>
        <div class="mui-slider-indicator mui-text-right">
            <div class="mui-indicator mui-active"></div>
            <div class="mui-indicator"></div>
            <div class="mui-indicator"></div>
        </div>
    </div>
    <div id="Gallery" class="mui-slider" style="margin-top: 0px;">
        <div class="mui-slider-group">
            <div class="mui-slider-item">
                <ul class="mui-table-view mui-grid-view mui-grid-9">
                    <%
                        MobileAppIconConfigMgr appMgr = new MobileAppIconConfigMgr();
                        JSONArray arr = appMgr.getAppIcons(request);
                        for (int i=0; i<arr.length(); i++) {
                            JSONObject json = arr.getJSONObject(i);
                            String mName = json.getString("mName");
                            String imgUrl = json.getString("imgUrl");
                            String code = json.getString("code");
                            int type = json.getInt("type");
                            %>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon app-icon"><img src="../<%=imgUrl%>"></span>
                            <div class="mui-media-body"><%=mName%></div>
                        </a>
                    </li>
                            <%
                        }
                    %>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon app-icon"><img src="../images/mobileAppIcons/notice.png"></span>
                            <div class="mui-media-body">个人中心</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon app-icon"><img src="../images/mobileAppIcons/notice.png"><span
                                class="mui-badge">5</span></span>
                            <div class="mui-media-body">通知</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-search"></span>
                            <div class="mui-media-body">查询</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-gear"></span>
                            <div class="mui-media-body">设置</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-info"></span>
                            <div class="mui-media-body">关于</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>

                </ul>
            </div>
            <div class="mui-slider-item">
                <ul class="mui-table-view mui-grid-view mui-grid-9">
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-person"></span>
                            <div class="mui-media-body">个人中心</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-chatbubble"><span
                                class="mui-badge">5</span></span>
                            <div class="mui-media-body">通知</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-search"></span>
                            <div class="mui-media-body">查询</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-gear"></span>
                            <div class="mui-media-body">设置</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-info"></span>
                            <div class="mui-media-body">关于</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>
                </ul>
            </div>
            <div class="mui-slider-item">
                <ul class="mui-table-view mui-grid-view mui-grid-9">
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-person"></span>
                            <div class="mui-media-body">个人中心</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-chatbubble"><span
                                class="mui-badge">5</span></span>
                            <div class="mui-media-body">通知</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-search"></span>
                            <div class="mui-media-body">查询</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-gear"></span>
                            <div class="mui-media-body">设置</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-info"></span>
                            <div class="mui-media-body">关于</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>
                    <li
                            class="mui-table-view-cell mui-media mui-col-xs-4 mui-col-sm-3">
                        <a href="#"> <span class="mui-icon mui-icon-more"></span>
                            <div class="mui-media-body">更多</div>
                        </a>
                    </li>
                </ul>
            </div>
        </div>
        <div class="mui-slider-indicator">
            <div class="mui-indicator mui-active"></div>
            <div class="mui-indicator"></div>
            <div class="mui-indicator"></div>
        </div>
    </div>
</div>
</body>
</html>
