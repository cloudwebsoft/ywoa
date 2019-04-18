<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.ui.menu.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	/*获取皮肤设置*/
	String skincode = UserSet.getSkin(request);
	if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
	SkinMgr skm = new SkinMgr();
	Skin skin = skm.getSkin(skincode);
	String skinPath = skin.getPath();
	
    int topHeight = skin.getTopHeight(); // 76
    int menuHeight = skin.getMenuHeight(); // 32
    int bottomHeight = skin.getBottomHeight(); // 34;
    int leftWidth = skin.getLeftWidth(); // 214;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>menu</title>
<script src="js/jquery.js"></script>
<script type="text/javascript" src="js/jquery.easing.min.js"></script>
<script type="text/javascript" src="js/jquery.lavalamp.min.js"></script>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" href="<%=skinPath%>/lavalamp.css" type="text/css" media="screen">
<script>
	var topHeight = <%=topHeight%>;
	var menuHeight = <%=menuHeight%>;
	var bottomHeight = <%=bottomHeight%>;

	var leftWidth = <%=leftWidth%>;

	var allFrameRows = topHeight + "," + menuHeight + ",*," + bottomHeight;
	var middleFrameCols = leftWidth + ",*";

	var switchTopButtonPosRight = 50;
		
	var currentTopHeight = topHeight;
	var currentLeftWidth = leftWidth;

	function init() {
		top.menuFrame.document.getElementById('menuM').style.width = (top.menuFrame.document.body.clientWidth - top.menuFrame.document.getElementById('menuL').clientWidth - top.menuFrame.document.getElementById('menuR').clientWidth) + "px";

        /*初始化menuFrame中的伸缩按钮位置*/
		top.menuFrame.document.getElementById('switchTopButton').style.top = (menuHeight - top.menuFrame.document.getElementById('switchTopButton').clientHeight)/2 + "px";
		top.menuFrame.document.getElementById('switchTopButton').style.right = switchTopButtonPosRight + "px";
		/*初始化menuFrame中的菜单位置*/
		top.menuFrame.document.getElementById('goOrReturn').style.width = "150px";
		top.menuFrame.document.getElementById('menuContent').style.top = "0px";
		// top.menuFrame.document.getElementById('menuContent').style.left = parseInt(top.menuFrame.document.getElementById('switchLeftButton').style.left) + 35 + "px";		
		top.menuFrame.document.getElementById('menuContent').style.left = ($('#goOrReturn').width() + 105) + "px";
	}

	function switcherInit() {
	}
	
	function switchLeft() {
		if(document.getElementById('switchLeftButton').isHidden == 0) {
			var s = "top.middleFrame.cols = \"" + 0 + ",*\"";
			eval(s);
			document.getElementById('switchLeftButton').isHidden = 1;
			document.getElementById('sLBImg').src = "<%=skinPath%>/images/show_left_button.gif";
		} else {
			var s = "top.middleFrame.cols = \"" + leftWidth + ",*\"";
			eval(s);
			document.getElementById('switchLeftButton').isHidden = 0;
			document.getElementById('sLBImg').src = "<%=skinPath%>/images/hide_left_button.gif";
		}
		
		//window.top.mainFrame.tabpanel.resize();
	}
	
	function refreshTab() {
		if (window.top.mainFrame.tabpanel) {
			var ifrm = window.top.mainFrame.tabpanel.getActiveTab().content.find('iframe');
			ifrm.attr("src", ifrm.attr("src"));
		}
		return;	
	}
	
	function switchTop() {
		if(document.getElementById('switchTopButton').isHidden == 0) {
			var s = "top.allFrame.rows = \"" + 0 + "," + menuHeight + ",*," + bottomHeight + "\"";
			eval(s);
			document.getElementById('switchTopButton').isHidden = 1;
			document.getElementById('sTBImg').src = "<%=skinPath%>/images/show_top_button.gif";
		} else {
			var s = "top.allFrame.rows = \"" + topHeight + "," + menuHeight + ",*," + bottomHeight + "\"";
			eval(s);
			document.getElementById('switchTopButton').isHidden = 0;
			document.getElementById('sTBImg').src = "<%=skinPath%>/images/hide_top_button.gif";
		}
		
		//window.top.mainFrame.tabpanel.resize();
	}
	
	function setLeft(name) {
		top.leftFrame.location.href = "left.jsp?item=" + name;
	}
	
	$(document).ready(function() {
		var ismobile = navigator.userAgent.match(/(iPad)|(iPhone)|(iPod)|(android)|(webOS)/i) != null;
		var triggerevt = (ismobile)? "click" : "mouseenter"
		$('#startImg').bind(triggerevt, function(e) {
			window.top.mainFrame.showMenu();
		});

	});
	
	$(document).bind("click", function(e){
		if (e.button==0){ //hide all flex menus (and their sub ULs) when left mouse button is clicked
			window.top.mainFrame.hideMenu();
		}
	})
	
	$(function() {
		$("#1").lavaLamp({
			fx: "backout", 
			speed: 700,
			click: function(event, menuItem) {
				return false;
			}
		});
	});
	
	function lamp(groupId) {
		var el = document.getElementById(groupId);
		$('.back').animate({
			width: el.offsetWidth,
			left: el.offsetLeft
		}, 700, "backout");
	}
</script>
</head>
<body onload="init()" onresize="init()">
	<div id="menu">
		<div id="menuL"></div>
		<div id="menuM"></div>
		<div id="menuR"></div>
	</div>
	<div id="goOrReturn">
	  <table style="width:100%" border="0" cellpadding="0" cellspacing="0">
	    <tr>
		  <td style="width:60px" align="left"><img title="开始菜单" id="startImg" onmouseout="this.src='<%=skinPath%>/images/startmenu.png'" onmouseover="this.src='<%=skinPath%>/images/startmenu.png'" src="<%=skinPath%>/images/startmenu.png" /></td>
		  <td style="width:50px" align="right"><img title="后退" id="returnImg" onclick="top.history.back()" onmouseout="this.src='<%=skinPath%>/images/return_button.gif'" onmouseover="this.src='<%=skinPath%>/images/return_button_light.gif'" src="<%=skinPath%>/images/return_button.gif" /></td>
		  <td style="width:50px" align="left"><img title="前进" id="goImg" onclick="top.history.forward()" onmouseout="this.src='<%=skinPath%>/images/go_button.gif'" onmouseover="this.src='<%=skinPath%>/images/go_button_light.gif'" src="<%=skinPath%>/images/go_button.gif" /></td>
	    </tr>
	  </table>
	</div>
	<div id="switchTopButton" onclick="refreshTab()" isHidden=0 style="cursor:pointer"><img title="刷新" id="sTBImg" src="<%=skinPath%>/images/hide_top_button.gif"  /></div>
	<div id="menuContent">
        <ul class="lavaLampBottomStyle" id="1">
        	<%
            SlideMenuGroupDb smgd = new SlideMenuGroupDb();
			String sql = "select id from " + smgd.getTable().getName() + " where user_name=? order by orders";
			Iterator ir = smgd.list(sql, new Object[]{privilege.getUser(request)}).iterator();
			while (ir.hasNext()) {
				smgd = (SlideMenuGroupDb)ir.next();
				%>
                <li id="<%=smgd.getLong("id")%>"><a href="javascript:;" onclick="showMenu('<%=smgd.getLong("id")%>')"><%=smgd.getString("name")%></a></li>
				<%
			}
			%>
        </ul>
	</div>
</body>
<script>
function showMenu(groupId) {
	var tab = window.top.mainFrame.getAndShowDesktopTab();
	tab.content.find('iframe')[0].contentWindow.frames[0].showMenu(groupId);
}
</script>
</html>
