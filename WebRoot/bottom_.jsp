<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.ui.menu.*"%>
<%@ page import="java.util.Properties" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals("")) skincode = UserSet.defaultSkin;
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
			if (idArray.length>0)
				leftIds = StrUtil.split(idArray[0], ",");
			String[] rightIds = null;
			if(idArray.length > 1) {
				rightIds = StrUtil.split(idArray[1], ",");
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
			if (rightIds != null && rightIds!=null) {
				for(int i = 0; i<rightIds.length; i++) {
					int id = Integer.parseInt(rightIds[i]);
					userDesktopItemDb = userDesktopItemDb.getUserDesktopSetupDb(id);
					userDesktopItemDb.setTd(userDesktopItemDb.TD_RIGHT);
					userDesktopItemDb.setOrderInTd(i);
					userDesktopItemDb.save();
				}
			}
		}
	}
}
	
UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(privilege.getUser(request));

String nick,room;
nick = privilege.getUser(request);
cfgparser.parse("config_oa.xml");
Properties props = cfgparser.getProps();
room = props.getProperty("defaultroom");	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>bottom</title>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css" />
<script src="inc/common.js"></script>
<script>
function init() {
	if (top.leftFrame.document.getElementById("left")!=null) {
		top.bottomFrame.document.getElementById("bottomL").style.width = top.leftFrame.document.getElementById("left").clientWidth + 16;//滚动条宽16px
		top.bottomFrame.document.getElementById("bottomR").style.width = document.body.clientWidth - top.bottomFrame.document.getElementById("bottomL").clientWidth;
	}
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
  	divMsg.innerHTML = "";
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
	  for (var data in msg) {
		// alert("data=" + data);
		// alert(msg[data].title);
		var id = msg[data].id
		var title = msg[data].title;
		var sender = msg[data].sender;
		divMsg.innerHTML += "<input name='ids' value='" + msg[data].id + "' type=hidden><a href='javascript:showmsg(" + msg[data].id + ")'>" + msg[data].title + "</a>&nbsp;&nbsp;" + msg[data].senderRealName + "&nbsp;[" + msg[data].rq + "]<BR>";
	  }
  	  if (msgWin!=null) {
	  	// msgWin.close()
		// msgWin = null;
		try {
			msgWin.focus();
			msgWin.getMsg();
		}
		catch (e) {
		  msgWin = null;
		  msgWin = window.open("message_oa/newmsg.jsp","_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=" + window.screen.availHeight + ",left=" + window.dialogLeft + ",width="+width+",height="+height);
		}
	  }
	  else
		  // 打开窗口，传递消息
		  msgWin = window.open("message_oa/newmsg.jsp","_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=" + window.screen.availHeight + ",left=" + window.dialogLeft + ",width="+width+",height="+height);
  }
}

function getDivMsg() {
	return divMsg.innerHTML;
}

function refreshMsg() {
	getNewMsg(userName);
}

function onlineUserNotify() {
	try {
    	OnlineNotifyLoader.src = "online_notify.jsp";
    } catch(e) {
        return false;
    }
}

<%
// 取刷新时间
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String refresh_message = cfg.get("refresh_message");
String refreshOnlineUserNotify = cfg.get("refreshOnlineUserNotify"); // 用户每隔多长时间，向服务器通报一次在线（秒）
%>

var refresh_message = <%=refresh_message%>;
var refreshOnlineUserNotify = <%=refreshOnlineUserNotify%>;

var oldmsgtime = 0;
var msgtimespace;

var oldRefreshOnlineUserNotifyTime = 0;
var refreshOnlineUserNotifyTimeSpace;

function refresh(){
	var d = new Date();
	sec = d.getSeconds()
	
	refreshOnlineUserNotifyTimeSpace = sec-oldRefreshOnlineUserNotifyTime
	if(refreshOnlineUserNotifyTimeSpace<0)
		refreshOnlineUserNotifyTimeSpace += 60
		
	if (refreshOnlineUserNotifyTimeSpace>=refreshOnlineUserNotify) {
		oldRefreshOnlineUserNotifyTime = sec;
		onlineUserNotify();
		
		refreshOnlineCount();
	}	
		
	<%if (usd.isMsgWinPopup()) {%>
	msgtimespace = sec - oldmsgtime;
	if (msgtimespace<0)
		msgtimespace += 60;
	if (msgtimespace>=refresh_message) {
		// 刷新消息
		oldmsgtime = sec;
		refreshMsg();
	}
	<%}%>
	
	timeoutid = window.setTimeout("refresh()", "3000"); // 每隔3秒钟刷新一次
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
  refresh();
}
</script>
<script id="OnlineNotifyLoader" language="JavaScript" type="text/javascript" defer></script>
</head>
<body onload="init();window_onload()" onresize="init()">
<div id="bottom">
	<div id="bottomL">※&nbsp;&nbsp;<a href="http://www.cloudwebsoft.com" target="_blank">欢迎使用云网OA系统</a></div>
	<div id="bottomR">
	  <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
	    <tr>
<%
LeafChildrenCacheMgr lccm = new LeafChildrenCacheMgr(Leaf.CODE_BOTTOM);
Iterator ir = lccm.getChildren().iterator();
while (ir.hasNext()) {
	Leaf lf = (Leaf)ir.next();
%>		
		  <td class="tdMenu">
			<%if (lf.getIcon().equals("")) {%>
				<img src="netdisk/images/folder_01.gif" />
			<%}else{%>
				<img src="<%=skinPath%>/icons/<%=lf.getIcon()%>" />
			<%}%>
			<a href="<%=lf.getLink(request)%>" hidefocus="true" target="<%=lf.getTarget()%>"><%=lf.getName()%></a>
		  </td>
<%}%>
		  <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;在线&nbsp;<a href="javascript:showOnline()"><span id="onlineCountDiv"></span></a>人</td>
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
</script>
</html>
