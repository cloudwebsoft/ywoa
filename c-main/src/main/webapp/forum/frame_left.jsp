<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.forum.*" %>
<%@ page import="com.redmoon.forum.person.*" %>
<%@ page import="com.redmoon.forum.ui.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt"%>
<%
String skinPath = SkinMgr.getSkinPath(request);
String imgPath = request.getContextPath() + "/forum/" + skinPath + "/images/board_tree";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link href="<%=skinPath%>/frame_left.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Board Menu</title>
<script>
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

function ShowChild(imgobj, name)
{
	var tableobj = findObj("childof"+name);
	if (tableobj.style.display=="none")
	{
		tableobj.style.display = "";
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "<%=imgPath%>/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus-1-1.gif")!=-1)
			imgobj.src = "<%=imgPath%>/i_plus2-2.gif";
		if (imgobj.src.indexOf("i_plus-1.gif")!=-1)
			imgobj.src = "<%=imgPath%>/i_plus2-1.gif";
	}
	else
	{
		tableobj.style.display = "none";
		if (imgobj.src.indexOf("i_puls-root.gif")!=-1)
			imgobj.src = "<%=imgPath%>/i_puls-root-1.gif";
		if (imgobj.src.indexOf("i_plus2-2.gif")!=-1)
			imgobj.src = "<%=imgPath%>/i_plus-1-1.gif";
		if (imgobj.src.indexOf("i_plus2-1.gif")!=-1)
			imgobj.src = "<%=imgPath%>/i_plus-1.gif";
	}
}

var curDirCode = "";

var curLeftClickDirCodeOld = "";
var curLeftClickDirCode = "";
var oldsrc = "";

function onMouseUp(dirCode) {
	if (event.button==1) {
		// 点击左键时切换folder图片
		curLeftClickDirCode = dirCode;
		if (curLeftClickDirCodeOld!=curLeftClickDirCode) {
			var curImgObj = findObj("imgFold" + curLeftClickDirCode);
			var oldImgObj = findObj("imgFold" + curLeftClickDirCodeOld);
			if (oldImgObj!=null) {
				oldImgObj.src = oldsrc; // "images/folder_01.gif";
			}
			oldsrc = curImgObj.src;
			curImgObj.src = "<%=imgPath%>/folder_open.gif";
			
			curLeftClickDirCodeOld = curLeftClickDirCode;
		}
	}
	if (event.button==2) {
		curDirCode = dirCode;
	}
}
</script>
</head>
<body>
<table width="100%"  border="0">
  <tr>
    <td align="left">
	<img src="../logo_left.png">
	<b><br>
      <br>
      <%
		  String appName = Global.AppName;
		  if (appName.endsWith("CWBBS")) {
		  	appName = appName.substring(0, appName.indexOf(" - Powered by CWBBS"));
		  }
		  %>
      <font style="font-size:14px"><%=appName%></font></b></td>
  </tr>
  <tr>
    <td align="left"><%
if (com.redmoon.forum.Privilege.isUserLogin(request)) {
	UserDb user = new UserDb();
	user = user.getUser(com.redmoon.forum.Privilege.getUser(request));
%>
      <a target="frame_main" href="../usercenter.jsp"><b><%=user.getNick()%></b></a><BR>
      <lt:Label res="res.label.forum.inc.position" key="welcome"/>
      <%=ForumSkin.formatDate(request, user.getLastTime())%>&nbsp;&nbsp;
      <%}else{%>
      <b>
      <lt:Label res="res.label.forum.inc.position" key="guest"/>
      </b>:&nbsp; <a target="frame_main" href="<%=request.getContextPath()%>/door.jsp?privurl=<%=StrUtil.getUrl(request)%>">[
      <lt:Label res="res.label.forum.inc.position" key="login"/>
      ]</a> <a target="frame_main" href="<%=request.getContextPath()%>/regist.jsp">[
      <lt:Label res="res.label.forum.inc.position" key="regist"/>
      ]</a>
      <%}%></td>
  </tr>
</table>
<table width="100%"  border="0">
  <tr>
    <td align="left"><%
Directory dir = new Directory();  
Leaf leaf = dir.getLeaf(Leaf.CODE_ROOT);
DirectoryView tv = new DirectoryView(request, leaf);
tv.ListSimple(request, out, "frame_main", "listtopic.jsp", "", "" ); // "tbg1", "tbg1sel");

OnlineInfo oli = new OnlineInfo();
int allcount = oli.getAllCount();
%></td>
  </tr>
  <tr>
    <td align="left"><lt:Label res="res.label.forum.index" key="online"/>
      ： <%=allcount%></td>
  </tr>
</table>
</body>
<script language="JavaScript1.2">
<!--
function get_cookie(Name) {
	var search = Name + "="
	var returnvalue = "";
	if (document.cookie.length > 0) {
		offset = document.cookie.indexOf(search)
		// if cookie exists
		if (offset != -1) { 
			offset += search.length
			// set index of beginning of value
			end = document.cookie.indexOf(";", offset);
			// set index of end of cookie value
			if (end == -1) end = document.cookie.length;
			returnvalue=unescape(document.cookie.substring(offset, end))
		}
	}
	return returnvalue;
}

function check(){
	var openones = "";

   	for(var i=0; i<document.images.length; i++) {
		var imgObj = document.images[i];
		try {
			if (imgObj.tableRelate!=null) {
				var tableobj = findObj("childof"+imgObj.tableRelate);
				if (tableobj.style.display=="none")	{
					if (openones=="")
						openones = imgObj.tableRelate;
					else
						openones += "|" + imgObj.tableRelate;
				}
			}
		}
		catch (e) {
		}
   	}

	var expdate = new Date();
	var expday = 60
	expdate.setTime(expdate.getTime() +  (24 * 60 * 60 * 1000 * expday));
	
	document.cookie=window.location.pathname+"="+openones+" ;expires="+expdate.toGMTString();
}

if (document.all)
	document.body.onunload=check

if (get_cookie(window.location.pathname) != ''){
	var openresults=get_cookie(window.location.pathname).split("|");
	for (i=0; i<openresults.length; i++){
		var imgObj = findObj("img_" + openresults[i]);
		try {
			ShowChild(imgObj, openresults[i]);
		}
		catch (e) {
			// alert(e);
		}
	}
}
//-->
</script>
</html>
