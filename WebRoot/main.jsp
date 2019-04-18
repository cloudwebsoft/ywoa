<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "cn.js.fan.util.*" %>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.println(StrUtil.Alert_Redirect("您未登录或您的登录已过期，请重新登录！", "index.jsp"));
	return;
}

String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals("")) skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String appName = cfg.get("enterprise");

/*获取皮肤设置*/
int topHeight = skin.getTopHeight(); // 76
int menuHeight = skin.getMenuHeight(); // 32
int bottomHeight = skin.getBottomHeight(); // 34;
//int leftWidth = skin.getLeftWidth(); // 214;
int leftWidth = 0;

String mainTitle = ParamUtil.get(request, "mainTitle");
String mainPage = ParamUtil.get(request, "mainPage");
if (mainPage.equals("")) {
	mainPage = "desktop_frame.jsp";
}
FormPretend fp = FormPretend.getInstance();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=appName%></title>
<link href="images/favicon.ico" rel="SHORTCUT ICON" />
<script src="js/forPNG.js" language="javascript" type="text/javascript"></script>
<script>
	var topHeight = <%=topHeight%>;
	var menuHeight = <%=menuHeight%>;
	var bottomHeight = <%=bottomHeight%>;

	var leftWidth = <%=leftWidth%>;

	var allFrameRows = topHeight + "," + menuHeight + ",*," + bottomHeight;
	var middleFrameCols = leftWidth + ",*";
	
	var switchTopButtonPosRight = 50;
	
	function init() {
		/*初始化框架结构*/
		top.allFrame.rows = allFrameRows;
		top.middleFrame.cols = middleFrameCols;

		/*初始化menuFrame中的伸缩按钮位置*/
		// top.menuFrame.document.getElementById('switchLeftButton').style.posTop = (menuHeight - top.menuFrame.document.getElementById('switchLeftButton').clientHeight)/2;
		// top.menuFrame.document.getElementById('switchLeftButton').style.posLeft = top.leftFrame.document.getElementById('left').clientWidth + 16;//滚动条宽16px	
		// top.menuFrame.document.getElementById('switchTopButton').style.posTop = (menuHeight - top.menuFrame.document.getElementById('switchTopButton').clientHeight)/2;
		// top.menuFrame.document.getElementById('switchTopButton').style.posRight = switchTopButtonPosRight;
		/*初始化menuFrame中的菜单位置*/
		// top.menuFrame.document.getElementById('goOrReturn').style.width = top.leftFrame.document.getElementById('left').clientWidth + 16;
		// top.menuFrame.document.getElementById('menuContent').style.posTop = 0;
		// top.menuFrame.document.getElementById('menuContent').style.posLeft = top.menuFrame.document.getElementById('switchLeftButton').style.posLeft + 35;
		/*初始化bottomFrame中的菜单位置*/
		// top.bottomFrame.document.getElementById('bottomL').style.width = top.leftFrame.document.getElementById('left').clientWidth + 16;//滚动条宽16px
		// top.bottomFrame.document.getElementById('bottomR').style.width = top.menuFrame.document.documentElement.clientWidth - top.bottomFrame.document.getElementById('bottomL').clientWidth;
	}
	
	function addTab(tabTitle, url, id){
		if (typeof(mainFrame.addTab)=="function")
			mainFrame.addTab(tabTitle, url, id);
		else
			window.mainFrame.location.href = url;
	}
	
	function setActiveTabTitle(title){
		if (typeof(mainFrame.setActiveTabTitle)=="function")
			mainFrame.setActiveTabTitle(title);
	}
	
	function refreshWindow(){
		window.location.reload();
	}	
</script>
</head>
<frameset id="allFrame" rows="<%=topHeight%>,<%=menuHeight%>,*,<%=bottomHeight%>" frameborder="0" framespacing="0" onload="init()"> 
	<noframes>
		<body>
			很抱歉，您使用的浏览器不支持框架功能，请转用新的浏览器。
		</body>
	</noframes>
	<frame name="topFrame" src="top.jsp?uiMode=<%=UserSetupDb.UI_MODE_FASHION%>" marginwidth="0" marginheight="0" scrolling="no" frameborder="0"  />
	<frame name="menuFrame" src="main_menu.jsp" marginwidth="0" marginheight="0" scrolling="no" frameborder="0"  />
	<frameset id="middleFrame" cols="<%=leftWidth%>,*" frameborder="0" framespacing="0">
		<frame id="leftFrame" name="leftFrame" src="UntitledFrame-1" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize/>
		<frame id="mainFrame" name="mainFrame" src="tabpanel.jsp?mainTitle=<%=StrUtil.UrlEncode(mainTitle)%>&mainPage=<%=mainPage%>" marginwidth="0" marginheight="0" scrolling="yes" frameborder="0" noresize />
	</frameset>
	<frame name="bottomFrame" src="main_bottom.jsp" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize />
</frameset>
</html>
