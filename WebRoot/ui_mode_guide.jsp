<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ include file="inc/inc.jsp"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%
String mainTitle = ParamUtil.get(request, "mainTitle");
String mainPage = ParamUtil.get(request, "mainPage");
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>界面向导</title>
<script src="inc/common.js"></script>
<script src="js/jquery.js"></script>
<script language="javascript">$(function(){$("#interface_mode").css({"position":"absolute","top":"50%","left":"50%"}).css("margin-top",function(){return -$(this).height()/2;}).css("margin-left",function(){return -$(this).width()/2});});</script>
<link href="ui_mode_guide.css" rel="stylesheet" type="text/css" />
<script>
function selUiMode() {
	if ($("input[name='mode']:checked").length==0) {
		alert("请选择界面模式");
		return;
	}
	window.location.href = "ui_mode_guide.jsp?op=set&mainTitle=<%=StrUtil.UrlEncode(mainTitle)%>&mainPage=<%=mainPage%>&mode=" + $("input[name='mode']:checked").val();
}

var imgDivId = ['#imgProfession','#imgFashion','#imgFloweriness'];
function checkedImg(imgdiv,radio){
	document.getElementById(radio).checked = "true";
	for(var i=0;i<imgDivId.length;i++){
		if(imgDivId[i]==imgdiv){			
			if ($(imgDivId[i]).hasClass('mode_border')) {
				$(imgDivId[i]).removeClass('mode_border');
				$(imgDivId[i]).addClass('mode_selected');
			}
		}else{
			if ($(imgDivId[i]).hasClass('mode_selected')) {
				$(imgDivId[i]).removeClass('mode_selected');
				$(imgDivId[i]).addClass('mode_border');
			}
		}
	}
}
function addClasses(imgdiv,radio){
	if(document.getElementById(radio).checked==false){
		if ($(imgdiv).hasClass('mode_border')) {
				$(imgdiv).removeClass('mode_border');
				$(imgdiv).addClass('mode_selected');
		}
	}
}
function removeClasses(imgdiv,radio){
	if(document.getElementById(radio).checked==false){
		if ($(imgdiv).hasClass('mode_selected')) {
				$(imgdiv).removeClass('mode_selected');
				$(imgdiv).addClass('mode_border');
		}
	}
} 

</script>
</head>

<body style="background-color:#f0f5f5" onload="checkedImg('#imgProfession','radioProfession')">
<%
String op = ParamUtil.get(request, "op");
if (op.equals("set")) {
	int mode = ParamUtil.getInt(request, "mode", 0);
	UserSetupDb usd = new UserSetupDb();
	usd = usd.getUserSetupDb(privilege.getUser(request));
	usd.setUiMode(mode);
	String queryStr = "";
	if (!mainPage.equals(""))
		queryStr = "?mainTitle=" + StrUtil.UrlEncode(mainTitle) + "&mainPage=" + mainPage;
	if (usd.save()) {
		if (mode==UserSetupDb.UI_MODE_FASHION) {
			response.sendRedirect("main.jsp" + queryStr);
		}
		else if (mode==UserSetupDb.UI_MODE_FLOWERINESS) {
			response.sendRedirect("mydesktop.jsp" + queryStr);
		}
		else {
			response.sendRedirect("oa.jsp" + queryStr);
		}
	}
	else {
		out.print(StrUtil.Alert_Back("操作失败！"));
	}
	return;
}
%>
<div class="interface_mode_fram" id="interface_mode" >
  <div class="modetitle"><img src="images/mode_icon.png" width="35" height="35" /> 请选择界面模式</div>
  <div class="mode_img_stencil">
     <div class="mode_imgbox" style="margin-right:30px">
        <div class="mode_border" id="imgProfession" onclick="checkedImg('#imgProfession','radioProfession')" onmouseover="addClasses('#imgProfession','radioProfession')" onmouseout="removeClasses('#imgProfession','radioProfession')"><img src="images/mode_img-1.jpg" width="270" height="206" /></div>
        <div><input id="radioProfession" name="mode" type="radio" value="<%=UserSetupDb.UI_MODE_PROFESSION%>" onclick="checkedImg('#imgProfession','radioProfession')" checked/>&nbsp;经典型 </div>
     </div>
     <div class="mode_imgbox" style="margin-right:30px"> 
        <div class="mode_border" id="imgFashion" onclick="checkedImg('#imgFashion','radioFashion')" onmouseover="addClasses('#imgFashion','radioFashion')" onmouseout="removeClasses('#imgFashion','radioFashion')"><img src="images/mode_img-2.jpg" width="270" height="206" /></div>
        <div><input id="radioFashion" name="mode" type="radio" value="<%=UserSetupDb.UI_MODE_FASHION%>" onclick="checkedImg('#imgFashion','radioFashion')"/>&nbsp;时尚型</div>
	 </div>
	 <%
	 	String displayFloweriness = "display:none";
	 	String displayImg = "";
		if (com.redmoon.oa.kernel.License.getInstance().isEnterprise() || com.redmoon.oa.kernel.License.getInstance().isGroup() || com.redmoon.oa.kernel.License.getInstance().isPlatform()) {
	 		displayFloweriness = "";
	 		displayImg = "display:none";
	 	}
	 %>
     <div style="<%=displayFloweriness %>"  class="mode_imgbox" > 
        <div class="mode_border" id="imgFloweriness" onclick="checkedImg('#imgFloweriness','radioFloweriness')" onmouseover="addClasses('#imgFloweriness','radioFloweriness')" onmouseout="removeClasses('#imgFloweriness','radioFloweriness')"><img src="images/mode_img-3.jpg" width="270" height="206" /></div>
        <div><input id="radioFloweriness" name="mode" type="radio" value="<%=UserSetupDb.UI_MODE_FLOWERINESS%>" onclick="checkedImg('#imgFloweriness','radioFloweriness')" />&nbsp;绚丽型</div>
     </div>
     <div style="<%=displayImg %>"  class="mode_imgbox" > 
			<img src="images/mode_img-4.jpg"/>
     </div>
  </div>
  <div class="comments">注：首次登录时需选择，以后可在控制面板中设置</div>
  <div ><a href="javascript:;" onclick="selUiMode()"><img src="images/mode_button.png" width="403" height="63" /></a> </div>
</div>
</body>
</html>
