<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.base.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.music.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("order")) {
	MusicUserMgr mum = new MusicUserMgr();
	String userName = ParamUtil.get(request, "userName");
	long musicId = ParamUtil.getLong(request, "musicId");
	boolean re = false;
	try {
		re = mum.orderMusicForUser(request, userName, musicId);
		if (re) {
			out.print(StrUtil.Alert_Redirect("点歌成功！", "music_order.jsp?userName=" + StrUtil.UrlEncode(userName)));
		}
		else
			out.print(StrUtil.Alert_Back("点歌失败！"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}

String boardcode = ParamUtil.get(request, "boardcode");
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<title><%=Global.AppName%> - <lt:Label res="res.label.forum.showonline" key="view_online"/></title>
</head>
<body>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<%@ include file="inc/position.jsp"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	response.sendRedirect("../door.jsp");
	return;
}

com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
if (!cfg.getBooleanProperty("forum.isOrderMusic")) {
	out.print(StrUtil.Alert_Back("点歌服务尚未开通！"));
	return;
}

long orderId = ParamUtil.getLong(request, "orderId", -1);
String url = "";
if (orderId!=-1) {
	MusicUserDb mud = new MusicUserDb();
	mud = (MusicUserDb)mud.getQObjectDb(new Long(orderId));
	if (mud!=null) {
		String userName = mud.getString("user_name");
		long musicId = mud.getLong("music_id");
		if (userName.equals(privilege.getUser(request))) {
			MusicFileDb mfd = new MusicFileDb();
			mfd = mfd.getMusicFileDb(musicId);
			url = mfd.getMusicUrl(request);
			String orderUser = mud.getString("order_user");
			UserMgr um = new UserMgr();
			UserDb user = um.getUser(orderUser);
		%>
		<div align=center>请听<%=user.getNick()%>为您点的歌曲&nbsp;-&nbsp;<%=mfd.getName()%></div>
		<%
		}
		else {
			out.print(StrUtil.Alert_Back("权限非法！"));
			return;
		}
	}
}
%>
  <div align="center"><br><object classid="clsid:22D6F312-B0F6-11D0-94AB-0080C74C7E95" id="MediaPlayer1" width="428" height="68">
    <param name="AudioStream" value="-1">
    <param name="AutoSize" value="0">
    <param name="AutoStart" value="-1">
    <param name="AnimationAtStart" value="-1">
    <param name="AllowScan" value="-1">
    <param name="AllowChangeDisplaySize" value="-1">
    <param name="AutoRewind" value="0">
    <param name="Balance" value="0">
    <param name="BaseURL" value>
    <param name="BufferingTime" value="5">
    <param name="CaptioningID" value>
    <param name="ClickToPlay" value="-1">
    <param name="CursorType" value="0">
    <param name="CurrentPosition" value="-1">
    <param name="CurrentMarker" value="0">
    <param name="DefaultFrame" value>
    <param name="DisplayBackColor" value="0">
    <param name="DisplayForeColor" value="16777215">
    <param name="DisplayMode" value="0">
    <param name="DisplaySize" value="2">
    <param name="Enabled" value="-1">
    <param name="EnableContextMenu" value="-1">
    <param name="EnablePositionControls" value="-1">
    <param name="EnableFullScreenControls" value="0">
    <param name="EnableTracker" value="-1">
    <param name="Filename" value="<%=url%>">
    <param name="InvokeURLs" value="-1">
    <param name="Language" value="-1">
    <param name="Mute" value="0">
    <param name="PlayCount" value="1">
    <param name="PreviewMode" value="0">
    <param name="Rate" value="1">
    <param name="SAMILang" value>
    <param name="SAMIStyle" value>
    <param name="SAMIFileName" value>
    <param name="SelectionStart" value="-1">
    <param name="SelectionEnd" value="-1">
    <param name="SendOpenStateChangeEvents" value="-1">
    <param name="SendWarningEvents" value="-1">
    <param name="SendErrorEvents" value="-1">
    <param name="SendKeyboardEvents" value="0">
    <param name="SendMouseClickEvents" value="0">
    <param name="SendMouseMoveEvents" value="0">
    <param name="SendPlayStateChangeEvents" value="-1">
    <param name="ShowCaptioning" value="0">
    <param name="ShowControls" value="-1">
    <param name="ShowAudioControls" value="-1">
    <param name="ShowDisplay" value="0">
    <param name="ShowGotoBar" value="0">
    <param name="ShowPositionControls" value="-1">
    <param name="ShowStatusBar" value="-1">
    <param name="ShowTracker" value="-1">
    <param name="TransparentAtStart" value="0">
    <param name="VideoBorderWidth" value="0">
    <param name="VideoBorderColor" value="0">
    <param name="VideoBorder3D" value="0">
    <param name="Volume" value="-40">
    <param name="WindowlessVideo" value="0">
  </object>
  </div>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</body>
</html>
