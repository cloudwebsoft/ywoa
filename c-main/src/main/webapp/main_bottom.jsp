<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.ui.menu.*"%>
<%@ page import="java.util.Properties" %>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="cn.js.fan.web.*" %>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
- 功能描述：底部工具条，刷新在线人员名单、刷新短消息
- 访问规则：oa.jsp的框架中
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：
==================
- 修改者：fgf
- 修改时间：2011.11.11
- 修改原因：原定时方法不太合理，超出1分钟时长的就无法定时，弹出窗口，在傲游等浏览器中会显示为一个新选项卡
- 修改点：改造刷新定时器，用jquery-ui改造消息弹出框
*/
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals("")) {
	skincode = UserSet.defaultSkin;
}
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
String op = ParamUtil.get(request, "op");
if(op.equals("save")) {
	String ids = ParamUtil.get(request, "ids");
	if(!ids.equals("")) {
		String[] idArray = StrUtil.split(ids, "\\|");			
		if (idArray!=null) {
			String[] leftIds = null;
			if (idArray.length>0) {
				leftIds = StrUtil.split(idArray[0], ",");
			}
			String[] rightIds = null;
			if(idArray.length > 1) {
				rightIds = StrUtil.split(idArray[1], ",");
			}
			String[] sideIds = null;
			if(idArray.length > 2) {
				sideIds = StrUtil.split(idArray[2], ",");
			}
			UserDesktopSetupDb userDesktopItemDb = new UserDesktopSetupDb();
			if (leftIds != null && rightIds!=null) {
				for(int i = 0; i<leftIds.length; i++) {
					int id = Integer.parseInt(leftIds[i]);
					userDesktopItemDb = userDesktopItemDb.getUserDesktopSetupDb(id);
					userDesktopItemDb.setTd(userDesktopItemDb.TD_LEFT);
					userDesktopItemDb.setOrderInTd(i);
					userDesktopItemDb.save();
				}
			}
			if (leftIds != null && rightIds!=null) {
				for(int i = 0; i<rightIds.length; i++) {
					int id = Integer.parseInt(rightIds[i]);
					userDesktopItemDb = userDesktopItemDb.getUserDesktopSetupDb(id);
					userDesktopItemDb.setTd(userDesktopItemDb.TD_RIGHT);
					userDesktopItemDb.setOrderInTd(i);
					userDesktopItemDb.save();
				}
			}
			if (sideIds != null) {
				for(int i = 0; i<sideIds.length; i++) {
					int id = Integer.parseInt(sideIds[i]);
					userDesktopItemDb = userDesktopItemDb.getUserDesktopSetupDb(id);
					userDesktopItemDb.setTd(userDesktopItemDb.TD_SIDEBAR);
					userDesktopItemDb.setOrderInTd(i);
					userDesktopItemDb.save();
				}
			}			
		}
	}
}
	
UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(privilege.getUser(request));
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<title>bottom</title>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css" />
<script src="inc/common.js"></script>
<script>
function init() {
	top.bottomFrame.document.getElementById("bottomL").style.width = "200px";
	top.bottomFrame.document.getElementById("bottomR").style.width = document.body.clientWidth - top.bottomFrame.document.getElementById("bottomL").clientWidth;
}
</script>
<script src='dwr/interface/MessageDb.js'></script>
<script src='dwr/engine.js'></script>
<script src='dwr/util.js'></script>
<script>
function handler(msg) {
	// alert("您与服务器的连接已断开，请刷新页面尝试重新连接！");
}
DWREngine.setErrorHandler(handler);
DWREngine.setTimeout(2000);

var userName = "<%=privilege.getUser(request)%>";

function getNewMsg(userName) {
  try {
  	o("divMsg").innerHTML = "";
  	MessageDb.getNewMsgsOfUser(showMsgWin, userName);
  }
  catch (e) {
  	alert(e);
  }
}
  
var msgWin;
var width = 320;
var height = 183;
function showMsgWin(msg) {
  if (msg.length>0) {
  	  var i = 0;
	  var str = "<ul>";
	  for (var data in msg) {
	  	i++;
		// alert("data=" + data);
		// alert(msg[data].title);
		var id = msg[data].id
		var title = msg[data].title;
		var sender = msg[data].sender;
				
		str += "<li><b>" + i + ".&nbsp;<input name='ids' value='" + msg[data].id + "' type=hidden><a href='javascript:showmsg(" + msg[data].id + ")'>" + msg[data].title + "</a></b>&nbsp;&nbsp;" + msg[data].senderRealName + "&nbsp;[" + msg[data].rq.substring(5) + "]</li>";
		// 最多取5条
		if (i>=5)
			break;
	  }
	  str += "</ul>";
	  window.top.mainFrame.notifyMsg(str);
	}
}

function doOnlineUserNotify(response){
	var rsp = response.responseText.trim();
	if (rsp.trim()=="multiLogin") {
		alert("系统提醒：您的帐户已在别处登录，您将被迫下线！");
		window.top.location.href = "<%=request.getContextPath()%>/index.jsp";
	}
	else {
		// 须catch，否则session过期后会报JS错误
		try {
			eval(rsp.trim());
		}
		catch (e) {}
	}
}

function onlineUserNotify() {
	/*
	try {
    	OnlineNotifyLoader.src = "online_notify.jsp";
    } catch(e) {
        return false;
    }
	*/
	var myAjax = new cwAjax.Request( 
		"online_notify.jsp",
		{
			method:"post",
			parameters:"",
			onComplete:doOnlineUserNotify,
			onError:errFunc
		}
	);
}

<%
// 取刷新时间
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String refresh_message = cfg.get("refresh_message");
String refreshOnlineUserNotify = cfg.get("refreshOnlineUserNotify"); // 用户每隔多长时间，向服务器通报一次在线（秒）
%>

var refresh_message = <%=refresh_message%>;
var refreshOnlineUserNotify = <%=refreshOnlineUserNotify%>;

function refreshOnlineUser(){
	onlineUserNotify();
	refreshOnlineCount();
	timeoutid = window.setTimeout("refreshOnlineUser()", refreshOnlineUserNotify * 1000); // 每隔N秒钟刷新一次
}

function refreshMsg() {
	getNewMsg(userName);
	timeoutid = window.setTimeout("refreshMsg()", refresh_message * 1000); // 每隔N秒钟刷新一次
}

function doBiz(response){
	var rsp = response.responseText.trim();
	if (rsp!="no") {
		$("onlineCountDiv").innerHTML = rsp;
	}
}

var errFunc = function(response) {
	// alert('Error ' + response.status + ' - ' + response.statusText);
	// alert(response.responseText);
}

function refreshOnlineCount() {
	var str = "";
	var myAjax = new cwAjax.Request(
		"ajax_online.jsp?op=count",
		{ 
			method:"post",
			parameters:str,
			onComplete:doBiz,
			onError:errFunc
		}
	);
}
	
function window_onload() {
  refreshOnlineUser();
  <%if (usd.isMsgWinPopup()) {%>  
  refreshMsg();
  <%}%>
  
  <%
  com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
  if (ssoCfg.getBooleanProperty("isUse")) {
	if ("true".equals(cfg.get("isLarkUsed"))) {
		if ("Lark".equals(cfg.get("larkType"))) {
			String sparkServer = ssoCfg.get("sparkServer");
			UserDb user = new UserDb();
			user = user.getUserDb(privilege.getUser(request));
		%>
			try {
				if (typeof(o("webedit").LaunchLark)!="undefined")
					o("webedit").LaunchLark("<%=privilege.getUser(request)%>", "<%=user.getPwdRaw()%>", "<%=sparkServer%>");
				else
					alert(typeof(o("webedit").LaunchLark));
			}
			catch (e) {}
		<%}	else {
			String sparkServer = ssoCfg.get("sparkServer");
			UserDb user = new UserDb();
			user = user.getUserDb(privilege.getUser(request));
		%>
			try {
				if (typeof(o("webedit").LaunchSpark)!="undefined")
					o("webedit").LaunchSpark("<%=privilege.getUser(request)%>", "<%=user.getPwdRaw()%>", "<%=sparkServer%>");
			}
			catch (e) {}
		<%
		}
	}
  }
  %>
}
</script>
<script id="OnlineNotifyLoader" language="JavaScript" type="text/javascript" defer></script>
</head>
<body onload="init();window_onload()" onresize="init()">
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
<div id="bottom">
	<div id="bottomL">&nbsp;&nbsp;<a href="http://www.cloudwebsoft.com" target="_blank">欢迎使用云网OA系统</a></div>
	<div id="bottomR">
	  <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
	    <tr>
<%
LeafChildrenCacheMgr lccm = new LeafChildrenCacheMgr(Leaf.CODE_BOTTOM);
Iterator ir = lccm.getChildren().iterator();
while (ir.hasNext()) {
	Leaf lf = (Leaf)ir.next();
	if (!lf.canUserSee(request))
		continue;
%>		
		  <td class="tdMenu">
			<%if (lf.getIcon().equals("")) {%>
				<img src="netdisk/images/folder_01.gif" />
			<%}else{%>
				<img src="<%=skinPath%>/icons/<%=lf.getIcon()%>" />
			<%}%>
			<a canRepeat="<%=lf.isCanRepeat()%>" href="<%=lf.getLink(request)%>" hidefocus="true" target="<%=lf.getTarget()%>"><%=lf.getName()%></a>
		  </td>
<%}%>
		  <td id="online_person" align="right">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:showOnline()">在线&nbsp;&nbsp;<span id="onlineCountDiv"></span>&nbsp;&nbsp;人</a></td>
		</tr>
	  </table>	
	</div>
</div>
<div id="divMsg" name="divMsg"></div>
</body>
<script>
function showOnline(){
	window.top.leftFrame.setMenu(3);
}

function onClickDoc(e) {
	if (event.shiftKey)
		return;
	var obj=isIE()? event.srcElement : e.target
	if (obj.tagName=="A") {
		if (obj.target=="mainFrame") {
			// 检查是否已有同样的title的选项卡
			var isAdded = false;
			var tabs = top.mainFrame.tabpanel.tabs;
			var tabsId;
			for (var i=0; i<tabs.length; i++) {
				if (tabs[i].title.text()==obj.innerText) {
					isAdded = true;
					tabsId = tabs[i].id;
					break;
				}
			}
			if (!isAdded) {
				top.mainFrame.addTab(obj.innerText, obj.href);
			}
			else {
				var isActive = false;
				if (obj.canRepeat) {
					if (obj.canRepeat=="false")
						isActive = true;
				}
												
				if (isActive) {
					// 如果不允许重复，则激活
					top.mainFrame.addTab(obj.innerText, obj.href, tabsId);
					
					var position = top.mainFrame.tabpanel.getTabPosision(tabsId);
					var iframes = top.mainFrame.tabpanel.tabs[position].content.find('iframe');
					iframes[0].src = iframes[0].src;	//刷新，注意不能用iframes[0].location.reload()，这样会失去tab
				}
				else {
					top.mainFrame.addTab(obj.innerText, obj.href);				
				}
			}
			return false;
		}
	}
}
document.onclick=onClickDoc;
</script>
</html>