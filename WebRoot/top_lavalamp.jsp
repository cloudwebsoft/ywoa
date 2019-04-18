<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%
	String skincode = UserSet.getSkin(request);
	if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
	SkinMgr skm = new SkinMgr();
	Skin skin = skm.getSkin(skincode);
	String skinPath = skin.getPath();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<title>top</title>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css" />
<script src="inc/common.js"></script>
<script src="js/jquery.js"></script>
<script type="text/javascript" src="js/jquery.easing.min.js"></script>
<script type="text/javascript" src="js/jquery.lavalamp.min.js"></script>
<link rel="stylesheet" href="<%=skinPath%>/lavalamp.css" type="text/css" media="screen">
<script>
var now=new Date();
var yy=now.getYear();
if (!isIE())
	yy = 1900 + yy;
var MM=now.getMonth()+1;
var dd=now.getDate();
var DD=now.getDay();
var x = new Array("星期日","星期一","星期二","星期三","星期四","星期五","星期六");
var date = yy+"年"+MM+"月"+dd+"日"+"  "+x[DD]+"  ";//+"<img src=\"<%=skinPath%>/images/OAimg04.gif\" />";
function refreshCalendarClock(){
	var now=new Date();
	var hh=now.getHours();
	var mm=now.getMinutes();
	var ss=now.getTime()%60000;
	ss=(ss-(ss%1000))/1000;
	if(hh<10)hh="0"+hh;
	if(mm<10)mm="0"+mm;
	if(ss<10)ss="0"+ss;
	// $("tdTime").innerHTML=date+hh+":"+mm+":"+ss;
	$("#spanDate").html(date);
	$("#spanTime").html(hh+":"+mm+":"+ss);
}

function init() {
	window.setInterval('refreshCalendarClock()',1000);
}

function onClickDoc(e) {
	if (isIE() && e.shiftKey)
	 	return;
	var obj = e.target;
	if (obj.tagName=="A") {
		if (obj.target=="mainFrame") {
			// 检查是否已有同样的title的选项卡
			var isAdded = false;
			var tabs = top.mainFrame.tabpanel.tabs;
			var tabsId;
			for (var i=0; i<tabs.length; i++) {
				if (tabs[i].title.text()==obj.innerHTML) {
					isAdded = true;
					tabsId = tabs[i].id;
					break;
				}
			}
			if (!isAdded) {
				top.mainFrame.addTab(obj.innerHTML, obj.href);
			}
			else {
				var isActive = false;
				if (obj.canRepeat) {
					if (obj.canRepeat=="false")
						isActive = true;
				}
			
				if (isActive) {
					// 如果不允许重复，则激活
					top.mainFrame.addTab(obj.innerHTML, obj.href, tabsId);
					
					var position = top.mainFrame.tabpanel.getTabPosision(tabsId);
					var iframes = top.mainFrame.tabpanel.tabs[position].content.find('iframe');
					iframes[0].src = iframes[0].src;	//刷新，注意不能用iframes[0].location.reload()，这样会失去tab
				}
				else {
					top.mainFrame.addTab(obj.innerHTML, obj.href);				
				}
			}
			e.preventDefault();
			return false;
		}
	}
}

$(document).bind("click", function(e){
	onClickDoc(e);
	if (e.button==0){ //hide all flex menus (and their sub ULs) when left mouse button is clicked
		window.top.mainFrame.hideMenu();
	}
})

function exit() {
	// window.top.location.href = "exit_oa.jsp";
}

$(function() {
	$("#1").lavaLamp({
		fx: "backout", 
		speed: 700,
		click: function(event, menuItem) {
			return false;
		}
	});
});
</script>
</head>
<body onload="init()" onunload="exit()">
<%
com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(pvg.getUser(request));
String desktopUrl;
if (usd.getUiMode()==UserSetupDb.UI_MODE_PROFESSION)
	desktopUrl = "desktop.jsp";
else
	desktopUrl = "desktop_frame.jsp";
UserDb user = new UserDb();
user = user.getUserDb(pvg.getUser(request));
%>
<div id="top">
	<div id="topLeft"><img src="<%=skinPath%>/images/top_left.png" /></div>
	<div id="topMiddle"></div>
	<div id="topRight">
	  <table border="0" cellpadding="0" cellspacing="0">
	    <tr>
		  <td colspan="5" height="5"></td>
	    </tr>
	    <tr>
		  <td class="tdMenu">&nbsp;</td>
		  <td class="tdMenu">
		  <%
		  if (usd.getUiMode()==UserSetupDb.UI_MODE_FASHION) {
		  %>
          <a canRepeat="false" title="控制面板" href="user/control_panel.jsp" target="mainFrame"><%=user.getRealName()%></a>
          <%}%>
          </td>
		  <td class="tdMenu"><img src="<%=skinPath%>/images/OAimg01.gif" /><a canRepeat="false" href="<%=desktopUrl%>" target="mainFrame">桌面</a></td>
		  <td class="tdMenu"><img src="<%=skinPath%>/images/OAimg02.gif" /><a canRepeat="false" title="控制面板" href="user/control_panel.jsp" target="mainFrame">配置</a></td>
		  <td class="tdMenu"><img src="<%=skinPath%>/images/OAimg03.gif" /><a href="exit_oa.jsp" target="_top" onclick="if(!confirm('您确定要退出吗？')) return false">退出</a></td>
		</tr>
		<tr>
		  <td colspan="5" id="tdTime" class="tdTime"><span id="spanDate"></span><img src="<%=skinPath%>/images/OAimg04.gif" /><span id="spanTime"></span></td>
	    </tr>
	  </table>
	</div>
</div>
<div id="portalBox" style="position:absolute">
    <ul class="lavaLampWithImage" id="1">
<%
PortalDb pd = new PortalDb();
String sql = "select id from " + pd.getTable().getName() + " where user_name=? order by orders";
Iterator ir = pd.list(sql, new Object[]{pvg.getUser(request)}).iterator();
while (ir.hasNext()) {
	pd = (PortalDb)ir.next();
	%>
	<li><a onclick="addTab('<%=pd.getString("name")%>', 'desktop.jsp?portalId=<%=pd.getLong("id")%>')" canRepeat="false" href="javascript:;"><%=pd.getString("name")%></a></li>
	<!--<div style="width:60px; height:30px; background-image:url(<%=skinPath%>/images/portal_btn.png); background-repeat:no-repeat; float:left; padding-top:8px; padding-left:10px"><a canRepeat="false" href="desktop.jsp?portalId=<%=pd.getLong("id")%>" target="mainFrame"><%=pd.getString("name")%></a></div>-->
	<%
}
%>
    </ul>    
</div>
</body>
<script>
$(document).ready(function() {
	$("#portalBox").css({position: "absolute", 'top':$("#tdTime").offset().top-27, 'left':$("#tdTime").offset().left - $("#portalBox").width()});
});
</script>
</html>
