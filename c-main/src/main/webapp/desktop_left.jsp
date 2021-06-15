<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.ui.menu.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import="com.cloudweb.oa.service.IUserSetupService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String skincode = UserSet.getSkin(request);
	if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
	SkinMgr skm = new SkinMgr();
	Skin skin = skm.getSkin(skincode);
	String skinPath = skin.getPath();
	String userName = privilege.getUser(request);
	
	long groupId = ParamUtil.getLong(request, "groupId", -1);
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
<link href="js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="js/jquery-showLoading/jquery.showLoading.js"></script>

<%
IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
String wallpaperPath = userSetupService.getWallpaperPath(userName);
%>
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
.container {width:100%; overflow:hidden; position:relative; z-index:10; cursor:url('images/ani/normal.cur');}
.pad {width:50px; height:300px; background: url("images/trans.gif");}
.right {float:right;}
.slide {width:60000px; position:absolute; left:0; top:0; z-index:-1;}
.slide .layer {display:block; float:left; width:700px; height:300px;}
.buttons {height:20px;}
.buttons b {display:block; width:10px; height:10px; overflow:hidden; margin:5px; background:#069; float:left; border-radius:10px; cursor:pointer}

ul{list-style:none;}
.layer .icon{float:left; padding-top:5px;}
.layer .icon li{width:100px; height:60px; margin:25px; float:left; text-align:center;}
.layer .icon li a {color:white;}

.appButton {
	position:absolute
}
.appButton_appIcon {
	margin-top:6px
}
.appbtn_b {height:21px; text-align: center; clear:both}
.appbtn {
	overflow:visible;
	margin:0 auto;
	margin-top:10px;
	text-align:center;
	display:inline-block; *display:inline; zoom:1;
	position:relative;
}
.appbtn_inner {
	width:auto;
	max-width:78px;
	display:inline-block;
	background:url("<%=SkinMgr.getSkinPath(request)%>/images/app_name_btn.png") no-repeat;
	_background:0;
	text-indent:10px;
	margin:0;
	padding:0;
	height:20px;
	line-height:18px;
	overflow:hidden;
	vertical-align:middle;
	position:relative;
	float:left;	
}
.appbtn_inner_right {
	width:10px;
	display:inline-block;
	background:url("<%=SkinMgr.getSkinPath(request)%>/images/app_name_btn.png") no-repeat -86px 0px;
	_background:0;
	margin:0;
	padding:0;
	height:20px;
	vertical-align:middle;
	position:relative;
	float:left;	
}
</style>
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
		
		$(".container").css("cursor", "url('images/ani/move.cur')");
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
			
		$(".container").css("cursor", "url(images/ani/normal.cur)");
			
		}
	}
	
function showMenu(groupId) {
	$('.buttons b').each(function (i, item) {
		if (groupId==$(this).attr("groupId")) {
			currentSlide = i;
			pixelOffset = currentSlide * -$('.slide .layer').width();
			$('.buttons b').css('background','#069');
			$('.buttons b').eq(currentSlide).css('background','#6cf');
			$('.slide').animate({left:pixelOffset}, 250);
		}
	});
	jQuery.event.triggered = null;
	slideEnd()
}

$(document).ready(function () {
	slideCount = $('.slide .layer').length;
	
	$('.slide .layer').each(function (i) {
		$('.buttons').append('<b groupId=' + $(this).attr("groupId") + ' title=' + $(this).attr("groupName") + '></b>')
	});
	// alert("margin:" + $('.buttons b').css("margin")); // chrome中取不到值
	var bw = $('.buttons b').outerWidth() + 2 * 5; // parseInt($('.buttons b').css("margin")); // 此时outerWidth()因为载入的原因尚未生效，未获取到margin
	$('.buttons').width(bw * slideCount + 10); // +10是为了防止IE6下面显示折行问题
	
	$('.buttons b').eq(0).css('background','#6cf');
	buttonWidth = $('.buttons').width();
	$('.buttons').css('left', ($('.slide .layer').width()-buttonWidth)/2 + 'px');
	
	$('.container').live('mousedown touchstart', slideStart);
	$('.container').live('mouseup mousestop touchend', slideEnd);
	$('.container').live('mousemove mousedown touchmove', slide);
	// 左右两侧各50个像素宽的触感条
	$('.container .pad').live('mousemove mousedown touchmove', slideEnd);
	
	$('.buttons b').bind('click', function() {
		var groupId = $(this).attr("groupId");
		showMenu(groupId);
		
		window.top.menuFrame.lamp(groupId);
	});
	
	$('#container').height($(document).height() - $('#buttons').height());
	$('.layer').height($('#container').height());
	$('.layer').width($(document).width());
	
	// 初始化背景
	$("#wallpaperBox").width($(window).width());
	$("#wallpaperBox").height($(window).height());
	$("#wallpaper").width($(window).width());
	$("#wallpaper").height($(window).height());

	$('#mainBox').hideLoading();
	
	<%if (groupId!=-1) {%>
	showMenu('<%=groupId%>');
	<%}%>
});

function onMouseWheel() {
	if (event.wheelDelta >= 120) {
		if (currentSlide <= 0)
			return;
		currentSlide --;
		pixelOffset = currentSlide * -$('.slide .layer').width();
		$('.buttons b').css('background','#069');
		$('.buttons b').eq(currentSlide).css('background','#6cf');
		$('.slide').animate({left:pixelOffset}, 250);
	}
	else if (event.wheelDelta <= -120) {
		if (currentSlide >= slideCount-1)
			return;
		currentSlide ++;
		pixelOffset = currentSlide * -$('.slide .layer').width();
		$('.buttons b').css('background','#069');
		$('.buttons b').eq(currentSlide).css('background','#6cf');
		$('.slide').animate({left:pixelOffset}, 250);
	}
}
</script>
</head>
<body onmousewheel="onMouseWheel();">
<div id="mainBox" style="height:100%;">

<script>
$('#mainBox').showLoading();
</script>

    <div id="buttons" class="buttons" style="margin:0 auto">
        
    </div>
    <div id="container" class="container"><div class="pad right"></div><div class="pad"></div>
        <div class="slide">
            <%
			SlideMenuGroupDb smgd = new SlideMenuGroupDb();
			SlideMenuDb smd = new SlideMenuDb();
			String sql = "select id from " + smgd.getTable().getName() + " where user_name=? order by orders";
			String sql2 = "select id from " + smd.getTable().getName() + " where group_id=?";
			com.redmoon.oa.ui.menu.Leaf lf = new com.redmoon.oa.ui.menu.Leaf();

			Iterator ir = smgd.list(sql, new Object[]{privilege.getUser(request)}).iterator();
			while (ir.hasNext()) {
				smgd = (SlideMenuGroupDb)ir.next();
				%>
                <div class="layer" groupId="<%=smgd.getLong("id")%>" groupName="<%=smgd.getString("name")%>">
                	<ul class="icon">
                    <%
					Iterator ir2 = smd.list(sql2, new Object[]{new Long(smgd.getLong("id"))}).iterator();
					while (ir2.hasNext()) {
						smd = (SlideMenuDb)ir2.next();
						lf = lf.getLeaf(smd.getString("code"));
						if (lf==null) {
							lf = new Leaf();
							continue;
						}
						if (!lf.canUserSee(request))
							continue;
						%>
                        <li>
							<%
                            String link = lf.getLink(request);
                            if (lf.getChildCount()>0) {
                                link = "desktop_sub.jsp?code=" + StrUtil.UrlEncode(lf.getCode());
                                %>
                                <a class="btnLink" btnName="<%=lf.getName()%>" btnUrl="<%=link%>" href="<%=link%>" title="<%=lf.getName()%>"><img id="icon_<%=lf.getCode()%>" alt="<%=lf.getName()%>" src="images/bigicons/<%=lf.getBigIcon()%>" /></a>
                                <%
                            }
                            else {
                                %>
                                <a class="btnLink" btnName="<%=lf.getName()%>" btnUrl="<%=link%>" href="javascript:;" onclick="addTab('<%=lf.getName()%>', '<%=link%>')" title="<%=lf.getName()%>"><img id="icon_<%=lf.getCode()%>" alt="<%=lf.getName()%>" src="images/bigicons/<%=lf.getBigIcon()%>" /></a>
                                <%
                            }
                            %>
                            
                            <div class="appbtn_b">
                            <div class="appbtn">
                                <div class="appbtn_inner">
                                <%if (lf.getChildCount()>0) {%>
                                <a class="btnLink" btnName="<%=lf.getName()%>" btnUrl="<%=link%>" href="<%=link%>" title="<%=lf.getName()%>"><%=lf.getName()%></a>
                                <%}else{%>
                                <a class="btnLink" btnName="<%=lf.getName()%>" btnUrl="<%=link%>" href="javascript:;" onclick="addTab('<%=lf.getName()%>', '<%=link%>')" title="<%=lf.getName()%>"><%=lf.getName()%></a>
                                <%}%>
                                </div><div class="appbtn_inner_right"></div>
                            </div>
                            </div>
                       
                        </li>
                        <%
						int count = lf.getCount(userName);
						if (count>0) {
						%>
						<img id="icon_num_<%=lf.getCode()%>" style="position:absolute; display:none; width:41px; height:45px" src="images/icon_num.png" />
                        <span id="count_<%=lf.getCode()%>" style="position:absolute; display:none; color:white; height:20px; width:17px; text-align:center"><%=count%></span>
                        <script>
						$(document).ready(function () {
						$('#icon_num_<%=lf.getCode()%>').css({"display":"block", "position":"absolute"});
						$('#count_<%=lf.getCode()%>').css({"display":"block", "position":"absolute"});
						
						$('#icon_num_<%=lf.getCode()%>').css('left', ($('#icon_<%=lf.getCode()%>').position().left + $('#icon_<%=lf.getCode()%>').width() + 20 - $('#icon_num_<%=lf.getCode()%>').width()) + 'px');
						$('#icon_num_<%=lf.getCode()%>').css('top', ($('#icon_<%=lf.getCode()%>').position().top-20) + 'px');
						$('#count_<%=lf.getCode()%>').css('left', ($('#icon_num_<%=lf.getCode()%>').position().left + ($('#icon_num_<%=lf.getCode()%>').width() - $('#count_<%=lf.getCode()%>').width())/2) + 'px');
						$('#count_<%=lf.getCode()%>').css('top', ($('#icon_num_<%=lf.getCode()%>').position().top + ($('#icon_num_<%=lf.getCode()%>').height() - $('#count_<%=lf.getCode()%>').height())/2) + 'px');
						
						$('#icon_num_<%=lf.getCode()%>').show();
						$('#count_<%=lf.getCode()%>').show();
						});
						</script>
                        <%}%>
                        <%
					}
					%>
                    </ul>
				</div>
				<%
			}
			%>
        </div>
        
    </div>
</div>
</body>
<script>
$('.btnLink').bind('touchend', function () {
	var name = $(this).attr("btnName");
	var url = $(this).attr("btnUrl");
	addTab(name, url);
}); 
</script>
</html>