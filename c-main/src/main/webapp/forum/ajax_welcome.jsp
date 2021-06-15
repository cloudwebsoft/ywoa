<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skincode = UserSet.getSkin(request);
if (skincode.equals(""))
	skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
if (skin==null)
	skin = skm.getSkin(UserSet.defaultSkin);
String skinPath = skin.getPath();
%>
<html>
<head>
<LINK href="forum/<%=skinPath%>/skin.css" type=text/css rel=stylesheet>
</head>
<body>
<div id="welcome" style="font-size:12px">
  <div style="background-color:#cccccc;width:480px;height:280px;padding:10px">
    <div style="width:480px;height:280px;background-color:white;margin:0px">
	  <div style="text-align:right"><a href="javascript:hidePopLayer()"><img border="0" src="<%=request.getContextPath()%>/images/close1.gif"></a></div>
	  <div style="text-align:center;padding:20px 0px 20px 0px">
      <img src="<%=request.getContextPath()%>/logo_b.jpg" width="200px">
	  </div>
      <div style="text-align:center;height:20px">欢迎您来到<%=Global.AppName%></div>
      <div style="text-align:center;height:20px">请先注册以避免此窗口再次出现&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="<%=request.getContextPath()%>/regist.jsp" style="font-weight:bold">注&nbsp;册</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:discardAutoClose();ajaxpage('ajax_login.jsp','popLayer')" style="font-weight:bold">登&nbsp;录</a></div>
      <div style="text-align:center;height:22px;font-weight:bold">(此窗口将于<span id="secondSpan">5</span>秒后自动关闭)</div>
    </div>
  </div>
</div>
</body>
</html>
