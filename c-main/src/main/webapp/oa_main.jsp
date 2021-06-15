<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserLogin(request)) {
        out.println(StrUtil.Alert_Redirect("您未登录或您的登录已过期，请重新登录！", "index.jsp"));
        return;
    }

    String skincode = UserSet.getSkin(request);
    if (skincode == null || skincode.equals("")) {
        skincode = UserSet.defaultSkin;
    }
    SkinMgr skm = new SkinMgr();
    Skin skin = skm.getSkin(skincode);
    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    String appName = cfg.get("enterprise");

    /*获取皮肤设置*/
    int topHeight = skin.getTopHeight(); // 76
    int leftWidth = 244; // skin.getLeftWidth(); // 214;
    int bottomHeight = skin.getBottomHeight();

    if (!privilege.isUserPrivValid(request, "admin") && privilege.isUserPrivValid(request, "menu.main.forbid")) {
        leftWidth = 0;
    }

    String mainTitle = ParamUtil.get(request, "mainTitle");
    String mainPage = ParamUtil.get(request, "mainPage");
    try {
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "mainTitle", mainTitle, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "mainPage", mainPage, getClass().getName());
    } catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
        return;
    }

    // clouddisk的页面public/clouddisk_login.jsp中会带参数跳转过来
    mainPage = mainPage.replaceAll("\\|", "&");
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title><%=appName%>
    </title>
    <link href="images/favicon.ico" rel="SHORTCUT ICON"/>
    <script src="inc/common.js"></script>
    <script>
        var topHeight = <%=topHeight%>;
        var leftWidth = <%=leftWidth%>;

        var allFrameRows = topHeight + ",*"
        var middleFrameCols = leftWidth + ",*";

        function init() {
            //初始化框架结构
            top.allFrame.rows = allFrameRows;
            top.middleFrame.cols = middleFrameCols;
        }

        function addTab(tabTitle, url, id) {
            if (typeof (mainFrame.addTab) == "function")
                mainFrame.setActiveTabTitle(tabTitle, url, id);
            else
                window.mainFrame.location.href = url;
        }

        function setActiveTabTitle(title) {
            if (typeof (mainFrame.setActiveTabTitle) == "function")
                mainFrame.setActiveTabTitle(title);
        }

        function hideLeft() {
            top.leftFrame.toggle();
        }

        function refreshWindow() {
            window.location.reload();
        }
    </script>
</head>
<frameset id="allFrame" rows="<%=topHeight%>,*,<%=bottomHeight%>" frameborder="0" framespacing="0" onload="init()">
    <noframes>
        <body>
        很抱歉，您使用的浏览器不支持框架功能，请转用新的浏览器。
        </body>
    </noframes>
    <frame name="topFrame" src="top.jsp" marginwidth="0" marginheight="0" scrolling="no" frameborder="0"/>
    <frameset id="middleFrame" cols="<%=leftWidth%>,*" frameborder="0" framespacing="0">
        <frame name="leftFrame" src="left_main.jsp" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize/>
        <frame id="mainFrame" name="mainFrame" src="tabpanel.jsp?mainTitle=<%=StrUtil.UrlEncode(mainTitle)%>&mainPage=<%=mainPage%>" marginwidth="0" marginheight="0" scrolling="yes" frameborder="0" noresize/>
    </frameset>
    <frame name="bottomFrame" src="bottom.jsp" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize/>
</frameset>
</html>
