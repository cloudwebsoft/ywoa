<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.ui.menu.*" %>
<%@ page import="org.json.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <%
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        String appName = cfg.get("enterprise");
    %>
    <title><%=appName%>
    </title>
    <script type="text/javascript" src="inc/common.js"></script>
    <script type="text/javascript" src="desktop/lib/js/jquery/jquery-1.8.2.min.js"></script>
    <script type="text/javascript" src="desktop/lib/js/mydesktop/myDesktopBase.js"></script>
    <%
        JSONArray jsonAry = new JSONArray();
        JSONObject desktopNameObj = new JSONObject();
        if (privilege.isUserLogin(request)) {
            SlideMenuGroupDb smgd = new SlideMenuGroupDb();
            SlideMenuDb smd = new SlideMenuDb();
            String sql = "select id from " + smgd.getTable().getName() + " where user_name=? order by orders";
            String sql2 = "select id from " + smd.getTable().getName() + " where group_id=?";
            com.redmoon.oa.ui.menu.Leaf lf = new com.redmoon.oa.ui.menu.Leaf();

            String userName = privilege.getUser(request);

            Iterator ir = smgd.list(sql, new Object[]{userName}).iterator();
            int k = 1;
            while (ir.hasNext()) {
                smgd = (SlideMenuGroupDb) ir.next();

                JSONArray ary = new JSONArray();
                Iterator ir2 = smd.list(sql2, new Object[]{new Long(smgd.getLong("id"))}).iterator();
                while (ir2.hasNext()) {
                    smd = (SlideMenuDb) ir2.next();
                    lf = lf.getLeaf(smd.getString("code"));
                    if (lf == null) {
                        lf = new Leaf();
                        continue;
                    }

                    if (!lf.canUserSee(request))
                        continue;

                    JSONObject json = new JSONObject();
                    String link = lf.getLink(request);
                    json.put("iconSrc", "images/bigicons/" + lf.getBigIcon());
                    json.put("windowsId", smd.getLong("id"));
                    json.put("windowTitle", lf.getName());
                    json.put("iframSrc", link);
                    int count = lf.getCount(userName);
                    if (count > 0) {
                        json.put("txNum", count);
                    }


                    if (lf.isWidget()) {
                        json.put("windowWidth", lf.getWidgetWidth());
                        json.put("windowHeight", lf.getWidgetHeight());
                        json.put("top", 0);
                        json.put("left", "auto");
                        json.put("right", 210);
                        json.put("isWidget", true);
                    }

                    ary.put(json);
                }

                JSONObject obj = new JSONObject();
                // String arystr = ary.toString();
                // arystr = arystr.substring(1, arystr.length() - 1);
                // obj.put("" + smgd.getLong("id"), ary);
                obj.put("desktop" + k, ary);
                desktopNameObj.put("desktop" + k + "_name", smgd.getString("name"));
                jsonAry.put(obj);

                k++;

            }
        }
        String iconData = jsonAry.toString();
        String iconDataJSONStr = iconData;
        iconData = iconData.substring(1, iconData.length() - 1);
        iconData = iconData.replaceAll("]\\},\\{", "],");
        if (iconData.equals(""))
            iconData = "{\"desktop1\":[]}";

        UserSetupDb usd = new UserSetupDb();
    %>
    <script>
        $(window).load(function () {
            //停止进度条
            myDesktop.stopProgress();

            //桌面应用json数据
            var iconData =<%=iconData%>;

            //禁止选择文本内容
            myDesktop.disableSelect();

            //初始化桌面背景
            <%if (!privilege.isUserLogin(request)) {%>
            myDesktop.wallpaper.init("desktop/theme/default/images/wallpaper.jpg", 1);
            <%
          }
          else {
          String wallpaperPath = UserSetupMgr.getWallpaperPath(privilege.getUser(request));
          %>
            // 1拉伸，2居中不拉伸，3作为背景平铺
            myDesktop.wallpaper.init("<%=wallpaperPath%>", 1);
            <%}%>
            //初始化桌面
            myDesktop.desktop.init(iconData, {
                    arrangeType: 2, // 横排
                    iconMarginLeft: 80,
                    iconMarginTop: 60,
                    desktopNames:<%=desktopNameObj%>
                }
            );

            //初始化任务栏
            myDesktop.taskBar.init();

            //初始化顶部栏
            myDesktop.topBar.init();

            //初始化全局视图
            myDesktop.appManagerPanel.init();

            //初始化桌面右键菜单
            var data = [
                [{
                    text: "显示桌面",
                    func: function () {
                        $("div.myWindow").not(".hideWin")
                            .each(function () {
                                $(this).find(".winMinBtn").trigger("click");
                            });
                    }
                }]
                , [{
                    text: "内部消息",
                    func: function () {
                        myDesktop.myWindow.init({
                            'iconSrc': 'desktop/icon/default.png',
                            'windowsId': 'message',
                            'windowTitle': '内部消息',
                            'iframSrc': 'message_oa/message_frame.jsp',
                            'windowWidth': 800,
                            'windowHeight': 530,
                            'parentPanel': ".currDesktop"
                        });

                        //添加到状态栏
                        if (!$("#taskTab_skins").size()) {
                            myDesktop.taskBar.addTask("message", "内部消息", "desktop/icon/default.png");
                        }
                    }
                }, {
                    text: "主题设置",
                    func: function () {
                        myDesktop.myWindow.init({
                            'iconSrc': 'desktop/icon/default.png',
                            'windowsId': 'skins',
                            'windowTitle': '主题设置',
                            'iframSrc': 'admin/slide_menu_group.jsp',
                            'windowWidth': 800,
                            'windowHeight': 530,
                            'parentPanel': ".currDesktop"
                        });

                        //添加到状态栏
                        if (!$("#taskTab_skins").size()) {
                            myDesktop.taskBar.addTask("skins", "主题设置", "desktop/icon/default.png");
                        }
                    }
                }]
                , [{
                    text: "退出系统",
                    func: function () {
                        if (confirm("您确定要退出么？"))
                            window.location.href = "exit_oa.jsp";
                    }
                }]
                , [{
                    text: "重置插件",
                    func: function () {
                        <%
                        if (privilege.isUserLogin(request)) {
                            usd = usd.getUserSetupDb(privilege.getUser(request));
                            if (!usd.getMydesktopProp().equals("")) {
                                JSONObject json = new JSONObject(usd.getMydesktopProp());
                                JSONArray ary = null;
                                try {
                                    ary = (JSONArray)json.get("widgets");
                                }
                                catch (Exception e) {
                                }
                                for (int i=0; i<ary.length(); i++) {
                                    JSONObject wjson = (JSONObject)ary.get(i);
                                    %>
                        showWidget('<%=wjson.get("id")%>', 'auto', 0);
                        myDesktop.widget.postWidgetStatus("moveWidget", '<%=wjson.get("id")%>', 'auto', 0)
                        <%
                    }
                }
            }
            %>
                    }
                }]
                , [{
                    text: "刷新",
                    func: function () {
                        // location.href="mydesktop.jsp";
                        window.location.reload();
                    }
                }]
            ];
            myDesktop.contextMenu($(document.body), data, "body", 10);

            // 初始化时钟、天气预报、标签
            // $("#shizhong,#weather,#memo").trigger("click");

            <%
            if (privilege.isUserLogin(request)) {
                usd = usd.getUserSetupDb(privilege.getUser(request));
                if (!usd.getMydesktopProp().equals("")) {
                    JSONObject json = new JSONObject(usd.getMydesktopProp());
                    JSONArray ary = null;
                    try {
                        ary = (JSONArray)json.get("widgets");
                    }
                    catch (Exception e) {
                    }
                    for (int i=0; i<ary.length(); i++) {
                        JSONObject wjson = (JSONObject)ary.get(i);
                        %>
            if ('<%=wjson.get("left")%>' == 'auto')
                showWidget('<%=wjson.get("id")%>', '<%=wjson.get("left")%>', <%=wjson.get("top")%>);
            else
                showWidget('<%=wjson.get("id")%>', <%=wjson.get("left")%>, <%=wjson.get("top")%>);
            <%
        }
    }
}
%>
        });

        var widgetCount = 1;

        function showWidget(wName, wleft, wtop, wright) {
            var left = 'auto';
            var top = 0;
            var right = 210;
            if (wleft)
                left = wleft;
            if (wtop)
                top = wtop;
            if (wright)
                right = wright;
            var data = {
                "note": {
                    'id': 'note',
                    'width': 162,
                    'height': 200,
                    'title': '便签',
                    isDrag: true,
                    'iframeSrc': 'plan/notepaper_mydesktop.jsp',
                    'parentTo': ".desktop:first",
                    'top': top,
                    'left': left,
                    'right': right
                }
                , "weather": {
                    'id': 'weather',
                    'width': 200,
                    'height': 320,
                    'title': '天气',
                    isDrag: true,
                    'iframeSrc': 'desktop/app_tools/weather/index.html',
                    'parentTo': ".desktop:first",
                    'top': top,
                    'left': left,
                    'right': right
                }
                , "clock": {
                    'id': 'clock',
                    'width': 160,
                    'height': 210,
                    'title': '时钟',
                    isDrag: true,
                    'iframeSrc': 'desktop/app_tools/shizhong/index.html',
                    'parentTo': ".desktop:first",
                    'top': top,
                    'left': left,
                    'right': right
                }

            }

            if (wName == "weather" || wName == "clock" || wName == "note") {
                myDesktop.widget.init(eval("data." + wName));
            } else {
                var jsonAry = $.parseJSON('<%=iconDataJSONStr%>');

                for (i in jsonAry) {
                    var json = jsonAry[i];
                    for (j in json) {
                        var jsn = eval("json." + j);
                        for (k in jsn) {
                            var jsonObj = jsn[k];
                            if (jsonObj.windowsId == wName) {
                                var wdata = {'id': wName, 'width': jsonObj.windowWidth, 'height': jsonObj.windowHeight, 'title': jsonObj.windowTitle, isDrag: true, 'iframeSrc': jsonObj.iframSrc, 'parentTo': '.desktop:first', 'top': top, 'left': left, 'right': right};
                                // alert(JSON.stringify(wdata));
                                myDesktop.widget.init(wdata);
                            }
                        }
                    }
                }
            }

            widgetCount++;
        }

        $(document).ready(function () {
            // 登录框
            $("#login").click(function () {
                myDesktop.login.init("desktop/login.jsp");
            });

            $("#exit").click(function () {
                if (confirm("您确定要退出么？"))
                    window.location.href = "exit_oa.jsp";
            });

            <%if (!privilege.isUserLogin(request)) {%>
            $("#login").trigger("click");
            <%}%>

            $("#home").click(function () {
                showWidget('weather');
            });

            $("#memo").click(function () {
                showWidget('note');
            });

            $('#control').click(function () {
                addTab('控制面板', 'user/control_panel.jsp');
            });

            $('#message').click(function () {
                addTab('内部消息', 'message_oa/message_frame.jsp');
            });

            $('#plan').click(function () {
                showWidget('clock');
                // addTab('日程安排', 'plan/plan_month.jsp');
            });

            $('#flowQuery').click(function () {
                addTab('流程查询', 'flow_query_frame.jsp');
            });
        });

    </script>
    <script src='dwr/interface/MessageDb.js'></script>
    <script src='dwr/engine.js'></script>
    <script src='dwr/util.js'></script>
    <script>
        function handler(msg) {
            // alert("您与服务器的连接已断开，请刷新页面尝试重新连接！");
        }

        <%if (privilege.isUserLogin(request)) {%>
        DWREngine.setErrorHandler(handler);
        DWREngine.setTimeout(2000);
        <%}%>

        <%
        // 取刷新时间
        String refresh_message = cfg.get("refresh_message");
        String refreshOnlineUserNotify = cfg.get("refreshOnlineUserNotify"); // 用户每隔多长时间，向服务器通报一次在线（秒）
        %>

        var refresh_message = <%=refresh_message%>;

        var userName = "<%=privilege.getUser(request)%>";

        function getNewMsg(userName) {
            try {
                MessageDb.getNewMsgsOfUser(showMsgWin, "<%=privilege.getUser(request)%>");
            } catch (e) {
                alert(e);
            }
        }

        function refreshMsg() {
            getNewMsg(userName);
            timeoutid = window.setTimeout("refreshMsg()", refresh_message * 1000); // 每隔N秒钟刷新一次
        }

        $(document).ready(function () {
            <%if (privilege.isUserLogin(request) && usd.isMsgWinPopup()) {%>
            refreshMsg();
            <%}%>

            <%
            out.print(OnlineUserMgr.getJSOrganization());
            %>

            <%
            com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
            if (privilege.isUserLogin(request) && ssoCfg.getBooleanProperty("isUse")) {
                if ("true".equals(cfg.get("isLarkUsed"))) {
                    if ("Lark".equals(cfg.get("larkType"))) {
                        String sparkServer = ssoCfg.get("sparkServer");
                        UserDb user = new UserDb();
                        user = user.getUserDb(privilege.getUser(request));
                    %>
            try {
                if (typeof (o("webedit").LaunchLark) != "undefined")
                    o("webedit").LaunchLark("<%=privilege.getUser(request)%>", "<%=user.getPwdRaw()%>", "<%=sparkServer%>");
                else
                    alert(typeof (o("webedit").LaunchLark));
            } catch (e) {
            }
            <%}	else {
                String sparkServer = ssoCfg.get("sparkServer");
                UserDb user = new UserDb();
                user = user.getUserDb(privilege.getUser(request));
            %>
            try {
                if (typeof (o("webedit").LaunchSpark) != "undefined")
                    o("webedit").LaunchSpark("<%=privilege.getUser(request)%>", "<%=user.getPwdRaw()%>", "<%=sparkServer%>");
            } catch (e) {
            }
            <%
            }
        }
    }
    %>
        });

        function showMsgWin(msg) {
            if (msg.length > 0) {
                var i = 0;

                var info = "";
                for (var data in msg) {
                    if (info == "")
                        info += "{'id':'" + msg[data].id + "', 'info':'" + msg[data].title + "'}";
                    else
                        info += ",{'id':'" + msg[data].id + "', 'info':'" + msg[data].title + "'}";

                    // 最多取5条
                    i++;
                    if (i >= 5)
                        break;
                }
                info = "[" + info + "]";

                //初始化消息框
                var infoData = eval(info);

                myDesktop.infoBar.init(infoData);
            } else
                $('#infoBlock').hide();
        }
    </script>
</head>
<body>
<div id="wallpaper"></div>
<div id="desktopWrapper">
    <div id="topBar">
        <div class="leftBar">
            <a href="#" id="home"><img src="desktop/theme/default/images/home.png" alt="" title="天气预报"/></a>
            <a href="#" id="login"><img src="desktop/theme/default/images/key.png" alt="" title="登录"/></a>
            <a href="#" id="memo"><img src="desktop/theme/default/images/print.png" alt="" title="便笺"/></a>
            <a href="#" id="message"><img src="desktop/theme/default/images/message.png" alt="" title="内部消息"/></a>
        </div>
        <div class="rightBar">
            <a href="#" id="flowQuery"><img src="desktop/theme/default/images/sousuo.png" alt="" title="流程查询"/></a>
            <a href="#" id="plan"><img src="desktop/theme/default/images/rili.png" alt="" title="时钟"/></a>
            <a href="#" id="control"><img src="desktop/theme/default/images/sys.png" alt="" title="控制面板"/></a>
            <a href="#" id="exit"><img src="desktop/theme/default/images/files.png" alt="" title="退出"/></a>
        </div>
    </div>

    <div id="desktopsContainer">
        <div id="desktopContainer"></div>
    </div>

    <div id="bottomBarBgTask"></div>

    <div id="taskBlock">
        <div class="taskNextBox" id="taskNextBox"><a href="javascript:void(0);" class="taskNext" id="taskNext"></a></div>
        <div id="taskOuterBlock">
            <div id="taskInnnerBlock"></div>
        </div>
        <div id="taskPreBox" class="taskPreBox"><a href="javascript:void(0);" id="taskPre" class="taskPre"></a></div>
    </div>

    <div id="infoBlock">
        <div class="infoC">
            <span class="icon group"></span>
            <div class="infoList"></div>
            <span class="count" id="zcount">(0)</span>
            <span title="消息管理器" class="icon setting" id="messageBubble"></span>
        </div>
        <div id="messageBubble_bubbleMsgList">
            <h3>未读消息(<span class="count" id="messageBubble_bubbleMsgList_userCount">1</span>)</h3>
            <div class="bubbleMsgListInner" id="infoWrap">
                <div class="bubbleMsgListContainer">
                    <ul id="messageBubble_bubbleMsgList_ul"></ul>
                </div>
            </div>
        </div>
    </div>
    <div id="navBar"><s class="l">
        <div class="indicator indicator_header" cmd="user">
            <%
                if (privilege.isUserLogin(request)) {
                    UserDb user = new UserDb();
                    user = user.getUserDb(privilege.getUser(request));
                    String photoUrl = "";
                    if (!"".equals(user.getPhoto())) {
                        photoUrl = request.getContextPath() + "/img_show.jsp?path=" + user.getPhoto();
                    } else {
                        if (user.getGender() == 0) {
                            photoUrl = request.getContextPath() + "/images/man.png";
                        } else {
                            photoUrl = request.getContextPath() + "/images/woman.png";
                        }
            %>
            <img alt="控制面板" title="<%=user.getRealName()%> 控制面板" class="indicator_header_img" id="navbarHeaderImg" src="<%=photoUrl%>">
            <%
                    }
                }
            %>
        </div>
    </s><span></span><s class="r"><a class="indicator indicator_manage" href="javascript:void(0);" hidefocus="true" cmd="manage" title="全局视图"></a></s></div>

</div>

<div id="appManagerPanel" class="appManagerPanel">
    <a class="aMg_close" href="javascript:void(0);"></a>

    <div class="aMg_dock_container"></div>
    <div class="aMg_line_x"></div>

    <div class="aMg_folder_container">
        <div class="aMg_folder_innercontainer"></div>
    </div>
    <a href="javascript:void(0);" id="aMg_prev"></a>
    <a href="javascript:void(0);" id="aMg_next"></a>
</div>

<object classid="CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C" codebase="activex/cloudym.CAB#version=1,2,0,1" width=400 style="height:75px; display:none" align="middle" id="webedit">
    <param name="Encode" value="utf-8">
    <param name="MaxSize" value="<%=Global.MaxSize%>">
    <!--上传字节-->
    <param name="ForeColor" value="(200,200,200)">
    <param name="BgColor" value="(255,255,255)">
    <param name="ForeColorBar" value="(255,255,255)">
    <param name="BgColorBar" value="(104,181,200)">
    <param name="ForeColorBarPre" value="(0,0,0)">
    <param name="BgColorBarPre" value="(230,230,230)">
    <param name="FilePath" value="">
    <param name="Relative" value="1">
    <!--上传后的文件需放在服务器上的路径-->
    <param name="Server" value="">
    <param name="Port" value="">
    <param name="VirtualPath" value="">
    <param name="PostScript" value="">
    <param name="PostScriptDdxc" value="">
    <param name="SegmentLen" value="204800">
    <param name="info" value="文件拖放区">
    <%
        License license = License.getInstance();
    %>
    <param name="Organization" value="<%=license.getCompany()%>">
    <param name="Key" value="<%=license.getKey()%>">
</object>

</body>
<%
    String mainTitle = ParamUtil.get(request, "mainTitle");
    String mainPage = ParamUtil.get(request, "mainPage");
    if (!mainPage.equals("")) {
%>
<script>
    $(window).load(function () {
        addTab("<%=mainTitle%>", "<%=mainPage%>");
    });
</script>
<%
    }
%>
<script>
    function ShowMsg(msg) {
        // alert("Lark 未安装：" + msg);
    }
</script>
</html>