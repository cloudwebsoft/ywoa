<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.forum.Config"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.module.pvg.*"%>
FCKConfig.ToolbarSets["Simple"] = [
	['Source','Bold','Italic','TextColor','BGColor','-','OrderedList','UnorderedList','-','Link','Unlink','Image','Flash','-','About']
];
<%
Privilege pvg = new Privilege();
User user = new User();
user = user.getUser(pvg.getUser(request));

Config cfg = Config.getInstance();
String p = ThreeDesUtil.encrypt2hex(cfg.getKey(), user.getPwdMD5());

String dir = ParamUtil.get(request, "dir");
%>
FCKConfig.ImageBrowser = true;
FCKConfig.ImageBrowserURL = FCKConfig.BasePath + 'filemanager/browser/img_frame.jsp?action=selectImageForEditor&dir=<%=StrUtil.UrlEncode(dir)%>&userName=<%=pvg.getUser(request)%>&p=<%=p%>';
FCKConfig.ImageBrowserWindowWidth  = FCKConfig.ScreenWidth * 0.7 ;	// 70% ;
FCKConfig.ImageBrowserWindowHeight = FCKConfig.ScreenHeight * 0.7 ;	// 70% ;

FCKConfig.FlashBrowser = true;
FCKConfig.FlashBrowserURL = FCKConfig.BasePath + 'filemanager/browser/flash_frame.jsp?action=selectImageForEditor&dir=<%=StrUtil.UrlEncode(dir)%>&userName=<%=pvg.getUser(request)%>&p=<%=p%>';
FCKConfig.FlashBrowserWindowWidth  = FCKConfig.ScreenWidth * 0.7 ;	//70% ;
FCKConfig.FlashBrowserWindowHeight = FCKConfig.ScreenHeight * 0.7 ;	//70% ;
