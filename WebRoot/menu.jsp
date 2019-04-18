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
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<title>menu</title>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css" />
<script src="inc/common.js"></script>
<script src="js/jquery.js"></script>
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
		if(top.leftFrame) { // top.leftFrame.document.getElementById('left') != null) {
			/*初始化menuFrame中的伸缩按钮位置*/
			top.menuFrame.document.getElementById('switchLeftButton').style.top = (menuHeight - top.menuFrame.document.getElementById('switchLeftButton').clientHeight)/2 + "px";
			// top.menuFrame.document.getElementById('switchLeftButton').style.left = (top.leftFrame.document.getElementById('left').clientWidth + 16) + "px";//滚动条宽16px	
			top.menuFrame.document.getElementById('switchLeftButton').style.left = <%=leftWidth%> + "px";//滚动条宽16px	
			
			top.menuFrame.document.getElementById('switchTopButton').style.top = (menuHeight - top.menuFrame.document.getElementById('switchTopButton').clientHeight)/2 + "px";
			top.menuFrame.document.getElementById('switchTopButton').style.right = switchTopButtonPosRight + "px";
			/*初始化menuFrame中的菜单位置*/
			top.menuFrame.document.getElementById('goOrReturn').style.width = <%=leftWidth%> + "px";
			top.menuFrame.document.getElementById('menuContent').style.top = "0px";
			top.menuFrame.document.getElementById('menuContent').style.left = parseInt(top.menuFrame.document.getElementById('switchLeftButton').style.left) + 35 + "px";
		}
		
	}

	function switcherInit() {
	}
	
	function switchLeft() {
		if(document.getElementById('switchLeftButton').getAttribute("isHidden") == 0) {
			var s = "top.middleFrame.cols = \"" + 0 + ",*\"";
			eval(s);
			document.getElementById('switchLeftButton').setAttribute("isHidden", 1);
			document.getElementById('sLBImg').src = "<%=skinPath%>/images/show_left_button.gif";
		} else {
			var s = "top.middleFrame.cols = \"" + leftWidth + ",*\"";
			eval(s);
			document.getElementById('switchLeftButton').setAttribute("isHidden", 0);
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
		init();
	});

</script>
</head>
<body onresize="init()">
<div id="bodyBox">
	<div id="menu">
		<div id="menuL"></div>
		<div id="menuM"></div>
		<div id="menuR"></div>
	</div>
	<div id="goOrReturn">
	  <table style="width:100%" border="0" cellpadding="0" cellspacing="0">
	    <tr>
		  <td style="width:20px">&nbsp;</td>
		  <td style="width:60px" align="right"><img title="后退" id="returnImg" onclick="top.history.back()" onmouseout="this.src='<%=skinPath%>/images/return_button.gif'" onmouseover="this.src='<%=skinPath%>/images/return_button_light.gif'" src="<%=skinPath%>/images/return_button.gif" /></td>
		  <td align="left"><img title="前进" id="goImg" onclick="top.history.forward()" onmouseout="this.src='<%=skinPath%>/images/go_button.gif'" onmouseover="this.src='<%=skinPath%>/images/go_button_light.gif'" src="<%=skinPath%>/images/go_button.gif" /></td>
	    </tr>
	  </table>
	</div>
	<div id="switchLeftButton" onclick="switchLeft()" isHidden=0 style="cursor:pointer;"><img title="折叠左侧菜单" id="sLBImg" src="<%=skinPath%>/images/hide_left_button.gif"  /></div>
	<div id="switchTopButton" onclick="refreshTab()" isHidden=0 style="cursor:pointer"><img title="刷新" id="sTBImg" src="<%=skinPath%>/images/hide_top_button.gif"  /></div>
	<div id="menuContent">
		<ul>
			<li><a href="javascript:;" onclick="setLeft('all')">全部菜单</a></li>
<%
LeafChildrenCacheMgr lccm = new LeafChildrenCacheMgr(Leaf.CODE_ROOT);
Iterator ir = lccm.getChildren().iterator();
while (ir.hasNext()) {
	Leaf lf = (Leaf)ir.next();
	if (!lf.isNav() || !lf.canUserSee(request))
		continue;
	if (lf.getChildren().size()>0) {
%>		
		<li><a href="#" onclick="setLeft('<%=lf.getCode()%>')"><%=lf.getName()%></a></li>
<%	}
}%>
		</ul>
	</div>
</div>    
</body>
</html>
