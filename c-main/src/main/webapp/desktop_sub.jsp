<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.ui.menu.*"%>
<%@ page import="com.cloudweb.oa.service.IUserSetupService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

String userName = privilege.getUser(request);

String parentCode;
long iconId = ParamUtil.getLong(request, "iconId", -1);
if (iconId!=-1) {
	SlideMenuDb smd = new SlideMenuDb();
	smd = (SlideMenuDb)smd.getQObjectDb(new Long(iconId));
	parentCode = smd.getString("code");
}
else {
	parentCode = ParamUtil.get(request, "code");
}

com.redmoon.oa.ui.menu.Leaf lf = new com.redmoon.oa.ui.menu.Leaf();
lf = lf.getLeaf(parentCode);
if (lf==null) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "节点不存在！"));
	return;
}

IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
String wallpaperPath = userSetupService.getWallpaperPath(userName);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>桌面</title>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="inc/common.js"></script>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript">
	var sliding = startClientX = startPixelOffset = pixelOffset = currentSlide = 0;
	var slideCount = 0;

	function slideStart(event) {
		if (event.originalEvent.touches)
			event = event.originalEvent.touches[0];
		var eventTarget = event.target || event.srcElement;	
		if (eventTarget.tagName=="IMG" || eventTarget.tagName=="A")
			return false;		
			
		if (sliding == 0) {
			sliding = 1;
			startClientX = event.clientX;
		}
	}
	
	function slide(event) {
		event.preventDefault();
		if (event.originalEvent.touches)
			event = event.originalEvent.touches[0];
		var deltaSlide = event.clientX - startClientX;
		if (sliding == 1 && deltaSlide != 0) {
			sliding = 2;
			startPixelOffset = pixelOffset;
		}
		if (sliding == 2) {
			var touchPixelRatio = 1;
			if ((currentSlide == 0 && event.clientX > startClientX) || (currentSlide == slideCount - 1 && event.clientX < startClientX))
				touchPixelRatio = 3;
			pixelOffset = startPixelOffset + deltaSlide / touchPixelRatio;
			$(".slide").css('left',pixelOffset +'px');
		}
	}
	
	function slideEnd(event) {
		if (sliding == 2) {
			sliding = 0;
			currentSlide = pixelOffset < startPixelOffset ? currentSlide + 1 : currentSlide - 1;
			currentSlide = Math.min(Math.max(currentSlide, 0), slideCount - 1);
			pixelOffset = currentSlide * -$('.slide .layer').width();
			$('.buttons b').css('background','#069');
			$('.buttons b').eq(currentSlide).css('background','#6cf');
			$(".slide").animate({left:pixelOffset}, 250);
		}
	}
	
function showMenu(groupId) {
	window.location.href = "desktop_left.jsp?groupId=" + groupId;
}

function init() {
	$('#container').height($(document).height() - $('#buttons').height());
	$('.layer').height($('#container').height());
	$('.layer').width($(document).width());
	
	// 初始化背景
	$("#wallpaperBox").width($(window).width());
	$("#wallpaperBox").height($(window).height());
	$("#wallpaper").width($(window).width());
	$("#wallpaper").height($(window).height());
	
	$("#backBtn").css({"display":"block", "position":"absolute"});
	$("#backBtn").show();
	$('#backBtn').css('left', ($(window).width()-$('#backBtn').width())/2 + 'px');
	$('#backBtn').css('top', ($(window).height()-$('#backBtn').height()-10) + 'px');
}

$(document).ready(function () {
	slideCount = $('.slide .layer').length;
	
	$('.slide .layer').each(function (i) {
		$('.buttons').append('<b groupId=' + $(this).attr("groupId") + ' title=' + $(this).attr("groupName") + '></b>')
	});
	// alert("margin:" + $('.buttons b').css("margin")); // chrome中取不到值
	var bw = $('.buttons b').outerWidth() + 2 * 5; // parseInt($('.buttons b').css("margin")); // 此时outerWidth()因为载入的原因尚未生效，未获取到margin
	$('.buttons').width(bw * slideCount);
	
	$('.buttons b').eq(0).css('background','#6cf');
	buttonWidth = $('.buttons').width();
	$('.buttons').css('left', ($('.slide .layer').width()-buttonWidth)/2 + 'px');
	
	/*
	$('.container').live('mousedown touchstart', slideStart);
	$('.container').live('mouseup mousestop touchend', slideEnd);
	$('.container').live('mousemove mousedown touchmove', slide);
	// 左右两侧各50个像素宽的触感条
	$('.container .pad').live('mousemove mousedown touchmove', slideEnd);
	*/
	
	$('.buttons b').bind('click', function() {
		var groupId = $(this).attr("groupId");
		showMenu(groupId);
	});
		
	init();
});

$(window).resize(function(){
	init();
});

</script>
<style type="text/css">
html, body { height:100%; }

html, body {
background:url(<%=request.getContextPath() + "/" + wallpaperPath%>);
filter:"progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod='scale')";
-moz-background-size:100% 100%;
background-size:100% 100%;
}

#columns {height:100%}
.slideBtn{position:relative;list-style:none;float:left;width:50px;height:50px;margin:10px 0 0 10px;cursor:pointer;}
.container {width:100%; overflow:hidden; position:relative; z-index:10;}
.pad {width:50px; height:300px; background: url("images/trans.gif");}
.right {float:right;}
.slide {width:60000px; position:absolute; left:0; top:0; z-index:-1;}
.slide .layer {display:block; float:left; width:700px; height:300px;}
.buttons {height:20px;}
.buttons b {display:block; width:10px; height:10px; overflow:hidden; margin:5px; background:#069; float:left; border-radius:10px; cursor:pointer}

ul{ list-style:none;}
.layer .icon{ float:left; padding-top:20px;}
.layer .icon li{width:90px; height:60px; margin:25px; float:left; text-align:center}
.layer .icon li a {color:white; }

</style>
<style>
.appButton {
	position:absolute
}
.appButton_appIcon {
	margin-top:6px
}
.appbtn {
	overflow:visible;
	margin-top:10px;
}
.appbtn_inner_right {
	display:none
}
.appbtn_inner {
	width:auto;
	display:inline-block;
	background:url("<%=SkinMgr.getSkinPath(request)%>/images/app_name_btn.png") no-repeat;
	_background:0;
	text-indent:10px;
	margin:0;
	padding:0;
	height:20px;
	line-height:18px;
	overflow:hidden;
	position:relative;
	max-width:78px;
	vertical-align:middle
}
.appbtn_inner_right {
	width:10px;
	display:inline-block;
	background:url("<%=SkinMgr.getSkinPath(request)%>/images/app_name_btn.png") no-repeat -86px 0px;
	_background:0;
	margin:0;
	padding:0;
	height:20px;
	vertical-align:middle
}
</style>
</head>
<body>
<div style="height:100%;">
<%
// 非mydesktop界面
String displayStr = "";
if (iconId!=-1) {
	displayStr = "display:none";
}
%>
    <div id="buttons" class="buttons" style="margin:0 auto;<%=displayStr%>">
        
    </div>
    <div id="container" class="container"><div class="pad right"></div><div class="pad"></div>
        <div class="slide">
                <div class="layer" groupId="1" groupName="1">
                	<ul class="icon">
                    <%
					Iterator ir = lf.getChildren().iterator();
					while (ir.hasNext()) {
						lf = (Leaf)ir.next();
						
						if (!lf.canUserSee(request))
							continue;

						String link = lf.getLink(request);
						if (lf.getChildCount()>0) {
							link = "desktop_sub.jsp?code=" + StrUtil.UrlEncode(lf.getCode());
						}
						%>
                        <li>
                        <%if (lf.getChildCount()>0) {%>
                        <a class="btnLink" btnName="<%=lf.getName()%>" btnUrl="<%=link%>" href="<%=link%>" title="<%=lf.getName()%>"><img id="icon_<%=lf.getCode()%>" alt="<%=lf.getName()%>" src="images/bigicons/<%=lf.getBigIcon()%>" /></a>
                        <%}else{%>
                        <a class="btnLink" btnName="<%=lf.getName()%>" btnUrl="<%=link%>" href="javascript:;" onclick="addTab('<%=lf.getName()%>', '<%=link%>')" title="<%=lf.getName()%>"><img id="icon_<%=lf.getCode()%>" alt="<%=lf.getName()%>" src="images/bigicons/<%=lf.getBigIcon()%>" /></a>
						<%}%>
                        <div class="appbtn">
                        <div class="appbtn_inner">
                        <%if (lf.getChildCount()>0) {%>
                        <a class="btnLink" btnName="<%=lf.getName()%>" btnUrl="<%=link%>" href="<%=link%>" title="<%=lf.getName()%>"><%=lf.getName()%></a>
                        <%}else{%>
                        <a class="btnLink" btnName="<%=lf.getName()%>" btnUrl="<%=link%>" href="javascript:;" onclick="addTab('<%=lf.getName()%>', '<%=link%>')" title="<%=lf.getName()%>"><%=lf.getName()%></a>
                        <%}%>
                        </div><div class="appbtn_inner_right">
                        </div>
                        </div>
                       
                        </li>
                        <%
						int count = lf.getCount(userName);
						if (count>0) {%>
						<img id="icon_num_<%=lf.getCode()%>" style="position:absolute; display:none;" src="images/icon_num.png" />
                        <span id="count_<%=lf.getCode()%>" style="position:absolute; color:white; display:none; height:20px"><%=count%></span>
                        <script>
						$(document).ready(function () {
						$('#icon_num_<%=lf.getCode()%>').css({"display":"block", "position":"absolute"});
						$('#count_<%=lf.getCode()%>').css({"display":"block", "position":"absolute"});
						$('#icon_num_<%=lf.getCode()%>').css('left', ($('#icon_<%=lf.getCode()%>').position().left + $('#icon_<%=lf.getCode()%>').width() + 20 - $('#icon_num_<%=lf.getCode()%>').width()) + 'px');
						$('#icon_num_<%=lf.getCode()%>').css('top', ($('#icon_<%=lf.getCode()%>').position().top-20) + 'px');
						$('#count_<%=lf.getCode()%>').css('left', ($('#icon_num_<%=lf.getCode()%>').position().left + ($('#icon_num_<%=lf.getCode()%>').width() - $('#count_<%=lf.getCode()%>').width())/2) + 'px');
						$('#count_<%=lf.getCode()%>').css('top', ($('#icon_num_<%=lf.getCode()%>').position().top + ($('#icon_num_<%=lf.getCode()%>').height() - $('#count_<%=lf.getCode()%>').height())/2) + 'px');
						});
						</script>
                        <%}%>                        
                        <%
					}
					%>
                    </ul>
				</div>
        </div>
        
    </div>
</div>
<%
// mydesktop界面中不需要返回按钮
if (iconId==-1) {
%>
<a id="backBtn" title="返回" href="#" style="position:absolute; z-index:1000"><img width="64" height="64" src="<%=skinPath%>/images/back.png" /></a>
<%}%>
</body>
<script>
$('.btnLink').bind('touchend', function () {
	var name = $(this).attr("btnName");
	var url = $(this).attr("btnUrl");
	addTab(name, url);
});

$('#backBtn').click(function() {
	// window.location.href='desktop_left.jsp';
	window.history.back();
});
</script>
</html>